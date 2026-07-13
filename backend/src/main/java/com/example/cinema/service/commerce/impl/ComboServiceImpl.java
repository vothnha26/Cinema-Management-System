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
                .map(c -> {
                    ComboResponse resp = modelMapper.map(c, ComboResponse.class);
                    resp.setPrice(null);
                    resp.setStockQuantity(null);
                    return resp;
                })
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
                        newBc.setPrice(java.math.BigDecimal.ZERO);
                        newBc.setStockQuantity(0);
                        newBc.setIsActive(true);
                        return branchComboRepository.save(newBc);
                    });
            
            ComboResponse resp = modelMapper.map(c, ComboResponse.class);
            resp.setPrice(bc.getPrice());
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
        resp.setPrice(bc.getPrice());
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
            bc.setPrice(request.getPrice() != null ? request.getPrice() : java.math.BigDecimal.ZERO);
            bc.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
            bc.setIsActive(true);
            branchComboRepository.save(bc);
        }

        ComboResponse resp = modelMapper.map(saved, ComboResponse.class);
        resp.setPrice(request.getPrice());
        resp.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        return resp;
    }

    @Override
    @Transactional
    @LogAction(action = "UPDATE", target = "COMBO")
    public ComboResponse updateCombo(Long id, ComboRequest request, String imageUrl) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy Combo"));

        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        if (imageUrl != null) {
            combo.setImageUrl(imageUrl);
        }
        Combo updated = comboRepository.save(combo);

        // Cập nhật giá bán cho tất cả chi nhánh từ yêu cầu
        if (request.getPrice() != null) {
            List<BranchCombo> branchCombos = branchComboRepository.findByCombo(updated);
            for (BranchCombo bc : branchCombos) {
                bc.setPrice(request.getPrice());
                branchComboRepository.save(bc);
            }
        }

        ComboResponse resp = modelMapper.map(updated, ComboResponse.class);
        resp.setPrice(request.getPrice());
        return resp;
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
                    newBc.setPrice(java.math.BigDecimal.ZERO);
                    return newBc;
                });

        bc.setStockQuantity(newStock);
        if (bc.getStockQuantity() < 0) {
            throw new AppException("Số lượng tồn kho không thể âm");
        }

        branchComboRepository.save(bc);
        
        ComboResponse resp = modelMapper.map(combo, ComboResponse.class);
        resp.setPrice(bc.getPrice());
        resp.setStockQuantity(bc.getStockQuantity());
        resp.setIsActive(bc.getIsActive());
        return resp;
    }

    @Override
    @Transactional
    @LogAction(action = "UPDATE_STOCK", target = "COMBO")
    public ComboResponse updateStock(Long id, Integer quantity) {
        throw new AppException("Không thể cập nhật trực tiếp tồn kho hệ thống. Vui lòng cập nhật theo từng chi nhánh.");
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
