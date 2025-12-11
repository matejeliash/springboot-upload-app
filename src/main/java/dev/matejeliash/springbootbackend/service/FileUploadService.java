package dev.matejeliash.springbootbackend.service;

import dev.matejeliash.springbootbackend.dto.FileInfo;
import dev.matejeliash.springbootbackend.dto.UploadedFileDto;
import dev.matejeliash.springbootbackend.exception.APIException;
import dev.matejeliash.springbootbackend.exception.ErrorCode;
import dev.matejeliash.springbootbackend.model.UploadedFile;
import dev.matejeliash.springbootbackend.model.User;
import dev.matejeliash.springbootbackend.repository.UploadedFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FileUploadService {

    private final UploadedFileRepository uploadedFileRepository;

    //private final Path uploadDir = Paths.get("/home/melias/uploads");
    private final Path uploadDir ;



    // load upload dir from properties
    public FileUploadService(UploadedFileRepository uploadedFileRepository,
            @Value("${upload.dir.path}") String uploadDirPath
    ){
        this.uploadedFileRepository = uploadedFileRepository;
        this.uploadDir = Paths.get(uploadDirPath);
    }

   // upload one file using multipart
    public UploadedFile uploadFile(MultipartFile file, User user) throws IOException {
        Files.createDirectories(uploadDir);

        Path userDir = uploadDir.resolve("user_" + user.getId());
        Files.createDirectories(userDir);

        // Resolve the file path within the user's directory
        Path path = userDir.resolve(file.getOriginalFilename());


        String filename= file.getOriginalFilename();
        // change filename and path if already exists with this name
        if (Files.exists(path)){
            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
            );
            path  = userDir.resolve(timestamp + "_" + file.getOriginalFilename());
            filename=timestamp + "_" + filename;

        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }

        UploadedFile uploadedFile = new UploadedFile(filename,path.toString(),file.getSize(), user);

        return uploadedFileRepository.save(uploadedFile);
    }

    // return FileStream for buffered file reading we use it in controller to create Resource downloadable in browser
    public InputStream getFileInputStream(FileInfo fileInfo, User user){

        Optional<UploadedFile> fileMaybe = uploadedFileRepository.findById(fileInfo.getId());
        if (fileMaybe.isPresent() ){
            UploadedFile uploadedFile = fileMaybe.get();
            if (Objects.equals(uploadedFile.getUser().getId(), user.getId())){
                File file  = new File(uploadedFile.getFilepath());
                try{
                    InputStream inputStream = new FileInputStream(file);
                    return  inputStream;
                // found in db but not in fs
                }catch (FileNotFoundException e){
                    throw  new APIException(
                            "file with this filename does not exits",
                            ErrorCode.FILE_NOT_EXIST,
                            HttpStatus.INTERNAL_SERVER_ERROR
                    );

                }
            //user does not own file
            }else{
                throw  new APIException(
                        "user does not own file",
                        ErrorCode.NOT_OWNER,
                        HttpStatus.UNAUTHORIZED
                );
            }

        }else{
            throw  new APIException(
                    "file with this filename does not exits",
                    ErrorCode.FILE_NOT_EXIST,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        }
    }

    // find if file with id exists in DB
    public UploadedFile getFileFromDBIfExists(FileInfo fileInfo){
        Optional<UploadedFile> fileMaybe = uploadedFileRepository.findById(fileInfo.getId());
        return fileMaybe.orElse(null);


    }
    // tell if file is owned by user from DB
    public boolean doesUserOwnFile(UploadedFile file, User user){
        return Objects.equals(user.getId(), file.getUser().getId());

    }

    // delete file, user must own file, it does not crash when file was removed in FS
    public FileInfo deleteFile(FileInfo fileInfo, User user) {


        UploadedFile uploadedFile = getFileFromDBIfExists(fileInfo);
        if (uploadedFile == null) {

            throw  new APIException(
                    "file with this filename does not exits",
                    ErrorCode.FILE_NOT_EXIST,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        if (!doesUserOwnFile(uploadedFile, user)) {
            throw  new APIException(
                    "user does not own file",
                    ErrorCode.NOT_OWNER,
                    HttpStatus.UNAUTHORIZED
            );
        }

        File file = new File(uploadedFile.getFilepath());

        // even if files does not exist ind FS do not throw just print, it may be in DB
        if (!file.exists()) {
            System.out.println("Warning: file is missing from filesystem: " + uploadedFile.getFilepath());
        } else {
            if (!file.delete()) {
                System.out.println("Warning: failed to delete file: " + uploadedFile.getFilepath());
            }
        }
        try {
            uploadedFileRepository.delete(uploadedFile);
        } catch (Exception e) {
            throw  new APIException(
                    "file with this filename does not exits",
                    ErrorCode.FILE_NOT_EXIST,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        return fileInfo;
    }


    // fetches all files recorded in DB and maps to dto object, for now it is overkill but i may add dome
    // confidential fields so it is better to use dto
   public List<UploadedFileDto> getAllUploadedFiles(User user){

        return uploadedFileRepository.findByUser(user).stream().map(f -> new UploadedFileDto(
                f.getFilename(),
                f.getFilepath(),
                f.getUploadedAt(),
                f.getSize(),
                f.getShareId(),
                f.getId()
                )).toList();



   }

}
