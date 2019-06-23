package com.apartmentmanager.controller;

import com.apartmentmanager.constant.ApartmentStatus;
import com.apartmentmanager.dto.Page;
import com.apartmentmanager.dto.apartment.*;
import com.apartmentmanager.dto.customer.CustomerEnrollment;
import com.apartmentmanager.dto.customer.CustomerExitForm;
import com.apartmentmanager.dto.customer.CustomerPayment;
import com.apartmentmanager.remote.FileRemoteClient;
import com.apartmentmanager.service.ApartmentService;
import com.apartmentmanager.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/apartments")
public class ApartmentController {

    @Value("${spring.application.name}")
    private String app;
    @Autowired
    private ApartmentService apartmentService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private FileRemoteClient fileRemoteClient;

    @GetMapping
    public Page<ApartmentInfoView> getApartments(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize, @RequestParam(name = "name", required = false) String name, @RequestParam(name = "status", required = false) ApartmentStatus status) {
        PageRequest pageRequest = PageRequest.of(page > 0 ? page - 1 : 0, pageSize);
        ApartmentFilter filter = ApartmentFilter.builder()
                .name(name).status(status)
                .build();
        ApartmentPageRequest apartmentPageRequest = ApartmentPageRequest.builder().filter(filter).pageRequest(pageRequest).build();
        return apartmentService.getApartments(apartmentPageRequest);
    }

    @GetMapping("/{id}")
    public ApartmentInfoView getApartment(@PathVariable("id") Integer id) {
        return apartmentService.getApartment(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addApartment(@Valid @RequestBody ApartmentForm apartmentFormInfo) {
        apartmentService.addApartment(apartmentFormInfo);
    }

    @PutMapping("/{apartmentId}/enroll")
    public void register(@PathVariable("apartmentId") Integer apartmentId, @Valid @RequestBody CustomerEnrollment enrollment) {
        customerService.enroll(apartmentId, enrollment);
    }

    @PutMapping("/{apartmentId}")
    public void updateApartment(@PathVariable("apartmentId") Integer apartmentId, @Valid @RequestBody ApartmentUpdateForm form) {
        apartmentService.updateApartment(apartmentId, form);
    }

    @PutMapping("/{apartmentId}/pay")
    public void pay(@PathVariable("apartmentId")Integer apartmentId, @RequestParam("customerId") Integer customerId, @Valid @RequestBody CustomerPayment payment) {
        customerService.pay(apartmentId, customerId, payment);
    }

    @PutMapping("/{apartmentId}/exit")
    public void exit(@PathVariable("apartmentId") Integer apartmentId, @RequestBody CustomerExitForm exitForm) {
        customerService.exit(apartmentId, exitForm);
    }

    @DeleteMapping("/{id}")
    public void removeApartment(@PathVariable("id") Integer id) {
        apartmentService.removeApartment(id);
    }

}
