package com.apartmentmanager.service;

import com.apartmentmanager.constant.*;
import com.apartmentmanager.dao.*;
import com.apartmentmanager.dao.ext.PaymentHistoryExtensionDao;
import com.apartmentmanager.dto.Page;
import com.apartmentmanager.dto.apartment.ApartmentPaymentSummary;
import com.apartmentmanager.dto.apartment.ApartmentPremiumParams;
import com.apartmentmanager.dto.customer.*;
import com.apartmentmanager.po.apartment.ApartmentInfo;
import com.apartmentmanager.po.apartment.ApartmentPrice;
import com.apartmentmanager.po.customer.ContactInfo;
import com.apartmentmanager.po.customer.CustomerInfo;
import com.apartmentmanager.po.enrollment.EnrollmentHistory;
import com.apartmentmanager.po.payment.PaymentHistory;
import com.apartmentmanager.po.payment.ext.PaymentHistoryExtension;
import com.apartmentmanager.po.payment.ext.PremiumPayment;
import com.apartmentmanager.po.payment.ext.PremiumPaymentByDate;
import com.apartmentmanager.po.payment.ext.PremiumPaymentByMeasurement;
import com.apartmentmanager.remote.FileRemoteClient;
import com.apartmentmanager.service.datecalculator.ExpiredDateCalculator;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.apartmentmanager.dto.customer.CustomerEnrollment.Customer;
import static com.apartmentmanager.po.apartment.QApartmentInfo.apartmentInfo;
import static com.apartmentmanager.po.customer.QCustomerInfo.customerInfo;
import static com.apartmentmanager.po.enrollment.QEnrollmentHistory.enrollmentHistory;
import static com.apartmentmanager.remote.FileRemoteClient.FileBindingInfo;
import static com.apartmentmanager.remote.FileRemoteClient.FileInfo;
import static com.apartmentmanager.service.ApartmentService.LIST_APARTMENTS_CACHE_KEY;

@Service
@Slf4j
public class CustomerService {
    @Value("${spring.application.name}")
    private String appName;

    @Getter
    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private FileRemoteClient fileRemoteClient;

    @Autowired
    private ContactInfoDao contactInfoDao;

    @Autowired
    private ApartmentDao apartmentDao;

    @Autowired
    private ApartmentPriceDao apartmentPriceDao;

    @Autowired
    private ApartmentExtensionService apartmentExtensionService;

    @Autowired
    private PaymentHistoryService paymentHistoryService;

    @Autowired
    private PaymentHistoryExtensionDao paymentHistoryExtensionDao;

    @Autowired
    private EnrollmentHistoryDao enrollmentHistoryDao;

    @Autowired
    private PaymentSummaryCalculator paymentSummaryCalculator;

    @Autowired
    private ExpiredDateCalculator dateCalculator;

    @Autowired
    private JPAQueryFactory queryFactory;

    public CustomerInfoView get(Integer customerId) {
        CustomerInfo po = customerDao.findById(customerId).orElseThrow(IllegalArgumentException::new);
        BeanCopier copier = BeanCopier.create(CustomerInfo.class, CustomerInfoView.class, false);
        CustomerInfoView result = CustomerInfoView.builder().build();
        copier.copy(po, result, null);
        List<FileInfo> files = fileRemoteClient.getFiles(appName, ModuleEnum.CUSTOMER.getModuleName(), String.valueOf(po.getId()));
        result.setFiles(files);
        return result;
    }

    public Page<CustomerInfoView> getCustomers(CustomerPageRequest request) {
        JPAQuery<Tuple> query = queryFactory
                .select(customerInfo.id, customerInfo.name, enrollmentHistory.enrollDate, apartmentInfo.id, apartmentInfo.name, customerInfo.status, customerInfo.idNum, customerInfo.isPrimary)
                .from(customerInfo)
                .innerJoin(apartmentInfo).on(customerInfo.apartment.id.eq(apartmentInfo.id).and(apartmentInfo.status.ne(ApartmentStatus.REMOVED)))
                .innerJoin(enrollmentHistory).on(customerInfo.id.eq(enrollmentHistory.customer.id))
                .where(buildConditionByFilter(request.getFilter()));
        long count = query.fetchCount();

        List<CustomerInfoView> content = query.offset(request.getPageRequest().getOffset()).limit(request.getPageRequest().getPageSize())
                .fetch().stream().map(t -> CustomerInfoView.builder()
                        .id(t.get(customerInfo.id)).status(t.get(customerInfo.status)).idNum(t.get(customerInfo.idNum)).name(t.get(customerInfo.name)).isPrimary(t.get(customerInfo.isPrimary))
                        .apartmentId(t.get(apartmentInfo.id)).apartmentName(t.get(apartmentInfo.name))
                        .enrollDate(t.get(enrollmentHistory.enrollDate))
                        .build()).collect(Collectors.toList());
        return new Page<>(count, content);
    }

    @Transactional
    @CacheEvict(value = LIST_APARTMENTS_CACHE_KEY, allEntries = true)
    public void updateCustomer(Integer customerId, CustomerUpdateForm updateForm) {
        CustomerInfo po = customerDao.findById(customerId).orElseThrow(IllegalArgumentException::new);
        po.setIdNum(updateForm.getIdNum());
        po.setName(updateForm.getName());
        po.setBirthday(updateForm.getBirthday());
        po.setGender(updateForm.getGender());
        customerDao.save(po);

        Example<ContactInfo> example = Example.of(new ContactInfo());
        example.getProbe().setCustomer(new CustomerInfo());
        example.getProbe().getCustomer().setId(customerId);
        List<ContactInfo> contacts = contactInfoDao.findAll(example);
        updateForm.getContacts().forEach((k, v) ->
            contacts.stream()
                    .filter(c -> c.getContactType() == ContactType.getFromCode(k))
                    .forEach(c -> c.setDetail(v)));
        contactInfoDao.saveAll(contacts);

        if (!CollectionUtils.isEmpty(updateForm.getFileIds())) {
            FileBindingInfo bindingInfo = FileBindingInfo.builder()
                    .externalId(String.valueOf(po.getId()))
                    .fileIds(updateForm.getFileIds())
                    .build();
            fileRemoteClient.bindFiles(Arrays.asList(bindingInfo));
        }
    }

    @Transactional
    @CacheEvict(value = LIST_APARTMENTS_CACHE_KEY, allEntries = true)
    public Integer enroll(Integer apartmentId, CustomerEnrollment enrollment) {
        ApartmentInfo apartmentInfo = apartmentDao.getOne(apartmentId);
        if (apartmentInfo.getStatus() == ApartmentStatus.OCCUPIED) {
            throw new IllegalStateException("Can not enroll due to apartment is occupied");
        }

        List<CustomerInfo> customerInfoList = saveApartmentCustomers(apartmentInfo, enrollment.getCustomers());
        CustomerInfo primaryCustomer = customerInfoList.stream().filter(CustomerInfo::getIsPrimary).findFirst().orElseThrow(IllegalArgumentException::new);

        saveApartmentEnrollment(apartmentInfo, primaryCustomer, enrollment);

        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setApartment(apartmentInfo);
        paymentHistory.setCustomer(primaryCustomer);
        paymentHistory.setPaymentType(PaymentType.getFromCode(enrollment.getPaymentType()));
        paymentHistory.setUnitPrice(enrollment.getConcertedPrice());
        paymentHistory.setQuantity(enrollment.getQuantity());
        paymentHistory.setFromDate(enrollment.getEnrollDate());
        paymentHistory.setToDate(dateCalculator.calculate(enrollment.getEnrollDate(), paymentHistory.getPaymentType(), enrollment.getQuantity()));
        ApartmentPaymentSummary summary = new ApartmentPaymentSummary();
        summary.setTotalPrice(BigDecimal.valueOf(enrollment.getQuantity()).multiply(enrollment.getConcertedPrice()).add(enrollment.getDeposit()));
        BeanCopier beanCopier = BeanCopier.create(ApartmentPremiumParams.class, PremiumPayment.class, false);
        List<PremiumPayment> premiumPayments = enrollment.getPremiums().stream()
                .filter(ApartmentPremiumParams::getSelected)
                .map(p -> {
                    if (p.getCurrentMeasurement() != null) {
                        PremiumPaymentByMeasurement premiumPayment = new PremiumPaymentByMeasurement();
                        beanCopier.copy(p, premiumPayment, null);
                        premiumPayment.setCurrentMeasurement(p.getCurrentMeasurement());
                        premiumPayment.setPremiumTotal(BigDecimal.ZERO);
                        premiumPayment.setCreatedTime(LocalDateTime.now());
                        return premiumPayment;
                    } else {
                        PremiumPaymentByDate premiumPayment = new PremiumPaymentByDate();
                        beanCopier.copy(p, premiumPayment, null);
                        premiumPayment.setToDate(LocalDate.now());
                        premiumPayment.setCreatedTime(LocalDateTime.now());
                        return premiumPayment;
                    }
                }).collect(Collectors.toList());
        summary.setPremiumPayments(premiumPayments);
        saveApartmentPayment(paymentHistory, enrollment.getReceipts(), summary);

        apartmentDao.updateStatusByApartmentId(ApartmentStatus.OCCUPIED, apartmentId);
        apartmentExtensionService.saveApartmentExtension(apartmentId, enrollment.getPremiums());

        return primaryCustomer.getId();
    }

    @Transactional
    @CacheEvict(value = LIST_APARTMENTS_CACHE_KEY, allEntries = true)
    public void exit(Integer apartmentId, CustomerExitForm exitForm) {
        List<CustomerInfo> customers = customerDao.findCurrentCustomersByApartmentId(apartmentId);
        customers.stream().filter(CustomerInfo::getIsPrimary).findFirst()
                .ifPresent(c -> {
                    CustomerPayment payment = new CustomerPayment();
                    payment.setReceipts(BigDecimal.ZERO.subtract(exitForm.getRefund()));
                    payment.setQuantity(0);
                    payment.setPremiums(exitForm.getPremiums());
                    pay(apartmentId, c.getId(), payment);
                });

        if (!CollectionUtils.isEmpty(exitForm.getPremiums())) {
            apartmentExtensionService.saveApartmentExtension(apartmentId, exitForm.getPremiums());
        }
        apartmentDao.updateStatusByApartmentId(ApartmentStatus.IDLE, apartmentId);
        customerDao.updateStatusByCustomerIds(CustomerStatus.EXITED, customers.stream().map(CustomerInfo::getId).collect(Collectors.toList()));
    }

    @Transactional
    @CacheEvict(value = LIST_APARTMENTS_CACHE_KEY, allEntries = true)
    public void pay(Integer apartmentId, Integer customerId, CustomerPayment payment) {
        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setQuantity(payment.getQuantity());

        ApartmentPaymentSummary summary = paymentSummaryCalculator.calculate(apartmentId, customerId, payment);
        paymentHistory.setPaymentType(summary.getPaymentType());
        paymentHistory.setUnitPrice(summary.getConcertedPrice());
        paymentHistory.setFromDate(summary.getFromDate());
        paymentHistory.setToDate(summary.getToDate());

        paymentHistory.setApartment(new ApartmentInfo());
        paymentHistory.getApartment().setId(apartmentId);
        paymentHistory.setCustomer(new CustomerInfo());
        paymentHistory.getCustomer().setId(customerId);

        saveApartmentPayment(paymentHistory, payment.getReceipts(), summary);
    }

    private BooleanExpression buildConditionByFilter(CustomerFilter filter) {
        BooleanExpression condition = customerInfo.status.ne(CustomerStatus.REMOVED);
        if (StringUtils.isNotBlank(filter.getName())) {
            condition = condition.and(customerInfo.name.startsWith(filter.getName()).or(apartmentInfo.name.startsWith(filter.getName())));
        }
        if (filter.getStatus() != null) {
            condition = condition.and(customerInfo.status.eq(filter.getStatus()));
        }

        return condition;
    }

    @Transactional
    private List<CustomerInfo> saveApartmentCustomers(ApartmentInfo apartmentInfo, List<Customer> customers) {
        BeanCopier customerCopier = BeanCopier.create(Customer.class, CustomerInfo.class, false);


        List<FileBindingInfo> bindingInfos = new ArrayList<>(customers.size());

        List<CustomerInfo> result = customers.parallelStream().map(c -> {
            CustomerInfo po = new CustomerInfo();
            customerCopier.copy(c, po, null);
            po.setApartment(apartmentInfo);
            po.setStatus(CustomerStatus.ENROLLED);
            customerDao.save(po);

            bindingInfos.add(FileBindingInfo.builder().externalId(String.valueOf(po.getId())).fileIds(c.getFileIds()).build());
            List<ContactInfo> contactInfos = c.getContacts().entrySet().stream().map(entry -> {
                ContactInfo contactInfo = new ContactInfo();
                contactInfo.setContactType(ContactType.getFromCode(entry.getKey()));
                contactInfo.setCustomer(po);
                contactInfo.setDetail(entry.getValue());
                return contactInfo;
            }).collect(Collectors.toList());
            contactInfoDao.saveAll(contactInfos);
            return po;
        }).collect(Collectors.toList());

        fileRemoteClient.bindFiles(bindingInfos);

        return result;
    }

    /**
     * Save apartment payment history
     *
     * @param paymentHistory
     * @param receipts
     * @param summary
     */
    @Transactional
    private void saveApartmentPayment(PaymentHistory paymentHistory, BigDecimal receipts, ApartmentPaymentSummary summary) {
        Assert.notNull(paymentHistory.getUnitPrice(), "unitPrice should not be null");
        Assert.notNull(paymentHistory.getQuantity(), "quantity should not be null");
        Assert.notNull(receipts, "receipts should not be null");
        Assert.notNull(summary, "summary should not be null");

        paymentHistory.setCreatedTime(LocalDateTime.now());
        paymentHistoryService.getPaymentHistoryDao().save(paymentHistory);
        saveApartmentPaymentExtension(paymentHistory.getId(), summary, receipts);
    }

    /**
     * Save apartment payment history extensions.
     *
     * @param paymentId
     * @param summary
     * @param receipts
     */
    @Transactional
    private void saveApartmentPaymentExtension(Integer paymentId, ApartmentPaymentSummary summary, BigDecimal receipts) {
        PaymentHistoryExtension paymentHistoryExtension = new PaymentHistoryExtension();
        paymentHistoryExtension.setPaymentHistoryId(paymentId);
        paymentHistoryExtension.setReceipts(receipts);
        paymentHistoryExtension.setTotalPrice(summary.getTotalPrice());
        paymentHistoryExtension.setPremiumPayments(summary.getPremiumPayments());
        paymentHistoryExtensionDao.save(paymentHistoryExtension);
    }

    @Transactional
    private void saveApartmentEnrollment(ApartmentInfo apartment, CustomerInfo customer, CustomerEnrollment enrollment) {
        PaymentType paymentType = PaymentType.getFromCode(enrollment.getPaymentType());
        ApartmentPrice chosenPrice = apartmentPriceDao.findByApartmentAndPaymentType(apartment, paymentType);
        EnrollmentHistory enrollmentHistory = new EnrollmentHistory();
        enrollmentHistory.setApartment(apartment);
        enrollmentHistory.setCustomer(customer);
        enrollmentHistory.setOriginalPrice(chosenPrice);
        enrollmentHistory.setConcertedPrice(enrollment.getConcertedPrice());
        enrollmentHistory.setCreateTime(LocalDateTime.now());
        enrollmentHistory.setEnrollDate(enrollment.getEnrollDate());
        enrollmentHistory.setDeposit(enrollment.getDeposit());
        enrollmentHistoryDao.save(enrollmentHistory);
    }
}
