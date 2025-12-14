package org.example.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.task.data.Author;
import org.example.task.data.Book;
import org.example.task.dto.*;
import org.example.task.repository.AuthorRepository;
import org.example.task.repository.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Task2SpringApplication.class)
@AutoConfigureMockMvc
class BookTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    public void beforeEach() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @AfterEach
    public void afterEach() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }


    @Test
    void testCreateBook() throws Exception {
        Author author = new Author();
        author.setName("Test Author");
        authorRepository.save(author);

        Long authorId = author.getId();

        String body = """
                {
                    "title": "Test Book",
                    "genre": "Fantasy",
                    "published": "2020-10-10",
                    "authorId": %d
                }
                """.formatted(authorId);

        // 3. Викликаємо POST /api/books
        MvcResult mvcResult = mvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        // 4. Парсимо відповідь
        BookSaveResponse response = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                BookSaveResponse.class
        );

        // 5. Перевіряємо, що ID повернувся
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Book");

        // 6. Перевіряємо, що книга реально в БД
        Book book = bookRepository.findById(response.getId()).orElse(null);
        assertThat(book).isNotNull();
        assertThat(book.getTitle()).isEqualTo("Test Book");
        assertThat(book.getGenre()).isEqualTo("Fantasy");
        assertThat(book.getAuthor().getId()).isEqualTo(authorId);
    }

    @Test
    void testGetBookById() throws Exception {

        Author author = new Author();
        author.setName("George Orwell");
        authorRepository.save(author);

        Book book = new Book(
                "1984",
                "Dystopian",
                LocalDate.of(1949, 1, 1),
                author
        );
        bookRepository.save(book);

        // 3. Викликаємо GET /api/books/{id}
        MvcResult result = mvc.perform(get("/api/book/list/" + book.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 4. Розбираємо відповідь
        String json = result.getResponse().getContentAsString();
        BookListResponse response = objectMapper.readValue(json, BookListResponse.class);

        // 5. Перевіряємо поля
        assertThat(response.getId()).isEqualTo(book.getId());
        assertThat(response.getTitle()).isEqualTo("1984");
        assertThat(response.getGenre()).isEqualTo("Dystopian");
        assertThat(response.getAuthor().getName()).isEqualTo("George Orwell");
    }

    @Test
    void testUpdateBookById() throws Exception {

        Author author = new Author();
        author.setName("George Orwell");
        authorRepository.save(author);
        Long authorId = author.getId();

        Book book = new Book(
                "1984",
                "Dystopian",
                LocalDate.of(1949, 1, 1),
                author
        );
        bookRepository.save(book);

        String body = """
                {
                    "title": "Test Book",
                    "genre": "Fantasy",
                    "published": "2020-10-10",
                    "authorId": %d
                }
                """.formatted(authorId);

        mvc.perform(patch("/api/book/update/" + book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent())
                .andReturn();

        Book updated = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Test Book");
        assertThat(updated.getGenre()).isEqualTo("Fantasy");
        assertThat(updated.getPublished()).isEqualTo(LocalDate.of(2020, 10, 10));

    }

    @Test
    void testDeleteBookById() throws Exception {

        Author author = new Author();
        author.setName("George Orwell");
        authorRepository.save(author);

        Book book = new Book(
                "1984",
                "Dystopian",
                LocalDate.of(1949, 1, 1),
                author
        );
        bookRepository.save(book);

        mvc.perform(delete("/api/book/delete/" + book.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        assertThat(bookRepository.findById(book.getId())).isEmpty();


    }

    @Test
    void testBookListWithPaginationAndFilter() throws Exception {
        // 1. Створюємо автора
        Author author = new Author("Test Author");
        authorRepository.save(author);

        // 2. Створюємо декілька книг
        for (int i = 1; i <= 5; i++) {
            Book book = new Book(
                    "Book " + i,
                    i % 2 == 0 ? "Fantasy" : "Fiction",
                    LocalDate.of(2020, 1, i),
                    author
            );
            bookRepository.save(book);
        }

        // 3. Створюємо тіло запиту з пагінацією і фільтром
        String requestBody = """
            {
                "authorId": %d,
                "genre": "Fiction",
                "title": null,
                "page": 0,
                "size": 2
            }
            """.formatted(author.getId());

        // 4. Викликаємо ендпоінт
        MvcResult result = mvc.perform(post("/api/book/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        // 5. Десеріалізуємо відповідь
        PaginationResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                PaginationResponse.class
        );

        // 6. Перевіряємо загальну кількість сторінок
        assertThat(response.getTotalPages()).isEqualTo(2); // 3 книги з жанром Fiction, size = 2 => 2 сторінки

        // 7. Перевіряємо кількість елементів на сторінці
        assertThat(response.getBookList()).hasSize(2);

        // 8. Перевіряємо поля першої книги
        BookShortDto firstBook = response.getBookList().get(0);
        assertThat(firstBook.getTitle()).isEqualTo("Book 1");
        assertThat(firstBook.getGenre()).isEqualTo("Fiction");

    }

    @Test
    void testGenerateCsvReport() throws Exception {

        Author author = new Author("Test Author");
        authorRepository.save(author);

        Book book1 = new Book("Book One", "Fiction", LocalDate.of(2020, 1, 1), author);
        Book book2 = new Book("Book Two", "Fantasy", LocalDate.of(2021, 5, 5), author);
        bookRepository.saveAll(List.of(book1, book2));


        String requestBody = """
            {
                "authorId": %d,
                "genre": null,
                "title": null
            }
            """.formatted(author.getId());

        MvcResult result = mvc.perform(post("/api/book/_report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"books_report.csv\""))
                .andExpect(content().contentType("text/csv; charset=UTF-8"))
                .andReturn();


        String csv = result.getResponse().getContentAsString();
        assertThat(csv).contains("Id,Title,Genre,Published,Author");
        assertThat(csv).contains("Book One");
        assertThat(csv).contains("Book Two");
        assertThat(csv).contains("Test Author");
    }

    @Test
    void testUploadBooks() throws Exception {
        // 1. Створюємо автора, бо книги прив'язані до існуючого автора
        Author author = new Author("Test Author");
        authorRepository.save(author);

        Long authorId = author.getId();

        // 2. JSON-файл для завантаження
        String jsonContent = """
            [
                {
                    "title": "Book One",
                    "genre": "Fiction",
                    "published": "2020-01-01",
                    "authorId": %d
                },
                {
                    "title": "Book Two",
                    "genre": "Fantasy",
                    "published": "2021-05-05",
                    "authorId": %d
                },
                {
                    "title": "Invalid Book",
                    "genre": "Unknown",
                    "published": "2022-01-01",
                    "authorId": 9999
                }
            ]
            """.formatted(authorId, authorId);

        // 3. Викликаємо ендпоінт upload
        MvcResult result = mvc.perform(multipart("/api/book/upload")
                        .file("file", jsonContent.getBytes())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        // 4. Парсимо відповідь
        UploadResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UploadResponse.class
        );

        // 5. Перевіряємо успішні та неуспішні записи
        assertThat(response.getSuccessCount()).isEqualTo(2); // дві валідні книги
        assertThat(response.getFailCount()).isEqualTo(1); // одна невалідна (автор 9999)

        // 6. Перевіряємо, що книги з БД збереглися
        List<Book> books = bookRepository.findAll();
        assertThat(books).hasSize(2);
        assertThat(books.stream().map(Book::getTitle))
                .containsExactlyInAnyOrder("Book One", "Book Two");
    }


}
