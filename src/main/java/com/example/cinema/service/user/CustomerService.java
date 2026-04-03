package com.example.cinema.service.user;

import com.example.cinema.model.dto.request.UpdateProfileRequest;
import com.example.cinema.model.dto.response.CustomerResponse;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.enums.MembershipTier;

import java.math.BigDecimal;

public interface CustomerService {
    CustomerResponse getMyProfile() throws Exception;

    CustomerResponse updateMyProfile(UpdateProfileRequest request);

    BigDecimal getDiscountPercentage(MembershipTier tier);

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
