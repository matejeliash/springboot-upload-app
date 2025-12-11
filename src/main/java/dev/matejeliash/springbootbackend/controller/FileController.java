package dev.matejeliash.springbootbackend.controller;

import dev.matejeliash.springbootbackend.dto.FileInfo;
import dev.matejeliash.springbootbackend.dto.UploadedFileDto;
import dev.matejeliash.springbootbackend.model.UploadedFile;
import dev.matejeliash.springbootbackend.model.User;
import dev.matejeliash.springbootbackend.service.FileUploadService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileUploadService fileUploadService;

    public FileController(FileUploadService fileUploadService) {

        this.fileUploadService = fileUploadService;
    }

   // Upload one file from browser file picker
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file")MultipartFile file,
                                             @AuthenticationPrincipal User user
    ){

        try{

            UploadedFile uploadedFile = fileUploadService.uploadFile(file,user);
            return ResponseEntity.ok(uploadedFile.getFilename() );

        }catch (IOException e){
            return ResponseEntity.badRequest().body("error while uploading file");
        }
    }


    //Returns list of files uploaded by user tied with session, we use dto so private data like
    // filepath is not provided
    @GetMapping
    public ResponseEntity<List<UploadedFileDto>> getFiles(@AuthenticationPrincipal User user){

        List<UploadedFileDto> files =fileUploadService.getAllUploadedFiles(user);
        return ResponseEntity.ok(files);

    }
    // Download file, file objet with all needed file infos is in request body as JSON
    @PostMapping("/download")
    public ResponseEntity<Resource> downloadFile(@AuthenticationPrincipal User user, @RequestBody FileInfo fileInfo){

        InputStream inputStream = fileUploadService.getFileInputStream(fileInfo,user);

        InputStreamResource resource = new InputStreamResource(inputStream);

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", fileInfo.getFilename());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // user OCTET for variability of types
        headers.setContentLength(fileInfo.getSize());

        return ResponseEntity.ok().headers(headers).body(resource);


    }

    //TODO remove
    @PostMapping("/delete")
    public ResponseEntity<FileInfo> removeFile(@AuthenticationPrincipal User user, @RequestBody FileInfo fileInfo){

        FileInfo dto = fileUploadService.deleteFile(fileInfo,user);
        return  ResponseEntity.ok(dto);
    }


}
