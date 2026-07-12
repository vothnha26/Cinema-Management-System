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
import java.util.stream.Collectors;

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

    private CustomerResponse mapToResponse(Customer customer) {
        return mapToResponseWithBenefits(customer, null);
    }

    private CustomerResponse mapToResponseWithBenefits(Customer customer, List<MembershipBenefit> preloadedBenefits) {
        CustomerResponse res = new CustomerResponse();
        res.setId(customer.getId());
        res.setFullName(customer.getFullName());
        res.setPhone(customer.getPhone());
        res.setPoints(customer.getPoints());
        res.setTotalSpending(customer.getTotalSpending());
        
        boolean isOfficial = customer.getUser() != null;
        res.setIsAccountLinked(isOfficial);

        if (customer.getMembershipLevel() != null) {
            res.setMembershipLevel(customer.getMembershipLevel().getName());
            res.setMembershipPriority(customer.getMembershipLevel().getPriority());
            
            // Lấy tỷ lệ giảm giá từ lợi ích hạng (sử dụng preloaded nếu có để tránh N+1 query)
            double rate = 0.0;
            if (preloadedBenefits != null) {
                rate = preloadedBenefits.stream()
                        .filter(b -> b.getMembershipLevel().getId().equals(customer.getMembershipLevel().getId()) 
                                && "DISCOUNT".equals(b.getBenefitType()))
                        .findFirst()
                        .map(b -> {
                            try { return Double.parseDouble(b.getBenefitValue()); }
                            catch(Exception e) { return 0.0; }
                        }).orElse(0.0);
            } else {
                rate = membershipBenefitRepository.findByMembershipLevelAndBenefitType(customer.getMembershipLevel(), "DISCOUNT")
                        .map(b -> {
                            try { return Double.parseDouble(b.getBenefitValue()); }
                            catch(Exception e) { return 0.0; }
                        }).orElse(0.0);
            }
            res.setDiscountRate(rate);
        } else {
            res.setMembershipLevel(isOfficial ? "STANDARD" : "GUEST");
            res.setMembershipPriority(isOfficial ? 1 : 0);
            res.setDiscountRate(0.0);
        }
        
        if (isOfficial) {
            res.setUsername(customer.getUser().getUsername());
            res.setEmail(customer.getUser().getEmail());
        } else {
            res.setEmail(customer.getEmail());
        }
        return res;
    }

    @Override
    public CustomerResponse getMyProfile() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            Customer customer = customerRepository.findByUserUsername(username)
                    .orElseThrow(() -> new com.example.cinema.exception.ResourceNotFoundException("Customer profile", "username", username));
            return mapToResponse(customer);
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
        List<Customer> customers = customerRepository.findAllWithUserAndLevel();
        List<MembershipBenefit> benefits = membershipBenefitRepository.findAll();
        return customers.stream()
                .map(c -> this.mapToResponseWithBenefits(c, benefits))
                .collect(Collectors.toList());
    }

    @Override
    public CustomerResponse getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone)
                .map(this::mapToResponse)
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

        return mapToResponse(customerRepository.save(customer));
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

        BigDecimal newSpending = customer.getTotalSpending().add(amount);
        customer.setTotalSpending(newSpending);

        double multiplier = 1.0;
        if (customer.getMembershipLevel() != null) {
            multiplier = membershipBenefitRepository.findByMembershipLevelAndBenefitType(customer.getMembershipLevel(), "POINT_MULTIPLIER")
                    .map(b -> Double.valueOf(b.getBenefitValue()))
                    .orElse(1.0);
        }

        int basePoints = amount.divide(BigDecimal.valueOf(10000), 0, java.math.RoundingMode.FLOOR).intValue();
        int additionalPoints = (int) (basePoints * multiplier);

        customer.setPoints(customer.getPoints() + additionalPoints);
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
