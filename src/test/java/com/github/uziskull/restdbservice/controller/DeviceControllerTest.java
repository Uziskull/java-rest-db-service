package com.github.uziskull.restdbservice.controller;

import com.github.uziskull.restdbservice.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceService deviceService;

    @Test
    void createDevice() {
    }

    @Test
    void getAllDevices() {
    }

    @Test
    void getDevicesByBrand() {
    }

    @Test
    void getDeviceById() {
    }

    @Test
    void updateDevice() {
    }

    @Test
    void deleteDevice() {
    }
}