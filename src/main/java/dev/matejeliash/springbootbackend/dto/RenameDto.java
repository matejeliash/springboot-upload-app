package dev.matejeliash.springbootbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RenameDto {


    public Long id;
    public boolean isFile;
    public String name;


    
}
