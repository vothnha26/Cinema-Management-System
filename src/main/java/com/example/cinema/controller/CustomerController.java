package com.example.cinema.controller;

import com.example.cinema.model.dto.request.UpdateProfileRequest;
import com.example.cinema.model.dto.response.CustomerResponse;
import com.example.cinema.service.user.CustomerService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile() throws Exception {
        return ResponseEntity.ok(customerService.getMyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateMyProfile(@RequestBody @Valid UpdateProfileRequest request) {
        return ResponseEntity.ok(customerService.updateMyProfile(request));
    }
}
