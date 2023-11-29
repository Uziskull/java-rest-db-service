package com.github.uziskull.restdbservice.controller;

import com.github.uziskull.restdbservice.model.dto.DeviceRequest;
import com.github.uziskull.restdbservice.model.dto.DeviceResponse;
import com.github.uziskull.restdbservice.model.dto.ErrorResponse;
import com.github.uziskull.restdbservice.model.exception.DeviceException;
import com.github.uziskull.restdbservice.service.DeviceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("api/v1/devices")
@AllArgsConstructor
@Slf4j
public class DeviceController {
    private final DeviceService deviceService;
    private final PagedResourcesAssembler<DeviceResponse> assembler;

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@NonNull @RequestBody DeviceRequest deviceRequest) {
        log.debug("Creating device: {}", deviceRequest);
        DeviceResponse deviceResponse = deviceService.addDevice(deviceRequest);
        Link selfRel = linkTo(methodOn(DeviceController.class)
                .getDeviceById(deviceResponse.getId()))
                .withSelfRel();
        deviceResponse.add(selfRel);
        return ResponseEntity.created(selfRel.toUri()).body(deviceResponse);
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<DeviceResponse>>> getAllDevices(Pageable pageable) {
        log.debug("Getting all devices, with pagination: {}", pageable);
        Page<DeviceResponse> deviceResponses = deviceService.listAllDevices(pageable);
        return ResponseEntity.ok(assembler.toModel(deviceResponses));
    }

    @GetMapping("brand/{brand}")
    public ResponseEntity<PagedModel<EntityModel<DeviceResponse>>> getDevicesByBrand(@PathVariable String brand,
                                                                                     Pageable pageable) {
        log.debug("Getting all devices from brand \"{}\", with pagination: {}", brand, pageable);
        Page<DeviceResponse> deviceResponses = deviceService.searchDeviceByBrand(brand, pageable);
        return ResponseEntity.ok(assembler.toModel(deviceResponses));
    }

    @GetMapping("{id}")
    public ResponseEntity<DeviceResponse> getDeviceById(@PathVariable UUID id) {
        log.debug("Getting device with ID \"{}\"", id);
        DeviceResponse deviceResponse = deviceService.getDeviceByIdentifier(id);
        deviceResponse.add(linkTo(methodOn(DeviceController.class)
                .getDeviceById(deviceResponse.getId()))
                .withSelfRel());
        return ResponseEntity.ok(deviceResponse);
    }

    @PutMapping("{id}")
    public ResponseEntity<Object> updateDevice(@PathVariable UUID id,
                                               @NonNull @RequestBody DeviceRequest deviceRequest) {
        log.debug("Updating device with ID \"{}\", with the following fields: {}", id, deviceRequest);
        DeviceResponse deviceResponse = deviceService.updateDevice(id, deviceRequest);
        deviceResponse.add(linkTo(methodOn(DeviceController.class)
                .getDeviceById(deviceResponse.getId()))
                .withSelfRel());
        return ResponseEntity.ok(deviceResponse);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<DeviceResponse> deleteDevice(@PathVariable UUID id) {
        log.debug("Deleting device with ID \"{}\"", id);
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleDeviceException(DeviceException e) {
        log.error("Error performing request:", e);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .message(e.getClass().getSimpleName())
                        .description(e.getMessage())
                        .build());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleInputBodyException(HttpMessageNotReadableException e) {
        log.error("Error performing request:", e);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .message(e.getClass().getSimpleName())
                        .description("The request body was unable to be parsed.")
                        .build());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleInputPathParamException(MethodArgumentTypeMismatchException e) {
        log.error("Error performing request:", e);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .message(e.getClass().getSimpleName())
                        .description("The following request path parameter was unable to be parsed: \"" +
                                e.getParameter().getParameterName() + "\"")
                        .build());
    }
}
