package com.yaquodorg.yaquod.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRatingCommentDto {
    @NotBlank(message = "comment cannot be blank")
    private String comment;
}
