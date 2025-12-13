package org.example.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.task.data.Author;
import org.example.task.dto.*;
import org.example.task.repository.AuthorRepository;
import org.example.task.repository.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Task2SpringApplication.class)
@AutoConfigureMockMvc
public class AuthorTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @AfterEach
    public void afterEach() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    void createAuthor() throws Exception{
        String body = """
                {
                    "name": "author Name"
                }
                """;

        MvcResult mvcResult = mvc.perform(post("/api/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        AuthorSaveResponse response = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                AuthorSaveResponse.class
        );

        Author author = authorRepository.findById(response.getId()).orElse(null);
        assertThat(author).isNotNull();
        assertThat(author.getName()).isEqualTo("author Name");
    }

    @Test
    void getListAuthor() throws Exception{

        Author author = new Author("Name");
        authorRepository.save(author);

        MvcResult result = mvc.perform(get("/api/author/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<AuthorListResponse> response = objectMapper.readValue(
                json, new TypeReference<>() {}
        );

        assertThat(response.getFirst().getId()).isEqualTo(author.getId());
        assertThat(response.getFirst().getName()).isEqualTo("Name");
    }

    @Test
    void updateAuthor() throws Exception{
        Author author = new Author("Name");
        authorRepository.save(author);

        String body = """
                {
                    "name": "Name22"
                }
                """;

        mvc.perform(put("/api/author/" + author.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent())
                .andReturn();

        Author updated = authorRepository.findById(author.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Name22");

    }

    @Test
    void deleteAuthor() throws Exception{
        Author author = new Author("Name");
        authorRepository.save(author);

        MvcResult result = mvc.perform(delete("/api/author/delete/" + author.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        AuthDeleteResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthDeleteResponse.class
        );

        assertThat(bookRepository.findById(author.getId())).isEmpty();
        assertThat(response.getMessage()).isEqualTo("OK");
    }

}
