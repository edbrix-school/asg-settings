package com.asg.settings.service;

import com.asg.common.lib.dto.AddressDetailsDTO;
import com.asg.common.lib.dto.AddressTypeMapDTO;
import com.asg.common.lib.dto.response.AddressMasterResponse;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.utility.ASGHelperUtils;
import com.asg.settings.entity.AddressDetails;
import com.asg.settings.entity.AddressMaster;
import com.asg.settings.entity.Country;
import com.asg.settings.repository.AddressDetailsRepository;
import com.asg.settings.repository.AddressMasterRepository;
import com.asg.settings.repository.AddressProcedureRepository;
import com.asg.settings.repository.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressMasterServiceTest {

    @Mock
    private CountryRepository countryRepo;
    @Mock
    private AddressMasterRepository masterRepo;
    @Mock
    private AddressDetailsRepository detailsRepo;
    @Mock
    private AddressProcedureRepository procRepo;

    @InjectMocks
    private AddressMasterService service;

    private AddressMaster testMaster;
    private AddressDetails testDetail;
    private Country testCountry;

    @BeforeEach
    void setUp() {
        testMaster = new AddressMaster();
        testMaster.setAddressMasterPoid(1L);
        testMaster.setAddressName("Test Company");
        testMaster.setCountryPoid(1L);
        testMaster.setActive("Y");

        testDetail = new AddressDetails();
        testDetail.setAddressPoid("1.1");
        testDetail.setAddressMasterPoid(1L);
        testDetail.setAddressType("MAIN");
        testDetail.setContactPerson("John Doe");
        testDetail.setMobile("1234567890");
        testDetail.setEmail("test@example.com");

        testCountry = new Country();
        testCountry.setCountryPoid(1L);
        testCountry.setCountryName("USA");
    }

    @Test
    void getMasterWithDetails_Success() {
        when(masterRepo.findById(1L)).thenReturn(Optional.of(testMaster));
        when(detailsRepo.findByAddressMasterPoidOrderByAddressType(1L)).thenReturn(Arrays.asList(testDetail));
        when(countryRepo.findById(1L)).thenReturn(Optional.of(testCountry));

        AddressMasterResponse result = service.getMasterWithDetails(1L);

        assertNotNull(result);
        assertEquals(1L, result.getAddressMasterPoid());
        assertEquals("Test Company", result.getAddressName());
        assertNotNull(result.getAddressTypeMap());
    }

    @Test
    void getMasterWithDetails_NotFound() {
        when(masterRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getMasterWithDetails(1L));
    }

//    @Test
//    void getAllMasters_Success() {
//        List<FilterDto> filters = Arrays.asList(new FilterDto("addressName", "Test"));
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<AddressMaster> masterPage = new PageImpl<>(Arrays.asList(testMaster));
//
//        when(masterRepo.findAddressMasters(filters, pageable)).thenReturn(masterPage);
//        when(countryRepo.findById(1L)).thenReturn(Optional.of(testCountry));
//
//        Page<AddressMasterLightDto> result = service.getAllMasters(filters, pageable);
//
//        assertNotNull(result);
//        assertEquals(1, result.getContent().size());
//        assertEquals("TEST COMPANY", result.getContent().get(0).addressName());
//    }

//    @Test
//    void getAllMasters_NullPageable() {
//        List<FilterDto> filters = Arrays.asList();
//        Page<AddressMaster> masterPage = new PageImpl<>(Arrays.asList(testMaster));
//
//        when(masterRepo.findAddressMasters(eq(filters), any(Pageable.class))).thenReturn(masterPage);
//        when(countryRepo.findById(1L)).thenReturn(Optional.of(testCountry));
//
//        Page<AddressMasterLightDto> result = service.getAllMasters(filters, null);
//
//        assertNotNull(result);
//        assertEquals(1, result.getContent().size());
//    }

//    @Test
//    void getAllMasters_EmptyResult() {
//        List<FilterDto> filters = Arrays.asList();
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<AddressMaster> emptyPage = new PageImpl<>(Arrays.asList());
//
//        when(masterRepo.findAddressMasters(filters, pageable)).thenReturn(emptyPage);
//
//        Page<AddressMasterLightDto> result = service.getAllMasters(filters, pageable);
//
//        assertNotNull(result);
//        assertTrue(result.getContent().isEmpty());
//    }

    @Test
    void saveAddressMaster_CreateSuccess() {
        try (MockedStatic<ASGHelperUtils> mockedUtils = mockStatic(ASGHelperUtils.class)) {
            mockedUtils.when(ASGHelperUtils::getCurrentUser).thenReturn("testUser");

            AddressMasterResponse request = createValidRequest();
            request.setAddressMasterPoid(null);

            when(masterRepo.existsByAddressNameIgnoreCase("Test Company")).thenReturn(false);
            when(masterRepo.save(any(AddressMaster.class))).thenReturn(testMaster);
            when(detailsRepo.findByAddressMasterPoidOrderByAddressType(1L)).thenReturn(Arrays.asList());

            Long result = service.saveAddressMaster(request);

            assertEquals(1L, result);
            verify(masterRepo).save(any(AddressMaster.class));
        }
    }

    @Test
    void saveAddressMaster_UpdateSuccess() {
        try (MockedStatic<ASGHelperUtils> mockedUtils = mockStatic(ASGHelperUtils.class)) {
            mockedUtils.when(ASGHelperUtils::getCurrentUser).thenReturn("testUser");

            AddressMasterResponse request = createValidRequest();
            request.setAddressMasterPoid(1L);

            when(masterRepo.existsByAddressNameIgnoreCaseAndAddressMasterPoidNot("Test Company", 1L)).thenReturn(false);
            when(masterRepo.findById(1L)).thenReturn(Optional.of(testMaster));
            when(masterRepo.save(any(AddressMaster.class))).thenReturn(testMaster);
            when(detailsRepo.findByAddressMasterPoidOrderByAddressType(1L)).thenReturn(Arrays.asList());

            Long result = service.saveAddressMaster(request);

            assertEquals(1L, result);
            verify(masterRepo).save(any(AddressMaster.class));
        }
    }

    @Test
    void saveAddressMaster_MissingAddressName() {
        AddressMasterResponse request = createValidRequest();
        request.setAddressName(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.saveAddressMaster(request));
        assertEquals("Address Name is mandatory", exception.getMessage());
    }

    @Test
    void saveAddressMaster_BlankAddressName() {
        AddressMasterResponse request = createValidRequest();
        request.setAddressName("   ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.saveAddressMaster(request));
        assertEquals("Address Name is mandatory", exception.getMessage());
    }

    @Test
    void saveAddressMaster_MissingSeqNo() {
        AddressMasterResponse request = createValidRequest();
        request.setSeqno(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.saveAddressMaster(request));
        assertEquals("Seq No is mandatory", exception.getMessage());
    }

    @Test
    void saveAddressMaster_MissingCountry() {
        AddressMasterResponse request = createValidRequest();
        request.setCountryId(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.saveAddressMaster(request));
        assertEquals("Country is mandatory", exception.getMessage());
    }

    @Test
    void saveAddressMaster_MissingMainContact() {
        AddressMasterResponse request = createValidRequest();
        request.setAddressTypeMap(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.saveAddressMaster(request));
        assertEquals("MAIN contact details are mandatory (Mobile & Email required)", exception.getMessage());
    }

    @Test
    void saveAddressMaster_EmptyMainContact() {
        AddressMasterResponse request = createValidRequest();
        AddressTypeMapDTO typeMap = new AddressTypeMapDTO();
        typeMap.setMAIN(Arrays.asList());
        request.setAddressTypeMap(typeMap);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.saveAddressMaster(request));
        assertEquals("MAIN contact details are mandatory (Mobile & Email required)", exception.getMessage());
    }

    @Test
    void saveAddressMaster_MissingMobile() {
        AddressMasterResponse request = new AddressMasterResponse();
        request.setAddressName("Test Company");
        request.setSeqno(1L);
        request.setCountryId(1L);
        request.setActive("Y");

        AddressDetailsDTO mainContact = AddressDetailsDTO.builder()
                .contactPerson("John Doe")
                .mobile(null)
                .email(Arrays.asList("test@example.com"))
                .build();

        AddressTypeMapDTO typeMap = new AddressTypeMapDTO();
        typeMap.setMAIN(Arrays.asList(mainContact));
        request.setAddressTypeMap(typeMap);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.saveAddressMaster(request));
        assertEquals("Mobile is mandatory", exception.getMessage());
    }

    @Test
    void saveAddressMaster_MissingEmail() {
        AddressMasterResponse request = new AddressMasterResponse();
        request.setAddressName("Test Company");
        request.setSeqno(1L);
        request.setCountryId(1L);
        request.setActive("Y");

        AddressDetailsDTO mainContact = AddressDetailsDTO.builder()
                .contactPerson("John Doe")
                .mobile("1234567890")
                .email(null)
                .build();

        AddressTypeMapDTO typeMap = new AddressTypeMapDTO();
        typeMap.setMAIN(Arrays.asList(mainContact));
        request.setAddressTypeMap(typeMap);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.saveAddressMaster(request));
        assertEquals("Email is mandatory", exception.getMessage());
    }

    @Test
    void saveAddressMaster_DuplicateNameCreate() {
        AddressMasterResponse request = createValidRequest();
        request.setAddressMasterPoid(null);

        when(masterRepo.existsByAddressNameIgnoreCase("Test Company")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.saveAddressMaster(request));
        assertEquals("Address Name already exists", exception.getMessage());
    }

    @Test
    void saveAddressMaster_DuplicateNameUpdate() {
        AddressMasterResponse request = createValidRequest();
        request.setAddressMasterPoid(1L);

        when(masterRepo.existsByAddressNameIgnoreCaseAndAddressMasterPoidNot("Test Company", 1L)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.saveAddressMaster(request));
        assertEquals("Address Name already exists", exception.getMessage());
    }

    @Test
    void softDeleteAddressMaster_Success() {
        try (MockedStatic<ASGHelperUtils> mockedUtils = mockStatic(ASGHelperUtils.class)) {
            mockedUtils.when(ASGHelperUtils::getCurrentUser).thenReturn("testUser");

            when(masterRepo.findById(1L)).thenReturn(Optional.of(testMaster));
            when(masterRepo.save(any(AddressMaster.class))).thenReturn(testMaster);

            assertDoesNotThrow(() -> service.softDeleteAddressMaster(1L));

            verify(detailsRepo).deleteByAddressMasterPoid(1L);
            verify(masterRepo).save(any(AddressMaster.class));
        }
    }

    @Test
    void softDeleteAddressMaster_NotFound() {
        when(masterRepo.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.softDeleteAddressMaster(1L));
        assertTrue(exception.getMessage().contains("Address Master"));
    }

    @Test
    void createAll_Success() {
        when(procRepo.createAllTypes(1L, null, 200L, 1L)).thenReturn("Success");

        String result = service.createAll(1L);

        assertEquals("Success", result);
        verify(procRepo).createAllTypes(1L, null, 200L, 1L);
    }

    @Test
    void copyAll_Success() {
        when(procRepo.copyAllTypes(1L, null, 200L, 1L)).thenReturn("Success");

        String result = service.copyAll(1L);

        assertEquals("Success", result);
        verify(procRepo).copyAllTypes(1L, null, 200L, 1L);
    }

    private AddressMasterResponse createValidRequest() {
        AddressMasterResponse request = new AddressMasterResponse();
        request.setAddressName("Test Company");
        request.setSeqno(1L);
        request.setCountryId(1L);
        request.setActive("Y");

        AddressDetailsDTO mainContact = AddressDetailsDTO.builder()
                .contactPerson("John Doe")
                .mobile("1234567890")
                .email(Arrays.asList("test@example.com"))
                .build();

        AddressTypeMapDTO typeMap = new AddressTypeMapDTO();
        typeMap.setMAIN(Arrays.asList(mainContact));
        request.setAddressTypeMap(typeMap);

        return request;
    }
}
