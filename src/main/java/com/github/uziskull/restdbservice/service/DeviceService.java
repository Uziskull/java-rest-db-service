package com.github.uziskull.restdbservice.service;

import com.github.uziskull.restdbservice.data.dao.DeviceDAO;
import com.github.uziskull.restdbservice.data.dto.DeviceRequest;
import com.github.uziskull.restdbservice.data.dto.DeviceResponse;
import com.github.uziskull.restdbservice.repository.DeviceRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DeviceService {

    private DeviceRepository deviceRepository;

    public DeviceResponse addDevice(DeviceRequest deviceRequest) {
        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.setName(deviceRequest.getName());
        deviceDAO.setBrand(deviceRequest.getBrand());
        return DeviceResponse.fromDAO(deviceRepository.save(deviceDAO));
    }

    public Optional<DeviceResponse> getDeviceByIdentifier(UUID id) {
        return deviceRepository.findById(id)
                .map(DeviceResponse::fromDAO);
    }

    public Page<DeviceResponse> listAllDevices(Pageable pageable) {
        return deviceRepository.findAll(pageable)
                .map(DeviceResponse::fromDAO);
    }

    public Optional<DeviceResponse> updateDevice(UUID deviceId, DeviceRequest deviceRequest) {
        Optional<DeviceDAO> foundDevice = deviceRepository.findById(deviceId);
        if (foundDevice.isPresent()) {
            DeviceDAO deviceDAO = foundDevice.get();
            if (deviceRequest.getName() != null) {
                deviceDAO.setName(deviceRequest.getName());
            }
            if (deviceRequest.getBrand() != null) {
                deviceDAO.setBrand(deviceRequest.getBrand());
            }
            deviceRepository.save(deviceDAO);
            return Optional.of(DeviceResponse.fromDAO(deviceDAO));
        }
        return Optional.empty();
    }

    public boolean deleteDevice(UUID deviceId) {
        return deviceRepository.removeById(deviceId) > 0;
    }

    public Page<DeviceResponse> searchDeviceByBrand(String brand, Pageable pageable) {
        if (brand != null) {
            return deviceRepository.findByBrand(brand, pageable)
                    .map(DeviceResponse::fromDAO);
        }
        return Page.empty();
    }
}
