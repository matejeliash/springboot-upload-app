package dev.matejeliash.springbootbackend.controller;

import dev.matejeliash.springbootbackend.dto.CreateDirDto;
import dev.matejeliash.springbootbackend.dto.DeleteDto;
import dev.matejeliash.springbootbackend.dto.GetDirsDto;
import dev.matejeliash.springbootbackend.dto.MoveDto;
import dev.matejeliash.springbootbackend.dto.RenameDto;
import dev.matejeliash.springbootbackend.model.Directory;
import dev.matejeliash.springbootbackend.model.UploadedFile;
import dev.matejeliash.springbootbackend.model.User;
import dev.matejeliash.springbootbackend.repository.DirectoryRepository;
import dev.matejeliash.springbootbackend.response.DirResponse;
import dev.matejeliash.springbootbackend.response.FileResponse;
import dev.matejeliash.springbootbackend.service.FileUploadService;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileUploadService fileUploadService;
    private final DirectoryRepository directoryRepository;

    public FileController(
        FileUploadService fileUploadService,
        DirectoryRepository directoryRepository
    ) {
        this.fileUploadService = fileUploadService;
        this.directoryRepository = directoryRepository;
    }

    @PostMapping("/createDir")
    public ResponseEntity<String> createDir(
        @RequestBody CreateDirDto createDirDto,
        @AuthenticationPrincipal User user
    ) {
            Directory dir = fileUploadService.createDir(createDirDto, user);
            return ResponseEntity.ok(String.format("directory %s created", dir.getFullPath()));
        }

    // Upload one file from browser file picker
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "dirId", required = false) String dirId,
        @AuthenticationPrincipal User user
    ) {
        if (dirId.equals("null") || dirId.isEmpty()) {
            UploadedFile uploadedFile = fileUploadService.uploadFile(
                file,
                null,
                user
            );
            return ResponseEntity.ok(uploadedFile.getFilename());
        } else {
            UploadedFile uploadedFile = fileUploadService.uploadFile(
                file,
                Long.parseLong(dirId),
                user
            );
            return ResponseEntity.ok(uploadedFile.getFilename());
        }
    }

    //Returns list of files uploaded by user tied with session, we use dto so private data like
    // filepath is not provided
    @PostMapping
    public ResponseEntity<List<FileResponse>> getFiles(
        @RequestBody GetDirsDto getDirsDto,
        @AuthenticationPrincipal User user
    ) {
        List<FileResponse> files = fileUploadService.getAllFilesFromDir(
            getDirsDto.getParentId(),
            user
        );
        return ResponseEntity.ok(files);
    }

    @PostMapping("/dirs")
    public ResponseEntity<List<DirResponse>> getDirs(
        @RequestBody GetDirsDto getDirsDto,
        @AuthenticationPrincipal User user
    ) {
        List<DirResponse> dirs = fileUploadService.getAllDirsFromDir(
            getDirsDto.getParentId(),
            user
        );

        return ResponseEntity.ok(dirs);
    }

    //    // Download file, file objet with all needed file infos is in request body as JSON
    @PostMapping("/download")
    public ResponseEntity<Resource> downloadFile(
        @AuthenticationPrincipal User user,
        @RequestBody FileResponse fileInfo
    ) {
        InputStream inputStream = fileUploadService.getFileInputStream(
            fileInfo,
            user
        );

        InputStreamResource resource = new InputStreamResource(inputStream);

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData(
            "attachment",
            fileInfo.getFilename()
        );
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // use OCTET for variability of types
        headers.setContentLength(fileInfo.getSize());

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    //TODO remove
    @PostMapping("/delete")
    public ResponseEntity<String> removeFile(
        @RequestBody DeleteDto deleteDto,
        @AuthenticationPrincipal User user
    ) {
        if (deleteDto.isFile()) {
            fileUploadService.deleteFile(deleteDto, user);
        } else {
            fileUploadService.deleteDir(deleteDto, user);
        }
        return ResponseEntity.ok("file / dir removed");
    }

    @PostMapping("/move")
    public ResponseEntity<String> moveFile(
        @RequestBody MoveDto moveDto,
        @AuthenticationPrincipal User user
    ) {
        System.out.println(moveDto.getDestDirId());
        System.out.println(moveDto.getMovedId());

        if (moveDto.isFile()) {
          UploadedFile file =   fileUploadService.moveFile(moveDto, user);
          return ResponseEntity.ok(String.format("file moved to %s", file.getFullPath()));
        }else{
           Directory dir =  fileUploadService.moveDir(moveDto, user);
          return ResponseEntity.ok(String.format("file moved to %s", dir.getFullPath()));


        }
    }


    @PostMapping("/rename")
    public ResponseEntity<String> renameFile(
        @RequestBody RenameDto renameDto,
        @AuthenticationPrincipal User user
    ) {
        if (renameDto.isFile){
            UploadedFile file = fileUploadService.renameFile(renameDto, user);
            return ResponseEntity.ok(String.format("file was renamed to:%s",file.getFilename()));

        }else{
           Directory dir  = fileUploadService.renameDir(renameDto, user);
            return ResponseEntity.ok(String.format("dir was renamed to:%s",dir.getName()));

        }
    }
}
