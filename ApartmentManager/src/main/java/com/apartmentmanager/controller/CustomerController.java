package com.apartmentmanager.controller;

import com.apartmentmanager.constant.CustomerStatus;
import com.apartmentmanager.constant.ModuleEnum;
import com.apartmentmanager.dto.Page;
import com.apartmentmanager.dto.customer.CustomerFilter;
import com.apartmentmanager.dto.customer.CustomerInfoView;
import com.apartmentmanager.dto.customer.CustomerPageRequest;
import com.apartmentmanager.dto.customer.CustomerUpdateForm;
import com.apartmentmanager.remote.FileRemoteClient;
import com.apartmentmanager.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import static com.apartmentmanager.remote.FileRemoteClient.FileForm;
import static com.apartmentmanager.remote.FileRemoteClient.IdCardInfo;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Value("${spring.application.name}")
    private String app;

    @Autowired
    private FileRemoteClient fileRemoteClient;
    @Autowired
    private CustomerService customerService;

    @GetMapping("/{customerId}")
    public CustomerInfoView getCustomer(@PathVariable("customerId") Integer customerId) {
        return customerService.get(customerId);
    }

    @PutMapping("/{customerId}")
    public void updateCustomer(@PathVariable("customerId") Integer customerId, @Valid @RequestBody CustomerUpdateForm updateForm) {
        customerService.updateCustomer(customerId, updateForm);
    }

    @GetMapping
    public Page<CustomerInfoView> getCustomers(@RequestParam("page") int page
            , @RequestParam("pageSize") int pageSize
            , @RequestParam(name = "name", required = false) String name
            , @RequestParam(name = "status", required = false) CustomerStatus status) {

        PageRequest pageRequest = PageRequest.of(page > 0 ? page - 1 : 0, pageSize);
        CustomerPageRequest request = CustomerPageRequest.builder()
                .filter(CustomerFilter.builder().name(name).status(status).build())
                .pageRequest(pageRequest)
                .build();
        return customerService.getCustomers(request);
    }
}
