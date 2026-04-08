package com.example.cinema.service.user.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.exception.ResourceNotFoundException;
import com.example.cinema.model.entity.MembershipBenefit;
import com.example.cinema.model.entity.MembershipLevel;
import com.example.cinema.repository.user.MembershipBenefitRepository;
import com.example.cinema.repository.user.MembershipLevelRepository;
import com.example.cinema.service.user.MembershipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MembershipServiceImpl implements MembershipService {

    private final MembershipLevelRepository levelRepository;
    private final MembershipBenefitRepository benefitRepository;

    public MembershipServiceImpl(MembershipLevelRepository levelRepository, 
                                 MembershipBenefitRepository benefitRepository) {
        this.levelRepository = levelRepository;
        this.benefitRepository = benefitRepository;
    }

    @Override
    public List<MembershipLevel> getAllLevels() {
        return levelRepository.findAll();
    }

    @Override
    public MembershipLevel getLevelByName(String name) {
        return levelRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("MembershipLevel", "name", name));
    }

    @Override
    public List<MembershipBenefit> getAllBenefits() {
        return benefitRepository.findAll();
    }

    @Override
    public List<MembershipBenefit> getBenefitsByLevel(Long levelId) {
        MembershipLevel level = levelRepository.findById(levelId)
                .orElseThrow(() -> new ResourceNotFoundException("MembershipLevel", "id", levelId.toString()));
        return benefitRepository.findByMembershipLevel(level);
    }

    @Override
    @Transactional
    public MembershipBenefit updateBenefit(Long levelId, String type, String value) {
        MembershipLevel level = levelRepository.findById(levelId)
                .orElseThrow(() -> new ResourceNotFoundException("MembershipLevel", "id", levelId.toString()));
        
        MembershipBenefit benefit = benefitRepository.findByMembershipLevelAndBenefitType(level, type)
                .orElse(new MembershipBenefit(level, type, value));
        
        benefit.setBenefitValue(value);
        return benefitRepository.save(benefit);
    }
}
