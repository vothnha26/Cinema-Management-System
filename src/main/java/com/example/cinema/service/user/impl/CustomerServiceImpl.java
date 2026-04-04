package com.example.cinema.service.user.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.UpdateProfileRequest;
import com.example.cinema.model.dto.response.CustomerResponse;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.MembershipBenefit;
import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.MembershipTier;
import com.example.cinema.model.enums.NotificationType;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.repository.user.MembershipBenefitRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.notification.INotificationAutomationService;
import com.example.cinema.service.user.CustomerService;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final MembershipBenefitRepository membershipBenefitRepository;
    private final ModelMapper modelMapper;
    private final INotificationAutomationService notificationAutomationService;

    public CustomerServiceImpl(CustomerRepository customerRepository,
            UserRepository userRepository,
            MembershipBenefitRepository membershipBenefitRepository,
            ModelMapper modelMapper,
            INotificationAutomationService notificationAutomationService) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.membershipBenefitRepository = membershipBenefitRepository;
        this.modelMapper = modelMapper;
        this.notificationAutomationService = notificationAutomationService;
    }

    @Override
    public CustomerResponse getMyProfile() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            Customer customer = customerRepository.findByUserUsername(username)
                    .orElseThrow(() -> new AppException("Customer profile not found"));

            CustomerResponse response = modelMapper.map(customer, CustomerResponse.class);
            response.setUsername(customer.getUser().getUsername());
            response.setEmail(customer.getUser().getEmail());
            return response;
        }
        throw new AppException("Unauthorized");
    }

    @Override
    @Transactional
    public CustomerResponse updateMyProfile(UpdateProfileRequest request) {
        Customer customer = getCurrentCustomerEntity();
        User user = customer.getUser();

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (!normalizedEmail.equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(normalizedEmail)) {
            throw new AppException("Email is already taken");
        }

        customer.setFullName(request.getFullName().trim());
        customer.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setEmail(normalizedEmail);

        customerRepository.save(customer);
        userRepository.save(user);
        return getMyProfile();
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(c -> modelMapper.map(c, CustomerResponse.class))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public BigDecimal getDiscountPercentage(MembershipTier tier) {
        if (tier == null)
            return BigDecimal.ZERO;
        return membershipBenefitRepository.findByTier(tier)
                .map(b -> BigDecimal.valueOf(b.getDiscountPercent()))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public void addLoyaltyPoints(Customer customer, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            return;

        // 1. Cập nhật tổng chi tiêu
        BigDecimal newSpending = customer.getTotalSpending().add(amount);
        customer.setTotalSpending(newSpending);

        // 2. Tính điểm thưởng dựa trên multiplier của hạng
        double multiplier = membershipBenefitRepository.findByTier(customer.getMembershipTier())
                .map(MembershipBenefit::getPointMultiplier)
                .orElse(1.0);

        int basePoints = amount.divide(BigDecimal.valueOf(10000), 0, java.math.RoundingMode.FLOOR).intValue();
        int additionalPoints = (int) (basePoints * multiplier);

        customer.setPoints(customer.getPoints() + additionalPoints);

        // 3. Cập nhật hạng thành viên
        updateMembershipTier(customer);

        customerRepository.save(customer);
    }

    private void updateMembershipTier(Customer customer) {
        BigDecimal spent = customer.getTotalSpending();
        MembershipTier currentTier = customer.getMembershipTier();
        MembershipTier newTier;

        if (spent.compareTo(BigDecimal.valueOf(5000000)) >= 0) {
            newTier = MembershipTier.PLATINUM;
        } else if (spent.compareTo(BigDecimal.valueOf(2000000)) >= 0) {
            newTier = MembershipTier.GOLD;
        } else if (spent.compareTo(BigDecimal.valueOf(500000)) >= 0) {
            newTier = MembershipTier.SILVER;
        } else {
            newTier = MembershipTier.STANDARD;
        }

        if (newTier != currentTier) {
            customer.setMembershipTier(newTier);
            notificationAutomationService.notifyUser(
                    customer.getUser(),
                    "Nang hang thanh vien",
                    "Chuc mung ban da duoc nang hang tu " + currentTier + " len " + newTier + ".",
                    NotificationType.SYSTEM);
        }
    }

    private Customer getCurrentCustomerEntity() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return customerRepository.findByUserUsername(userDetails.getUsername())
                    .orElseThrow(() -> new AppException("Customer profile not found"));
        }
        throw new AppException("Unauthorized");
    }
}
