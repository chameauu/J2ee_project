package com.hospital.management.utils;

import com.hospital.management.entities.*;
import com.hospital.management.enums.Gender;
import com.hospital.management.enums.UserRole;
import com.hospital.management.repositories.*;
import com.hospital.management.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Test utility for JWT token generation and test user creation.
 * Use this in integration tests to set up authentication.
 */
@Component
@RequiredArgsConstructor
public class TestAuthenticationUtils {

    private final JwtTokenProvider jwtTokenProvider;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PharmacistRepository pharmacistRepository;
    private final AdministratorRepository administratorRepository;
    private final HospitalDirectorRepository hospitalDirectorRepository;

    /**
     * Generate JWT token for a user with given email and role.
     */
    public String generateToken(String email, String role) {
        return jwtTokenProvider.generateToken(email, role);
    }

    /**
     * Generate JWT token for a user with given email and role enum.
     */
    public String generateToken(String email, UserRole role) {
        return generateToken(email, role.name());
    }

    /**
     * Create a test hospital.
     */
    public Hospital createTestHospital(String name) {
        Hospital hospital = new Hospital();
        hospital.setName(name);
        hospital.setRegistrationNumber("REG-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000));
        hospital.setEmail(name.toLowerCase().replace(" ", "") + "@hospital.com");
        hospital.setPhone("+1234567890");
        hospital.setAddress("123 Test Street");
        hospital.setEstablishedDate(LocalDate.now());
        return hospitalRepository.save(hospital);
    }

    /**
     * Create a test patient.
     */
    public Patient createTestPatient(String email, Hospital hospital) {
        Patient patient = new Patient();
        patient.setFirstName("Test");
        patient.setLastName("Patient");
        patient.setEmail(email);
        patient.setPhone("+1234567890");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender(Gender.MALE);
        patient.setHospital(hospital);
        return patientRepository.save(patient);
    }

    /**
     * Create a test doctor.
     */
    public Doctor createTestDoctor(String email, Hospital hospital) {
        Doctor doctor = new Doctor();
        doctor.setFirstName("Test");
        doctor.setLastName("Doctor");
        doctor.setEmail(email);
        doctor.setPhone("+1234567890");
        doctor.setSpecialization("Cardiology");
        doctor.setLicenseNumber("LIC-" + System.currentTimeMillis());
        doctor.setYearsOfExperience(10);
        doctor.setQualification("MD");
        doctor.setHospital(hospital);
        return doctorRepository.save(doctor);
    }

    /**
     * Create a test pharmacist.
     */
    public Pharmacist createTestPharmacist(String email, Hospital hospital) {
        Pharmacist pharmacist = new Pharmacist();
        pharmacist.setFirstName("Test");
        pharmacist.setLastName("Pharmacist");
        pharmacist.setEmail(email);
        pharmacist.setPhone("+1234567890");
        pharmacist.setLicenseNumber("PHARM-" + System.currentTimeMillis());
        pharmacist.setQualification("PharmD");
        pharmacist.setHospital(hospital);
        return pharmacistRepository.save(pharmacist);
    }

    /**
     * Create a test administrator.
     */
    public Administrator createTestAdmin(String email) {
        Administrator admin = new Administrator();
        admin.setFirstName("Test");
        admin.setLastName("Admin");
        admin.setEmail(email);
        admin.setPhone("+1234567890");
        return administratorRepository.save(admin);
    }

    /**
     * Create a test hospital director.
     */
    public HospitalDirector createTestDirector(String email, Hospital hospital) {
        HospitalDirector director = new HospitalDirector();
        director.setFirstName("Test");
        director.setLastName("Director");
        director.setEmail(email);
        director.setPhone("+1234567890");
        director.setHospital(hospital);
        return hospitalDirectorRepository.save(director);
    }

    /**
     * Create authorization header with JWT token.
     */
    public String getAuthorizationHeader(String token) {
        return "Bearer " + token;
    }
}
