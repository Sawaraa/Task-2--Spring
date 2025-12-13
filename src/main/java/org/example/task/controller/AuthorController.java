package org.example.task.controller;

import jakarta.validation.Valid;
import org.example.task.dto.*;
import org.example.task.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/author")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorSaveResponse create(@Valid @RequestBody CreateAuthorRequest authorRequest){
        return authorService.create(authorRequest);
    }

    @GetMapping("/list")
    public List<AuthorListResponse> getList(){
        return authorService.get();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public AuthorSaveResponse update(@Valid @PathVariable Long id,
                                     @RequestBody AuthorUpdateRequest updateRequest){
        updateRequest.setId(id);
        return authorService.update(updateRequest);
    }

    @DeleteMapping("/delete/{id}")
    public AuthDeleteResponse delete(@PathVariable Long id){
        authorService.delete(id);
        return new AuthDeleteResponse("OK");
    }

}
