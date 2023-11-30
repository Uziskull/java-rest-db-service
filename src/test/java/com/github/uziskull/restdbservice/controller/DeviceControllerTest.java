package com.github.uziskull.restdbservice.controller;

import com.github.uziskull.restdbservice.model.dto.DeviceRequest;
import com.github.uziskull.restdbservice.model.dto.DeviceResponse;
import com.github.uziskull.restdbservice.model.exception.DeviceNotFoundException;
import com.github.uziskull.restdbservice.model.exception.MissingDeviceFieldsException;
import com.github.uziskull.restdbservice.service.DeviceService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    private static final String DEVICE_CONTROLLER_PATH = "/api/v1/devices";
    private static final String DEVICE_NAME = "deviceName";
    private static final String DEVICE_BRAND = "deviceBrand";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceService deviceService;

    @Test
    @DisplayName("Create a device successfully")
    void createDevice_successful() throws Exception {
        DeviceResponse deviceResponse = new DeviceResponse();
        deviceResponse.setId(UUID.randomUUID());
        deviceResponse.setName(DEVICE_NAME);
        deviceResponse.setBrand(DEVICE_BRAND);
        when(deviceService.addDevice(argThat(dr -> DEVICE_NAME.equals(dr.getName()) && DEVICE_BRAND.equals(dr.getBrand()))))
                .thenReturn(deviceResponse);

        JSONObject result = new JSONObject(mockMvc.perform(post(DEVICE_CONTROLLER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONObject()
                                .put("name", DEVICE_NAME)
                                .put("brand", DEVICE_BRAND)
                                .toString()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        assertThat(result.get("name")).isEqualTo(DEVICE_NAME);
        assertThat(result.get("brand")).isEqualTo(DEVICE_BRAND);
    }

    @Test
    @DisplayName("Create a device using wrong or empty input body")
    void createDevice_wrongOrEmpty() throws Exception {
        for (String body : List.of("wrong body", "")) {
            JSONObject result = new JSONObject(mockMvc.perform(post(DEVICE_CONTROLLER_PATH)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse().getContentAsString());

            assertThat(result.get("description")).isEqualTo("The request body was unable to be parsed.");
        }
    }

    @Test
    @DisplayName("Create a device using null mandatory fields")
    void createDevice_invalidFields() throws Exception {
        when(deviceService.addDevice(argThat(dr -> dr.getName() == null || dr.getBrand() == null)))
                .thenThrow(new MissingDeviceFieldsException());

        JSONObject result = new JSONObject(mockMvc.perform(post(DEVICE_CONTROLLER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONObject()
                                .put("name", DEVICE_NAME)
                                .toString()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString());

        assertThat(result.get("description")).isEqualTo(new MissingDeviceFieldsException().getMessage());
    }

    @Test
    @DisplayName("Get all devices")
    void getAllDevices() throws Exception {
        List<DeviceResponse> deviceList = IntStream.range(0, 10)
                .mapToObj(i -> {
                    DeviceResponse dr = new DeviceResponse();
                    dr.setId(UUID.randomUUID());
                    dr.setName(DEVICE_NAME + i);
                    dr.setBrand(DEVICE_BRAND);
                    return dr;
                })
                .toList();
        when(deviceService.listAllDevices(any(Pageable.class)))
                .thenAnswer(i -> {
                    Pageable pageable = i.getArgument(0);
                    return new PageImpl<>(deviceList.stream()
                            .skip(pageable.getOffset())
                            .limit(pageable.getPageSize())
                            .toList(), pageable, deviceList.size());
                });

        JSONObject resultAll = new JSONObject(mockMvc.perform(get(DEVICE_CONTROLLER_PATH)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        JSONObject resultHalf = new JSONObject(mockMvc.perform(get(DEVICE_CONTROLLER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("size", "5"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        JSONObject resultNone = new JSONObject(mockMvc.perform(get(DEVICE_CONTROLLER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("size", "10")
                        .queryParam("page", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        assertThat(resultAll.has("_embedded")).isTrue();
        JSONObject resultAllJSONObject = resultAll.getJSONObject("_embedded");
        assertThat(resultAllJSONObject.has("deviceResponseList")).isTrue();
        JSONArray resultAllJSONArray = resultAllJSONObject.getJSONArray("deviceResponseList");
        assertThat(resultAllJSONArray.length()).isEqualTo(deviceList.size());

        assertThat(resultHalf.has("_embedded")).isTrue();
        JSONObject resultHalfJSONObject = resultHalf.getJSONObject("_embedded");
        assertThat(resultHalfJSONObject.has("deviceResponseList")).isTrue();
        JSONArray resultHalfJSONArray = resultHalfJSONObject.getJSONArray("deviceResponseList");
        assertThat(resultHalfJSONArray.length()).isEqualTo(deviceList.size() / 2);

        assertThat(resultNone.has("_embedded")).isFalse();
    }

    @Test
    @DisplayName("Get all devices with specific brand")
    void getDevicesByBrand() throws Exception {
        String anotherBrand = "anotherBrand";
        List<DeviceResponse> deviceList = IntStream.range(0, 10)
                .mapToObj(i -> {
                    DeviceResponse dr = new DeviceResponse();
                    dr.setId(UUID.randomUUID());
                    dr.setName(DEVICE_NAME + i);
                    dr.setBrand(i % 2 == 0 ? DEVICE_BRAND : anotherBrand);
                    return dr;
                })
                .toList();
        when(deviceService.searchDeviceByBrand(any(String.class), any(Pageable.class)))
                .thenAnswer(i -> new PageImpl<>(deviceList.stream()
                        .filter(dr -> i.getArgument(0).equals(dr.getBrand())).toList(),
                        i.getArgument(1), 0L));

        JSONObject resultDeviceBrand = new JSONObject(mockMvc.perform(
                get(String.format("%s/brand/%s", DEVICE_CONTROLLER_PATH, DEVICE_BRAND))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        JSONObject resultNone = new JSONObject(mockMvc.perform(
                get(String.format("%s/brand/%s", DEVICE_CONTROLLER_PATH, "noneMatchBrand"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        assertThat(resultDeviceBrand.has("_embedded")).isTrue();
        JSONObject resultAllJSONObject = resultDeviceBrand.getJSONObject("_embedded");
        assertThat(resultAllJSONObject.has("deviceResponseList")).isTrue();
        JSONArray resultAllJSONArray = resultAllJSONObject.getJSONArray("deviceResponseList");
        assertThat(resultAllJSONArray.length())
                .isEqualTo(deviceList.stream().filter(dr -> DEVICE_BRAND.equals(dr.getBrand())).count());

        assertThat(resultNone.has("_embedded")).isFalse();
    }

    @Test
    @DisplayName("Getting a device by its identifier")
    void getDeviceById_successful() throws Exception {
        DeviceResponse deviceResponse = new DeviceResponse();
        deviceResponse.setId(UUID.randomUUID());
        deviceResponse.setName(DEVICE_NAME);
        deviceResponse.setBrand(DEVICE_BRAND);
        deviceResponse.setCreationTimestamp(Instant.now());
        when(deviceService.getDeviceByIdentifier(deviceResponse.getId()))
                .thenReturn(deviceResponse);

        String requestPath = String.format("%s/%s", DEVICE_CONTROLLER_PATH, deviceResponse.getId());
        JSONObject resultDevice = new JSONObject(mockMvc.perform(get(requestPath)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        assertThat(resultDevice.get("id")).isEqualTo(deviceResponse.getId().toString());
        assertThat(resultDevice.has("_links")).isTrue();
        JSONObject resultLinks = resultDevice.getJSONObject("_links");
        assertThat(resultLinks.has("self")).isTrue();
        JSONObject resultSelfLink = resultLinks.getJSONObject("self");
        assertThat(resultSelfLink.has("href")).isTrue();
        assertThat(resultSelfLink.get("href")).asString().contains(requestPath);
    }

    @Test
    @DisplayName("Getting a device by a non-existent identifier")
    void getDeviceById_wrongId() throws Exception {
        when(deviceService.getDeviceByIdentifier(any(UUID.class)))
                .thenThrow(new DeviceNotFoundException());
        JSONObject resultDevice = new JSONObject(mockMvc.perform(
                        get(String.format("%s/%s", DEVICE_CONTROLLER_PATH, UUID.randomUUID()))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString());
        assertThat(resultDevice.get("description"))
                .isEqualTo(new DeviceNotFoundException().getMessage());
    }

    @Test
    @DisplayName("Getting a device with an invalid identifier")
    void getDeviceById_paramError() throws Exception {
        JSONObject resultDevice = new JSONObject(mockMvc.perform(
                        get(String.format("%s/%s", DEVICE_CONTROLLER_PATH, "invalidId"))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString());
        assertThat(resultDevice.get("description"))
                .isEqualTo("The following request path parameter was unable to be parsed: \"id\"");
    }

    @Test
    @DisplayName("Updating a device with its identifier")
    void updateDevice_successful() throws Exception {
        DeviceResponse deviceResponse = new DeviceResponse();
        deviceResponse.setId(UUID.randomUUID());
        deviceResponse.setName(DEVICE_NAME);
        deviceResponse.setBrand(DEVICE_BRAND);
        when(deviceService.updateDevice(eq(deviceResponse.getId()), any(DeviceRequest.class)))
                .thenAnswer(i -> {
                    DeviceRequest dr = i.getArgument(1);
                    if (dr.getName() != null) {
                        deviceResponse.setName(dr.getName());
                    }
                    if (dr.getBrand() != null) {
                        deviceResponse.setBrand(dr.getBrand());
                    }
                    return deviceResponse;
                });
        String newName = "newName";

        JSONObject result = new JSONObject(mockMvc.perform(
                put(String.format("%s/%s", DEVICE_CONTROLLER_PATH, deviceResponse.getId()))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONObject()
                                .put("name", "newName")
                                .toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        assertThat(result.get("id")).isEqualTo(deviceResponse.getId().toString());
        assertThat(result.get("name")).isEqualTo(newName);
    }

    @Test
    @DisplayName("Updating a device with a non-existent identifier")
    void updateDevice_wrongId() throws Exception {
        when(deviceService.updateDevice(any(UUID.class), any(DeviceRequest.class)))
                .thenThrow(new DeviceNotFoundException());
        JSONObject resultDevice = new JSONObject(mockMvc.perform(
                        put(String.format("%s/%s", DEVICE_CONTROLLER_PATH, UUID.randomUUID()))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new JSONObject()
                                        .put("name", DEVICE_NAME)
                                        .put("brand", DEVICE_BRAND)
                                        .toString()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString());
        assertThat(resultDevice.get("description"))
                .isEqualTo(new DeviceNotFoundException().getMessage());
    }

    @Test
    @DisplayName("Updating a device with an invalid identifier")
    void updateDevice_paramError() throws Exception {
        JSONObject resultDevice = new JSONObject(mockMvc.perform(
                put(String.format("%s/%s", DEVICE_CONTROLLER_PATH, "invalidId"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString());
        assertThat(resultDevice.get("description"))
                .isEqualTo("The following request path parameter was unable to be parsed: \"id\"");
    }

    @Test
    @DisplayName("Updating a device with a wrong or empty input body")
    void updateDevice_wrongOrEmpty() throws Exception {
        for (String body : List.of("wrong body", "")) {
            JSONObject result = new JSONObject(mockMvc.perform(
                    put(String.format("%s/%s", DEVICE_CONTROLLER_PATH, UUID.randomUUID()))
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse().getContentAsString());

            assertThat(result.get("description")).isEqualTo("The request body was unable to be parsed.");
        }
    }

    @Test
    @DisplayName("Deleting a device with its identifier")
    void deleteDevice_successful() throws Exception {
        DeviceResponse deviceResponse = new DeviceResponse();
        deviceResponse.setId(UUID.randomUUID());
        deviceResponse.setName(DEVICE_NAME);
        deviceResponse.setBrand(DEVICE_BRAND);

        String result = mockMvc.perform(
                delete(String.format("%s/%s", DEVICE_CONTROLLER_PATH, deviceResponse.getId()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn().getResponse().getContentAsString();

        verify(deviceService, times(1)).deleteDevice(any(UUID.class));
        assertThat(result).isNullOrEmpty();
    }

    @Test
    @DisplayName("Deleting a device with a non-existent identifier")
    void deleteDevice_wrongId() throws Exception {
        doThrow(new DeviceNotFoundException()).when(deviceService).deleteDevice(any(UUID.class));
        JSONObject resultDevice = new JSONObject(mockMvc.perform(
                        delete(String.format("%s/%s", DEVICE_CONTROLLER_PATH, UUID.randomUUID()))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString());
        assertThat(resultDevice.get("description"))
                .isEqualTo(new DeviceNotFoundException().getMessage());
    }

    @Test
    @DisplayName("Deleting a device with an invalid identifier")
    void deleteDevice_paramError() throws Exception {
        JSONObject resultDevice = new JSONObject(mockMvc.perform(
                        delete(String.format("%s/%s", DEVICE_CONTROLLER_PATH, "invalidId"))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString());
        assertThat(resultDevice.get("description"))
                .isEqualTo("The following request path parameter was unable to be parsed: \"id\"");
    }
}