package com.hospital.management.services;

import com.hospital.management.dto.PrescriptionItemDTO;
import com.hospital.management.entities.Medication;
import com.hospital.management.entities.Pharmacist;
import com.hospital.management.entities.Prescription;
import com.hospital.management.entities.PrescriptionItem;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.PrescriptionItemMapper;
import com.hospital.management.repositories.MedicationRepository;
import com.hospital.management.repositories.PharmacistRepository;
import com.hospital.management.repositories.PrescriptionItemRepository;
import com.hospital.management.repositories.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionItemServiceImpl implements IPrescriptionItemService {

    private final PrescriptionItemRepository prescriptionItemRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicationRepository medicationRepository;
    private final PharmacistRepository pharmacistRepository;
    private final PrescriptionItemMapper prescriptionItemMapper;

    @Override
    @Transactional
    public PrescriptionItemDTO createPrescriptionItem(PrescriptionItemDTO dto) {
        Prescription prescription = prescriptionRepository.findById(dto.getPrescriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + dto.getPrescriptionId()));

        Medication medication = medicationRepository.findById(dto.getMedicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + dto.getMedicationId()));

        PrescriptionItem item = prescriptionItemMapper.toEntity(dto);
        item.setPrescription(prescription);
        item.setMedication(medication);

        PrescriptionItem saved = prescriptionItemRepository.save(item);
        return prescriptionItemMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionItemDTO getPrescriptionItemById(Long id) {
        PrescriptionItem item = prescriptionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription item not found with id: " + id));
        return prescriptionItemMapper.toDTO(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionItemDTO> getItemsByPrescriptionId(Long prescriptionId) {
        return prescriptionItemRepository.findByPrescriptionId(prescriptionId).stream()
                .map(prescriptionItemMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionItemDTO> getItemsByMedicationId(Long medicationId) {
        return prescriptionItemRepository.findByMedicationId(medicationId).stream()
                .map(prescriptionItemMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PrescriptionItemDTO updatePrescriptionItem(Long id, PrescriptionItemDTO dto) {
        PrescriptionItem existing = prescriptionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription item not found with id: " + id));

        if (dto.getMedicationId() != null && !dto.getMedicationId().equals(existing.getMedication().getId())) {
            Medication medication = medicationRepository.findById(dto.getMedicationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + dto.getMedicationId()));
            existing.setMedication(medication);
        }

        existing.setDosage(dto.getDosage());
        existing.setFrequency(dto.getFrequency());
        existing.setDurationDays(dto.getDurationDays());
        existing.setQuantity(dto.getQuantity());
        existing.setInstructions(dto.getInstructions());

        PrescriptionItem updated = prescriptionItemRepository.save(existing);
        return prescriptionItemMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deletePrescriptionItem(Long id) {
        if (!prescriptionItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prescription item not found with id: " + id);
        }
        prescriptionItemRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PrescriptionItemDTO dispenseItem(Long id, Long pharmacistId) {
        PrescriptionItem item = prescriptionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription item not found with id: " + id));

        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacist not found with id: " + pharmacistId));

        item.markAsDispensed(pharmacist);
        PrescriptionItem updated = prescriptionItemRepository.save(item);
        return prescriptionItemMapper.toDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionItemDTO> getUndispensedItemsByPrescriptionId(Long prescriptionId) {
        return prescriptionItemRepository.findUndispensedByPrescriptionId(prescriptionId).stream()
                .map(prescriptionItemMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionItemDTO> getDispensedItemsByPharmacistId(Long pharmacistId) {
        return prescriptionItemRepository.findDispensedByPharmacistId(pharmacistId).stream()
                .map(prescriptionItemMapper::toDTO)
                .collect(Collectors.toList());
    }
}
