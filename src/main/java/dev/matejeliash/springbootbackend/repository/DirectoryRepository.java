package dev.matejeliash.springbootbackend.repository;

import dev.matejeliash.springbootbackend.model.Directory;
import dev.matejeliash.springbootbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public interface DirectoryRepository  extends JpaRepository<Directory,Long> {
   public List<Directory> findByUser(User user);
   public List<Directory> findByUserAndName(User user, String name);
   public Optional<Directory> findById(Long id);
   public Optional<Directory> findByIdAndUserId(Long id, Long userId);
   public Optional<Directory> findByIdAndUser(Long id, User user);
    public List<Directory> findByParentIdAndUserAndName(Long parentId, User user,String name);
    //public List<Directory> findByParentIdAndUser(Long parentId, User user);
    List<Directory> findByParentIsNullAndUser(User user);
    List<Directory> findByParentIsNullAndUserId(Long userId);
    //public List<Directory> findByParentIdAndUserId(Long parentId, Long userId);
//    @Query("SELECT d FROM Directory d WHERE d.parent.id = :parentId AND d.user.id = :userId")
//    List<Directory> findByParentIdAndUserId(@Param("parentId") Long parentId, @Param("userId") Long userId);
    List<Directory> findByParentIdAndUserId(Long parentId,Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
    boolean existsByParentIdAndUserIdAndName(Long parentId, Long userId, String name);
}
