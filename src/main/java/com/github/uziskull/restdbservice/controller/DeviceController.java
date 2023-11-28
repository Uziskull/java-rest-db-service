package com.github.uziskull.restdbservice.controller;

import com.github.uziskull.restdbservice.data.dto.DeviceRequest;
import com.github.uziskull.restdbservice.data.dto.DeviceResponse;
import com.github.uziskull.restdbservice.service.DeviceService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController("api/v1/devices")
@AllArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@RequestBody DeviceRequest deviceRequest) {
        DeviceResponse deviceResponse = deviceService.addDevice(deviceRequest);
        Link selfRel = linkTo(methodOn(DeviceController.class)
                .getDeviceById(deviceResponse.getId()))
                .withSelfRel();
        deviceResponse.add(selfRel);
        return ResponseEntity.created(selfRel.toUri()).body(deviceResponse);
    }

    @GetMapping
    public ResponseEntity<PagedModel<DeviceResponse>> getAllDevices(Pageable pageable) {
        Page<DeviceResponse> deviceResponses = deviceService.listAllDevices(pageable);
    }

    @GetMapping("brand/{brand}")
    public ResponseEntity<PagedModel<DeviceResponse>> getDevicesByBrand(@PathVariable String brand,
                                                                        Pageable pageable) {
        Page<DeviceResponse> deviceResponses = deviceService.searchDeviceByBrand(brand, pageable);

    }

    @GetMapping("{id}")
    public ResponseEntity<DeviceResponse> getDeviceById(@PathVariable UUID id) {
        Optional<DeviceResponse> deviceResponse = deviceService.getDeviceByIdentifier(id);
    }

    @PutMapping("{id}")
    public ResponseEntity<DeviceResponse> updateDevice(@PathVariable UUID id,
                                                       @RequestBody DeviceRequest deviceRequest) {
        Optional<DeviceResponse> deviceResponse = deviceService.updateDevice(id, deviceRequest);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<DeviceResponse> deleteDevice(@PathVariable UUID id) {
        boolean isDeleted = deviceService.deleteDevice(id);
    }
}
