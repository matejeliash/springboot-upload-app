package dev.matejeliash.springbootbackend.response;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import dev.matejeliash.springbootbackend.model.Directory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Data
public class DirResponse {
    private Long id;
    private Long parentId;
    private String name;

    private String createdAt;

    public DirResponse(){}

    public void print() {
        System.out.println();
        System.out.println(id);
        System.out.println(parentId);
        System.out.println(name);
    }

    public static DirResponse convertTo(Directory dir){
            DirResponse response = new DirResponse();
            response.setId(dir.getId());
            response.setName(dir.getName());
            response.setParentId(
                dir.getParent() == null ? null : dir.getParent().getId()
            );

            response.setCreatedAt(
                    dir.getCreatedAt()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            );
            return response;

    }

}
