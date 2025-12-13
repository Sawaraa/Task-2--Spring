package org.example.task.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookCreateRequest {
    @NotBlank(message = "title is required")
    @NotNull(message = "title is required")
    private String title;
    @NotBlank(message = "genre is required")
    @NotNull(message = "genre is required")
    private String genre;
    @NotNull(message = "published is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate published;
    @NotNull(message = "authorId is required")
    private Long authorId;
}
