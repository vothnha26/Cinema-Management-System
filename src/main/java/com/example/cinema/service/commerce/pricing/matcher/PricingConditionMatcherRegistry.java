package com.example.cinema.service.commerce.pricing.matcher;

import com.example.cinema.model.enums.PricingConditionType;
import com.example.cinema.service.commerce.pricing.matcher.impl.*;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class PricingConditionMatcherRegistry {
    private final Map<PricingConditionType, PricingConditionMatcher> matchers = new EnumMap<>(PricingConditionType.class);

    public PricingConditionMatcherRegistry(
            DayOfWeekMatcher dayOfWeekMatcher,
            TimeRangeMatcher timeRangeMatcher,
            RoomTypeMatcher roomTypeMatcher,
            DateRangeMatcher dateRangeMatcher,
            SeatTypeMatcher seatTypeMatcher,
            ShowFormatMatcher showFormatMatcher,
            BranchMatcher branchMatcher,
            MemberTierMatcher memberTierMatcher
    ) {
        matchers.put(PricingConditionType.DAY_OF_WEEK, dayOfWeekMatcher);
        matchers.put(PricingConditionType.TIME_RANGE, timeRangeMatcher);
        matchers.put(PricingConditionType.ROOM_TYPE, roomTypeMatcher);
        matchers.put(PricingConditionType.DATE_RANGE, dateRangeMatcher);
        matchers.put(PricingConditionType.SEAT_TYPE, seatTypeMatcher);
        matchers.put(PricingConditionType.SHOW_FORMAT, showFormatMatcher);
        matchers.put(PricingConditionType.BRANCH, branchMatcher);
        matchers.put(PricingConditionType.MEMBER_TIER, memberTierMatcher);
    }

    public PricingConditionMatcher getMatcher(PricingConditionType type) {
        return matchers.get(type);
    }
}
