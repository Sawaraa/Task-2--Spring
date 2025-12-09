package org.example.task.service.Impl;

import org.example.task.data.Author;
import org.example.task.dto.*;
import org.example.task.exceptions.AuthorAlreadyExistsException;
import org.example.task.exceptions.NotFoundException;
import org.example.task.repository.AuthorRepository;
import org.example.task.service.AuthorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public AuthorSaveResponse create(CreateAuthorRequest request) {

        if (authorRepository.existsByName(request.getName())){
            throw new AuthorAlreadyExistsException(request.getName());
        }

        Author author = new Author(request.getName());
        authorRepository.save(author);

        return new AuthorSaveResponse(author.getId(), author.getName());
    }

    @Override
    public List<AuthorListResponse> get() {
        return  authorRepository.findAll()
                .stream()
                .map(a -> new AuthorListResponse(a.getId(), a.getName()))
                .toList();
    }

    @Override
    public AuthorSaveResponse update(AuthorUpdateRequest request) {
        Author author = authorRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Author with id " + request.getId() + " not found"));

        if (authorRepository.existsByName(request.getName())){
            throw new AuthorAlreadyExistsException(request.getName());
        }

        author.setName(request.getName());
        authorRepository.save(author);

        return new AuthorSaveResponse(author.getId(), author.getName());
    }

    @Override
    public void delete(Long id) {
        authorRepository.deleteById(id);
    }
}
