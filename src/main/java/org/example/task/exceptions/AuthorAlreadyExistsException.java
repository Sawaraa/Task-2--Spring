package org.example.task.exceptions;

public class AuthorAlreadyExistsException extends RuntimeException {
    public AuthorAlreadyExistsException(String name) {
        super("Author with name '" + name + "' already exists");
    }
}

