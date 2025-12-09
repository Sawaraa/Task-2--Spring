package org.example.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAuthorRequest {
    @NotBlank(message = "name is required")
    @NotNull(message = "name is required")
    private String name;
}
