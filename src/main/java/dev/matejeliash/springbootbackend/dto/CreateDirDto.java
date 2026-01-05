package dev.matejeliash.springbootbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateDirDto {


        private String name;
        private Long parentDirId;


    public void print() {
        System.out.println(name);
        System.out.println(parentDirId);
    }

}
