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
import org.example.task.service.kafka.EmailProducer;
import org.springframework.beans.factory.annotation.Value;
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
    private final EmailProducer emailProducer;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${spring.application.name}")
    private String applicationName;

    public BookServiceImpl(BookRepository bookRepository, AuthorRepository authorRepository, EmailProducer emailProducer) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.emailProducer = emailProducer;
    }

    /**
     * Creates a new book and associates it with an existing author.
     *
     * This method first retrieves the Author entity by the provided author ID.
     * If the author is not found, it throws a NotFoundException.
     * It then creates a new Book entity with the given title, genre, and published date,
     * associates it with the retrieved author, saves it to the repository,
     * and returns a response containing the book's ID and title.
     *
     * @param request the request object containing book details and the author ID
     * @return BookSaveResponse containing the ID and title of the newly created book
     * @throws NotFoundException if no author with the given ID exists
     */

    @Override
    public BookSaveResponse create(BookCreateRequest request) {

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new NotFoundException("Author not found"));

        Book book = new Book(request.getTitle(), request.getGenre(), request.getPublished(), author);
        bookRepository.save(book);

        EmailDto email = new EmailDto();
        email.setTo(adminEmail);
        email.setSubject("Created book");
        email.setContent("Book '" + book.getTitle() + "' (ID: " + book.getId() + ") created");
        email.setSourceService(applicationName);

        emailProducer.sendEmail(email);
        return new BookSaveResponse(book.getId(), book.getTitle());
    }


    /**
     * Retrieves a single book by its ID along with its author's information.
     *
     * This method fetches the Book entity from the repository using the given ID.
     * If the book is not found, it throws a NotFoundException.
     * It then retrieves the associated Author entity, creates an AuthorShortDto,
     * and returns a BookListResponse containing the book's details and author information.
     *
     * @param id the ID of the book to retrieve
     * @return BookListResponse containing the book's details and a short representation of the author
     * @throws NotFoundException if no book with the given ID exists
     */
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
    public List<BookListResponse> getAll() {

        return bookRepository.findAll()
                .stream()
                .map(b -> new BookListResponse(
                        b.getId(),
                        b.getTitle(),
                        b.getGenre(),
                        b.getPublished(),
                        new AuthorShortDto(
                                b.getAuthor().getId(),
                                b.getAuthor().getName()
                        )
                ))
                .toList();
    }

    /**
     * Updates the details of an existing book.
     *
     * This method retrieves the Book entity by its ID. If the book is not found,
     * it throws a NotFoundException. Each field in the BookUpdateRequest is checked:
     * if a field is non-null, the corresponding property of the Book entity is updated.
     * If a new author ID is provided, the method fetches the Author entity and
     * associates it with the book. Changes are persisted automatically due to
     * the @Transactional annotation.
     *
     * @param id the ID of the book to update
     * @param dto the request object containing the updated book details
     * @throws NotFoundException if the book or the specified author does not exist
     */
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


    /**
     * Deletes a book by its ID.
     *
     * This method removes the Book entity with the specified ID from the repository.
     * If no book with the given ID exists, the repository may throw an exception
     * depending on its implementation.
     *
     * @param id the ID of the book to delete
     */

    @Override
    public void delete(Long id) {
        bookRepository.deleteById(id);
    }


    /**
     * Retrieves a paginated list of books with optional filtering by author, genre, or title.
     *
     * This method constructs a Pageable object based on the requested page and size,
     * applies optional filters, fetches the filtered page of books from the repository,
     * converts each Book entity to a BookShortDto, and returns a PaginationResponse
     * containing the list of DTOs and the total number of pages.
     *
     * @param request the pagination and filter request containing page, size, authorId, genre, and title
     * @return PaginationResponse containing the list of BookShortDto and total pages
     */
    @Override
    public PaginationResponse getList(PaginationRequest request) {

        // Create Pageable object based on page number and size
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        // Prepare the title filter pattern if a title is provided
        String pattern = request.getTitle() != null ? "%" + request.getTitle().toLowerCase() + "%" : null;
        // Fetch the filtered page of books from the repository
        Page<Book> page = bookRepository.findFiltered(
                request.getAuthorId(),
                request.getGenre(),
                pattern,
                pageable
        );

        // Convert each Book entity to a short DTO
        List<BookShortDto> dtoList = page.getContent().stream()
                .map(book -> new BookShortDto(
                        book.getId(),
                        book.getTitle(),
                        book.getGenre()
                ))
                .toList();


        return new PaginationResponse(dtoList, page.getTotalPages());
    }

    /**
     * Generates a CSV representation of books based on optional filters.
     *
     * This method applies filters for author, genre, and title (if provided),
     * retrieves the filtered list of books from the repository, and builds a CSV string.
     * Each book's details (ID, title, genre, published date, author name) are included in the CSV.
     *
     * @param request the request object containing optional filters for authorId, genre, and title
     * @return a CSV-formatted string containing the filtered books
     */
    @Override
    public String generateCsv(BookReportRequest request) {
        // Prepare the title filter pattern if a title is provided
        String pattern = request.getTitle() != null
                ? "%" + request.getTitle().toLowerCase() + "%"
                : null;

        // Fetch the filtered list of books from the repository
        List<Book> books = bookRepository.findFilter(
                request.getAuthorId(),
                request.getGenre(),
                pattern
        );

        // Initialize StringBuilder and add CSV header
        StringBuilder sb = new StringBuilder();
        sb.append("Id,Title,Genre,Published,Author\n");
        // Append each book's details as a CSV row
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

    /**
     * Uploads a JSON file containing multiple books and saves them to the repository.
     *
     * This method reads the JSON file, deserializes it into a list of BookCreateRequest objects,
     * and attempts to create each book in the database. For each book:
     *   - It looks up the author by ID and throws NotFoundException if the author does not exist.
     *   - It creates a new Book entity and saves it.
     *   - Successful and failed inserts are counted separately.
     *
     * @param file the MultipartFile containing the JSON array of books
     * @return UploadResponse containing the number of successfully and unsuccessfully processed books
     * @throws RuntimeException if the JSON file cannot be read or deserialized
     */
    @Override public UploadResponse upload(MultipartFile file) {
        int successCount = 0;
        int failCount = 0;

        try{
            // Initialize ObjectMapper with JavaTimeModule to handle LocalDate
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // Deserialize the JSON file into a list of BookCreateRequest
            List<BookCreateRequest> bookList = Arrays.asList(
                    objectMapper.readValue(file.getInputStream(), BookCreateRequest[].class) );


            // Process each book in the list
            for(BookCreateRequest dto: bookList){
                try {
                    // Retrieve author by ID; throw if not found
                    Author author = authorRepository.findById(dto.getAuthorId())
                            .orElseThrow(() -> new NotFoundException("Author not found: " + dto.getAuthorId()));

                    // Create and save the book
                    Book book = new Book(dto.getTitle(), dto.getGenre(), dto.getPublished(), author);
                    bookRepository.save(book); successCount++; // Increment success counter
                } catch (Exception e) {
                    failCount++; // Increment fail counter if any exception occurs for this book
                }
            }
        } catch (Exception e)
        {
            throw new RuntimeException("Failed to read JSON file", e);
        }
        return new UploadResponse(successCount, failCount); }


    /**
     * Escapes a string value for safe inclusion in a CSV file.
     *
     * This method handles null values, doubles any quotes within the value,
     * and wraps the value in quotes if it contains commas, quotes, or newlines.
     *
     * @param value the string to escape
     * @return the escaped string suitable for CSV output
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }


}
