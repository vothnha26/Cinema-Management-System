package com.example.cinema.service.commerce.impl;

import com.example.cinema.config.LogAction;
import com.example.cinema.model.dto.request.SeatPriceRequest;
import com.example.cinema.model.dto.response.SeatPriceResponse;
import com.example.cinema.model.dto.response.PriceCalculationResult;
import com.example.cinema.model.entity.SeatPrice;
import com.example.cinema.model.entity.PricingRule;
import com.example.cinema.model.enums.PricingRuleType;
import com.example.cinema.repository.room.SeatPriceRepository;
import com.example.cinema.service.commerce.PricingService;
import com.example.cinema.service.commerce.pricing.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PricingServiceImpl implements PricingService {

        private final SeatPriceRepository seatPriceRepository;
        private final com.example.cinema.repository.commerce.PricingRuleRepository pricingRuleRepository;
        private final ModelMapper modelMapper;

        public PricingServiceImpl(SeatPriceRepository seatPriceRepository, 
                                  com.example.cinema.repository.commerce.PricingRuleRepository pricingRuleRepository,
                                  ModelMapper modelMapper) {
                this.seatPriceRepository = seatPriceRepository;
                this.pricingRuleRepository = pricingRuleRepository;
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
                List<SeatPrice> oldPrices = seatPriceRepository
                                .findAllByRoomTypeAndSeatTypeAndIsActiveTrue(request.getRoomType(),
                                                request.getSeatType());

                for (SeatPrice old : oldPrices) {
                        old.setIsActive(false);
                }
                seatPriceRepository.saveAll(oldPrices);

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
        public PriceCalculationResult calculateTicketPrice(com.example.cinema.model.entity.Showtime showtime,
                        com.example.cinema.model.entity.Seat seat) {
                BigDecimal basePrice = seatPriceRepository
                                .findByRoomTypeAndSeatTypeAndIsActiveTrue(showtime.getRoom().getType(), seat.getType())
                                .map(SeatPrice::getPrice)
                                .orElse(new BigDecimal("80000.00"));

                PriceCalculator calculator = new BasePriceCalculator(basePrice);
                List<String> appliedRules = new ArrayList<>();
                appliedRules.add("Giá gốc: " + basePrice.intValue() + "đ");

                String format = showtime.getFormat() != null ? showtime.getFormat().getName() : "2D";
                List<PricingRule> rules = pricingRuleRepository.findActiveRulesByContext(
                        showtime.getRoom().getType(),
                        seat.getType(),
                        format
                );

                java.time.DayOfWeek dayOfWeek = showtime.getStartTime().getDayOfWeek();
                java.time.LocalTime showTime = showtime.getStartTime().toLocalTime();

                for (PricingRule rule : rules) {
                    if (rule.getApplicableDays() != null && !rule.getApplicableDays().isEmpty()) {
                        if (!rule.getApplicableDays().contains(dayOfWeek)) continue;
                    }
                    if (rule.getStartTime() != null && rule.getEndTime() != null) {
                        if (showTime.isBefore(rule.getStartTime()) || showTime.isAfter(rule.getEndTime())) continue;
                    }

                    if (rule.getType() == PricingRuleType.ADDITIVE) {
                        calculator = new AdditiveDecorator(calculator, rule.getValue());
                        appliedRules.add(rule.getName() + " (+" + rule.getValue().intValue() + "đ)");
                    } else if (rule.getType() == PricingRuleType.SUBTRACTIVE) {
                        calculator = new AdditiveDecorator(calculator, rule.getValue().negate());
                        appliedRules.add(rule.getName() + " (-" + rule.getValue().intValue() + "đ)");
                    } else if (rule.getType() == PricingRuleType.PERCENTAGE) {
                        calculator = new PercentageDecorator(calculator, rule.getValue());
                        appliedRules.add(rule.getName() + " (x" + rule.getValue() + ")");
                    }
                }

                return new PriceCalculationResult(calculator.calculate(), appliedRules);
        }
}
