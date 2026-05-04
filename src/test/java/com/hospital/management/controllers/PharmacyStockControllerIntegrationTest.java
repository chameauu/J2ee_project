package com.hospital.management.controllers;

import com.hospital.management.dto.MedicationDTO;
import com.hospital.management.dto.PharmacyStockDTO;
import com.hospital.management.entities.Hospital;
import com.hospital.management.enums.MedicationType;
import com.hospital.management.enums.UserRole;
import com.hospital.management.repositories.AdministratorRepository;
import com.hospital.management.repositories.HospitalRepository;
import com.hospital.management.repositories.MedicationRepository;
import com.hospital.management.repositories.PharmacyStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PharmacyStockControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PharmacyStockRepository pharmacyStockRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    private Hospital testHospital;
    private MedicationDTO medicationDTO;
    private PharmacyStockDTO pharmacyStockDTO;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        pharmacyStockRepository.deleteAll();
        medicationRepository.deleteAll();
        administratorRepository.deleteAll();
        hospitalRepository.deleteAll();
        
        // Create test hospital using testAuthUtils
        testHospital = testAuthUtils.createTestHospital("Test Hospital");
        
        // Create admin user and generate token
        testAuthUtils.createTestAdmin("admin@example.com");
        adminToken = testAuthUtils.generateToken("admin@example.com", UserRole.ADMIN);
        
        // Setup medication DTO
        medicationDTO = new MedicationDTO();
        medicationDTO.setName("Aspirin");
        medicationDTO.setGenericName("Acetylsalicylic Acid");
        medicationDTO.setManufacturer("PharmaCorp");
        medicationDTO.setType(MedicationType.TABLET);
        medicationDTO.setStrength("500mg");
        medicationDTO.setDescription("Pain reliever");
    }

    private Long createMedication() throws Exception {
        String response = mockMvc.perform(post("/api/pharmacy/medications")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicationDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        MedicationDTO created = objectMapper.readValue(response, MedicationDTO.class);
        return created.getId();
    }

    @Test
    void shouldCreateStock() throws Exception {
        Long medicationId = createMedication();

        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(medicationId);
        pharmacyStockDTO.setHospitalId(testHospital.getId());
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        mockMvc.perform(post("/api/pharmacy/stock")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity", is(100)))
                .andExpect(jsonPath("$.reorderLevel", is(20)))
                .andExpect(jsonPath("$.batchNumber", is("BATCH001")))
                .andExpect(jsonPath("$.unitPrice", is(5.0)));
    }

    @Test
    void shouldReturn400WhenInvalidData() throws Exception {
        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(null);

        mockMvc.perform(post("/api/pharmacy/stock")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetStockById() throws Exception {
        Long medicationId = createMedication();

        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(medicationId);
        pharmacyStockDTO.setHospitalId(testHospital.getId());
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        String response = mockMvc.perform(post("/api/pharmacy/stock")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PharmacyStockDTO created = objectMapper.readValue(response, PharmacyStockDTO.class);

        mockMvc.perform(get("/api/pharmacy/stock/{id}", created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                .andExpect(jsonPath("$.quantity", is(100)));
    }

    @Test
    void shouldReturn404WhenStockNotFound() throws Exception {
        mockMvc.perform(get("/api/pharmacy/stock/{id}", 999L)
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateStock() throws Exception {
        Long medicationId = createMedication();

        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(medicationId);
        pharmacyStockDTO.setHospitalId(testHospital.getId());
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        String response = mockMvc.perform(post("/api/pharmacy/stock")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PharmacyStockDTO created = objectMapper.readValue(response, PharmacyStockDTO.class);
        created.setQuantity(150);
        created.setUnitPrice(6.0);

        mockMvc.perform(put("/api/pharmacy/stock/{id}", created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(150)))
                .andExpect(jsonPath("$.unitPrice", is(6.0)));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentStock() throws Exception {
        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(1L);
        pharmacyStockDTO.setHospitalId(testHospital.getId());
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        mockMvc.perform(put("/api/pharmacy/stock/{id}", 999L)
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteStock() throws Exception {
        Long medicationId = createMedication();

        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(medicationId);
        pharmacyStockDTO.setHospitalId(testHospital.getId());
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        String response = mockMvc.perform(post("/api/pharmacy/stock")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PharmacyStockDTO created = objectMapper.readValue(response, PharmacyStockDTO.class);

        mockMvc.perform(delete("/api/pharmacy/stock/{id}", created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/pharmacy/stock/{id}", created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentStock() throws Exception {
        mockMvc.perform(delete("/api/pharmacy/stock/{id}", 999L)
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllStock() throws Exception {
        Long medicationId = createMedication();

        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(medicationId);
        pharmacyStockDTO.setHospitalId(testHospital.getId());
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        mockMvc.perform(post("/api/pharmacy/stock")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/pharmacy/stock")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReduceStock() throws Exception {
        Long medicationId = createMedication();

        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(medicationId);
        pharmacyStockDTO.setHospitalId(testHospital.getId());
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        String response = mockMvc.perform(post("/api/pharmacy/stock")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PharmacyStockDTO created = objectMapper.readValue(response, PharmacyStockDTO.class);

        mockMvc.perform(put("/api/pharmacy/stock/{id}/reduce", created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .param("amount", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAddStock() throws Exception {
        Long medicationId = createMedication();

        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(medicationId);
        pharmacyStockDTO.setHospitalId(testHospital.getId());
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        String response = mockMvc.perform(post("/api/pharmacy/stock")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PharmacyStockDTO created = objectMapper.readValue(response, PharmacyStockDTO.class);

        mockMvc.perform(put("/api/pharmacy/stock/{id}/add", created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .param("amount", "50"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetLowStockItems() throws Exception {
        mockMvc.perform(get("/api/pharmacy/stock/low")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetExpiringSoonItems() throws Exception {
        mockMvc.perform(get("/api/pharmacy/stock/expiring")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetExpiredStock() throws Exception {
        mockMvc.perform(get("/api/pharmacy/stock/expired")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk());
    }
}
