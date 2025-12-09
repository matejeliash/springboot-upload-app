package dev.matejeliash.springbootbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class UploadedFileDto{

    private String filename;
    private String filepath;
    private LocalDateTime uploadedAt;
    private Long size;
    private String shareId;
    private Long id;

}

