package com.example.cinema.service.booking.infrastructure;

import com.example.cinema.model.entity.*;
import com.example.cinema.repository.booking.*;
import com.example.cinema.repository.user.CustomerRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class BookingRepositoryFacade {
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository detailRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;

    public BookingRepositoryFacade(BookingRepository bookingRepository, 
                                  BookingDetailRepository detailRepository, 
                                  PaymentRepository paymentRepository, 
                                  CustomerRepository customerRepository) {
        this.bookingRepository = bookingRepository;
        this.detailRepository = detailRepository;
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
    }

    public Booking save(Booking b) { return bookingRepository.save(b); }
    public Optional<Booking> findByCode(String code) { return bookingRepository.findByBookingCode(code); }
    public Customer saveCustomer(Customer c) { return customerRepository.save(c); }
    public Optional<Customer> findCustomerByPhone(String phone) { return customerRepository.findFirstByPhoneOrderByIdDesc(phone); }
    public Optional<Customer> findCustomerByUsername(String user) { return customerRepository.findFirstByUserUsernameOrderByIdDesc(user); }
    public void savePayment(Payment p) { paymentRepository.save(p); }
    public java.util.List<Long> findBookedSeatIds(Long showtimeId) { return detailRepository.findBookedSeatIdsByShowtime(showtimeId); }
    public java.util.List<Booking> findByCustomerId(Long cId) { return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(cId); }
    public java.util.List<Booking> findAll() { return bookingRepository.findAll(); }
    public long countByShowtime(Long sId) { return bookingRepository.countByShowtimeId(sId); }
}
