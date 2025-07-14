package com.project.virtualopenbooklibrary.controller;

import com.project.virtualopenbooklibrary.dto.BookDTO;
import com.project.virtualopenbooklibrary.dto.UserDTO;
import com.project.virtualopenbooklibrary.external.BookFeignClient;
import com.project.virtualopenbooklibrary.repository.UserRepository;
import com.project.virtualopenbooklibrary.security.JwtUtil;
import com.project.virtualopenbooklibrary.services.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
        //soft hard
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookFeignClient bookFeignClient;

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    //add user
    @PostMapping("/register")
    public ResponseEntity<UserDTO> addUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(userDTO));
    }

    //get specific user with userId
    @GetMapping("/{userId}")
    @PreAuthorize("@userRepository.findById(#userId).get().username == authentication.name or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    //get all users
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUser());
    }


    //updating user
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateUser(@PathVariable String id, @RequestBody UserDTO userDTO) {
        userService.updateUser(id, userDTO);
        return ResponseEntity.ok("User updated successfully!");
    }


    //delete user
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        userService.deleteById(id);
        return ResponseEntity.ok("User deleted successfully!");
    }

    // Add Book for User
    @PostMapping("/{userId}/books")
    public ResponseEntity<BookDTO> addBookForUser(@PathVariable String userId, @RequestBody BookDTO bookDTO) {
        return ResponseEntity.ok(userService.addBookForUser(userId, bookDTO));
    }

    // Update Book for User
    @PutMapping("/{userId}/books/{bookId}")
    public ResponseEntity<BookDTO> updateBookForUser(
            @PathVariable String userId,
            @PathVariable String bookId,
            @RequestBody BookDTO bookDTO) {

        // Set User ID in BookDTO before calling UserService
        bookDTO.setUserId(userId);
        return ResponseEntity.ok(userService.updateBookForUser(bookId, bookDTO));
    }
    // Delete Book for User
    @DeleteMapping("/{userId}/books/{bookId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // ✅ Only ADMIN can delete books
    public ResponseEntity<String> deleteBookForUser(
            @PathVariable String userId,
            @PathVariable String bookId) {

        userService.deleteBookForUser(userId, bookId);
        return ResponseEntity.ok("Book deleted successfully!");
    }

    // Get all recommended books
    @GetMapping("/recommendations/all")
    public List<BookDTO> getRecommendedBooks() {
        return userService.getRecommendedBooksForUser();
    }

    // Get new launches
    @GetMapping("/recommendations/new")
    public List<BookDTO> getNewLaunches() {
        return userService.getNewLaunchesForUser();
    }

    // Get books by genre
    @GetMapping("/recommendations/genre/{genre}")
    public List<BookDTO> getBooksByGenre(@PathVariable String genre) {
        return userService.getBooksByGenreForUser(genre);
    }

    // Get books by author
    @GetMapping("/recommendations/author/{author}")
    public List<BookDTO> getBooksByAuthor(@PathVariable String author) {
        return userService.getBooksByAuthorForUser(author);
    }
    // Download Book through Feign Client
    @GetMapping("/download/{bookId}/format/{format}/user/{userId}")
    public ResponseEntity<String> downloadBook(
            @PathVariable String bookId,
            @PathVariable String format,
            @PathVariable String userId) {
        return bookFeignClient.downloadBook(bookId, format, userId);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password)
    {
        logger.info("Username"+username);
        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));// checking password is correct or not
            UserDetails userDetails = (UserDetails) authentication.getPrincipal(); //calling UserServiceDetails internally Load user details

            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("USER");  // Default to USER if no role found

            logger.info("User Role -> " + role);
            String jwt= jwtUtil.generateToken(userDetails.getUsername() , role);// generating token for that user
            return new ResponseEntity<>(jwt, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("exception in authenticating the user",e);
            return new ResponseEntity<>("Incorrect username or password", HttpStatus.BAD_REQUEST);
        }
    }

}
