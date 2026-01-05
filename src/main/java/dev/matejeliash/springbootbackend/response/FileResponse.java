package dev.matejeliash.springbootbackend.response;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonFormat;

import dev.matejeliash.springbootbackend.model.UploadedFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileResponse {

    private Long id;
    private String filename;
    private Long dirId;
    private Long size;

    private String createdAt;





    public static FileResponse convertTo(UploadedFile file){

            FileResponse fileResponse = new FileResponse();
            fileResponse.setDirId(
                file.getDirectory() == null ? null : file.getDirectory().getId()
            );
            fileResponse.setSize(file.getSize());
            fileResponse.setFilename(file.getFilename());
            fileResponse.setId(file.getId());
//            fileResponse.setCreatedAt(file.getCreatedAt());
            //fileResponse.setCreatedAt(file.getCreatedAt().atOffset(ZoneOffset.UTC));

            fileResponse.setCreatedAt(
                    file.getCreatedAt()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            );
            
            //System.out.println(fileResponse.getCreatedAt());

            return fileResponse;
    }
}
