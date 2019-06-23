package com.apartmentmanager.controller;

import com.apartmentmanager.service.FetchAdviser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/adviser")
public class FetchAdviserController {

    @Autowired
    private FetchAdviser fetchAdviser;

    @GetMapping("/names")
    public Set<String> adviseNames() {
        return fetchAdviser.adviseNames(null);
    }
}
