package dev.matejeliash.springbootbackend.service;

import dev.matejeliash.springbootbackend.dto.FileDto;
import dev.matejeliash.springbootbackend.dto.UploadedFileDto;
import dev.matejeliash.springbootbackend.model.UploadedFile;
import dev.matejeliash.springbootbackend.model.User;
import dev.matejeliash.springbootbackend.repository.UploadedFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FileUploadService {

    private final UploadedFileRepository uploadedFileRepository;

    //private final Path uploadDir = Paths.get("/home/melias/uploads");
    private final Path uploadDir ;




    public FileUploadService(UploadedFileRepository uploadedFileRepository,
            @Value("${upload.dir.path}") String uploadDirPath
    ){
        this.uploadedFileRepository = uploadedFileRepository;
        this.uploadDir = Paths.get(uploadDirPath);
    }

   // upload one file using multipart
    public UploadedFile uploadFile(MultipartFile file, User user) throws IOException {
        Files.createDirectories(uploadDir);
        //
        Path userDir = uploadDir.resolve("user_" + user.getId());
        Files.createDirectories(userDir);

        // Resolve the file path within the user's directory
        Path path = userDir.resolve(file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }


        UploadedFile uploadedFile = new UploadedFile(file.getOriginalFilename(),path.toString(),file.getSize(), user);

        return uploadedFileRepository.save(uploadedFile);
    }

    // return FileStream for buffered file reading we use it in controller to create Resource downloadable in browser
    public InputStream getFileInputStream(FileDto fileDto, User user){

        Optional<UploadedFile> fileMaybe = uploadedFileRepository.findById(fileDto.getId());
        if (fileMaybe.isPresent() ){
            UploadedFile uploadedFile = fileMaybe.get();
            if (Objects.equals(uploadedFile.getUser().getId(), user.getId())){
                File file  = new File(uploadedFile.getFilepath());
                try{
                    InputStream inputStream = new FileInputStream(file);
                    return  inputStream;

                }catch (FileNotFoundException e){
                    throw new RuntimeException("file with this id do not exist");
                }

            }else{
                throw new RuntimeException("user does not own this file");
            }

        }else{

            throw new RuntimeException("file with this id do not exist");

        }
    }

    // find if file with id exists in DB
    public UploadedFile getFileFromDBIfExists(FileDto fileDto){
        Optional<UploadedFile> fileMaybe = uploadedFileRepository.findById(fileDto.getId());
        return fileMaybe.orElse(null);


    }
    // tell if file is owned by user from DB
    public boolean doesUserOwnFile(UploadedFile file, User user){
        return Objects.equals(user.getId(), file.getUser().getId());

    }

    // delete file, user must own file, it does not crash when file was removed in FS
    public FileDto deleteFile(FileDto fileDto, User user) {


        UploadedFile uploadedFile = getFileFromDBIfExists(fileDto);
        if (uploadedFile == null) {
            throw new RuntimeException("file does not exist");
        }

        if (!doesUserOwnFile(uploadedFile, user)) {
            throw new RuntimeException("user does not own file");
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
            throw new RuntimeException("failed to delete file entry from DB", e);
        }

        return fileDto;
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
