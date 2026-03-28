package com.example.cinema.service.impl;

import com.example.cinema.config.LogAction;
import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.ComboRequest;
import com.example.cinema.model.dto.response.ComboResponse;
import com.example.cinema.model.entity.Combo;
import com.example.cinema.repository.ComboRepository;
import com.example.cinema.service.ComboService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComboServiceImpl implements ComboService {

    private final ComboRepository comboRepository;
    private final ModelMapper modelMapper;

    public ComboServiceImpl(ComboRepository comboRepository, ModelMapper modelMapper) {
        this.comboRepository = comboRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<ComboResponse> getAllCombos() {
        return comboRepository.findAll().stream()
                .map(c -> modelMapper.map(c, ComboResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @LogAction(action = "CREATE", target = "COMBO")
    public ComboResponse createCombo(ComboRequest request) {
        Combo combo = modelMapper.map(request, Combo.class);
        combo.setIsActive(true);
        Combo saved = comboRepository.save(combo);
        return modelMapper.map(saved, ComboResponse.class);
    }

    @Override
    @Transactional
    @LogAction(action = "UPDATE", target = "COMBO")
    public ComboResponse updateCombo(Long id, ComboRequest request) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy Combo"));
        
        modelMapper.map(request, combo);
        Combo updated = comboRepository.save(combo);
        return modelMapper.map(updated, ComboResponse.class);
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
