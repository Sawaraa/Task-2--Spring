package org.example.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthorUpdateRequest {
    private Long id;
    private String name;
}
