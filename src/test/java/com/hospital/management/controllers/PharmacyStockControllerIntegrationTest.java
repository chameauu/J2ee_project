package com.hospital.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.management.config.TestSecurityConfig;
import com.hospital.management.dto.HospitalDTO;
import com.hospital.management.dto.MedicationDTO;
import com.hospital.management.dto.PharmacyStockDTO;
import com.hospital.management.enums.MedicationType;
import com.hospital.management.repositories.PharmacyStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class PharmacyStockControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PharmacyStockRepository pharmacyStockRepository;

    private MedicationDTO medicationDTO;
    private PharmacyStockDTO pharmacyStockDTO;
    private Long hospitalId;

    @BeforeEach
    void setUp() throws Exception {
        // Phase 10.6: Create hospital first
        hospitalId = createHospital();
        
        medicationDTO = new MedicationDTO();
        medicationDTO.setName("Aspirin");
        medicationDTO.setGenericName("Acetylsalicylic Acid");
        medicationDTO.setManufacturer("PharmaCorp");
        medicationDTO.setType(MedicationType.TABLET);
        medicationDTO.setStrength("500mg");
        medicationDTO.setDescription("Pain reliever");
    }

    private Long createHospital() throws Exception {
        HospitalDTO hospitalDTO = new HospitalDTO();
        hospitalDTO.setName("Test Hospital");
        hospitalDTO.setAddress("123 Test St");
        hospitalDTO.setPhone("555-0001");
        hospitalDTO.setEmail("test@hospital.com");
        hospitalDTO.setRegistrationNumber("REG-TEST-001");
        hospitalDTO.setEstablishedDate(LocalDate.of(2000, 1, 1));

        String response = mockMvc.perform(post("/api/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hospitalDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        HospitalDTO created = objectMapper.readValue(response, HospitalDTO.class);
        return created.getId();
    }

    private Long createMedication() throws Exception {
        String response = mockMvc.perform(post("/api/pharmacy/medications")
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
        pharmacyStockDTO.setHospitalId(hospitalId); // Phase 10.6
        pharmacyStockDTO.setHospitalId(hospitalId); // Phase 10.6
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        mockMvc.perform(post("/api/pharmacy/stock")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetStockById() throws Exception {
        Long medicationId = createMedication();

        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(medicationId);
        pharmacyStockDTO.setHospitalId(hospitalId); // Phase 10.6
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        String response = mockMvc.perform(post("/api/pharmacy/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PharmacyStockDTO created = objectMapper.readValue(response, PharmacyStockDTO.class);

        mockMvc.perform(get("/api/pharmacy/stock/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                .andExpect(jsonPath("$.quantity", is(100)));
    }

    @Test
    void shouldReturn404WhenStockNotFound() throws Exception {
        mockMvc.perform(get("/api/pharmacy/stock/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateStock() throws Exception {
        Long medicationId = createMedication();

        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(medicationId);
        pharmacyStockDTO.setHospitalId(hospitalId); // Phase 10.6
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        String response = mockMvc.perform(post("/api/pharmacy/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PharmacyStockDTO created = objectMapper.readValue(response, PharmacyStockDTO.class);
        created.setQuantity(150);
        created.setUnitPrice(6.0);

        mockMvc.perform(put("/api/pharmacy/stock/{id}", created.getId())
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
        pharmacyStockDTO.setHospitalId(hospitalId); // Phase 10.6
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        mockMvc.perform(put("/api/pharmacy/stock/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteStock() throws Exception {
        Long medicationId = createMedication();

        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(medicationId);
        pharmacyStockDTO.setHospitalId(hospitalId); // Phase 10.6
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        String response = mockMvc.perform(post("/api/pharmacy/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PharmacyStockDTO created = objectMapper.readValue(response, PharmacyStockDTO.class);

        mockMvc.perform(delete("/api/pharmacy/stock/{id}", created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/pharmacy/stock/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentStock() throws Exception {
        mockMvc.perform(delete("/api/pharmacy/stock/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllStock() throws Exception {
        Long medicationId = createMedication();

        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(medicationId);
        pharmacyStockDTO.setHospitalId(hospitalId); // Phase 10.6
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        mockMvc.perform(post("/api/pharmacy/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/pharmacy/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReduceStock() throws Exception {
        Long medicationId = createMedication();

        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(medicationId);
        pharmacyStockDTO.setHospitalId(hospitalId); // Phase 10.6
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        String response = mockMvc.perform(post("/api/pharmacy/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PharmacyStockDTO created = objectMapper.readValue(response, PharmacyStockDTO.class);

        mockMvc.perform(put("/api/pharmacy/stock/{id}/reduce", created.getId())
                        .param("amount", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAddStock() throws Exception {
        Long medicationId = createMedication();

        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setMedicationId(medicationId);
        pharmacyStockDTO.setHospitalId(hospitalId); // Phase 10.6
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);

        String response = mockMvc.perform(post("/api/pharmacy/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pharmacyStockDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PharmacyStockDTO created = objectMapper.readValue(response, PharmacyStockDTO.class);

        mockMvc.perform(put("/api/pharmacy/stock/{id}/add", created.getId())
                        .param("amount", "50"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetLowStockItems() throws Exception {
        mockMvc.perform(get("/api/pharmacy/stock/low"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetExpiringSoonItems() throws Exception {
        mockMvc.perform(get("/api/pharmacy/stock/expiring"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetExpiredStock() throws Exception {
        mockMvc.perform(get("/api/pharmacy/stock/expired"))
                .andExpect(status().isOk());
    }
}
