package org.example.task.service;

import org.example.task.dto.*;

import java.util.List;

public interface AuthorService {

    AuthorSaveResponse create(CreateAuthorRequest request);

    List<AuthorListResponse> get();

    AuthorSaveResponse update(AuthorUpdateRequest request);

    void delete(Long id);

}
