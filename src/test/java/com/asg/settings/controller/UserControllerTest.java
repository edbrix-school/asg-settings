package com.asg.settings.controller;

import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.settings.dto.CreateUserRequest;
import com.asg.settings.dto.UserDto;
import com.asg.settings.dto.UserResponse;
import com.asg.settings.dto.request.ResetPasswordRequest;
import com.asg.settings.exceptions.GlobalExceptionHandler;
import com.asg.settings.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userController, "loggingService", loggingService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getUserListByRolePoid_Success() throws Exception {
        UserResponse userResponse = new UserResponse();
        when(userService.getUserDetailsByRolePoid(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/v1/users")
                        .param("userRoleId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User list fetched successfully"));

        verify(userService).getUserDetailsByRolePoid(1L);
    }

    @Test
    void resetPassword_Success() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setUserId("testUser");

        when(userService.resetUserPassword("testUser")).thenReturn("TRUE");
        when(userService.getUserPoidByUserId("testUser")).thenReturn(1L);
        doNothing().when(loggingService).createLogSummaryEntry(any(), any(), any());

        mockMvc.perform(post("/v1/users/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successful"));
    }

    @Test
    void resetPassword_Failure() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setUserId("testUser");

        when(userService.resetUserPassword("testUser")).thenReturn("FALSE");

        mockMvc.perform(post("/v1/users/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void resetPassword_BlankResult() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setUserId("testUser");

        when(userService.resetUserPassword("testUser")).thenReturn("");

        mockMvc.perform(post("/v1/users/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void resetPassword_NullResult() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setUserId("testUser");

        when(userService.resetUserPassword("testUser")).thenReturn(null);

        mockMvc.perform(post("/v1/users/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void listUsers_Success() throws Exception {
        Map<String, Object> users = new HashMap<>();
        users.put("content", Collections.emptyList());
        users.put("totalElements", 0);
        
        when(userService.listUsers(eq("000-005"), any(FilterRequestDto.class), any(Pageable.class)))
                .thenReturn(users);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("000-005");
            
            mockMvc.perform(post("/v1/users/list")
                            .param("documentId", "000-005")
                            .param("actionRequested", "VIEW")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"operator\":\"AND\",\"isDeleted\":\"N\",\"filters\":[]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Users list fetched successfully"));
        }
    }

    @Test
    void getUserDetails_Success() throws Exception {
        UserDto userDto = new UserDto("testUser", 1L, "Test User", "Test User", 1L, 1L,
                null, null, null, null, "US", "1234567890", "test@example.com",
                null, null, 1, "Y", "L", "N", null, "N", "admin", null, "admin", null);
        when(userService.getUserDetails(1L)).thenReturn(userDto);
        doNothing().when(loggingService).createLogSummaryEntry(any(), any(), any());

        mockMvc.perform(get("/v1/users/userdetails")
                        .param("userPoid", "1")
                        .param("documentId", "000-005")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User details fetched successfully"));
    }

    @Test
    void createUser_Success() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserId("testUser");
        request.setUserName("Test User");
        request.setUserEmail("test@example.com");
        request.setAuthenticationMethod("L");

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn("1");
        when(userService.createNewUserPassword(1L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/v1/users/create")
                        .param("documentId", "000-005")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"));
    }
}
