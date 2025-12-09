package org.example.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.task.data.Author;

import java.util.List;

@Getter
@AllArgsConstructor
public class AuthorListResponse {
    private Long id;
    private String name;

}
