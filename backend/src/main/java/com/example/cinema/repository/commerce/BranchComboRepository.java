package com.example.cinema.repository.commerce;

import com.example.cinema.model.entity.Branch;
import com.example.cinema.model.entity.BranchCombo;
import com.example.cinema.model.entity.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchComboRepository extends JpaRepository<BranchCombo, Long> {
    List<BranchCombo> findByBranch(Branch branch);
    List<BranchCombo> findByBranchAndIsActiveTrue(Branch branch);
    Optional<BranchCombo> findByBranchAndCombo(Branch branch, Combo combo);
    List<BranchCombo> findAllByBranchId(Long branchId);
    List<BranchCombo> findByCombo(Combo combo);
}
