package org.example.task.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
public class PaginationRequest {

    private Long authorId;
    private String genre;
    private String title;
    private Integer page = 0;
    private Integer size = 20;

}
