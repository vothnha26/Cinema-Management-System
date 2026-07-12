package com.example.cinema.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentReturnController {

    @GetMapping("/payment/return")
    public String paymentReturn(@RequestParam(name = "bookingCode", required = false) String bookingCode,
                                @RequestParam(name = "status", required = false) String status) {
        StringBuilder redirect = new StringBuilder("redirect:/history.html?tab=payments");
        if (bookingCode != null && !bookingCode.isBlank()) {
            redirect.append("&bookingCode=").append(bookingCode);
        }
        if (status != null && !status.isBlank()) {
            redirect.append("&status=").append(status);
        }
        return redirect.toString();
    }
}
