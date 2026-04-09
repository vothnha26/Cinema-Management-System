package com.example.cinema.service.commerce.impl;

import com.example.cinema.config.LogAction;
import com.example.cinema.model.dto.request.SeatPriceRequest;
import com.example.cinema.model.dto.response.SeatPriceResponse;
import com.example.cinema.model.dto.response.PriceCalculationResult;
import com.example.cinema.model.entity.SeatPrice;
import com.example.cinema.model.entity.PricingRule;
import com.example.cinema.model.entity.RoomType;
import com.example.cinema.model.entity.SeatType;
import com.example.cinema.repository.room.SeatPriceRepository;
import com.example.cinema.repository.room.RoomTypeRepository;
import com.example.cinema.repository.room.SeatTypeRepository;
import com.example.cinema.repository.commerce.BranchPricingRuleRepository;
import com.example.cinema.repository.user.MembershipBenefitRepository;
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
        private final RoomTypeRepository roomTypeRepository;
        private final SeatTypeRepository seatTypeRepository;
        private final BranchPricingRuleRepository branchPricingRuleRepository;
        private final MembershipBenefitRepository membershipBenefitRepository;
        private final PricingRuleMatcher pricingRuleMatcher;
        private final ModelMapper modelMapper;

        public PricingServiceImpl(SeatPriceRepository seatPriceRepository, 
                                  com.example.cinema.repository.commerce.PricingRuleRepository pricingRuleRepository,
                                  RoomTypeRepository roomTypeRepository,
                                  SeatTypeRepository seatTypeRepository,
                                  BranchPricingRuleRepository branchPricingRuleRepository,
                                  MembershipBenefitRepository membershipBenefitRepository,
                                  PricingRuleMatcher pricingRuleMatcher,
                                  ModelMapper modelMapper) {
                this.seatPriceRepository = seatPriceRepository;
                this.pricingRuleRepository = pricingRuleRepository;
                this.roomTypeRepository = roomTypeRepository;
                this.seatTypeRepository = seatTypeRepository;
                this.branchPricingRuleRepository = branchPricingRuleRepository;
                this.membershipBenefitRepository = membershipBenefitRepository;
                this.pricingRuleMatcher = pricingRuleMatcher;
                this.modelMapper = modelMapper;
        }

        public SeatPriceRepository getSeatPriceRepository() { return seatPriceRepository; }
        public BranchPricingRuleRepository getBranchPricingRuleRepository() { return branchPricingRuleRepository; }
        public PricingRuleMatcher getPricingRuleMatcher() { return pricingRuleMatcher; }

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
                RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                        .orElseThrow(() -> new RuntimeException("RoomType not found: " + request.getRoomTypeId()));
                SeatType seatType = seatTypeRepository.findById(request.getSeatTypeId())
                        .orElseThrow(() -> new RuntimeException("SeatType not found: " + request.getSeatTypeId()));

                // Tìm bản ghi giá hiện tại để cập nhật hoặc tạo mới
                List<SeatPrice> existing = seatPriceRepository.findByRoomTypeAndSeatType(roomType, seatType);
                SeatPrice seatPrice = existing.isEmpty() ? new SeatPrice() : existing.get(0);
                
                seatPrice.setRoomType(roomType);
                seatPrice.setSeatType(seatType);
                seatPrice.setPrice(request.getPrice());
                seatPrice.setIsActive(true);

                return modelMapper.map(seatPriceRepository.save(seatPrice), SeatPriceResponse.class);
        }

        @Override
        public PriceCalculationResult calculateTicketPrice(com.example.cinema.model.entity.Showtime showtime,
                        com.example.cinema.model.entity.Seat seat,
                        com.example.cinema.model.entity.Customer customer) {
                
                BigDecimal basePrice = seatPriceRepository
                                .findByRoomTypeAndSeatTypeAndIsActiveTrue(showtime.getRoom().getRoomType(), seat.getSeatType())
                                .stream().findFirst()
                                .map(SeatPrice::getPrice)
                                .orElse(new BigDecimal("80000.00"));

                PriceCalculator calculator = new BasePriceCalculator(basePrice);
                List<String> appliedRules = new ArrayList<>();
                appliedRules.add("Giá gốc: " + basePrice.intValue() + "đ");

                Long branchId = showtime.getRoom().getBranch().getId();
                List<com.example.cinema.model.entity.BranchPricingRule> branchRules = 
                        branchPricingRuleRepository.findAllByBranchIdOrderByPriorityAsc(branchId);

                for (com.example.cinema.model.entity.BranchPricingRule link : branchRules) {
                    PricingRule rule = link.getRule();
                    if (rule.isActive() && pricingRuleMatcher.matches(rule, showtime, seat, customer)) {
                        if (rule.getImpactType() == com.example.cinema.model.enums.PricingImpactType.ADDITIVE) {
                            calculator = new AdditiveDecorator(calculator, rule.getImpactValue());
                            appliedRules.add(rule.getName() + " (+" + rule.getImpactValue().intValue() + "đ)");
                        } 
                        else if (rule.getImpactType() == com.example.cinema.model.enums.PricingImpactType.SUBTRACTIVE) {
                            calculator = new AdditiveDecorator(calculator, rule.getImpactValue().negate());
                            appliedRules.add(rule.getName() + " (-" + rule.getImpactValue().intValue() + "đ)");
                        } 
                        else if (rule.getImpactType() == com.example.cinema.model.enums.PricingImpactType.PERCENTAGE) {
                            calculator = new PercentageDecorator(calculator, rule.getImpactValue());
                            appliedRules.add(rule.getName() + " (x" + rule.getImpactValue() + ")");
                        }
                        else if (rule.getImpactType() == com.example.cinema.model.enums.PricingImpactType.FIXED) {
                            final BigDecimal fixedPrice = rule.getImpactValue();
                            calculator = () -> fixedPrice;
                            appliedRules.add(rule.getName() + " (Cố định: " + fixedPrice.intValue() + "đ)");
                        }
                        if (!rule.isStackable()) break;
                    }
                }

                BigDecimal priceAfterRules = calculator.calculate();
                return new PriceCalculationResult(priceAfterRules, appliedRules);
        }
}
