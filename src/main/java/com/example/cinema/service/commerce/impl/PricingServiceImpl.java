package com.example.cinema.service.commerce.impl;

import com.example.cinema.config.LogAction;
import com.example.cinema.model.dto.request.SeatPriceRequest;
import com.example.cinema.model.dto.response.SeatPriceResponse;
import com.example.cinema.model.entity.SeatPrice;
import com.example.cinema.repository.room.SeatPriceRepository;
import com.example.cinema.service.commerce.PricingService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PricingServiceImpl implements PricingService {

        private final SeatPriceRepository seatPriceRepository;
        private final ModelMapper modelMapper;

        public PricingServiceImpl(SeatPriceRepository seatPriceRepository, ModelMapper modelMapper) {
                this.seatPriceRepository = seatPriceRepository;
                this.modelMapper = modelMapper;
        }

        @Override
        public List<SeatPriceResponse> getAllSeatPrices() {
                return seatPriceRepository.findAll().stream()
                                .map(sp -> modelMapper.map(sp, SeatPriceResponse.class))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        @LogAction(action = "UPDATE", target = "PRICING")
        public SeatPriceResponse updateSeatPrice(SeatPriceRequest request) {
                // 1. Vô hiệu hóa tất cả các cấu hình giá cũ của cùng loại phòng/ghế
                List<SeatPrice> oldPrices = seatPriceRepository
                                .findAllByRoomTypeAndSeatTypeAndIsActiveTrue(request.getRoomType(),
                                                request.getSeatType());

                for (SeatPrice old : oldPrices) {
                        old.setIsActive(false);
                }
                seatPriceRepository.saveAll(oldPrices);

                // 2. Kiểm tra xem đã có cấu hình cho ngày hiệu lực này chưa
                SeatPrice seatPrice = seatPriceRepository
                                .findByRoomTypeAndSeatTypeAndEffectiveDate(request.getRoomType(), request.getSeatType(),
                                                request.getEffectiveDate())
                                .orElse(new SeatPrice());

                seatPrice.setRoomType(request.getRoomType());
                seatPrice.setSeatType(request.getSeatType());
                seatPrice.setPrice(request.getPrice());
                seatPrice.setEffectiveDate(request.getEffectiveDate());
                seatPrice.setIsActive(true);

                SeatPrice saved = seatPriceRepository.save(seatPrice);
                return modelMapper.map(saved, SeatPriceResponse.class);
        }

        @Override
        public BigDecimal calculateTicketPrice(com.example.cinema.model.entity.Showtime showtime,
                        com.example.cinema.model.entity.Seat seat) {
                // Lấy giá gốc cho loại ghế và loại phòng từ DB
                BigDecimal basePrice = seatPriceRepository
                                .findByRoomTypeAndSeatTypeAndIsActiveTrue(showtime.getRoom().getType(), seat.getType())
                                .map(SeatPrice::getPrice)
                                .orElse(new BigDecimal("80000.00"));

                // Áp dụng Decorator Pattern để tính toán
                com.example.cinema.service.commerce.pricing.PriceCalculator calculator = new com.example.cinema.service.commerce.pricing.BasePriceCalculator(
                                basePrice);

                // Bọc thêm lớp Room Type Surcharge
                calculator = new com.example.cinema.service.commerce.pricing.RoomTypeDecorator(calculator,
                                showtime.getRoom().getType());

                // Bọc thêm lớp Seat Type Surcharge
                calculator = new com.example.cinema.service.commerce.pricing.SeatTypeDecorator(calculator,
                                seat.getType());

                // Bọc thêm lớp Day of Week (Monday/Tuesday discounts, Weekend surcharge)
                calculator = new com.example.cinema.service.commerce.pricing.DayOfWeekDecorator(calculator,
                                showtime.getStartTime());

                // Bọc thêm lớp Time Slot (Happy Hour)
                calculator = new com.example.cinema.service.commerce.pricing.TimeSlotDecorator(calculator,
                                showtime.getStartTime());

                return calculator.calculate();
        }
}
