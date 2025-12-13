package org.example.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.task.data.Author;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorListResponse {
    private Long id;
    private String name;

}
