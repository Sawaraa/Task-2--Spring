package org.example.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthorSaveResponse {
    private Long id;
    private String name;
}
