package com.example.cinema.controller.public_api;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.CustomerResponse;
import com.example.cinema.service.user.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/customers")
public class PublicCustomerController {

    private final CustomerService customerService;

    public PublicCustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CustomerResponse>> searchByPhone(@RequestParam String phone) {
        CustomerResponse response = customerService.getCustomerByPhone(phone);
        if (response == null) {
            return ResponseEntity.ok(ApiResponse.error("Không tìm thấy thông tin hội viên", 404));
        }
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
