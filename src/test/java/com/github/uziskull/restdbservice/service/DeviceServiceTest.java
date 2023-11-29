package com.github.uziskull.restdbservice.service;

import com.github.uziskull.restdbservice.model.dao.DeviceDAO;
import com.github.uziskull.restdbservice.model.dto.DeviceRequest;
import com.github.uziskull.restdbservice.model.dto.DeviceResponse;
import com.github.uziskull.restdbservice.model.exception.DeviceNotFoundException;
import com.github.uziskull.restdbservice.model.exception.DuplicateDeviceException;
import com.github.uziskull.restdbservice.repository.DeviceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@DataJpaTest
class DeviceServiceTest {

    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    @DisplayName("Adding a device successfully")
    void addDevice_successful() {
        String name = "deviceName";
        String brand = "deviceBrand";

        DeviceRequest deviceRequest = new DeviceRequest();
        deviceRequest.setName(name);
        deviceRequest.setBrand(brand);

        DeviceResponse deviceResponse = deviceService.addDevice(deviceRequest);
        assertThat(deviceResponse.getName()).isEqualTo(name);
        assertThat(deviceResponse.getBrand()).isEqualTo(brand);
        assertThat(deviceRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Adding a device that already exists")
    void addDevice_duplicated() {
        String name = "deviceName";
        String brand = "deviceBrand";
        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.setName(name);
        deviceDAO.setBrand(brand);
        deviceRepository.saveAndFlush(deviceDAO);

        DeviceRequest deviceRequest = new DeviceRequest();
        deviceRequest.setName(name);
        deviceRequest.setBrand(brand);

        assertThatExceptionOfType(DuplicateDeviceException.class)
                .isThrownBy(() -> deviceService.addDevice(deviceRequest));
        assertThat(deviceRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Getting a device by its identifier")
    void getDeviceByIdentifier_successful() {
        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.setName("deviceName");
        deviceDAO.setBrand("deviceBrand");
        DeviceDAO currentDeviceDAO = deviceRepository.saveAndFlush(deviceDAO);

        DeviceResponse deviceResponse = deviceService.getDeviceByIdentifier(currentDeviceDAO.getId());
        assertThat(deviceResponse.getId()).isEqualTo(currentDeviceDAO.getId());
        assertThat(deviceResponse.getName()).isEqualTo(currentDeviceDAO.getName());
        assertThat(deviceResponse.getBrand()).isEqualTo(currentDeviceDAO.getBrand());
    }

    @Test
    @DisplayName("Getting a device by a non-existent identifier")
    void getDeviceByIdentifier_wrongId() {
        assertThatExceptionOfType(DeviceNotFoundException.class)
                .isThrownBy(() -> deviceService.getDeviceByIdentifier(UUID.randomUUID()));
    }

    @Test
    void listAllDevices() {
    }

    @Test
    void updateDevice() {
    }

    @Test
    void deleteDevice() {
    }

    @Test
    void searchDeviceByBrand() {
    }
}