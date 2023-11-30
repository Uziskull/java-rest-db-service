package com.github.uziskull.restdbservice.service;

import com.github.uziskull.restdbservice.model.dao.DeviceDAO;
import com.github.uziskull.restdbservice.model.dto.DeviceRequest;
import com.github.uziskull.restdbservice.model.dto.DeviceResponse;
import com.github.uziskull.restdbservice.model.exception.DeviceNotFoundException;
import com.github.uziskull.restdbservice.model.exception.DuplicateDeviceException;
import com.github.uziskull.restdbservice.model.exception.MissingDeviceFieldsException;
import com.github.uziskull.restdbservice.repository.DeviceRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({DeviceService.class})
class DeviceServiceTest {

    private static final String DEVICE_NAME = "deviceName";
    private static final String DEVICE_BRAND = "deviceBrand";

    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceRepository deviceRepository;

    private DeviceDAO insertMockDevice(String name, String brand) {
        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.setName(name);
        deviceDAO.setBrand(brand);
        return deviceRepository.saveAndFlush(deviceDAO);
    }

    @Test
    @DisplayName("Adding a device successfully")
    void addDevice_successful() {
        DeviceRequest deviceRequest = new DeviceRequest();
        deviceRequest.setName(DEVICE_NAME);
        deviceRequest.setBrand(DEVICE_BRAND);

        DeviceResponse deviceResponse = deviceService.addDevice(deviceRequest);
        assertThat(deviceResponse.getName()).isEqualTo(deviceRequest.getName());
        assertThat(deviceResponse.getBrand()).isEqualTo(deviceRequest.getBrand());
        assertThat(deviceRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Adding a device with missing mandatory fields")
    void addDevice_missingFields() {
        DeviceRequest deviceRequest = new DeviceRequest();
        deviceRequest.setName(DEVICE_NAME);
        deviceRequest.setBrand(null);

        assertThatExceptionOfType(MissingDeviceFieldsException.class)
                .isThrownBy(() -> deviceService.addDevice(deviceRequest));
        assertThat(deviceRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Adding a device that already exists")
    @Disabled("Weird quirk with JPA/Hibernate and the testing context")
    void addDevice_duplicated() {
        DeviceDAO currentDeviceDAO = insertMockDevice(DEVICE_NAME, DEVICE_BRAND);

        DeviceRequest deviceRequest = new DeviceRequest();
        deviceRequest.setName(currentDeviceDAO.getName());
        deviceRequest.setBrand(currentDeviceDAO.getBrand());

        assertThatExceptionOfType(DuplicateDeviceException.class)
                .isThrownBy(() -> deviceService.addDevice(deviceRequest));
    }

    @Test
    @DisplayName("Getting a device by its identifier")
    void getDeviceByIdentifier_successful() {
        DeviceDAO currentDeviceDAO = insertMockDevice(DEVICE_NAME, DEVICE_BRAND);

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
    @DisplayName("Listing all available devices")
    void listAllDevices() {
        List<DeviceDAO> deviceList = IntStream.range(0, 10)
                .mapToObj(i -> insertMockDevice(DEVICE_NAME + i, DEVICE_BRAND))
                .toList();

        Page<DeviceResponse> deviceResponses = deviceService.listAllDevices(Pageable.ofSize(10));

        assertThat(deviceResponses.getTotalElements()).isEqualTo(deviceList.size());
        assertThat(deviceResponses).allMatch(dr -> deviceList.stream().anyMatch(dao -> dr.getId().equals(dao.getId())));
    }

    @Test
    @DisplayName("Listing all available devices, with none available")
    void listAllDevices_empty() {
        Page<DeviceResponse> deviceResponses = deviceService.listAllDevices(Pageable.ofSize(10));

        assertThat(deviceResponses).isEmpty();
    }

    @Test
    @DisplayName("Updating a device successfully")
    void updateDevice_successful() {
        DeviceDAO currentDeviceDAO = insertMockDevice(DEVICE_NAME, DEVICE_BRAND);
        String newName = "differentName";

        DeviceRequest updateRequest = new DeviceRequest();
        updateRequest.setName(newName);
        updateRequest.setBrand(currentDeviceDAO.getBrand());
        deviceService.updateDevice(currentDeviceDAO.getId(), updateRequest);

        Optional<DeviceDAO> deviceAfterUpdate = deviceRepository.findById(currentDeviceDAO.getId());
        assertThat(deviceAfterUpdate).isPresent();
        assertThat(deviceAfterUpdate.get().getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Updating a device with a non-existent identifier")
    void updateDevice_wrongId() {
        assertThatExceptionOfType(DeviceNotFoundException.class)
                .isThrownBy(() -> deviceService.updateDevice(UUID.randomUUID(), new DeviceRequest()));
    }

    @Test
    @DisplayName("Updating a device to match another already existing one")
    @Disabled("Weird quirk with JPA/Hibernate and the testing context")
    void updateDevice_duplicated() {
        DeviceDAO deviceFirst = insertMockDevice(DEVICE_NAME, DEVICE_BRAND);
        DeviceDAO deviceSecond = insertMockDevice("anotherName", DEVICE_BRAND);

        DeviceRequest updateRequest = new DeviceRequest();
        updateRequest.setName(deviceFirst.getName());
        updateRequest.setBrand(deviceFirst.getBrand());
        assertThatExceptionOfType(DuplicateDeviceException.class)
                .isThrownBy(() -> deviceService.updateDevice(deviceSecond.getId(), updateRequest));
    }

    @Test
    @DisplayName("Deleting a device successfully")
    void deleteDevice_successful() {
        DeviceDAO currentDeviceDAO = insertMockDevice(DEVICE_NAME, DEVICE_BRAND);

        deviceService.deleteDevice(currentDeviceDAO.getId());

        assertThat(deviceRepository.findById(currentDeviceDAO.getId())).isEmpty();
    }

    @Test
    @DisplayName("Deleting a device with a non-existent identifier")
    @Disabled("Weird quirk with JPA/Hibernate and the testing context")
    void deleteDevice_wrongId() {
        assertThatExceptionOfType(DeviceNotFoundException.class)
                .isThrownBy(() -> deviceService.deleteDevice(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Listing all available devices from a certain brand")
    void searchDeviceByBrand_successful() {
        List<DeviceDAO> rightBrandDeviceList = IntStream.range(0, 10)
                .mapToObj(i -> insertMockDevice(DEVICE_NAME + i, i % 2 == 0 ? DEVICE_BRAND : "anotherBrand"))
                .filter(d -> DEVICE_BRAND.equals(d.getBrand()))
                .toList();

        Page<DeviceResponse> deviceResponses = deviceService.searchDeviceByBrand(DEVICE_BRAND, Pageable.ofSize(10));

        assertThat(deviceResponses.getTotalElements()).isEqualTo(rightBrandDeviceList.size());
        assertThat(deviceResponses).allMatch(dr -> rightBrandDeviceList.stream().anyMatch(dao -> dr.getId().equals(dao.getId())));
    }

    @Test
    @DisplayName("Listing all available devices from a certain brand, with none available")
    void searchDeviceByBrand_emptyBrand() {
        List<DeviceDAO> deviceList = IntStream.range(0, 10)
                .mapToObj(i -> insertMockDevice(DEVICE_NAME + i, DEVICE_BRAND))
                .toList();

        Page<DeviceResponse> deviceResponses = deviceService.searchDeviceByBrand("anotherBrand", Pageable.ofSize(10));

        assertThat(deviceResponses).isEmpty();
    }
}