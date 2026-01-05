package com.asg.settings.service;

import com.asg.common.lib.dto.CountryDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.entity.GroupEntity;
import com.asg.common.lib.exception.ResourceAlreadyExistsException;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.repository.GroupRepository;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.settings.entity.Country;
import com.asg.settings.repository.CountryRepository;
import com.asg.settings.service.impl.CountryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CountryServiceImplTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private DocumentSearchService documentService;

    @InjectMocks
    private CountryServiceImpl countryService;

    private Country country;
    private CountryDto countryDto;

    @BeforeEach
    void setUp() {
        country = new Country();
        country.setCountryPoid(1L);
        country.setCountryCode("US");
        country.setCountryName("United States");
        country.setCountryName2("USA");
        country.setActive("Y");
        country.setGroupPoid(1L);
        country.setRegionPoid(2L);
        country.setCreatedBy("testUser");
        country.setCreatedDate(LocalDateTime.now());
        country.setCountryTicketRate(10.5);

        countryDto = new CountryDto();
        countryDto.setCountryCode("US");
        countryDto.setCountryName("United States");
        countryDto.setCountryName2("USA");
        countryDto.setActive("Y");
        countryDto.setGroupPoid(1L);

        countryDto.setCountryTicketRate(10.5);
    }

    @Test
    void getCountryById_WhenCountryExists_ShouldReturnCountryDto() {
        when(countryRepository.existsByCountryPoid(1L)).thenReturn(true);
        when(countryRepository.findByCountryPoid(1L)).thenReturn(country);

        CountryDto result = countryService.getCountryById(1L);

        assertNotNull(result);
        assertEquals("United States", result.getCountryName());
        assertEquals("US", result.getCountryCode());
        assertEquals("Y", result.getActive());
        verify(countryRepository, times(1)).existsByCountryPoid(1L);
        verify(countryRepository, times(1)).findByCountryPoid(1L);
    }

    @Test
    void getCountryById_WhenCountryNotExists_ShouldThrowException() {
        when(countryRepository.existsByCountryPoid(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                countryService.getCountryById(1L)
        );
        verify(countryRepository, times(1)).existsByCountryPoid(1L);
        verify(countryRepository, never()).findByCountryPoid(anyLong());
    }

    @Test
    void createCountry_ShouldSaveAndReturnCountryDto() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(countryRepository.existsByCountryCode("US")).thenReturn(false);
            when(groupRepository.findById(1L)).thenReturn(Optional.of(new GroupEntity()));
            when(countryRepository.save(any(Country.class))).thenAnswer(invocation -> {
                Country saved = invocation.getArgument(0);
                saved.setCountryPoid(1L);
                return saved;
            });

            CountryDto result = countryService.createCountry(countryDto);

            assertNotNull(result);
            assertEquals("US", result.getCountryCode());
            assertEquals("United States", result.getCountryName());
            assertEquals("Y", result.getActive());
            assertEquals(1L, result.getCountryPoid());
            verify(countryRepository).existsByCountryCode("US");
            verify(groupRepository).findById(1L);
            verify(countryRepository).save(any(Country.class));
        }
    }

    @Test
    void createCountry_ShouldSetDefaultActiveToY_WhenNotProvided() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            countryDto.setActive(null);
            when(countryRepository.existsByCountryCode("US")).thenReturn(false);
            when(groupRepository.findById(1L)).thenReturn(Optional.of(new GroupEntity()));
            when(countryRepository.save(any(Country.class))).thenAnswer(invocation -> {
                Country saved = invocation.getArgument(0);
                saved.setCountryPoid(1L);
                return saved;
            });

            CountryDto result = countryService.createCountry(countryDto);

            assertNotNull(result);
            assertEquals("Y", result.getActive());
            verify(countryRepository).save(any(Country.class));
        }
    }

    @Test
    void updateCountry_WhenCountryExists_ShouldUpdateAndReturnUpdatedDto() {
        CountryDto updateDto = new CountryDto();
        updateDto.setCountryName("Updated Country");
        updateDto.setCountryName2("Updated USA");
        updateDto.setActive("N");
        updateDto.setCountryTicketRate(15.0);
        updateDto.setGroupPoid(1L);

        Country updatedCountry = new Country();
        BeanUtils.copyProperties(country, updatedCountry);
        updatedCountry.setCountryName("Updated Country");
        updatedCountry.setCountryName2("Updated USA");
        updatedCountry.setActive("N");
        updatedCountry.setCountryTicketRate(15.0);

        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(new GroupEntity()));
        when(countryRepository.save(any(Country.class))).thenReturn(updatedCountry);

        CountryDto result = countryService.updateCountry(1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Country", result.getCountryName());
        assertEquals("Updated USA", result.getCountryName2());
        assertEquals("N", result.getActive());
        assertEquals(15.0, result.getCountryTicketRate());
        verify(countryRepository).findById(1L);
        verify(groupRepository).findById(1L);
        verify(countryRepository).save(any(Country.class));
    }

    @Test
    void updateCountry_WhenCountryNotExists_ShouldThrowException() {

        when(countryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                countryService.updateCountry(1L, new CountryDto())
        );
        verify(countryRepository, times(1)).findById(1L);
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void updateCountry_ShouldNotUpdateFields_WhenNullValuesProvided() {
        CountryDto updateDto = new CountryDto();
        updateDto.setGroupPoid(1L); // Required field

        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(new GroupEntity()));
        when(countryRepository.save(any(Country.class))).thenReturn(country);

        CountryDto result = countryService.updateCountry(1L, updateDto);

        assertNotNull(result);
        assertEquals(country.getCountryName(), result.getCountryName());
        assertEquals(country.getActive(), result.getActive());
        verify(countryRepository).findById(1L);
        verify(groupRepository).findById(1L);
        verify(countryRepository).save(any(Country.class));
    }

//    @Test
//    void getAllcountries_ShouldReturnPagedResults() {
//        List<Country> countries = Arrays.asList(country);
//        Pageable pageable = PageRequest.of(0, 10, Sort.by("countryName"));
//        Page<Country> countryPage = new PageImpl<>(countries, pageable, countries.size());
//
//        when(countryRepository.getAllCountries(anyList(), eq(pageable))).thenReturn(countryPage);
//
//
//        Page<CountryDto> result = countryService.getAllcountries(Collections.emptyList(), pageable);
//
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        assertEquals("United States", result.getContent().get(0).getCountryName());
//        verify(countryRepository, times(1)).getAllCountries(anyList(), eq(pageable));
//    }

    @Test
    void updateCountry_ShouldUpdateOnlyProvidedFields() {
        CountryDto updateDto = new CountryDto();
        updateDto.setCountryName("Updated Name Only");
        updateDto.setGroupPoid(1L);

        Country updatedCountry = new Country();
        BeanUtils.copyProperties(country, updatedCountry);
        updatedCountry.setCountryName("Updated Name Only");

        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(new GroupEntity()));
        when(countryRepository.save(any(Country.class))).thenReturn(updatedCountry);

        CountryDto result = countryService.updateCountry(1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Name Only", result.getCountryName());
        assertEquals(country.getCountryCode(), result.getCountryCode());
        verify(countryRepository).findById(1L);
        verify(groupRepository).findById(1L);
        verify(countryRepository).save(any(Country.class));
    }

    @Test
    void createCountry_WithDuplicateCountryCode_ShouldThrowException() {
        when(countryRepository.existsByCountryCode("US")).thenReturn(true);

        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class,
                () -> countryService.createCountry(countryDto));

        assertEquals("countryCode already exists with value: US", exception.getMessage());
        verify(countryRepository).existsByCountryCode("US");
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void createCountry_WithInvalidGroup_ShouldThrowException() {
        when(countryRepository.existsByCountryCode("US")).thenReturn(false);
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> countryService.createCountry(countryDto));

        assertEquals("Group not found with groupPoid : '1'", exception.getMessage());
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void updateCountry_WithInvalidGroup_ShouldThrowException() {
        CountryDto updateDto = new CountryDto();
        updateDto.setGroupPoid(999L);

        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> countryService.updateCountry(1L, updateDto));

        assertEquals("Group not found with groupPoid : '999'", exception.getMessage());
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void softDeleteCountry_ShouldMarkAsDeletedAndInactive() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
            when(countryRepository.save(any(Country.class))).thenAnswer(invocation -> invocation.getArgument(0));

            countryService.softDeleteCountry(1L);

            assertEquals("Y", country.getDeleted());
            assertEquals("N", country.getActive());
            assertNotNull(country.getLastModifiedDate());
            assertEquals("testUser", country.getLastModifiedBy());
            verify(countryRepository).findById(1L);
            verify(countryRepository).save(country);
        }
    }

    @Test
    void softDeleteCountry_WithNonExistentCountry_ShouldThrowException() {
        when(countryRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> countryService.softDeleteCountry(999L));

        assertEquals("Country not found with countryPoid : '999'", exception.getMessage());
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void listCountries_ShouldReturnWrappedPage() {
        String docId = "COUNTRY_LIST";
        FilterRequestDto request = new FilterRequestDto("OR", "N", List.of());
        Pageable pageable = PageRequest.of(0, 10);

        RawSearchResult rawResult = new RawSearchResult(
                List.of(Map.of("COUNTRY_NAME", "United States", "COUNTRY_POID", 1L)),
                Map.of("COUNTRY_NAME", "string", "COUNTRY_POID", "number"),
                1L
        );

        when(documentService.resolveOperator(request)).thenReturn("OR");
        when(documentService.resolveIsDeleted(request)).thenReturn("N");
        when(documentService.resolveFilters(request)).thenReturn(List.of());

        when(documentService.search(eq(docId), anyList(), eq("OR"), eq(pageable), eq("N"),
                eq("COUNTRY_NAME"), eq("COUNTRY_POID"))).thenReturn(rawResult);

        Map<String, Object> result = countryService.listCountries(docId, request, pageable);

        assertNotNull(result);
        verify(documentService).search(eq(docId), anyList(), eq("OR"), eq(pageable), eq("N"),
                eq("COUNTRY_NAME"), eq("COUNTRY_POID"));
    }

    @Test
    void createCountry_WithNullUserContext_ShouldUseSystemUser() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(null);

            when(countryRepository.existsByCountryCode("US")).thenReturn(false);
            when(groupRepository.findById(1L)).thenReturn(Optional.of(new GroupEntity()));
            when(countryRepository.save(any(Country.class))).thenAnswer(invocation -> {
                Country saved = invocation.getArgument(0);
                saved.setCountryPoid(1L);
                return saved;
            });

            CountryDto result = countryService.createCountry(countryDto);

            assertNotNull(result);
            ArgumentCaptor<Country> captor = ArgumentCaptor.forClass(Country.class);
            verify(countryRepository).save(captor.capture());
            assertEquals("SYSTEM", captor.getValue().getCreatedBy());
        }
    }
}
