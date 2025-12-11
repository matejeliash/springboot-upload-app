package dev.matejeliash.springbootbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileInfo {

    private Long id;
    private String filename;
    private Long size;
}
