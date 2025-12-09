package dev.matejeliash.springbootbackend.repository;

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
}
