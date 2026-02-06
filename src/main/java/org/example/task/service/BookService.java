package org.example.task.service;

import org.example.task.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface BookService {
    BookSaveResponse create(BookCreateRequest request);
    BookListResponse get(Long id);
    List<BookListResponse> getAll();
    void update(Long id, BookUpdateRequest request);
    void delete(Long id);
    PaginationResponse getList(PaginationRequest request);
    String generateCsv(BookReportRequest request);
    UploadResponse upload(MultipartFile file);


}
