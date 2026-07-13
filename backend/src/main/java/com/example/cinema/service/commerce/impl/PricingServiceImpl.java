package com.example.cinema.service.commerce.impl;

import com.example.cinema.config.LogAction;
import com.example.cinema.model.dto.request.SeatPriceRequest;
import com.example.cinema.model.dto.response.SeatPriceResponse;
import com.example.cinema.model.dto.response.PriceCalculationResult;
import com.example.cinema.model.entity.*;
import com.example.cinema.repository.room.SeatPriceRepository;
import com.example.cinema.repository.room.RoomTypeRepository;
import com.example.cinema.repository.room.SeatTypeRepository;
import com.example.cinema.repository.commerce.BranchPricingRuleRepository;
import com.example.cinema.repository.user.MembershipBenefitRepository;
import com.example.cinema.service.commerce.PricingService;
import com.example.cinema.service.commerce.pricing.*;
import com.example.cinema.service.commerce.pricing.matcher.PricingRuleMatcher;
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
    private final RoomTypeRepository roomTypeRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final BranchPricingRuleRepository branchPricingRuleRepository;
    private final PricingRuleMatcher pricingRuleMatcher;
    private final ModelMapper modelMapper;
    private final List<IPriceModifierHandler> pricingHandlers;

    public PricingServiceImpl(SeatPriceRepository seatPriceRepository, 
                              RoomTypeRepository roomTypeRepository,
                              SeatTypeRepository seatTypeRepository,
                              BranchPricingRuleRepository branchPricingRuleRepository,
                              PricingRuleMatcher pricingRuleMatcher,
                              ModelMapper modelMapper,
                              List<IPriceModifierHandler> pricingHandlers) {
        this.seatPriceRepository = seatPriceRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.seatTypeRepository = seatTypeRepository;
        this.branchPricingRuleRepository = branchPricingRuleRepository;
        this.pricingRuleMatcher = pricingRuleMatcher;
        this.modelMapper = modelMapper;
        this.pricingHandlers = pricingHandlers;
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
        RoomType roomType = roomTypeRepository.findByCode(request.getRoomTypeId()).orElseThrow();
        SeatType seatType = seatTypeRepository.findByCode(request.getSeatTypeId()).orElseThrow();
        List<SeatPrice> existing = seatPriceRepository.findByRoomTypeAndSeatType(roomType, seatType);
        SeatPrice seatPrice = existing.isEmpty() ? new SeatPrice() : existing.get(0);
        seatPrice.setRoomType(roomType); seatPrice.setSeatType(seatType); seatPrice.setPrice(request.getPrice()); seatPrice.setIsActive(true);
        return modelMapper.map(seatPriceRepository.save(seatPrice), SeatPriceResponse.class);
    }

    @Override
    public PriceCalculationResult calculateTicketPrice(Showtime showtime, Seat seat, Customer customer) {
        BigDecimal basePriceValue = seatPriceRepository
                .findByRoomTypeAndSeatTypeAndIsActiveTrue(showtime.getRoom().getRoomType(), seat.getSeatType())
                .stream().findFirst().map(SeatPrice::getPrice).orElse(new BigDecimal("80000.00"));

        final BigDecimal originalBase = basePriceValue;
        PriceCalculator calculator = new BasePriceCalculator(basePriceValue);
        List<String> appliedRules = new ArrayList<>();
        appliedRules.add("Giá gốc: " + basePriceValue.intValue() + "đ");

        // Giả sử mức giảm tối đa tổng thể là 50% giá gốc hoặc một con số cố định (ví dụ 50k)
        // Ở đây tôi sẽ lấy giá trị Max từ quy tắc có ưu tiên cao nhất hoặc mặc định là 50,000đ
        BigDecimal globalMaxDiscount = new BigDecimal("50000.00"); 
        
        List<BranchPricingRule> branchRules = branchPricingRuleRepository.findAllByBranchIdOrderByPriorityAsc(showtime.getRoom().getBranch().getId());

        for (BranchPricingRule link : branchRules) {
            PricingRule rule = link.getRule();
            if (rule.isActive() && pricingRuleMatcher.matches(rule, showtime, seat, customer)) {
                
                // Cập nhật globalMaxDiscount nếu có rule quy định (lấy rule cuối cùng có set giá trị này)
                if (rule.getMaxDiscountLimit() != null) globalMaxDiscount = rule.getMaxDiscountLimit();

                BigDecimal currentPrice = calculator.calculate();
                boolean handled = false;
                
                for (IPriceModifierHandler handler : pricingHandlers) {
                    if (handler.canHandle(rule)) {
                        PriceCalculator nextCalculator = handler.handle(calculator, rule);
                        BigDecimal nextPrice = nextCalculator.calculate();
                        
                        // Nếu là rule GIẢM GIÁ (giá mới thấp hơn giá hiện tại)
                        if (nextPrice.compareTo(currentPrice) < 0) {
                            BigDecimal totalDiscountSoFar = originalBase.subtract(nextPrice);
                            
                            if (totalDiscountSoFar.compareTo(globalMaxDiscount) > 0) {
                                // Nếu tổng giảm vượt trần, ép giá về mức: Giá gốc - Trần giảm
                                final BigDecimal floorPrice = originalBase.subtract(globalMaxDiscount);
                                if (currentPrice.compareTo(floorPrice) <= 0) {
                                    // Nếu giá hiện tại đã chạm sàn rồi thì không áp dụng thêm rule giảm giá nữa
                                    appliedRules.add(rule.getName() + " (Bỏ qua - Đã đạt mức giảm tối đa)");
                                } else {
                                    calculator = () -> floorPrice;
                                    appliedRules.add(formatRuleLabel(rule) + " (Chạm trần giảm giá tổng thể)");
                                }
                            } else {
                                calculator = nextCalculator;
                                appliedRules.add(formatRuleLabel(rule));
                            }
                        } else {
                            // Nếu là rule PHỤ THU (tăng giá) thì áp dụng bình thường
                            calculator = nextCalculator;
                            appliedRules.add(formatRuleLabel(rule));
                        }
                        
                        handled = true;
                        break;
                    }
                }
                if (handled && !rule.isStackable()) break;
            }
        }

        return new PriceCalculationResult(calculator.calculate(), appliedRules);
    }

    private String formatRuleLabel(PricingRule rule) {
        switch (rule.getImpactType()) {
            case ADDITIVE: return rule.getName() + " (+" + rule.getImpactValue().intValue() + "đ)";
            case SUBTRACTIVE: return rule.getName() + " (-" + rule.getImpactValue().intValue() + "đ)";
            case PERCENTAGE: return rule.getName() + " (x" + rule.getImpactValue() + ")";
            case FIXED: return rule.getName() + " (Cố định: " + rule.getImpactValue().intValue() + "đ)";
            default: return rule.getName();
        }
    }
}
