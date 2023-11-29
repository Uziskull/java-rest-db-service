package com.github.uziskull.restdbservice.model.dto;

import com.github.uziskull.restdbservice.model.dao.DeviceDAO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.Instant;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceResponse extends RepresentationModel<DeviceResponse> {
    private UUID id;
    private String name;
    private String brand;
    private Instant creationTimestamp;

    public static DeviceResponse fromDAO(DeviceDAO deviceDAO) {
        DeviceResponse deviceResponse = new DeviceResponse();
        deviceResponse.setId(deviceDAO.getId());
        deviceResponse.setName(deviceDAO.getName());
        deviceResponse.setBrand(deviceDAO.getBrand());
        deviceResponse.setCreationTimestamp(deviceDAO.getCreationTimestamp());
        return deviceResponse;
    }
}