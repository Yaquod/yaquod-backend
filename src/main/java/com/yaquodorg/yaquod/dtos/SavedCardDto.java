package com.yaquodorg.yaquod.dtos;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedCardDto {
    private Long id;
    private String maskedPan;
    private String cardSubtype;
    private String cardholderName;
    private Timestamp createdAt;
}
