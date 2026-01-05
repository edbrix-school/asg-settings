package com.asg.settings.controller;

import com.asg.common.lib.dto.CountryDto;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.settings.exceptions.GlobalExceptionHandler;
import com.asg.settings.service.CountryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CountryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CountryService countryService;

    @InjectMocks
    private CountryController countryController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CountryDto countryDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(countryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        countryDto = new CountryDto();
        countryDto.setCountryPoid(1L);
        countryDto.setCountryCode("US");
        countryDto.setCountryName("United States");
        countryDto.setActive("Y");
    }

    @Test
    void getCountryByCountryPoid_ShouldReturnCountry() throws Exception {

        when(countryService.getCountryById(1L)).thenReturn(countryDto);

        mockMvc.perform(get("/api/v1/country-master/1")
                        .param("documentId", "800-320")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Task fetched successfully")))
                .andExpect(jsonPath("$.result.data.countryName", is("United States")))
                .andExpect(jsonPath("$.result.data.countryCode", is("US")));

        verify(countryService, times(1)).getCountryById(1L);
    }

    @Test
    void getCountryByCountryPoid_WhenNotFound_ShouldReturn404() throws Exception {

        when(countryService.getCountryById(1L)).thenThrow(new ResourceNotFoundException("Country", "countryPoid", 1L));

        mockMvc.perform(get("/api/v1/country-master/1")
                        .param("documentId", "800-320")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(countryService, times(1)).getCountryById(1L);
    }

    @Test
    void createCountry_ShouldCreateAndReturnCreatedCountry() throws Exception {

        when(countryService.createCountry(any(CountryDto.class))).thenReturn(countryDto);

        CountryDto createDto = new CountryDto();
        createDto.setCountryCode("US");
        createDto.setCountryName("United States");
        createDto.setActive("Y");
        createDto.setGroupPoid(1L);  // Add required groupPoid

        mockMvc.perform(post("/api/v1/country-master")
                        .param("documentId", "800-320")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Country created successfully")))
                .andExpect(jsonPath("$.result.data.countryName", is("United States")))
                .andExpect(jsonPath("$.result.data.countryCode", is("US")));

        verify(countryService, times(1)).createCountry(any(CountryDto.class));
    }
    @Test
    void createCountry_WithInvalidInput_ShouldReturnBadRequest() throws Exception {

        CountryDto invalidDto = new CountryDto(); // Missing required fields

        mockMvc.perform(post("/api/v1/country-master")
                        .param("documentId", "800-320")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(countryService, never()).createCountry(any(CountryDto.class));
    }

    @Test
    void updateCountry_ShouldUpdateAndReturnUpdatedCountry() throws Exception {

        when(countryService.updateCountry(anyLong(), any(CountryDto.class))).thenReturn(countryDto);

        CountryDto updateDto = new CountryDto();
        updateDto.setCountryPoid(1L);
        updateDto.setCountryCode("US");
        updateDto.setCountryName("United States Updated");
        updateDto.setActive("Y");
        updateDto.setGroupPoid(1L);

        mockMvc.perform(put("/api/v1/country-master/1")
                        .param("documentId", "800-320")
                        .param("actionRequested", "UPDATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Country updated successfully")))
                .andExpect(jsonPath("$.result.data.countryName", is("United States")))
                .andExpect(jsonPath("$.result.data.countryCode", is("US")));

        verify(countryService, times(1)).updateCountry(anyLong(), any(CountryDto.class));
    }
    @Test
    void updateCountry_WhenNotFound_ShouldReturn404() throws Exception {
        when(countryService.updateCountry(anyLong(), any(CountryDto.class)))
                .thenThrow(new ResourceNotFoundException("Country", "countryPoid", 1L));

        CountryDto updateDto = new CountryDto();
        updateDto.setCountryPoid(1L);
        updateDto.setCountryCode("US");
        updateDto.setCountryName("Non-existent Country");
        updateDto.setActive("Y");
        updateDto.setGroupPoid(1L);

        mockMvc.perform(put("/api/v1/country-master/1")
                        .param("documentId", "800-320")
                        .param("actionRequested", "UPDATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()))
                .andExpect(status().isNotFound());

        verify(countryService, times(1)).updateCountry(anyLong(), any(CountryDto.class));
    }

    @Test
    void updateCountry_WithMismatchedIds_ShouldReturnBadRequest() throws Exception {

        countryDto.setCountryPoid(2L); // Different from path variable

        mockMvc.perform(put("/api/v1/country-master/1")
                        .param("documentId", "800-320")
                        .param("actionRequested", "UPDATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(countryDto)))
                .andExpect(status().isBadRequest());

        verify(countryService, never()).updateCountry(anyLong(), any(CountryDto.class));
    }
}
