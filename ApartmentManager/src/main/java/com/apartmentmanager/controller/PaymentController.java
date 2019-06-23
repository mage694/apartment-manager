package com.apartmentmanager.controller;

import com.apartmentmanager.dto.Page;
import com.apartmentmanager.dto.payment.PaymentHistoryExtensionUpdateForm;
import com.apartmentmanager.dto.payment.PaymentHistoryFilter;
import com.apartmentmanager.dto.payment.PaymentHistoryPageRequest;
import com.apartmentmanager.dto.payment.PaymentHistoryView;
import com.apartmentmanager.service.PaymentHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentHistoryService paymentHistoryService;

    @GetMapping
    public Page<PaymentHistoryView> getPayments(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize, @RequestParam(name = "name", required = false) String name) {
        PageRequest pageRequest = PageRequest.of(page > 0 ? page - 1 : 0, pageSize);
        PaymentHistoryPageRequest request = PaymentHistoryPageRequest.builder()
                .pageRequest(pageRequest).filter(PaymentHistoryFilter.builder().name(name).build())
                .build();

        return paymentHistoryService.getPayments(request);
    }


    @DeleteMapping("/{paymentHistoryId}/revert")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void exit(@PathVariable("paymentHistoryId") Integer paymentId) {
        paymentHistoryService.revertPayment(paymentId);
    }

    @PutMapping("/{id}")
    public void updatePaymentExtension(@PathVariable("id") Integer id, @RequestBody PaymentHistoryExtensionUpdateForm updateForm) {
        paymentHistoryService.updatePaymentExtension(id, updateForm);
    }

}
