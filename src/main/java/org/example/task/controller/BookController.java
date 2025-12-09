package org.example.task.controller;

import jakarta.validation.Valid;

import org.example.task.dto.BookCreateRequest;
import org.example.task.dto.BookListResponse;
import org.example.task.dto.BookSaveResponse;
import org.example.task.dto.BookUpdateRequest;
import org.example.task.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public BookSaveResponse create(@Valid @RequestBody BookCreateRequest request){
        return bookService.create(request);
    }

    @GetMapping("/list/{id}")
    public BookListResponse get(@PathVariable Long id){
        return bookService.get(id);
    }

    @PatchMapping("/update/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id, @RequestBody BookUpdateRequest updateRequest){
        bookService.update(id, updateRequest);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id){
        bookService.delete(id);
    }

}
