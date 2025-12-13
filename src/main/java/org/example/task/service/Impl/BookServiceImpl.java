package org.example.task.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import org.example.task.data.Author;
import org.example.task.data.Book;
import org.example.task.dto.*;
import org.example.task.exceptions.NotFoundException;
import org.example.task.repository.AuthorRepository;
import org.example.task.repository.BookRepository;
import org.example.task.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.Arrays;
import java.util.List;

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

    @Override
    public PaginationResponse getList(PaginationRequest request) {

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        String pattern = request.getTitle() != null ? "%" + request.getTitle().toLowerCase() + "%" : null;
        Page<Book> page = bookRepository.findFiltered(
                request.getAuthorId(),
                request.getGenre(),
                pattern,
                pageable
        );


        List<BookShortDto> dtoList = page.getContent().stream()
                .map(book -> new BookShortDto(
                        book.getId(),
                        book.getTitle(),
                        book.getGenre()
                ))
                .toList();


        return new PaginationResponse(dtoList, page.getTotalPages());
    }

    @Override
    public String generateCsv(BookReportRequest request) {
        String pattern = request.getTitle() != null
                ? "%" + request.getTitle().toLowerCase() + "%"
                : null;

        List<Book> books = bookRepository.findFilter(
                request.getAuthorId(),
                request.getGenre(),
                pattern
        );

        StringBuilder sb = new StringBuilder();
        sb.append("Id,Title,Genre,Published,Author\n");
        for (Book book : books) {
            sb.append(book.getId()).append(",")
                    .append(escapeCsv(book.getTitle())).append(",")
                    .append(escapeCsv(book.getGenre())).append(",")
                    .append(book.getPublished()).append(",")
                    .append(escapeCsv(book.getAuthor().getName()))
                    .append("\n");
        }
        return sb.toString();
    }

    @Override
    public UploadResponse upload(MultipartFile file) {

        int successCount = 0;
        int failCount = 0;

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            List<BookCreateRequest> bookList = Arrays.asList(
                    objectMapper.readValue(file.getInputStream(), BookCreateRequest[].class)
            );

            for(BookCreateRequest dto: bookList){
                try {
                    Author author = authorRepository.findById(dto.getAuthorId())
                            .orElseThrow(() -> new NotFoundException("Author not found: " + dto.getAuthorId()));

                    Book book = new Book(dto.getTitle(), dto.getGenre(), dto.getPublished(), author);

                    bookRepository.save(book);
                    successCount++;

                } catch (Exception e) {
                    failCount++;
                }
            }

        }
        catch (Exception e){
            throw new RuntimeException("Failed to read JSON file", e);
        }

        return new UploadResponse(successCount, failCount);
    }


    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }


}
