package dev.matejeliash.springbootbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveDto {
    private Long movedId;
    private Long destDirId;
    private  boolean isFile;
}
