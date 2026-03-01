package com.yaquodorg.yaquod.response;

import com.yaquodorg.yaquod.entity.Vehicle;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateVehicleResponse {
    private Vehicle vehicle;
    private String apiKey;
    private String apiSecret;
}
