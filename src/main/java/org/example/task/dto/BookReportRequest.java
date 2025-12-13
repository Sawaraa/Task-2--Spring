package org.example.task.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookReportRequest {
    private Long authorId;
    private String genre;
    private String title;
}
