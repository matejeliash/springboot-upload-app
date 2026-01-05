package dev.matejeliash.springbootbackend.repository;

import dev.matejeliash.springbootbackend.model.Directory;
import dev.matejeliash.springbootbackend.model.UploadedFile;
import dev.matejeliash.springbootbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


// create sql table that is mapped to UploadedFile class
@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile,Long> {

    public List<UploadedFile> findByUser(User user);
    public Optional<UploadedFile> findByFilename(String filename);
    public List<UploadedFile> findByUserAndDirectoryId(User user, Long directoryId);
    public List<UploadedFile> findByUserIdAndDirectoryId(Long userId, Long directoryId);
    public Optional<UploadedFile> findByUserIdAndId(Long userId, Long id);
    public List<UploadedFile> findByUserAndDirectory(User user, Directory directory);
    public List<UploadedFile> findByUserAndDirectoryIsNull (User user);
    public boolean existsByDirectoryIdAndUserIdAndFilename(Long directoryId, Long userId, String filename);


}
