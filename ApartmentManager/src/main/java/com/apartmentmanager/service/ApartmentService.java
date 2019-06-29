package com.apartmentmanager.service;

import com.apartmentmanager.constant.ApartmentStatus;
import com.apartmentmanager.constant.CustomerStatus;
import com.apartmentmanager.constant.ModuleEnum;
import com.apartmentmanager.constant.PaymentType;
import com.apartmentmanager.dao.ApartmentDao;
import com.apartmentmanager.dao.CustomerDao;
import com.apartmentmanager.dao.EnrollmentHistoryDao;
import com.apartmentmanager.dto.Page;
import com.apartmentmanager.dto.apartment.*;
import com.apartmentmanager.po.apartment.ApartmentInfo;
import com.apartmentmanager.po.apartment.ApartmentPrice;
import com.apartmentmanager.po.customer.CustomerInfo;
import com.apartmentmanager.po.enrollment.EnrollmentHistory;
import com.apartmentmanager.po.payment.PaymentHistory;
import com.apartmentmanager.po.payment.ext.PaymentHistoryExtension;
import com.apartmentmanager.po.payment.ext.PremiumPayment;
import com.apartmentmanager.remote.FileRemoteClient;
import com.apartmentmanager.service.datecalculator.IDateCalculator;
import com.apartmentmanager.service.premiumresolver.ApartmentPremiumParamsResolver;
import com.apartmentmanager.service.util.ApartmentInfoConverter;
import com.apartmentmanager.service.util.PaymentHistoryConverter;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.apartmentmanager.po.apartment.QApartmentInfo.apartmentInfo;
import static com.apartmentmanager.po.apartment.QApartmentPrice.apartmentPrice;
import static com.apartmentmanager.po.customer.QCustomerInfo.customerInfo;
import static com.apartmentmanager.po.enrollment.QEnrollmentHistory.enrollmentHistory;
import static com.apartmentmanager.po.payment.QPaymentHistory.paymentHistory;
import static com.apartmentmanager.remote.FileRemoteClient.FileBindingInfo;


@Service
@Slf4j
public class ApartmentService {
    public static final String LIST_APARTMENTS_CACHE_KEY = "com.apartmentmanager.cache.apartments";

    @Value("${spring.application.name}")
    private String appName;

    @Getter
    @Autowired
    private ApartmentDao apartmentDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private EnrollmentHistoryDao enrollmentHistoryDao;

    @Autowired
    private ApartmentPriceService apartmentPriceService;

    @Autowired
    private ApartmentExtensionService apartmentExtensionService;

    @Autowired
    private PaymentHistoryService paymentHistoryService;

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private FileRemoteClient fileRemoteClient;

    @Autowired
    private FetchAdviser fetchAdviser;

    @Autowired
    private IDateCalculator dateCalculator;

    @Autowired
    private ApartmentPremiumParamsResolver apartmentPremiumParamsResolver;

    @Cacheable(value = LIST_APARTMENTS_CACHE_KEY, key = "#id")
    public ApartmentInfoView getApartment(Integer id) {
        ApartmentInfo apartmentInfo = apartmentDao.findById(id).orElseThrow(IllegalArgumentException::new);
        ApartmentInfoView result = ApartmentInfoConverter.toView(apartmentInfo);
        apartmentInfo.getCustomers().stream()
                .filter(c -> CustomerStatus.ENROLLED == c.getStatus() && c.getIsPrimary())
                .findFirst().ifPresent(c -> {
            result.setPrimaryCustomerId(c.getId());
            result.setPrimaryCustomerName(c.getName());
            EnrollmentHistory enrollmentHistory = enrollmentHistoryDao.findByApartmentIdAndCustomerId(id, c.getId());

            paymentHistoryService.getPaymentHistoryDao().findTopByApartmentIdAndCustomerIdOrderByCreatedTimeDesc(id, c.getId())
                    .ifPresent(h -> result.setLatestPayment(PaymentHistoryConverter.toLatestPaymentView(h)));

            result.setDeposit(enrollmentHistory.getDeposit());
            result.setChosenPaymentType(enrollmentHistory.getOriginalPrice().getPaymentType());
            result.setConcertedPrice(enrollmentHistory.getConcertedPrice());
        });
        bindApartmentExtraInfo(Arrays.asList(result));
        result.setFiles(fileRemoteClient.getFiles(appName, ModuleEnum.APARTMENT.getModuleName(), String.valueOf(apartmentInfo.getId())));
        return result;
    }

    @Cacheable(value = LIST_APARTMENTS_CACHE_KEY, key = "#apartmentPageRequest.toString()")
    public Page<ApartmentInfoView> getApartments(ApartmentPageRequest apartmentPageRequest) {
        JPQLQuery<Integer> latestPaymentQuery = JPAExpressions.select(paymentHistory.id.max()).from(paymentHistory)
                .where(apartmentInfo.id.eq(paymentHistory.apartment.id)).groupBy(paymentHistory.apartment.id);

        JPAQuery<Tuple> query = queryFactory
                .select(apartmentInfo.id, apartmentInfo.name, apartmentInfo.status,
                        apartmentInfo.description, apartmentPrice.paymentType,
                        enrollmentHistory.concertedPrice, enrollmentHistory.deposit,
                        customerInfo.id, customerInfo.name,
                        paymentHistory.id, paymentHistory.toDate)
                .from(apartmentInfo)
                .leftJoin(customerInfo).on(apartmentInfo.id.eq(customerInfo.apartment.id), customerInfo.isPrimary.eq(true), customerInfo.status.eq(CustomerStatus.ENROLLED))
                .leftJoin(enrollmentHistory).on(apartmentInfo.id.eq(enrollmentHistory.apartment.id), enrollmentHistory.customer.id.eq(customerInfo.id))
                .leftJoin(apartmentPrice).on(apartmentInfo.id.eq(apartmentPrice.apartment.id), enrollmentHistory.originalPrice.id.eq(apartmentPrice.id))
                .leftJoin(paymentHistory).on(paymentHistory.id.eq(latestPaymentQuery), paymentHistory.customer.id.eq(customerInfo.id))
                .where(buildConditionByFilter(apartmentPageRequest.getFilter()));

        long count = query.fetchCount();
        List<ApartmentInfoView> content = query
                .orderBy(paymentHistory.toDate.asc())
                .offset(apartmentPageRequest.getPageRequest().getOffset()).limit(apartmentPageRequest.getPageRequest().getPageSize())
                .fetch().stream().map(t -> ApartmentInfoView.builder()
                        .apartmentId(t.get(apartmentInfo.id)).name(t.get(apartmentInfo.name)).status(t.get(apartmentInfo.status))
                        .description(t.get(apartmentInfo.description)).chosenPaymentType(t.get(apartmentPrice.paymentType))
                        .concertedPrice(t.get(enrollmentHistory.concertedPrice)).deposit(t.get(enrollmentHistory.deposit))
                        .primaryCustomerId(t.get(customerInfo.id)).primaryCustomerName(t.get(customerInfo.name))
                        .latestPayment(LatestPaymentView.builder().id(t.get(paymentHistory.id)).toDate(t.get(paymentHistory.toDate)).build())
                        .build()).collect(Collectors.toList());

        apartmentPriceService.bindPrices(content);
        bindApartmentExtraInfo(content);
        Page<ApartmentInfoView> result = new Page<>(count, content);
        fetchAdviser.saveNameSuggestions(apartmentPageRequest.getFilter().getUserId(), content);
        return result;
    }

    private BooleanExpression buildConditionByFilter(ApartmentFilter filter) {
        BooleanExpression condition = apartmentInfo.status.ne(ApartmentStatus.REMOVED);
        if (StringUtils.isNotBlank(filter.getUserId())) {
            condition = condition.and(apartmentInfo.userId.eq(filter.getUserId()));
        }
        if (StringUtils.isNotBlank(filter.getName())) {
            condition = condition.and(apartmentInfo.name.startsWith(filter.getName()).or(customerInfo.name.startsWith(filter.getName())));
        }
        if (filter.getStatus() != null) {
            switch (filter.getStatus()) {
                case IDLE:
                    condition = condition.and(apartmentInfo.status.eq(ApartmentStatus.IDLE));
                    break;
                case OCCUPIED:
                    condition = condition.and(apartmentInfo.status.eq(ApartmentStatus.OCCUPIED));
                    break;
                case EXPIRED:
                    condition = condition.and(paymentHistory.toDate.before(LocalDate.now().plusDays(1)));
                    break;
            }
        }
        return condition;
    }

    @Transactional
    @CacheEvict(value = LIST_APARTMENTS_CACHE_KEY, allEntries = true)
    public void addApartment(ApartmentForm apartmentForm) {
        ApartmentInfo po = new ApartmentInfo();
        po.setStatus(ApartmentStatus.IDLE);
        po.setName(apartmentForm.getName());
        po.setCreatedTime(LocalDateTime.now());
        po.setDescription(apartmentForm.getDescription());

        apartmentDao.save(po);
        saveApartmentPrices(po, apartmentForm);

        apartmentExtensionService.saveApartmentExtension(po.getId(), apartmentForm.getPremiums());

        if (!CollectionUtils.isEmpty(apartmentForm.getFileIds())) {
            FileBindingInfo bindingInfo = FileBindingInfo.builder()
                    .externalId(String.valueOf(po.getId()))
                    .fileIds(apartmentForm.getFileIds())
                    .build();
            fileRemoteClient.bindFiles(Arrays.asList(bindingInfo));
        }
    }

    @Transactional
    @CacheEvict(value = LIST_APARTMENTS_CACHE_KEY, allEntries = true)
    public void updateApartment(Integer id, ApartmentUpdateForm apartmentForm) {
        ApartmentInfo po = apartmentDao.findById(id).orElseThrow(IllegalArgumentException::new);
        po.setName(apartmentForm.getName());
        po.setDescription(apartmentForm.getDescription());

        apartmentDao.save(po);

        if (ApartmentStatus.OCCUPIED == po.getStatus() && apartmentForm.getDeposit() != null) {
            updateDeposit(po.getId(), apartmentForm.getDeposit());
        }

        if ((apartmentForm.getPricePerMonth() != null && apartmentForm.getPricePerMonth().compareTo(BigDecimal.ZERO) > 0)
                || (apartmentForm.getPricePerWeek() != null && apartmentForm.getPricePerWeek().compareTo(BigDecimal.ZERO) > 0)
                || (apartmentForm.getPricePerDay() != null) && apartmentForm.getPricePerDay().compareTo(BigDecimal.ZERO) > 0) {
            List<ApartmentPrice> prices = apartmentPriceService.getApartmentPriceDao().findByApartmentId(id);
            apartmentPriceService.getApartmentPriceDao().deleteInBatch(prices);
            saveApartmentPrices(po, apartmentForm);
        }

        updateApartmentPayments(po, apartmentForm);

        if (!CollectionUtils.isEmpty(apartmentForm.getFileIds())) {
            FileBindingInfo bindingInfo = FileBindingInfo.builder()
                    .externalId(String.valueOf(po.getId()))
                    .fileIds(apartmentForm.getFileIds())
                    .build();
            fileRemoteClient.bindFiles(Arrays.asList(bindingInfo));
        }
    }


    @Transactional
    @CacheEvict(value = LIST_APARTMENTS_CACHE_KEY, allEntries = true)
    public void removeApartment(Integer apartmentId) {
        ApartmentInfo po = apartmentDao.getOne(apartmentId);
        List<Integer> customerIds = po.getCustomers().stream().map(CustomerInfo::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(customerIds)) {
            customerDao.updateStatusByCustomerIds(CustomerStatus.REMOVED, customerIds);
        }
        apartmentDao.updateStatusByApartmentId(ApartmentStatus.REMOVED, apartmentId);
        fetchAdviser.removeSuggestionsByApartmentId(null, apartmentId);
    }

    @Transactional
    private void updateApartmentPayments(ApartmentInfo po, ApartmentUpdateForm apartmentForm) {
        if (ApartmentStatus.OCCUPIED == po.getStatus()) {
            CustomerInfo cQuery = new CustomerInfo();
            cQuery.setIsPrimary(true);
            cQuery.setStatus(CustomerStatus.ENROLLED);
            cQuery.setApartment(po);
            CustomerInfo primaryCustomer = customerDao.findOne(Example.of(cQuery)).orElseThrow(IllegalArgumentException::new);
            PaymentHistory latestPayment = paymentHistoryService.getPaymentHistoryDao().findTopByApartmentIdAndCustomerIdOrderByCreatedTimeDesc(po.getId(), primaryCustomer.getId()).orElseThrow(IllegalArgumentException::new);
            if (!latestPayment.getToDate().equals(apartmentForm.getToDate())) { //Update payment histories if to date is changed.
                PaymentHistory pQuery = new PaymentHistory();
                pQuery.setCustomer(cQuery);
                Example<PaymentHistory> example = Example.of(pQuery);
                List<PaymentHistory> paymentHistories = paymentHistoryService.getPaymentHistoryDao().findAll(example, Sort.by(Sort.Order.desc("id")));
                LocalDate toDate = apartmentForm.getToDate();
                for (PaymentHistory p : paymentHistories) {
                    p.setToDate(toDate);
                    p.setFromDate(dateCalculator.calculate(toDate, p.getPaymentType(), -p.getQuantity()));
                    toDate = p.getFromDate();
                }
                paymentHistoryService.getPaymentHistoryDao().saveAll(paymentHistories);
            }
            updatePremiumPayments(apartmentForm.getPremiums(), latestPayment.getId());
        } else {
            apartmentExtensionService.saveApartmentExtension(po.getId(), apartmentForm.getPremiums());
        }
    }

    @Transactional
    private void updateDeposit(Integer apartmentId, BigDecimal deposit) {
        Example<CustomerInfo> query = Example.of(new CustomerInfo());
        query.getProbe().setApartment(new ApartmentInfo());
        query.getProbe().getApartment().setId(apartmentId);
        query.getProbe().setIsPrimary(true);
        query.getProbe().setStatus(CustomerStatus.ENROLLED);
        Optional<CustomerInfo> primaryCustomer = customerDao.findOne(query);
        primaryCustomer.ifPresent(c -> {
            EnrollmentHistory enrollmentHistory = enrollmentHistoryDao.findByApartmentIdAndCustomerId(apartmentId, c.getId());
            if (enrollmentHistory.getDeposit().compareTo(deposit) == 0) {
                return;
            }
            enrollmentHistory.setDeposit(deposit);
            enrollmentHistoryDao.save(enrollmentHistory);

            //Update first(enrollment) payment correspondingly.
            paymentHistoryService.findFirstPaymentIdSet(Set.of(apartmentId))
                    .stream().findFirst().ifPresent(id -> paymentHistoryService
                    .getPaymentHistoryExtensionDao().findById(id).ifPresent(ext -> {
                        BigDecimal newTotal = enrollmentHistory.getConcertedPrice().add(deposit);
                        if (ext.getTotalPrice().compareTo(ext.getReceipts()) == 0) {
                            ext.setReceipts(newTotal);
                        }
                        ext.setTotalPrice(newTotal);
                        paymentHistoryService.getPaymentHistoryExtensionDao().save(ext);
                    }));

        });
    }

    @Transactional
    private void updatePremiumPayments(List<ApartmentPremiumParams> premiumParamsList, Integer latestPaymentId) {
        PaymentHistoryExtension latestPaymentExt = paymentHistoryService.getPaymentHistoryExtensionDao().findById(latestPaymentId).orElseThrow(IllegalArgumentException::new);
        apartmentPremiumParamsResolver.resolve(premiumParamsList, latestPaymentExt.getPremiumPayments()); //Update firstly, then delete
        if (premiumParamsList.size() < latestPaymentExt.getPremiumPayments().size()) { //Means user want to delete premium
            if (premiumParamsList.size() > 0) {
                Iterator<? extends PremiumPayment> premiumPaymentIt = latestPaymentExt.getPremiumPayments().iterator();
                while (premiumPaymentIt.hasNext()) {
                    PremiumPayment pp = premiumPaymentIt.next();
                    boolean noneMatch = premiumParamsList.stream().noneMatch(p -> p.getPremiumFlag().equals(pp.getPremiumFlag()));
                    if (noneMatch) {
                        premiumPaymentIt.remove();
                    }
                }
            } else {
                latestPaymentExt.setPremiumPayments(Collections.emptyList());
            }
        } else if (premiumParamsList.size() > latestPaymentExt.getPremiumPayments().size()) {
            List latestPremiumPayments = latestPaymentExt.getPremiumPayments();
            List<? extends PremiumPayment> newPremiumPayments = premiumParamsList.stream()
                    .filter(p -> latestPaymentExt.getPremiumPayments().stream().noneMatch(pp -> p.getPremiumFlag().equals(pp.getPremiumFlag())))
                    .map(apartmentPremiumParamsResolver::toPremiumPayment).collect(Collectors.toList());
            latestPremiumPayments.addAll(newPremiumPayments);
        }
        paymentHistoryService.getPaymentHistoryExtensionDao().save(latestPaymentExt);

    }

    @Transactional
    private void saveApartmentPrices(ApartmentInfo po, ApartmentForm apartmentForm) {
        List<ApartmentPrice> prices = new ArrayList<>(3);
        if (apartmentForm.getPricePerDay() != null && apartmentForm.getPricePerDay().compareTo(BigDecimal.ZERO) > 0) {
            prices.add(new ApartmentPrice(po, PaymentType.PER_DAY, apartmentForm.getPricePerDay()));
        }
        if (apartmentForm.getPricePerWeek() != null && apartmentForm.getPricePerWeek().compareTo(BigDecimal.ZERO) > 0) {
            prices.add(new ApartmentPrice(po, PaymentType.PER_WEEK, apartmentForm.getPricePerWeek()));
        }
        if (apartmentForm.getPricePerMonth() != null && apartmentForm.getPricePerMonth().compareTo(BigDecimal.ZERO) > 0) {
            prices.add(new ApartmentPrice(po, PaymentType.PER_MONTH, apartmentForm.getPricePerMonth()));
        }
        apartmentPriceService.getApartmentPriceDao().saveAll(prices);
    }

    /**
     * Bind apartment extra info
     * <p>Bind premiums' info, etc: measurement of electricity and water.</p>
     *
     * @param apartmentInfoViews
     */
    private void bindApartmentExtraInfo(List<ApartmentInfoView> apartmentInfoViews) {
        if (CollectionUtils.isEmpty(apartmentInfoViews)) {
            return;
        }
        paymentHistoryService.bindApartmentLatestPaymentExt(apartmentInfoViews);

        List<ApartmentInfoView> notYetBoundPremiumApartments = apartmentInfoViews.stream()
                .filter(a -> a.getLatestPayment() == null || a.getLatestPayment().getExtension() == null).collect(Collectors.toList());
        apartmentExtensionService.bindApartmentPremiums(notYetBoundPremiumApartments);
    }
}


