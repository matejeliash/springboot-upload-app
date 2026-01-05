package dev.matejeliash.springbootbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeleteDto {

    public Long id;
    public boolean isFile;
}
