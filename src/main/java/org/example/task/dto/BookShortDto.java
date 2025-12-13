package org.example.task.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookShortDto {
    private Long id;
    private String title;
    private String genre;
}
