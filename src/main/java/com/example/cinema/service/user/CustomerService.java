package com.example.cinema.service.user;

import com.example.cinema.model.dto.request.UpdateProfileRequest;
import com.example.cinema.model.dto.response.CustomerResponse;
import com.example.cinema.model.entity.Customer;
import java.math.BigDecimal;
import java.util.List;

public interface CustomerService {
    CustomerResponse getMyProfile();

    CustomerResponse updateMyProfile(UpdateProfileRequest request);

    List<CustomerResponse> getAllCustomers();
    CustomerResponse getCustomerByPhone(String phone);
    CustomerResponse updateCustomerByAdmin(Long id, com.example.cinema.model.dto.request.AdminUpdateCustomerRequest request);
    BigDecimal getDiscountPercentage(String levelName);

    /**
     * Updates customer loyalty points and total spending after a successful
     * booking.
     * $1$ point is awarded for every $10,000$ VND spent.
     *
     * @param customer the customer to update
     * @param amount   the total amount spent in the booking
     */
    void addLoyaltyPoints(Customer customer, BigDecimal amount);
}
