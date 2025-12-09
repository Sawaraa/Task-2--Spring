package org.example.task.service;

import org.example.task.dto.*;
import org.springframework.stereotype.Service;


public interface BookService {
    BookSaveResponse create(BookCreateRequest request);
    BookListResponse get(Long id);
    void update(Long id, BookUpdateRequest request);
    void delete(Long id);
}
