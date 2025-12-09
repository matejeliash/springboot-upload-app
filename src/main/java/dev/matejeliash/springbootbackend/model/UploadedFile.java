package dev.matejeliash.springbootbackend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "uploaded_files") // force table name
@Getter
@Setter
@AllArgsConstructor
// base class for table creation for storing basic information about file
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false,unique = true)
    private String filepath;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column(nullable = false)
    private Long size;

    // user can have 0..* files and file 1 user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = true)
    private String shareId;



    public UploadedFile(String filename, String filepath, Long size, User user) {
        this.filename = filename;
        this.filepath = filepath;
        this.uploadedAt = LocalDateTime.now();
        this.size = size;
        this.user = user;
    }

    public UploadedFile() {}
}
