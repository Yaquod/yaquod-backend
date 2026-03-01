package com.yaquodorg.yaquod.response;

import java.util.Date;

import com.yaquodorg.yaquod.entity.Vehicle;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleLoginResponse {
    private String accessToken;
    private String refreshToken;
    private Date accessTokenExpiresIn;
    private Date refreshTokenExpiresIn;
    private Vehicle vehicle;
}
