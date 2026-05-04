package com.hospital.management.services;

import com.hospital.management.dto.PharmacyStockDTO;
import com.hospital.management.entities.Hospital;
import com.hospital.management.entities.Medication;
import com.hospital.management.entities.PharmacyStock;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.PharmacyStockMapper;
import com.hospital.management.repositories.HospitalRepository;
import com.hospital.management.repositories.MedicationRepository;
import com.hospital.management.repositories.PharmacyStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PharmacyStockServiceImpl implements IPharmacyStockService {

    private final PharmacyStockRepository pharmacyStockRepository;
    private final MedicationRepository medicationRepository;
    private final HospitalRepository hospitalRepository;
    private final PharmacyStockMapper pharmacyStockMapper;

    @Override
    @Transactional
    public PharmacyStockDTO createStock(PharmacyStockDTO dto) {
        Medication medication = medicationRepository.findById(dto.getMedicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + dto.getMedicationId()));

        Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + dto.getHospitalId()));

        PharmacyStock stock = pharmacyStockMapper.toEntity(dto);
        stock.setMedication(medication);
        stock.setHospital(hospital); // Phase 10.6: Set hospital (required)

        PharmacyStock saved = pharmacyStockRepository.save(stock);
        return pharmacyStockMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacyStockDTO getStockById(Long id) {
        PharmacyStock stock = pharmacyStockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy stock not found with id: " + id));
        return pharmacyStockMapper.toDTO(stock);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyStockDTO> getAllStock() {
        return pharmacyStockRepository.findAll().stream()
                .map(pharmacyStockMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PharmacyStockDTO updateStock(Long id, PharmacyStockDTO dto) {
        PharmacyStock existing = pharmacyStockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy stock not found with id: " + id));

        if (dto.getMedicationId() != null && !dto.getMedicationId().equals(existing.getMedication().getId())) {
            Medication medication = medicationRepository.findById(dto.getMedicationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + dto.getMedicationId()));
            existing.setMedication(medication);
        }

        if (dto.getHospitalId() != null && !dto.getHospitalId().equals(existing.getHospital().getId())) {
            Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + dto.getHospitalId()));
            existing.setHospital(hospital);
        }

        existing.setQuantity(dto.getQuantity());
        existing.setReorderLevel(dto.getReorderLevel());
        existing.setExpiryDate(dto.getExpiryDate());
        existing.setBatchNumber(dto.getBatchNumber());
        existing.setUnitPrice(dto.getUnitPrice());

        PharmacyStock updated = pharmacyStockRepository.save(existing);
        return pharmacyStockMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteStock(Long id) {
        if (!pharmacyStockRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pharmacy stock not found with id: " + id);
        }
        pharmacyStockRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyStockDTO> getStockByMedicationId(Long medicationId) {
        return pharmacyStockRepository.findByMedicationId(medicationId).stream()
                .map(pharmacyStockMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyStockDTO> getLowStockItems() {
        return pharmacyStockRepository.findLowStockItems().stream()
                .map(pharmacyStockMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyStockDTO> getExpiringSoonItems() {
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return pharmacyStockRepository.findExpiringSoon(thirtyDaysFromNow).stream()
                .map(pharmacyStockMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyStockDTO> getExpiredStock() {
        return pharmacyStockRepository.findExpiredStock(LocalDate.now()).stream()
                .map(pharmacyStockMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PharmacyStockDTO reduceStock(Long id, Integer amount) {
        PharmacyStock stock = pharmacyStockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy stock not found with id: " + id));

        stock.reduceStock(amount);
        PharmacyStock updated = pharmacyStockRepository.save(stock);
        return pharmacyStockMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public PharmacyStockDTO addStock(Long id, Integer amount) {
        PharmacyStock stock = pharmacyStockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy stock not found with id: " + id));

        stock.addStock(amount);
        PharmacyStock updated = pharmacyStockRepository.save(stock);
        return pharmacyStockMapper.toDTO(updated);
    }
}
