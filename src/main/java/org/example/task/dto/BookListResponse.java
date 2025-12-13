package org.example.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookListResponse {
    private Long id;
    private String title;
    private String genre;
    private LocalDate published;
    private AuthorShortDto author;

}
