package org.example.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookUpdateRequest {
    @NotBlank(message = "title is required")
    @NotNull(message = "title is required")
    private String title;
    @NotBlank(message = "genre is required")
    @NotNull(message = "genre is required")
    private String genre;
    @NotNull(message = "published is required")
    private LocalDate published;
    @NotNull(message = "authorId is required")
    private Long authorId;
}
