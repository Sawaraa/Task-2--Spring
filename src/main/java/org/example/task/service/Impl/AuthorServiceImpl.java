package org.example.task.service.Impl;

import org.example.task.data.Author;
import org.example.task.dto.*;
import org.example.task.exceptions.AuthorAlreadyExistsException;
import org.example.task.exceptions.NotFoundException;
import org.example.task.repository.AuthorRepository;
import org.example.task.service.AuthorService;
import org.example.task.service.kafka.EmailProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    private final EmailProducer emailProducer;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${spring.application.name}")
    private String applicationName;

    public AuthorServiceImpl(AuthorRepository authorRepository, EmailProducer emailProducer) {
        this.authorRepository = authorRepository;
        this.emailProducer = emailProducer;
    }


    /**
     * Creates a new Author in the system.
     *
     * This method first checks if an author with the given name already exists.
     * If an author with the same name exists, it throws an AuthorAlreadyExistsException.
     * Otherwise, it creates a new Author entity, saves it to the repository,
     * and returns a response containing the author's ID and name.
     *
     * @param request the request object containing the author's name
     * @return AuthorSaveResponse containing the ID and name of the newly created author
     * @throws AuthorAlreadyExistsException if an author with the given name already exists
     */
    @Override
    public AuthorSaveResponse create(CreateAuthorRequest request) {

        if (authorRepository.existsByName(request.getName())){
            throw new AuthorAlreadyExistsException(request.getName());
        }

        Author author = new Author(request.getName());
        authorRepository.save(author);

        EmailDto email = new EmailDto();
        email.setTo(adminEmail);
        email.setSubject("Created author");
        email.setContent("Author '" + author.getName() + "' (ID: " + author.getId() + ") was created.");
        email.setSourceService(applicationName);

        emailProducer.sendEmail(email);

        return new AuthorSaveResponse(author.getId(), author.getName());
    }


    /**
     * Retrieves all authors from the repository.
     *
     * This method fetches all Author entities from the database,
     * converts each entity into an AuthorListResponse containing the author's ID and name,
     * and returns the list of these response objects.
     *
     * @return a list of AuthorListResponse representing all authors
     */
    @Override
    public List<AuthorListResponse> get() {
        return  authorRepository.findAll()
                .stream()
                .map(a -> new AuthorListResponse(a.getId(), a.getName()))
                .toList();
    }

    /**
     * Updates the name of an existing author.
     *
     * This method first retrieves the Author entity by its ID.
     * If the author is not found, it throws a NotFoundException.
     * It then checks if another author with the new name already exists;
     * if so, it throws an AuthorAlreadyExistsException.
     * Otherwise, it updates the author's name, saves the changes,
     * and returns a response containing the updated author's ID and name.
     *
     * @param request the request object containing the author's ID and the new name
     * @return AuthorSaveResponse containing the ID and updated name of the author
     * @throws NotFoundException if no author with the given ID exists
     * @throws AuthorAlreadyExistsException if another author with the new name already exists
     */
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

    /**
     * Deletes an author by their ID.
     *
     * This method removes the Author entity with the specified ID from the repository.
     * If no author with the given ID exists, the repository may throw an exception
     * depending on its implementation.
     *
     * @param id the ID of the author to delete
     */
    @Override
    public void delete(Long id) {
        authorRepository.deleteById(id);
    }
}
