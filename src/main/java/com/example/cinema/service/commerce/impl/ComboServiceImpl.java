package com.example.cinema.service.commerce.impl;

import com.example.cinema.config.LogAction;
import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.ComboRequest;
import com.example.cinema.model.dto.response.ComboResponse;
import com.example.cinema.model.entity.Combo;
import com.example.cinema.model.entity.Branch;
import com.example.cinema.model.entity.BranchCombo;
import com.example.cinema.repository.commerce.ComboRepository;
import com.example.cinema.repository.commerce.BranchComboRepository;
import com.example.cinema.repository.branch.BranchRepository;
import com.example.cinema.service.commerce.ComboService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComboServiceImpl implements ComboService {

    private final ComboRepository comboRepository;
    private final BranchComboRepository branchComboRepository;
    private final BranchRepository branchRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ComboServiceImpl(ComboRepository comboRepository, 
                            BranchComboRepository branchComboRepository,
                            BranchRepository branchRepository,
                            ModelMapper modelMapper) {
        this.comboRepository = comboRepository;
        this.branchComboRepository = branchComboRepository;
        this.branchRepository = branchRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<ComboResponse> getAllCombos() {
        return comboRepository.findAll().stream()
                .map(c -> modelMapper.map(c, ComboResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ComboResponse> getCombosByBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException("Không tìm thấy chi nhánh"));
        
        List<Combo> allCombos = comboRepository.findAll();
        return allCombos.stream().map(c -> {
            BranchCombo bc = branchComboRepository.findByBranchAndCombo(branch, c)
                    .orElseGet(() -> {
                        BranchCombo newBc = new BranchCombo();
                        newBc.setBranch(branch);
                        newBc.setCombo(c);
                        newBc.setPrice(c.getPrice());
                        newBc.setStockQuantity(0);
                        newBc.setIsActive(true);
                        return branchComboRepository.save(newBc);
                    });
            
            ComboResponse resp = modelMapper.map(c, ComboResponse.class);
            resp.setStockQuantity(bc.getStockQuantity());
            resp.setIsActive(bc.getIsActive()); // Lấy trạng thái từ chi nhánh
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComboResponse toggleBranchActive(Long branchId, Long comboId, Boolean active) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException("Không tìm thấy chi nhánh"));
        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() -> new AppException("Không tìm thấy Combo"));
        
        BranchCombo bc = branchComboRepository.findByBranchAndCombo(branch, combo)
                .orElseThrow(() -> new AppException("Combo chưa được phân phối cho chi nhánh này"));
        
        bc.setIsActive(active);
        branchComboRepository.save(bc);
        
        ComboResponse resp = modelMapper.map(combo, ComboResponse.class);
        resp.setStockQuantity(bc.getStockQuantity());
        resp.setIsActive(bc.getIsActive());
        return resp;
    }

    @Override
    @Transactional
    @LogAction(action = "CREATE", target = "COMBO")
    public ComboResponse createCombo(ComboRequest request, String imageUrl) {
        Combo combo = modelMapper.map(request, Combo.class);
        combo.setImageUrl(imageUrl);
        combo.setIsActive(true);
        Combo saved = comboRepository.save(combo);

        // Khởi tạo tồn kho cho tất cả chi nhánh
        List<Branch> branches = branchRepository.findAll();
        for (Branch b : branches) {
            BranchCombo bc = new BranchCombo();
            bc.setBranch(b);
            bc.setCombo(saved);
            bc.setPrice(saved.getPrice());
            bc.setStockQuantity(0);
            branchComboRepository.save(bc);
        }

        return modelMapper.map(saved, ComboResponse.class);
    }

    @Override
    @Transactional
    @LogAction(action = "UPDATE", target = "COMBO")
    public ComboResponse updateCombo(Long id, ComboRequest request, String imageUrl) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy Combo"));

        modelMapper.map(request, combo);
        if (imageUrl != null) {
            combo.setImageUrl(imageUrl);
        }
        Combo updated = comboRepository.save(combo);
        return modelMapper.map(updated, ComboResponse.class);
    }

    @Override
    @Transactional
    @LogAction(action = "UPDATE_BRANCH_STOCK", target = "COMBO")
    public ComboResponse updateBranchStock(Long branchId, Long comboId, Integer newStock) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException("Không tìm thấy chi nhánh"));
        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() -> new AppException("Không tìm thấy Combo"));

        BranchCombo bc = branchComboRepository.findByBranchAndCombo(branch, combo)
                .orElseGet(() -> {
                    BranchCombo newBc = new BranchCombo();
                    newBc.setBranch(branch);
                    newBc.setCombo(combo);
                    newBc.setPrice(combo.getPrice());
                    return newBc;
                });

        bc.setStockQuantity(newStock);
        if (bc.getStockQuantity() < 0) {
            throw new AppException("Số lượng tồn kho không thể âm");
        }

        branchComboRepository.save(bc);
        
        ComboResponse resp = modelMapper.map(combo, ComboResponse.class);
        resp.setStockQuantity(bc.getStockQuantity());
        return resp;
    }

    @Override
    @Transactional
    @LogAction(action = "UPDATE_STOCK", target = "COMBO")
    public ComboResponse updateStock(Long id, Integer quantity) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy Combo"));

        combo.setStockQuantity(combo.getStockQuantity() + quantity);
        if (combo.getStockQuantity() < 0) {
            throw new AppException("Số lượng tồn kho không thể âm");
        }

        Combo updated = comboRepository.save(combo);
        return modelMapper.map(updated, ComboResponse.class);
    }

    @Override
    @Transactional
    @LogAction(action = "DELETE", target = "COMBO")
    public void deleteCombo(Long id) {
        if (!comboRepository.existsById(id)) {
            throw new AppException("Không tìm thấy Combo");
        }
        comboRepository.deleteById(id);
    }
}
