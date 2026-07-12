package com.example.cinema.service.booking.infrastructure;

import com.example.cinema.model.entity.*;
import com.example.cinema.repository.commerce.*;
import com.example.cinema.repository.room.SeatRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class ProductCatalogFacade {
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final ComboRepository comboRepository;
    private final BranchComboRepository branchComboRepository;
    private final PromotionRepository promotionRepository;

    public ProductCatalogFacade(ShowtimeRepository showtimeRepository, SeatRepository seatRepository, 
                               ComboRepository comboRepository, BranchComboRepository branchComboRepository, 
                               PromotionRepository promotionRepository) {
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.comboRepository = comboRepository;
        this.branchComboRepository = branchComboRepository;
        this.promotionRepository = promotionRepository;
    }

    public Optional<Showtime> findShowtime(Long id) { return showtimeRepository.findById(id); }
    public Optional<Seat> findSeat(Long id) { return seatRepository.findById(id); }
    public Optional<Combo> findCombo(Long id) { return comboRepository.findById(id); }
    public Optional<BranchCombo> findBranchCombo(Branch b, Combo c) { return branchComboRepository.findByBranchAndCombo(b, c); }
    public Optional<Promotion> findPromotion(String code) { return promotionRepository.findByCodeAndIsActiveTrue(code); }
    public void saveBranchCombo(BranchCombo bc) { branchComboRepository.save(bc); }
}
