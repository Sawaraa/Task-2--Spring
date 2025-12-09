package org.example.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookSaveResponse {
    private Long id;
    private String title;
}
