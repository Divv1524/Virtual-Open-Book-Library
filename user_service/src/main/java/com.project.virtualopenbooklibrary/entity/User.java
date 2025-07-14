package com.project.virtualopenbooklibrary.entity;

import com.project.virtualopenbooklibrary.dto.BookDTO;
import jakarta.persistence.*;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "users")
public class User {
  @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "Name" , length = 20, nullable = false)
    private String name;
    @Column(name = "Username" , length = 20, nullable = false,unique = true)
    private String username;
    @Column(name = "Email" , nullable = false)
    private String email;
    @Column(name = "Password" , nullable = false)
    private String password;
    @Column(name = "last_download_date")
    private LocalDate lastDownloadDate; // Stores the last download date
    @Transient //we dont want it to save in DB
    private List<BookDTO> books = new ArrayList<>();
    @Column(nullable = false) // Ensure role is stored properly
    @Enumerated(EnumType.STRING) // Store as a string in the database
    private Role role;
}
