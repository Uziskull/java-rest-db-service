package com.github.uziskull.restdbservice.service;

import com.github.uziskull.restdbservice.model.dao.DeviceDAO;
import com.github.uziskull.restdbservice.model.dto.DeviceRequest;
import com.github.uziskull.restdbservice.model.dto.DeviceResponse;
import com.github.uziskull.restdbservice.model.exception.DeviceNotFoundException;
import com.github.uziskull.restdbservice.model.exception.DuplicateDeviceException;
import com.github.uziskull.restdbservice.model.exception.MissingDeviceFieldsException;
import com.github.uziskull.restdbservice.repository.DeviceRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DeviceService {

    private DeviceRepository deviceRepository;

    public DeviceResponse addDevice(@NonNull DeviceRequest deviceRequest) {
        if (deviceRequest.getName() == null || deviceRequest.getBrand() == null) {
            throw new MissingDeviceFieldsException();
        }
        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.setName(deviceRequest.getName());
        deviceDAO.setBrand(deviceRequest.getBrand());
        try {
            return DeviceResponse.fromDAO(deviceRepository.save(deviceDAO));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateDeviceException();
        }
    }

    public DeviceResponse getDeviceByIdentifier(@NonNull UUID id) {
        return deviceRepository.findById(id)
                .map(DeviceResponse::fromDAO)
                .orElseThrow(DeviceNotFoundException::new);
    }

    public Page<DeviceResponse> listAllDevices(Pageable pageable) {
        return deviceRepository.findAll(pageable)
                .map(DeviceResponse::fromDAO);
    }

    public DeviceResponse updateDevice(@NonNull UUID deviceId,
                                       @NonNull DeviceRequest deviceRequest) {
        Optional<DeviceDAO> foundDevice = deviceRepository.findById(deviceId);
        if (foundDevice.isEmpty()) {
            throw new DeviceNotFoundException();
        }
        DeviceDAO deviceDAO = foundDevice.get();
        if (deviceRequest.getName() != null) {
            deviceDAO.setName(deviceRequest.getName());
        }
        if (deviceRequest.getBrand() != null) {
            deviceDAO.setBrand(deviceRequest.getBrand());
        }
        try {
            return DeviceResponse.fromDAO(deviceRepository.save(deviceDAO));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateDeviceException();
        }
    }

    public void deleteDevice(@NonNull UUID deviceId) {
        try {
            deviceRepository.deleteById(deviceId);
        } catch (EmptyResultDataAccessException e) {
            throw new DeviceNotFoundException();
        }
    }

    public Page<DeviceResponse> searchDeviceByBrand(@NonNull String brand, Pageable pageable) {
        return deviceRepository.findByBrand(brand, pageable)
                .map(DeviceResponse::fromDAO);
    }
}
