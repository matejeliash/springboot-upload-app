package dev.matejeliash.springbootbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class GetDirsDto {

    private Long parentId;
}
