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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dir_id",nullable = true)
    private Directory directory;



    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Long size;

    // user can have 0..* files and file 1 user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = true)
    private String shareId;

    public String getFullPath() {
        if (directory == null){
            return filename;
        }
        return directory.getFullPath() + "/" + filename;
    }

    public UploadedFile(String filename, Directory directory, Long size, User user) {
        this.filename = filename;
        this.directory = directory;
        this.createdAt = LocalDateTime.now();
        this.size = size;
        this.user = user;
    }

    public UploadedFile() {}
}
