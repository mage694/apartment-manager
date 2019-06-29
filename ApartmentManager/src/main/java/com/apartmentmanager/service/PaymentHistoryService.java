package com.apartmentmanager.service;

import com.apartmentmanager.constant.ApartmentStatus;
import com.apartmentmanager.constant.CustomerStatus;
import com.apartmentmanager.dao.PaymentHistoryDao;
import com.apartmentmanager.dao.ext.ApartmentExtensionDao;
import com.apartmentmanager.dao.ext.PaymentHistoryExtensionDao;
import com.apartmentmanager.dto.Page;
import com.apartmentmanager.dto.apartment.ApartmentInfoView;
import com.apartmentmanager.dto.payment.PaymentHistoryExtensionUpdateForm;
import com.apartmentmanager.dto.payment.PaymentHistoryFilter;
import com.apartmentmanager.dto.payment.PaymentHistoryPageRequest;
import com.apartmentmanager.dto.payment.PaymentHistoryView;
import com.apartmentmanager.po.payment.ext.PaymentHistoryExtension;
import com.apartmentmanager.service.datecalculator.IDateCalculator;
import com.apartmentmanager.service.premiumprocessor.PremiumProcessorDelegate;
import com.apartmentmanager.service.premiumresolver.ApartmentPremiumParamsResolver;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.apartmentmanager.po.apartment.QApartmentInfo.apartmentInfo;
import static com.apartmentmanager.po.customer.QCustomerInfo.customerInfo;
import static com.apartmentmanager.po.payment.QPaymentHistory.paymentHistory;
import static com.apartmentmanager.service.ApartmentService.LIST_APARTMENTS_CACHE_KEY;


@Service
public class PaymentHistoryService {
    @Getter
    @Autowired
    private PaymentHistoryDao paymentHistoryDao;

    @Getter
    @Autowired
    private PaymentHistoryExtensionDao paymentHistoryExtensionDao;

    @Autowired
    private ApartmentExtensionDao apartmentExtensionDao;

    @Autowired
    private IDateCalculator dateCalculator;

    @Autowired
    private PremiumProcessorDelegate premiumProcessorDelegate;

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private ApartmentPremiumParamsResolver premiumParamsResolver;

    public Page<PaymentHistoryView> getPayments(PaymentHistoryPageRequest pageRequest) {
        JPAQuery<Tuple> query = queryFactory
                .select(paymentHistory.id, paymentHistory.fromDate, paymentHistory.toDate, paymentHistory.paymentType, paymentHistory.quantity, paymentHistory.unitPrice
                        , customerInfo.name, apartmentInfo.id, apartmentInfo.name)
                .from(paymentHistory)
                .innerJoin(apartmentInfo).on(paymentHistory.apartment.id.eq(apartmentInfo.id).and(apartmentInfo.status.ne(ApartmentStatus.REMOVED)))
                .innerJoin(customerInfo).on(paymentHistory.customer.id.eq(customerInfo.id).and(customerInfo.isPrimary.eq(true)));
        Optional.ofNullable(buildConditionByFilter(pageRequest.getFilter())).ifPresent(query::where);

        long count = query.fetchCount();

        List<PaymentHistoryView> content = query.orderBy(paymentHistory.id.desc())
                .offset(pageRequest.getPageRequest().getOffset()).limit(pageRequest.getPageRequest().getPageSize())
                .fetch().stream()
                .map(t -> PaymentHistoryView.builder()
                        .id(t.get(paymentHistory.id)).fromDate(t.get(paymentHistory.fromDate)).toDate(t.get(paymentHistory.toDate)).paymentType(t.get(paymentHistory.paymentType)).quantity(t.get(paymentHistory.quantity)).unitPrice(t.get(paymentHistory.unitPrice))
                        .primaryCustomerName(t.get(customerInfo.name)).apartmentId(t.get(apartmentInfo.id)).apartmentName(t.get(apartmentInfo.name))
                        .build())
                .collect(Collectors.toList());

        bindExtraPaymentInfo(content);

        Page<PaymentHistoryView> result = new Page<>(count, content);

        return result;
    }


    public void bindApartmentLatestPaymentExt(List<ApartmentInfoView> apartmentInfoViews) {
        if (CollectionUtils.isEmpty(apartmentInfoViews)) {
            return;
        }

        List<ApartmentInfoView> hasPaymentApartments = apartmentInfoViews.stream()
                .filter(apartmentInfoView -> apartmentInfoView.getStatus() == ApartmentStatus.OCCUPIED).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(hasPaymentApartments)) {
            return;
        }

        List<Integer> paymentIds = hasPaymentApartments.stream().map(a -> a.getLatestPayment().getId()).collect(Collectors.toList());
        Iterable<PaymentHistoryExtension> extensions = paymentHistoryExtensionDao.findAllById(paymentIds);
        extensions.forEach(ext ->
                hasPaymentApartments.stream()
                        .map(ApartmentInfoView::getLatestPayment)
                        .filter(payment -> payment.getId().equals(ext.getPaymentHistoryId()))
                        .findFirst()
                        .ifPresent(payment -> payment.setExtension(ext)));

    }

    @Transactional
    @CacheEvict(value = LIST_APARTMENTS_CACHE_KEY, allEntries = true)
    public void revertPayment(Integer paymentId) {
        paymentHistoryDao.deleteById(paymentId);
        paymentHistoryExtensionDao.deleteById(paymentId);
    }

    @Transactional
    @CacheEvict(value = LIST_APARTMENTS_CACHE_KEY, allEntries = true)
    public void updatePaymentExtension(Integer paymentId, PaymentHistoryExtensionUpdateForm form) {
        PaymentHistoryExtension extension = paymentHistoryExtensionDao.findById(paymentId).orElseThrow(IllegalArgumentException::new);
        extension.setReceipts(form.getReceipts());
        extension.setTotalPrice(form.getTotalPrice());
        premiumParamsResolver.resolve(form.getPremiums(), extension.getPremiumPayments());
        paymentHistoryExtensionDao.save(extension);
    }

    private BooleanExpression buildConditionByFilter(PaymentHistoryFilter filter) {
        BooleanExpression condition = null;
        if (StringUtils.isNotBlank(filter.getName())) {
            condition = apartmentInfo.name.startsWith(filter.getName()).or(customerInfo.name.startsWith(filter.getName()));
        }
        return condition;
    }

    private void bindExtraPaymentInfo(List<PaymentHistoryView> paymentHistoryViews) {
        if (CollectionUtils.isEmpty(paymentHistoryViews)) {
            return;
        }

        Set<Integer> apartmentIdSet = paymentHistoryViews.stream().map(PaymentHistoryView::getApartmentId).collect(Collectors.toSet());
        Set<Integer> canBeRevertedPaymentIdSet = findCanBeRevertedPaymentIdSet(apartmentIdSet);
        paymentHistoryViews.stream().filter(p -> canBeRevertedPaymentIdSet.contains(p.getId()))
                .forEach(p -> p.setCanBeReverted(true));

        List<Integer> paymentIds = paymentHistoryViews.stream().map(PaymentHistoryView::getId).collect(Collectors.toList());
        Iterable<PaymentHistoryExtension> extensions = paymentHistoryExtensionDao.findAllById(paymentIds);
        extensions.forEach(ext ->
                paymentHistoryViews.stream()
                        .filter(v -> v.getId().equals(ext.getPaymentHistoryId()))
                        .peek(v -> v.setTotalPrice(ext.getTotalPrice()))
                        .peek(v -> v.setReceipts(ext.getReceipts()))
                        .forEach(v -> v.setPremiums(ext.getPremiumPayments()))
        );
    }

    public Set<Integer> findFirstPaymentIdSet(Set<Integer> apartmentIdSet) {
        return findLatestAndFirstPaymentIds(apartmentIdSet)
                .stream().map(t -> t.get(1, Integer.class)).collect(Collectors.toSet());
    }

    /**
     * Find first and last payment id tuple via apartmentIds.
     * <p>index 0: latestPaymentId, index 1: firstPaymentId.</p>
     *
     * @param apartmentIdSet
     * @return
     */
    private List<Tuple> findLatestAndFirstPaymentIds(Set<Integer> apartmentIdSet) {
        JPAQuery<Tuple> query = queryFactory
                .select(paymentHistory.id.max(), paymentHistory.id.min())
                .from(paymentHistory)
                .innerJoin(customerInfo).on(paymentHistory.customer.id.eq(customerInfo.id).and(customerInfo.status.eq(CustomerStatus.ENROLLED)))
                .where(paymentHistory.apartment.id.in(apartmentIdSet))
                .groupBy(paymentHistory.customer.id);

        return query.fetch();
    }

    /**
     * Find payment id which can be reverted.
     * <p>Only latest payment history (expect first payment history) can be reverted.</p>
     *
     * @param apartmentIdSet
     * @return
     */
    private Set<Integer> findCanBeRevertedPaymentIdSet(Set<Integer> apartmentIdSet) {
        List<Tuple> tupleList = findLatestAndFirstPaymentIds(apartmentIdSet);

        return tupleList.stream().filter(t -> !t.get(0, Integer.class).equals(t.get(1, Integer.class)))
                .map(t -> t.get(0, Integer.class)).collect(Collectors.toSet());
    }
}
