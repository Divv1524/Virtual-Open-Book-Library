package com.project.virtualopenbooklibrary.external;

import com.project.virtualopenbooklibrary.dto.BookDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "VOBL.BOOKS")
public interface BookFeignClient {
  @GetMapping("/books/user/{userId}")
    List<BookDTO> getBooksByUser(@PathVariable String userId);

    // If a user is adding a book via User Service, User Service should call Book Service via Feign Client.
    @PostMapping("/books/add")
    BookDTO addBook(@RequestBody BookDTO bookDTO);


    //If a user wants to update a book's details through User Service.
    @PutMapping("/books/{bookId}")
    BookDTO updateBook(@PathVariable String bookId, @RequestBody BookDTO bookDTO);

    // If a user wants to delete a book via User Service
    @DeleteMapping("/books/{bookId}/{userId}")
    void deleteBook(@PathVariable String bookId, @PathVariable String userId);

    @GetMapping("/books/recommendations/all")
    List<BookDTO> getRecommendedBooks();  // All genres (random)

    @GetMapping("/books/recommendations/new")
    List<BookDTO> getNewLaunches();  // New Launches

    @GetMapping("/books/recommendations/genre/{genre}")
    List<BookDTO> getBooksByGenre(@PathVariable String genre);  // Specific Genre

    @GetMapping("/books/recommendations/author/{author}")
    List<BookDTO> getBooksByAuthor(@PathVariable String author);  // Author-specific

    @GetMapping("/books/download/{bookId}/format/{format}/user/{userId}")
    ResponseEntity<String> downloadBook(
            @PathVariable String bookId,
            @PathVariable String format,
            @PathVariable String userId);

}
