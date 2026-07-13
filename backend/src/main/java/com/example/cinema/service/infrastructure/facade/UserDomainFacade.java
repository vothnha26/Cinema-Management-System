package com.example.cinema.service.infrastructure.facade;

import com.example.cinema.model.entity.*;
import com.example.cinema.repository.user.*;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class UserDomainFacade {
    private final UserRepository userRepo;
    private final StaffRepository staffRepo;
    private final CustomerRepository customerRepo;

    public UserDomainFacade(UserRepository userRepo, StaffRepository staffRepo, 
                           CustomerRepository customerRepo) {
        this.userRepo = userRepo;
        this.staffRepo = staffRepo;
        this.customerRepo = customerRepo;
    }

    public Optional<User> findUserByUsername(String username) { 
        return userRepo.findByUsername(username).or(() -> userRepo.findByEmail(username)); 
    }
    
    public Optional<Staff> findStaffByUser(User u) { return staffRepo.findByUser(u); }
    public Optional<Customer> findCustomerByUser(User u) { return customerRepo.findByUser(u); }
    public Optional<Customer> findCustomerByUsername(String username) { return customerRepo.findByUserUsername(username); }
    public Optional<Customer> findCustomerByPhone(String phone) { return customerRepo.findByPhone(phone); }
    public Customer saveCustomer(Customer c) { return customerRepo.save(c); }
    public Optional<User> findUserByEmail(String email) { return userRepo.findByEmail(email); }
    public User saveUser(User u) { return userRepo.save(u); }
}
