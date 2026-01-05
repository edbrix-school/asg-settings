package com.asg.settings.service;

import com.asg.common.lib.dto.CompanyDto;
import com.asg.common.lib.entity.Company;
import com.asg.common.lib.entity.TimeZoneEntity;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.repository.TimeZoneDataRepository;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.settings.dto.UserCompanyDto;
import com.asg.settings.dto.request.CreateCompanyRequest;
import com.asg.settings.dto.request.UpdateCompanyRequest;
import com.asg.settings.entity.Country;
import com.asg.settings.entity.State;
import com.asg.settings.entity.UsersCompanyEntity;
import com.asg.settings.entity.key.CompanyEntityKey;
import com.asg.settings.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CountryRepository countryRepository;
    @Mock
    private StateRepository stateRepository;
    @Mock
    private UsersCompanyRepository usersCompanyRepository;
    @Mock
    private CompanyDivisionRepository companyDivisionRepository;
    @Mock
    private UserRepository userRepository;
    //    @Mock
//    private TimeZoneRepository timeZoneRepository;
    @Mock
    private TimeZoneDataRepository timeZoneRepository;
    @Mock
    private CurrencyService currencyService;
    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private CompanyService companyService;

    private Company company;
    private UsersCompanyEntity usersCompanyEntity;
    private TimeZoneEntity timeZoneEntity;
    private Country country;
    private State state;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(companyService, "companyRepository", companyRepository);
        ReflectionTestUtils.setField(companyService, "countryRepository", countryRepository);
        ReflectionTestUtils.setField(companyService, "stateRepository", stateRepository);
        ReflectionTestUtils.setField(companyService, "usersCompanyRepository", usersCompanyRepository);
        ReflectionTestUtils.setField(companyService, "companyDivisionRepository", companyDivisionRepository);
        ReflectionTestUtils.setField(companyService, "userRepository", userRepository);
        ReflectionTestUtils.setField(companyService, "timeZoneRepository", timeZoneRepository);

        company = createTestCompany();
        usersCompanyEntity = createTestUsersCompanyEntity();
        timeZoneEntity = createTestTimeZoneEntity();
        country = createTestCountry();
        state = createTestState();
    }

    private Company createTestCompany() {
        Company company = new Company();
        company.setCompanyPoid(1L);
        company.setCompanyCode("TEST");
        company.setCompanyName("Test Company");
        company.setEmail("test@company.com");
        company.setContactPerson("John Doe");
        company.setCountryId("1");
        company.setStateId("1");
        company.setTimezoneId(1L);
        company.setDateFormat("DD-MM-YYYY");
        company.setActive("Y");
        company.setDeleted("N");
        company.setSubmissionPeriod(30L);
        return company;
    }

    private UsersCompanyEntity createTestUsersCompanyEntity() {
        UsersCompanyEntity entity = new UsersCompanyEntity();
        CompanyEntityKey key = new CompanyEntityKey();
        key.setUserPoid(1L);
        key.setDetRowId(1L);
        key.setCompanyPoid(1L);
        entity.setId(key);
        entity.setExpiryDate(Date.valueOf("2024-12-31"));
        return entity;
    }

    private TimeZoneEntity createTestTimeZoneEntity() {
        TimeZoneEntity entity = new TimeZoneEntity();
        entity.setTimezoneId(1L);
        entity.setTimezoneCode("UTC");
        entity.setTimezoneName("UTC");
        return entity;
    }

    private Country createTestCountry() {
        Country country = new Country();
        country.setCountryPoid(1L);
        country.setCountryCode("US");
        country.setCountryName("United States");
        return country;
    }

    private State createTestState() {
        State state = new State();
        state.setStatePoid(1L);
        state.setStateName("California");
        state.setCountryPoid(1L);
        return state;
    }

    @Test
    void getUsersCompanies_Success() {
        // Mock user-company access
        when(usersCompanyRepository.findCompanyAccess(1L))
                .thenReturn(Arrays.asList(usersCompanyEntity));

        when(companyRepository.findByCompanyPoid(1L)).thenReturn(company);

        // Mock companyId -> countryId
        when(companyRepository.findCountryIdByCompanyPoid(1L)).thenReturn("1");

        // Mock countryId -> countryCode
        when(countryRepository.findCountryCodeByCountryPoid(1L)).thenReturn("US");

        // Mock timezone
        when(timeZoneRepository.findByTimezoneId(1L)).thenReturn(timeZoneEntity);

        // Mock state checks
        when(stateRepository.existsByCountryPoidAndStatePoid(1L, 1L)).thenReturn(true);
        when(stateRepository.findByCountryPoidAndStatePoid(1L, 1L)).thenReturn(state);

        List<UserCompanyDto> result = companyService.getUsersCompanies(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Company", result.get(0).companyName());
        assertEquals("US", result.get(0).countryCode()); // new assertion for scalar

        verify(usersCompanyRepository).findCompanyAccess(1L);
    }

    @Test
    void getUsersCompanies_Exception() {
        when(usersCompanyRepository.findCompanyAccess(1L)).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> companyService.getUsersCompanies(1L));
        assertEquals("Database error", exception.getMessage());
    }

    @Test
    void getCompanies_Success() {
        // Mock user-company access
        when(usersCompanyRepository.findCompanyAccess(1L))
                .thenReturn(Arrays.asList(usersCompanyEntity));

        when(companyRepository.findByCompanyPoid(1L)).thenReturn(company);

        // Mock companyId -> countryId
        when(companyRepository.findCountryIdByCompanyPoid(1L)).thenReturn("1");

        // Mock countryId -> countryCode
        when(countryRepository.findCountryCodeByCountryPoid(1L)).thenReturn("US");

        // Mock state checks
        when(stateRepository.existsByCountryPoidAndStatePoid(1L, 1L)).thenReturn(true);
        when(stateRepository.findByCountryPoidAndStatePoid(1L, 1L)).thenReturn(state);

        // Mock timezone
        when(timeZoneRepository.findByTimezoneId(1L)).thenReturn(timeZoneEntity);

        List<Company> result = companyService.getCompanies(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Company", result.get(0).getLabel());
        assertEquals(1L, result.get(0).getValue());
        assertEquals("US", result.get(0).getCountryCode()); // new assertion for scalar
    }

    @Test
    void getCompany_Success() {
        when(companyRepository.findByCompanyPoid(1L)).thenReturn(company);
        // Mock companyId -> countryId
        when(companyRepository.findCountryIdByCompanyPoid(1L)).thenReturn("1");
        // Mock countryId -> countryCode
        when(countryRepository.findCountryCodeByCountryPoid(1L)).thenReturn("US");
        // Mock timezone
        when(timeZoneRepository.findByTimezoneId(1L)).thenReturn(timeZoneEntity);
        // Mock company division
        when(companyDivisionRepository.findById_CompanyPoid(1L)).thenReturn(Arrays.asList());
        // Mock state checks
        when(stateRepository.existsByCountryPoidAndStatePoid(1L, 1L)).thenReturn(true);
        when(stateRepository.findByCountryPoidAndStatePoid(1L, 1L)).thenReturn(state);

        CompanyDto result = companyService.getCompany(1L);

        assertNotNull(result);
        assertEquals("Test Company", result.getCompanyName());
        assertEquals(1L, result.getCompanyPoid());
        assertEquals("US", result.getCountryCode()); // new assertion for scalar
    }

    @Test
    void getCompany_NotFound() {
        when(companyRepository.findByCompanyPoid(1L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> companyService.getCompany(1L));
        assertTrue(exception.getMessage().contains("Company not found with companyId"));
    }

//    @Test
//    void getCountryForCompany_Success() {
//        when(companyRepository.findByCompanyPoid(1L)).thenReturn(company);
//        when(countryRepository.findByCountryPoid(1L)).thenReturn(country);
//
//        Country result = companyService.getCountryForCompany(1L);
//
//        assertNotNull(result);
//        assertEquals("US", result.getCountryCode());
//    }
//
//    @Test
//    void getCountryForCompany_NoCountryId() {
//        company.setCountryId(null);
//        when(companyRepository.findByCompanyPoid(1L)).thenReturn(company);
//
//        Country result = companyService.getCountryForCompany(1L);
//
//        assertNull(result);
//    }

    @Test
    void getCountryForCompany_Success() {
        // Mock only the scalar methods
        when(companyRepository.findCountryIdByCompanyPoid(1L)).thenReturn("1");
        when(countryRepository.findCountryCodeByCountryPoid(1L)).thenReturn("US");

        String result = companyService.getCountryCodeForCompany(1L);

        assertNotNull(result);
        assertEquals("US", result);
    }

    @Test
    void getCountryForCompany_NoCountryId() {
        // Mock company with no countryId
        when(companyRepository.findCountryIdByCompanyPoid(1L)).thenReturn(null);

        String result = companyService.getCountryCodeForCompany(1L);

        assertNull(result);
    }


    @Test
    void getStateForCompany_Success() {
        when(stateRepository.existsByCountryPoidAndStatePoid(1L, 1L)).thenReturn(true);
        when(stateRepository.findByCountryPoidAndStatePoid(1L, 1L)).thenReturn(state);

        State result = companyService.getStateForCompany("1", "1");

        assertNotNull(result);
        assertEquals("California", result.getStateName());
    }

    @Test
    void getStateForCompany_NullParameters() {
        State result = companyService.getStateForCompany(null, "1");
        assertNull(result);

        result = companyService.getStateForCompany("1", null);
        assertNull(result);
    }

//    @Test
//    void getCompaniesMinimal_Success() {
//        List<FilterDto> filters = Arrays.asList(new FilterDto("companyName", "Test"));
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Company> companyPage = new PageImpl<>(Arrays.asList(company));
//
//        when(companyRepository.findAllCompaniesWithContacts(filters, pageable)).thenReturn(companyPage);
//
//        ResponseEntity<?> result = companyService.getCompaniesMinimal(filters, pageable);
//
//        assertEquals(200, result.getStatusCodeValue());
//    }

//    @Test
//    void getCompaniesMinimal_Exception() {
//        List<FilterDto> filters = Arrays.asList();
//        Pageable pageable = PageRequest.of(0, 10);
//
//        when(companyRepository.findAllCompaniesWithContacts(filters, pageable))
//            .thenThrow(new RuntimeException("Database error"));
//
//        ResponseEntity<?> result = companyService.getCompaniesMinimal(filters, pageable);
//
//        assertEquals(500, result.getStatusCodeValue());
//    }

    @Test
    void createCompany_Success() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("000-002");

            CreateCompanyRequest request = new CreateCompanyRequest();
            request.setCompanyCode("TEST");
            request.setCompanyName("Test Company");
            request.setEmail("test@company.com");
            request.setContactPerson("John Doe");

            when(companyRepository.existsByCompanyNameIgnoreCase(anyString())).thenReturn(false);
            when(companyRepository.existsByCompanyCodeIgnoreCase(anyString())).thenReturn(false);
            when(companyRepository.saveAndFlush(any(Company.class))).thenReturn(company);

            String result = companyService.createCompany(request);

            assertEquals("1", result);
            verify(companyRepository).saveAndFlush(any(Company.class));
        }
    }

    @Test
    void createCompany_DuplicateName() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            CreateCompanyRequest request = new CreateCompanyRequest();
            request.setCompanyCode("TEST");
            request.setCompanyName("Test Company");

            when(companyRepository.existsByCompanyNameIgnoreCase(anyString())).thenReturn(true);

            ValidationException exception = assertThrows(ValidationException.class,
                    () -> companyService.createCompany(request));
            assertTrue(exception.getMessage().contains("Company Name already exists"));
        }
    }

    @Test
    void createCompany_DuplicateTinNumber() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            CreateCompanyRequest request = new CreateCompanyRequest();
            request.setCompanyCode("TEST");
            request.setCompanyName("Test Company");
            request.setTinNumber("123456");

            when(companyRepository.existsByCompanyNameIgnoreCase(anyString())).thenReturn(false);
            when(companyRepository.existsByCompanyCodeIgnoreCase(anyString())).thenReturn(false);
            when(companyRepository.existsByTinNumberIgnoreCase(any())).thenReturn(true);

            ValidationException exception = assertThrows(ValidationException.class,
                    () -> companyService.createCompany(request));
            assertTrue(exception.getMessage().contains("Tin number already exists"));
        }
    }

    @Test
    void updateCompany_Success() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("000-002");

            UpdateCompanyRequest request = new UpdateCompanyRequest();
            request.setCompanyPoid(1L);
            request.setCompanyCode("TEST");
            request.setCompanyName("Test Company");
            request.setEmail("test@company.com");
            request.setContactPerson("John Doe");

            when(companyRepository.findByCompanyPoid(1L)).thenReturn(company);
            when(companyRepository.saveAndFlush(any(Company.class))).thenReturn(company);

            String result = companyService.updateCompany(request);

            assertEquals("1", result);
            verify(companyRepository).saveAndFlush(any(Company.class));
        }
    }

    @Test
    void updateCompany_NotFound() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            UpdateCompanyRequest request = new UpdateCompanyRequest();
            request.setCompanyPoid(1L);
            request.setCompanyCode("TEST");
            request.setCompanyName("Test Company");

            when(companyRepository.findByCompanyPoid(1L)).thenReturn(null);

            ValidationException exception = assertThrows(ValidationException.class,
                    () -> companyService.updateCompany(request));
            assertTrue(exception.getMessage().contains("Company not found with poid"));
        }
    }

    @Test
    void updateCompany_DataIntegrityViolation() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("000-002");

            UpdateCompanyRequest request = new UpdateCompanyRequest();
            request.setCompanyPoid(1L);
            request.setCompanyCode("TEST");
            request.setCompanyName("Test Company");

            when(companyRepository.findByCompanyPoid(1L)).thenReturn(company);
            when(companyRepository.saveAndFlush(any(Company.class)))
                    .thenThrow(new DataIntegrityViolationException("GLOBAL_COMPANY_MAST_UK_VAT"));

            ValidationException exception = assertThrows(ValidationException.class,
                    () -> companyService.updateCompany(request));
            assertTrue(exception.getMessage().contains("Tin number already exists"));
        }
    }

    @Test
    void softDeleteCompany_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        assertDoesNotThrow(() -> companyService.softDeleteCompany(1L));

        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void softDeleteCompany_NotFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> companyService.softDeleteCompany(1L));
        assertTrue(exception.getMessage().contains("Company"));
    }

    @Test
    void getNextDetRowIdForCompanyDivison_FirstDivision() {
        when(companyDivisionRepository.findMaxDetRowIdByCompanyPoid(1L)).thenReturn(null);

        Long result = companyService.getNextDetRowIdForCompanyDivison(1L);

        assertEquals(1L, result);
    }

    @Test
    void getNextDetRowIdForCompanyDivison_ExistingDivisions() {
        when(companyDivisionRepository.findMaxDetRowIdByCompanyPoid(1L)).thenReturn(5L);

        Long result = companyService.getNextDetRowIdForCompanyDivison(1L);

        assertEquals(6L, result);
    }
}
