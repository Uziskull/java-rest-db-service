package com.github.uziskull.restdbservice.data.dto;

import com.github.uziskull.restdbservice.data.dao.DeviceDAO;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

@Data
public class DeviceResponse extends RepresentationModel<DeviceResponse> {
    private UUID id;
    private String name;
    private String brand;

    public static DeviceResponse fromDAO(DeviceDAO deviceDAO) {
        DeviceResponse deviceResponse = new DeviceResponse();
        deviceResponse.setId(deviceDAO.getId());
        deviceResponse.setName(deviceDAO.getName());
        deviceResponse.setBrand(deviceDAO.getBrand());
        return deviceResponse;
    }
}
