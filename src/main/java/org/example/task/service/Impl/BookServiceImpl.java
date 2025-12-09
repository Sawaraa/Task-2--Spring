package org.example.task.service.Impl;

import jakarta.transaction.Transactional;
import org.example.task.data.Author;
import org.example.task.data.Book;
import org.example.task.dto.*;
import org.example.task.exceptions.NotFoundException;
import org.example.task.repository.AuthorRepository;
import org.example.task.repository.BookRepository;
import org.example.task.service.BookService;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookServiceImpl(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    @Override
    public BookSaveResponse create(BookCreateRequest request) {

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new NotFoundException("Author not found"));

        Book book = new Book(request.getTitle(), request.getGenre(), request.getPublished(), author);
        bookRepository.save(book);
        return new BookSaveResponse(book.getId(), book.getTitle());
    }

    @Override
    public BookListResponse get(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        Author author = book.getAuthor();

        AuthorShortDto shortDto = new AuthorShortDto(author.getId(), author.getName());
        return new BookListResponse(book.getId(),
                                    book.getTitle(),
                                    book.getGenre(),
                                    book.getPublished(),
                                    shortDto);

    }

    @Override
    @Transactional
    public void update(Long id, BookUpdateRequest dto) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        if (dto.getTitle() != null) book.setTitle(dto.getTitle());
        if (dto.getGenre() != null) book.setGenre(dto.getGenre());
        if (dto.getPublished() != null) book.setPublished(dto.getPublished());
        if (dto.getAuthorId() != null) {
            Author author = authorRepository.findById(dto.getAuthorId())
                    .orElseThrow(() -> new NotFoundException("Author not found"));
            book.setAuthor(author);
        }
    }

    @Override
    public void delete(Long id) {
        bookRepository.deleteById(id);
    }


}
