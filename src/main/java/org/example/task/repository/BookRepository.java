package org.example.task.repository;

import org.example.task.data.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findByAuthorId(Long authorId, Pageable pageable);

    Page<Book> findAll(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE " +
            "(:authorId IS NULL OR b.author.id = :authorId) AND " +
            "(:genre IS NULL OR b.genre = :genre) AND " +
            "(:title IS NULL OR LOWER(b.title) LIKE :title)")
    Page<Book> findFiltered(@Param("authorId") Long authorId,
                            @Param("genre") String genre,
                            @Param("title") String title,
                            Pageable pageable);

    @Query("SELECT b FROM Book b WHERE " +
            "(:authorId IS NULL OR b.author.id = :authorId) AND " +
            "(:genre IS NULL OR b.genre = :genre) AND " +
            "(:title IS NULL OR LOWER(b.title) LIKE :title)")
    List<Book> findFilter(@Param("authorId") Long authorId,
                            @Param("genre") String genre,
                            @Param("title") String title);


}
