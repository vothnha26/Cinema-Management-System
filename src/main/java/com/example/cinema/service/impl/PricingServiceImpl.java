package com.example.cinema.service.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.SeatPriceRequest;
import com.example.cinema.model.dto.response.SeatPriceResponse;
import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.entity.SeatPrice;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.model.enums.RoomType;
import com.example.cinema.model.enums.SeatType;
import com.example.cinema.repository.SeatPriceRepository;
import com.example.cinema.service.PricingService;
import com.example.cinema.service.pricing.PricingEngine;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PricingServiceImpl implements PricingService {

    private final SeatPriceRepository seatPriceRepository;
    private final PricingEngine pricingEngine;
    private final ModelMapper modelMapper;

    public PricingServiceImpl(SeatPriceRepository seatPriceRepository, 
                              PricingEngine pricingEngine, 
                              ModelMapper modelMapper) {
        this.seatPriceRepository = seatPriceRepository;
        this.pricingEngine = pricingEngine;
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
    public SeatPriceResponse updateSeatPrice(SeatPriceRequest request) {
        // 1. Vô hiệu hóa tất cả các cấu hình giá cũ của cùng loại phòng/ghế
        List<SeatPrice> oldPrices = seatPriceRepository
                .findAllByRoomTypeAndSeatTypeAndIsActiveTrue(request.getRoomType(), request.getSeatType());
        
        for (SeatPrice old : oldPrices) {
            old.setIsActive(false);
        }
        seatPriceRepository.saveAll(oldPrices);

        // 2. Kiểm tra xem đã có cấu hình cho ngày hiệu lực này chưa
        SeatPrice seatPrice = seatPriceRepository
                .findByRoomTypeAndSeatTypeAndEffectiveDate(request.getRoomType(), request.getSeatType(), request.getEffectiveDate())
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
    public BigDecimal calculateTicketPrice(Showtime showtime, Seat seat) {
        // Lấy giá gốc cho loại ghế và loại phòng từ DB
        // Nếu không có cấu hình cụ thể, lấy mặc định (ví dụ 80,000)
        BigDecimal basePrice = seatPriceRepository
                .findByRoomTypeAndSeatTypeAndIsActiveTrue(showtime.getRoom().getType(), seat.getType())
                .map(SeatPrice::getPrice)
                .orElse(new BigDecimal("80000.00"));

        // Gọi Engine để tính toán thêm các phụ phí (Strategy Pattern)
        return pricingEngine.calculateTotal(basePrice, showtime, seat);
    }
}
