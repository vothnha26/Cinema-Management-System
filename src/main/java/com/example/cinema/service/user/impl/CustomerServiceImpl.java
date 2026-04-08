package com.example.cinema.service.user.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.UpdateProfileRequest;
import com.example.cinema.model.dto.response.CustomerResponse;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.MembershipBenefit;
import com.example.cinema.model.entity.User;
import com.example.cinema.model.entity.MembershipLevel;
import com.example.cinema.model.enums.NotificationType;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.repository.user.MembershipBenefitRepository;
import com.example.cinema.repository.user.MembershipLevelRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.notification.INotificationAutomationService;
import com.example.cinema.service.user.CustomerService;
import java.util.List;
import java.util.Comparator;

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
    private final MembershipLevelRepository membershipLevelRepository;
    private final MembershipBenefitRepository membershipBenefitRepository;
    private final ModelMapper modelMapper;
    private final INotificationAutomationService notificationAutomationService;

    public CustomerServiceImpl(CustomerRepository customerRepository,
            UserRepository userRepository,
            MembershipLevelRepository membershipLevelRepository,
            MembershipBenefitRepository membershipBenefitRepository,
            ModelMapper modelMapper,
            INotificationAutomationService notificationAutomationService) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.membershipLevelRepository = membershipLevelRepository;
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
                    .orElseThrow(() -> new com.example.cinema.exception.ResourceNotFoundException("Customer profile", "username", username));

            CustomerResponse response = modelMapper.map(customer, CustomerResponse.class);
            response.setUsername(customer.getUser().getUsername());
            response.setEmail(customer.getUser().getEmail());
            if (customer.getMembershipLevel() != null) {
                response.setMembershipLevel(customer.getMembershipLevel().getName());
            }
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
                .map(c -> {
                    CustomerResponse response = modelMapper.map(c, CustomerResponse.class);
                    if (c.getUser() != null) {
                        response.setUsername(c.getUser().getUsername());
                        response.setEmail(c.getUser().getEmail());
                    }
                    if (c.getMembershipLevel() != null) {
                        response.setMembershipLevel(c.getMembershipLevel().getName());
                    }
                    return response;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public CustomerResponse getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone)
                .map(c -> {
                    CustomerResponse response = modelMapper.map(c, CustomerResponse.class);
                    if (c.getMembershipLevel() != null) {
                        response.setMembershipLevel(c.getMembershipLevel().getName());
                    }
                    return response;
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomerByAdmin(Long id, com.example.cinema.model.dto.request.AdminUpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException("Customer not found"));

        customer.setFullName(request.getFullName().trim());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        
        if (request.getMembershipLevel() != null) {
            MembershipLevel level = membershipLevelRepository.findByName(request.getMembershipLevel())
                .orElseThrow(() -> new AppException("Membership level not found: " + request.getMembershipLevel()));
            customer.setMembershipLevel(level);
        }
        
        customer.setPoints(request.getPoints());
        customer.setTotalSpending(request.getTotalSpending());

        // Update associated user if exists
        if (customer.getUser() != null) {
            User user = customer.getUser();
            if (request.getEmail() != null) {
                String normalizedEmail = request.getEmail().trim().toLowerCase();
                if (!normalizedEmail.equalsIgnoreCase(user.getEmail())
                        && userRepository.existsByEmail(normalizedEmail)) {
                    throw new AppException("Email is already taken");
                }
                user.setEmail(normalizedEmail);
                userRepository.save(user);
            }
        }

        customerRepository.save(customer);
        return modelMapper.map(customer, CustomerResponse.class);
    }

    @Override
    public BigDecimal getDiscountPercentage(String levelName) {
        if (levelName == null)
            return BigDecimal.ZERO;
        
        return membershipLevelRepository.findByName(levelName)
                .flatMap(level -> membershipBenefitRepository.findByMembershipLevelAndBenefitType(level, "DISCOUNT"))
                .map(b -> new BigDecimal(b.getBenefitValue()))
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

        // 2. Tính điểm thưởng dựa trên quy tắc của hạng
        double multiplier = 1.0;
        if (customer.getMembershipLevel() != null) {
            multiplier = membershipBenefitRepository.findByMembershipLevelAndBenefitType(customer.getMembershipLevel(), "POINT_MULTIPLIER")
                    .map(b -> Double.valueOf(b.getBenefitValue()))
                    .orElse(1.0);
        }

        int basePoints = amount.divide(BigDecimal.valueOf(10000), 0, java.math.RoundingMode.FLOOR).intValue();
        int additionalPoints = (int) (basePoints * multiplier);

        customer.setPoints(customer.getPoints() + additionalPoints);

        // 3. Cập nhật hạng thành viên
        updateMembershipLevel(customer);

        customerRepository.save(customer);
    }

    private void updateMembershipLevel(Customer customer) {
        BigDecimal spent = customer.getTotalSpending();
        MembershipLevel currentLevel = customer.getMembershipLevel();
        
        List<MembershipLevel> allLevels = membershipLevelRepository.findAll();
        MembershipLevel newLevel = allLevels.stream()
                .filter(l -> spent.compareTo(l.getMinSpending()) >= 0)
                .max(Comparator.comparingInt(MembershipLevel::getPriority))
                .orElse(currentLevel);

        if (newLevel != null && (currentLevel == null || !newLevel.getId().equals(currentLevel.getId()))) {
            customer.setMembershipLevel(newLevel);
            notificationAutomationService.notifyUser(
                    customer.getUser(),
                    "Nang hang thanh vien",
                    "Chuc mung ban da duoc nang hang tu " + (currentLevel != null ? currentLevel.getName() : "GUEST") + " len " + newLevel.getName() + ".",
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
