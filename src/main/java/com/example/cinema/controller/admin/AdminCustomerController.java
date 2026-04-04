package com.example.cinema.controller.admin;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.CustomerResponse;
import com.example.cinema.service.user.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
public class AdminCustomerController {

    private final CustomerService customerService;

    public AdminCustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getAllCustomers()));
    }

    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<CustomerResponse>> lookupCustomer(@RequestParam String phone) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getCustomerByPhone(phone)));
    }
}
