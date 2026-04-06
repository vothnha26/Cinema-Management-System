package com.example.cinema.config;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.*;
import com.example.cinema.repository.booking.BookingRepository;
import com.example.cinema.repository.booking.PaymentRepository;
import com.example.cinema.repository.commerce.ComboRepository;
import com.example.cinema.repository.commerce.PromotionRepository;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.repository.room.SeatRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.repository.user.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Configuration
@Order(2) // Run after DataSeeder
public class BigDataSeeder {

    @Bean
    public CommandLineRunner seedBigData(
            MovieRepository movieRepository,
            RoomRepository roomRepository,
            ShowtimeRepository showtimeRepository,
            BookingRepository bookingRepository,
            CustomerRepository customerRepository,
            SeatRepository seatRepository,
            ComboRepository comboRepository,
            PromotionRepository promotionRepository,
            PaymentRepository paymentRepository) {
        return args -> {
            if (bookingRepository.count() > 10) {
                System.out.println("⏭️ Dữ liệu lớn đã tồn tại, bỏ qua bước BigDataSeeder.");
                return;
            }

            System.out.println("🚀 Đang khởi tạo dữ liệu lớn cho Báo cáo & Thống kê...");

            List<Movie> movies = movieRepository.findAll();
            List<Room> rooms = roomRepository.findAll();
            List<Customer> customers = customerRepository.findAll();
            List<Combo> combos = comboRepository.findAll();
            List<Promotion> promotions = promotionRepository.findAll();

            if (movies.isEmpty() || rooms.isEmpty()) {
                System.out.println("❌ Thiếu Movie hoặc Room để tạo dữ liệu. Vui lòng kiểm tra DataSeeder.");
                return;
            }

            Random random = new Random();
            LocalDateTime now = LocalDateTime.now();

            // 1. Tạo thêm Combo nếu chưa có
            if (combos.isEmpty()) {
                combos = new ArrayList<>();
                combos.add(comboRepository.save(new Combo(null, "Combo Đơn", "1 Bắp + 1 Nước", BigDecimal.valueOf(85000), "https://res.cloudinary.com/dynd7id7f/image/upload/v1715421255/combo1.png", 100, true)));
                combos.add(comboRepository.save(new Combo(null, "Combo Đôi", "1 Bắp + 2 Nước", BigDecimal.valueOf(115000), "https://res.cloudinary.com/dynd7id7f/image/upload/v1715421255/combo2.png", 100, true)));
                combos.add(comboRepository.save(new Combo(null, "Combo Gia Đình", "2 Bắp + 4 Nước", BigDecimal.valueOf(220000), "https://res.cloudinary.com/dynd7id7f/image/upload/v1715421255/combo3.png", 100, true)));
            }

            // 2. Tạo Suất chiếu trong 30 ngày qua
            System.out.println("📅 Tạo suất chiếu lịch sử...");
            for (int i = 0; i < 30; i++) {
                LocalDateTime date = now.minusDays(i).withHour(8).withMinute(0);
                for (Room room : rooms) {
                    List<Seat> availableSeats = seatRepository.findByRoomId(room.getId());
                    if (availableSeats.isEmpty()) continue;

                    // Mỗi phòng 4 suất chiếu/ngày
                    for (int j = 0; j < 4; j++) {
                        Movie movie = movies.get(random.nextInt(movies.size()));
                        LocalDateTime startTime = date.plusHours(j * 4);
                        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());
                        
                        Showtime showtime = new Showtime();
                        showtime.setMovie(movie);
                        showtime.setRoom(room);
                        showtime.setFormat(movie.getFormats().stream().findFirst().orElse(null));
                        showtime.setStartTime(startTime);
                        showtime.setEndTime(endTime);
                        showtime.setStatus(startTime.isBefore(now) ? ShowtimeStatus.ENDED : ShowtimeStatus.UPCOMING);
                        showtime.setTotalSeats(availableSeats.size());
                        showtime = showtimeRepository.save(showtime);

                        // 3. Tạo Booking cho mỗi suất chiếu
                        int numBookings = random.nextInt(15) + 5; // 5-20 đơn mỗi suất
                        for (int k = 0; k < numBookings; k++) {
                            Booking booking = new Booking();
                            booking.setShowtime(showtime);
                            booking.setBookingCode("ST" + showtime.getId() + "B" + k + UUID.randomUUID().toString().substring(0,4).toUpperCase());
                            
                            // 70% là khách hàng đã đăng ký, 30% vãng lai
                            if (random.nextDouble() < 0.7 && !customers.isEmpty()) {
                                booking.setCustomer(customers.get(random.nextInt(customers.size())));
                            }

                            // Trạng thái đơn hàng
                            double p = random.nextDouble();
                            if (startTime.isBefore(now)) {
                                booking.setStatus(p < 0.9 ? BookingStatus.CONFIRMED : BookingStatus.CANCELLED);
                            } else {
                                booking.setStatus(p < 0.8 ? BookingStatus.CONFIRMED : BookingStatus.PENDING);
                            }

                            // Details (Tickets)
                            int numTickets = random.nextInt(4) + 1;
                            List<BookingDetail> details = new ArrayList<>();
                            BigDecimal ticketSum = BigDecimal.ZERO;
                            
                            for (int t = 0; t < numTickets; t++) {
                                Seat seat = availableSeats.get(random.nextInt(availableSeats.size()));
                                BookingDetail detail = new BookingDetail();
                                detail.setBooking(booking);
                                detail.setSeat(seat);
                                detail.setPrice(BigDecimal.valueOf(85000 + (random.nextInt(5) * 10000)));
                                details.add(detail);
                                ticketSum = ticketSum.add(detail.getPrice());
                            }
                            booking.setDetails(details);

                            // Combos
                            List<BookingCombo> bookingCombos = new ArrayList<>();
                            if (random.nextDouble() < 0.4) {
                                Combo combo = combos.get(random.nextInt(combos.size()));
                                BookingCombo bc = new BookingCombo();
                                bc.setBooking(booking);
                                bc.setCombo(combo);
                                bc.setQuantity(random.nextInt(2) + 1);
                                bc.setPrice(combo.getPrice());
                                bookingCombos.add(bc);
                                ticketSum = ticketSum.add(bc.getPrice().multiply(BigDecimal.valueOf(bc.getQuantity())));
                            }
                            booking.setCombos(bookingCombos);

                            // Promotion
                            if (random.nextDouble() < 0.2 && !promotions.isEmpty()) {
                                Promotion promo = promotions.get(random.nextInt(promotions.size()));
                                booking.setPromotion(promo);
                                ticketSum = ticketSum.subtract(promo.getDiscountValue());
                            }

                            booking.setTotalPrice(ticketSum.max(BigDecimal.ZERO));
                            booking.setCreatedAt(startTime.minusHours(random.nextInt(48) + 1));
                            
                            Booking savedBooking = bookingRepository.save(booking);

                            // 4. Tạo Payment nếu đã thanh toán
                            if (booking.getStatus() != BookingStatus.PENDING) {
                                Payment payment = new Payment();
                                payment.setBooking(savedBooking);
                                payment.setAmount(savedBooking.getTotalPrice());
                                payment.setPaymentMethod(random.nextBoolean() ? PaymentMethod.CASH : PaymentMethod.VNPAY);
                                payment.setPaymentStatus(booking.getStatus() == BookingStatus.CANCELLED ? PaymentStatus.REFUNDED : PaymentStatus.SUCCESS);
                                payment.setTransactionId("TXN" + System.nanoTime());
                                payment.setPaidAt(booking.getCreatedAt().plusMinutes(random.nextInt(15)));
                                paymentRepository.save(payment);
                            }
                        }
                    }
                }
                if (i % 5 == 0) System.out.println("... Đã xong dữ liệu ngày thứ " + i);
            }

            System.out.println("✅ Hoàn tất BigDataSeeder: Hàng ngàn đơn hàng đã được tạo!");
        };
    }
}
