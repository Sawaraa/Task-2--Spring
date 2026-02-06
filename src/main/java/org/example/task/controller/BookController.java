package org.example.task.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.example.task.data.Book;
import org.example.task.dto.*;
import org.example.task.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    @GetMapping("/list")
    public List<BookListResponse> getAll(){
        return bookService.getAll();
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        bookService.delete(id);
    }

    @PostMapping("/_list")
    public PaginationResponse list(@RequestBody PaginationRequest request){
        return bookService.getList(request);
    }

    @PostMapping("/_report")
    public void generateReport(@RequestBody BookReportRequest request,
                               HttpServletResponse response) throws IOException {

        String csvContent = bookService.generateCsv(request);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"books_report.csv\"");

        response.getOutputStream().write(csvContent.getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
    }

    @PostMapping("/upload")
    public UploadResponse uploadFile(@RequestParam("file") MultipartFile file) {
        return bookService.upload(file);
    }


}
