package dev.matejeliash.springbootbackend.service;

import dev.matejeliash.springbootbackend.dto.CreateDirDto;
import dev.matejeliash.springbootbackend.dto.DeleteDto;
import dev.matejeliash.springbootbackend.dto.FileInfo;
import dev.matejeliash.springbootbackend.dto.MoveDto;
import dev.matejeliash.springbootbackend.dto.RenameDto;
import dev.matejeliash.springbootbackend.exception.APIException;
import dev.matejeliash.springbootbackend.exception.ErrorCode;
import dev.matejeliash.springbootbackend.model.Directory;
import dev.matejeliash.springbootbackend.model.UploadedFile;
import dev.matejeliash.springbootbackend.model.User;
import dev.matejeliash.springbootbackend.repository.DirectoryRepository;
import dev.matejeliash.springbootbackend.repository.UploadedFileRepository;
import dev.matejeliash.springbootbackend.response.DirResponse;
import dev.matejeliash.springbootbackend.response.FileResponse;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {

    private final UploadedFileRepository uploadedFileRepository;
    private final DirectoryRepository directoryRepository;

    //private final Path uploadDir = Paths.get("/home/melias/uploads");
    private final Path uploadDir;

    // load upload dir from properties
    public FileUploadService(
        UploadedFileRepository uploadedFileRepository,
        DirectoryRepository directoryRepository,
        @Value("${upload.dir.path}") String uploadDirPath
    ) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.uploadDir = Paths.get(uploadDirPath);
        this.directoryRepository = directoryRepository;
    }

    // create  empty user dir for file uploading 
    // it is based on user id, dir is named  user_{id}
    public Path createUserDir(Long userId) {
        try {
            Files.createDirectories(uploadDir);
            Path userDirPath = uploadDir.resolve("user_" + userId);
            Files.createDirectories(userDirPath);

            return userDirPath;
        } catch (IOException e) {
            throw new APIException(
                e.getMessage(),
                ErrorCode.FAILED_TO_CREATE_DIR,
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // return Path to user dir
    public Path getUserUploadDir(Long userId) {
        return uploadDir.resolve("user_" + userId);
    }

    // create expected file Path from root user dir, Directory and name
    // works for files and also dirs
    public Path createFinalPath(
        Path userDirPath,
        Directory parentDir,
        String name
    ) {
        Path finalDirPath;
        if (parentDir != null) {
            finalDirPath = userDirPath.resolve(parentDir.getFullPath());
        } else {
            // parentDir == null == user_dir
            finalDirPath = userDirPath;
        }

        Path finalPath = finalDirPath.resolve(name);
        return finalPath;
    }
    



    // fetches all files recorded in DB and maps to response objects, for now it is overkill but i may add dome
    // confidential fields so it is better to use dto
    public List<FileResponse> getAllFilesFromDir(Long parentDirId, User user) {
        List<UploadedFile> files =
            uploadedFileRepository.findByUserIdAndDirectoryId(
                user.getId(),
                parentDirId
            );
        List<FileResponse> fileResponses = new ArrayList<>();

        // map all UPloadedFiles to FileResponses
        for (var f : files) {
            FileResponse fileResponse = FileResponse.convertTo(f);
            // FileResponse fileResponse = new FileResponse();

            // fileResponse.setDirId(
            //     f.getDirectory() == null ? null : f.getDirectory().getId()
            // );
            // fileResponse.setSize(f.getSize());
            // fileResponse.setFilename(f.getFilename());
            // fileResponse.setId(f.getId());
            fileResponses.add(fileResponse);
        }


        return fileResponses;
    }

    // similar to getAllFilesFromDir
    // get all files in certain dir, we use DirResponse so we do not leak user
    // [0] is previous dir if cur dir is not null
    public List<DirResponse> getAllDirsFromDir(Long dirId, User user) {

        List<Directory> dirs;

        dirs = directoryRepository.findByParentIdAndUserId(
            dirId,
            user.getId()
        );

        // map to DirResponses so we do not leak user
        List<DirResponse> responses = new ArrayList<>();
        for (Directory dir : dirs) {
            DirResponse response = DirResponse.convertTo(dir);
            responses.add(response);
        }


        // add previous dir to allow to go back  in dir structure
        if (dirId !=null){
            Directory parentDir = directoryRepository.findByIdAndUserId(dirId, user.getId())
            .orElse(null);
            if (parentDir!=null){
                DirResponse prevDir;

                if (parentDir.getParent() == null){
                    prevDir = new DirResponse();
                    prevDir.setId(
                        parentDir.getParent() == null ? null : parentDir.getParent().getId()
                    );
                    prevDir.setName(".."); // add generic .. name
                    prevDir.setParentId(null); // parentId is not important here


                }else{
                    Directory prev = directoryRepository.findByIdAndUserId(
                        parentDir.getParent().getId(), 
                        user.getId()
                    ).get();
                    
                    prevDir =  DirResponse.convertTo(prev);
                    prevDir.setName("..");
                    
                }
                
                responses.add(0,prevDir); // put as first 

                DirResponse curDir = DirResponse.convertTo(parentDir);
                curDir.setName(".");
                responses.add(1,curDir);

            }
            
        }

        return responses;
    }


    public boolean isNameUsedInDir(Long dirId, Long userId, String name){

        // find if dir already contains dir with specified name
        boolean dirAlreadyExists =
            directoryRepository.existsByParentIdAndUserIdAndName(
                dirId,
                userId,
                name
            );


        boolean fileAlreadyExists = uploadedFileRepository.existsByDirectoryIdAndUserIdAndFilename(
            dirId,
             userId,
              name
            );

        return dirAlreadyExists || fileAlreadyExists;

    }


   // find Directory in DB matching id and user
   //  throws APIException if dir not found 
    public Directory findDir(Long dirId, Long userId) throws APIException{
        Directory parentDir;

        if (dirId == null) {
            parentDir = null;
        } else if (directoryRepository.existsByIdAndUserId(dirId, userId)) {
            parentDir = directoryRepository.findById(dirId).get();
        } else {
            throw new APIException(
                String.format("dir with id:%d does not exist",dirId),
                ErrorCode.DIR_NOT_FOUND,
                HttpStatus.NOT_FOUND
            );
        }

        return parentDir;
    }

    public Directory renameDir(RenameDto renameDto, User user){

        // dir name is not present
        if (renameDto.getName().isEmpty() || renameDto.id == null ){
            throw new APIException(
                "empty fields detected",
                ErrorCode.EMPTY_FIELDS,
                HttpStatus.BAD_REQUEST
            );
        }

        Long id = renameDto.getId();
        String name = renameDto.getName();
        Long userId = user.getId();

        // find actual directory obj in DB
        Directory dir = directoryRepository
            .findByIdAndUserId(id, userId)
            .orElseThrow(() ->
                new APIException(
                String.format("dir with id:%d does not exist",id),
                    ErrorCode.DIR_NOT_FOUND,
                    HttpStatus.NOT_FOUND
                )
            );


        // create User dir in case it does not exist
        Path userDirPath = createUserDir(userId);

        Long parentDirId =dir.getParent() == null ? null : dir.getParent().getId();

        // find if dir already contains dir with specified name
        boolean alreadyExists =isNameUsedInDir(parentDirId, userId, name);

        if (alreadyExists) {
            throw new APIException(
                String.format(
                    "dir or file with name:%s already exists in current dir",
                    name
                ),
                ErrorCode.DIR_ALREADY_EXISTS,
                HttpStatus.CONFLICT
            );
        }

        Path oldPath = userDirPath.resolve(dir.getFullPath());
        Path newPath = oldPath.resolveSibling(name);


        try{
    
            Files.move(oldPath, newPath, StandardCopyOption.ATOMIC_MOVE);


        }catch (IOException e){
            throw new APIException(
                String.format("failed to rename dir to:%s",name),
                ErrorCode.FAILED_TO_CREATE_DIR,
                HttpStatus.BAD_REQUEST
            );

        }

        dir.setName(name);


        return directoryRepository.save(dir);

    }


    public UploadedFile renameFile(RenameDto renameDto, User user){

        // dir name is not present
        if (renameDto.getName().isEmpty() || renameDto.id == null ){
            throw new APIException(
                "empty fields detected",
                ErrorCode.EMPTY_FIELDS,
                HttpStatus.BAD_REQUEST
            );
        }

        Long id = renameDto.getId();
        String name = renameDto.getName();
        Long userId = user.getId();

        // find actual directory obj in DB
        UploadedFile file = uploadedFileRepository
            .findByUserIdAndId(userId, id)
            .orElseThrow(() ->
                new APIException(
                String.format("file with id:%d does not exist",id),
                    ErrorCode.FILE_NOT_EXIST,
                    HttpStatus.NOT_FOUND
                )
            );


        // create User dir in case it does not exist
        Path userDirPath = createUserDir(userId);

        Long parentDirId = file.getDirectory() == null ? null : file.getDirectory().getId();

        // find if dir already contains dir with specified name
        boolean alreadyExists =isNameUsedInDir(parentDirId, userId, name);

        if (alreadyExists) {
            throw new APIException(
                String.format(
                    "dir or file with name:%s already exists in current dir",
                    name
                ),
                ErrorCode.DIR_ALREADY_EXISTS,
                HttpStatus.CONFLICT
            );
        }

        Path oldPath = userDirPath.resolve(file.getFullPath());
        Path newPath = oldPath.resolveSibling(name);


        try{
    
            Files.move(oldPath, newPath, StandardCopyOption.ATOMIC_MOVE);


        }catch (IOException e){
            throw new APIException(
                String.format("failed to rename dir to:%s",name),
                ErrorCode.FAILED_TO_CREATE_DIR,
                HttpStatus.BAD_REQUEST
            );

        }

        file.setFilename(name);


        return uploadedFileRepository.save(file);

    }
    
    public Directory createDir(CreateDirDto createDirDto, User user){
        // dir name is not present
        if (createDirDto.getName().isEmpty()) {
            throw new APIException(
                "empty fields detected",
                ErrorCode.EMPTY_FIELDS,
                HttpStatus.BAD_REQUEST
            );
        }

        Long dirId = createDirDto.getParentDirId();
        String name = createDirDto.getName();
        Long userId = user.getId();

        // create User dir in case it does not exist
        Path userDirPath = createUserDir(userId);



        // find if dir already contains dir with specified name
        boolean alreadyExists =isNameUsedInDir(dirId, userId, name);

        if (alreadyExists) {
            throw new APIException(
                String.format(
                    "dir or file with name:%s already exists in current dir",
                    createDirDto.getName()
                ),
                ErrorCode.DIR_ALREADY_EXISTS,
                HttpStatus.CONFLICT
            );
        }

        // create Dir object
        Directory parentDir = findDir(dirId, userId);

        Path finalDirPath = createFinalPath(userDirPath, parentDir, name);

        try{
        Files.createDirectories(finalDirPath);

        }catch (IOException e){
            throw new APIException(
                String.format("failed to create dir %s",name),
                ErrorCode.FAILED_TO_CREATE_DIR,
                HttpStatus.BAD_REQUEST
            );

        }

        // put to DB after dir created in FS
        Directory directory = new Directory();
        directory.setUser(user);
        directory.setName(name);
        directory.setParent(parentDir);
        directory.setCreatedAt(LocalDateTime.now());

        return directoryRepository.save(directory);
    }



    // upload one file using multipart
    public UploadedFile uploadFile(MultipartFile file, Long dirId, User user) {
        Long userId = user.getId();
        // create root user dir
        Path userDirPath = createUserDir(userId);


        Directory parentDir = findDir(dirId, userId);
        String filename = file.getOriginalFilename();

        Path finalFilePath = createFinalPath(userDirPath, parentDir, filename);

        // change filename and path if already exists with this name
        // bt prepending current timestamp
        if (Files.exists(finalFilePath)) {
            String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
            );
            finalFilePath = finalFilePath
                .getParent()
                .resolve(timestamp + "_" + filename);
            filename = timestamp + "_" + filename;
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(
                inputStream,
                finalFilePath,
                StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new APIException(
                e.getMessage(),
                ErrorCode.UPLOAD_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        // crete new file in DB after successful upload
        UploadedFile uploadedFile = new UploadedFile(
            filename,
            parentDir,
            file.getSize(),
            user
        );

        return uploadedFileRepository.save(uploadedFile);
    }

    // return FileStream that we use in uploadFile method, this give us stream of file bytes
    public InputStream getFileInputStream(FileResponse fileInfo, User user) {
        Long userId = user.getId();
        Long dirId = fileInfo.getDirId();

        Directory parentDir = findDir(dirId, userId);
        String filename = fileInfo.getFilename();
        Path userDirPath = getUserUploadDir(userId);

        Optional<UploadedFile> maybeFile =
            uploadedFileRepository.findByUserIdAndId(userId, fileInfo.getId());
        if (maybeFile.isEmpty()) {
            throw new APIException(
                String.format("file with this id:%d does not exist",fileInfo.getDirId()),
                ErrorCode.FILE_NOT_EXIST,
                HttpStatus.NOT_FOUND
            );
        }

        Path finalFilePath = createFinalPath(userDirPath, parentDir, filename);

        try {
            InputStream inputStream = Files.newInputStream(finalFilePath);
            return inputStream;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new APIException(
                "file exists in DB but not in filesystem",
                ErrorCode.FILE_NOT_EXIST,
                HttpStatus.NOT_FOUND
            );
        }
    }

    // // find if file with id exists in DB
    // public UploadedFile getFileFromDBIfExists(FileInfo fileInfo) {
    //     Optional<UploadedFile> fileMaybe = uploadedFileRepository.findById(
    //         fileInfo.getId()
    //     );
    //     return fileMaybe.orElse(null);
    // }

    // // tell if file is owned by user from DB
    // public boolean doesUserOwnFile(UploadedFile file, User user) {
    //     return Objects.equals(user.getId(), file.getUser().getId());
    // }



    // move file from dir to other dir 
    public UploadedFile moveFile(MoveDto moveDto, User user) {
        Path userDirPath = getUserUploadDir(user.getId());
        Long fileId = moveDto.getMovedId();
        Long userId = user.getId();
        Long destDirId = moveDto.getDestDirId();

        UploadedFile file = uploadedFileRepository
            .findByUserIdAndId(userId, fileId)
            .orElseThrow(() ->
                new APIException(
                    String.format("file with id:%d does not exist", fileId),
                    ErrorCode.FILE_NOT_EXIST,
                    HttpStatus.NOT_FOUND
                )
            );
         

        
        Path srcFilePath = userDirPath.resolve(file.getFullPath());
        Path destFilePath;
        Directory destDir;
       
        // dest dir is not user dir
        if(destDirId !=null){

            destDir = directoryRepository
                .findByIdAndUserId(destDirId, userId)
                .orElseThrow(() ->
                    new APIException(
                    String.format("dir with id:%d does not exist",destDirId),
                        ErrorCode.DIR_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                    )
                );

            

            destFilePath = userDirPath
                .resolve(destDir.getFullPath())
                .resolve(file.getFilename());
        }else{
            destDir = null;
            destFilePath = userDirPath
                .resolve(file.getFilename());
        }

        // find if file or fir in dest fir used name and reject moving
        boolean alreadyExists =isNameUsedInDir(destDirId, userId, file.getFilename());

        if (alreadyExists) {
            throw new APIException(
                String.format(
                    "dir or file with name:%s already exists in current dir",
                    file.getFilename()
                ),
                ErrorCode.DIR_ALREADY_EXISTS,
                HttpStatus.CONFLICT
            );
        }

        
        file.setDirectory(destDir); 


        try {
            Files.move(
                srcFilePath,
                destFilePath,
                StandardCopyOption.REPLACE_EXISTING
            );
        // throw exception and stop whole moving
        } catch (IOException e) {
            throw new APIException(
                String.format(
                    "could not move %s -> %s",
                    srcFilePath,
                    destFilePath
                ),
                ErrorCode.MOVE_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        return uploadedFileRepository.save(file);
    }

    // move all dir also with its content 
    public Directory moveDir(MoveDto moveDto, User user) {
        Path userDirPath = getUserUploadDir(user.getId());
        Long srcDirId = moveDto.getMovedId();
        Long userId = user.getId();
        Long destDirId = moveDto.getDestDirId();

        // System.out.print( "from " + srcDirId);
        // System.out.print( "->" + destDirId);

        Directory srcDir = directoryRepository
            .findByIdAndUserId(srcDirId, userId)
            .orElseThrow(() ->
                new APIException(
                String.format("dir with id:%d does not exist",srcDirId),
                    ErrorCode.DIR_NOT_FOUND,
                    HttpStatus.NOT_FOUND
                )
            );
        
        Path srcDirPath = userDirPath.resolve(srcDir.getFullPath());
        Directory destDir ;
        Path newDirPath;
       
        // dest dir is not  user upload root dir == null
        if(destDirId !=null){

            destDir = directoryRepository
                .findByIdAndUserId(destDirId, userId)
                .orElseThrow(() ->
                    new APIException(
                    String.format("dir with id:%d does not exist",destDirId),
                        ErrorCode.DIR_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                    )
                );

            newDirPath = userDirPath
                .resolve(destDir.getFullPath())
                .resolve(srcDir.getName());
            
        //dest dir is user root upload  dir == null
        }else{
            destDir = null;
            newDirPath = userDirPath
                .resolve(srcDir.getName());

        }


        boolean alreadyExists =isNameUsedInDir(destDirId, userId, srcDir.getName());

        if (alreadyExists) {
            throw new APIException(
                String.format(
                    "dir or file with name:%s already exists in current dir",
                    srcDir.getName()
                ),
                ErrorCode.DIR_ALREADY_EXISTS,
                HttpStatus.CONFLICT
            );
        }
        
        srcDir.setParent(destDir);
        


        try {
            Files.move(srcDirPath, newDirPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new APIException(
                String.format(
                    "could not move %s -> %s",
                    srcDirPath,
                    newDirPath
                ),
                ErrorCode.MOVE_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
       return  directoryRepository.save(srcDir);

    }

    public void deleteFile(DeleteDto deleteDto, User user) {
        Path userDirPath = getUserUploadDir(user.getId());

        Optional<UploadedFile> maybeFile =
            uploadedFileRepository.findByUserIdAndId(
                user.getId(),
                deleteDto.getId()
            );

        if (maybeFile.isEmpty()) {
            throw new APIException(
                String.format("file with id:%d  does not exits",deleteDto.getId()),
                ErrorCode.FILE_NOT_EXIST,
                HttpStatus.NOT_FOUND
            );
        }
        UploadedFile uploadedFile = maybeFile.get();
        Path finalFilePath = userDirPath.resolve(uploadedFile.getFullPath());

        try {
            Files.delete(finalFilePath);
        } catch (IOException e) {
            // print error not throw, because this error may happen only if file
            // was deleted on fs 
            System.out.println(
                "Warning: failed to delete file: " + finalFilePath
            );
        }

        uploadedFileRepository.delete(uploadedFile);
    }

    public void deleteDir(DeleteDto deleteDto, User user) {
        if (deleteDto.id == null) {
            throw new APIException(
                "directory id cannot be null",
                ErrorCode.FILE_NOT_EXIST,
                HttpStatus.NOT_ACCEPTABLE
            );
        }

        Long rootDirId = deleteDto.getId();
        Path userDirPath = getUserUploadDir(user.getId());

        Optional<Directory> maybeDir = directoryRepository.findByIdAndUserId(
            rootDirId,
            user.getId()
        );
        Directory rootDir = maybeDir.orElseThrow(() ->
            new APIException(
                "directory with this filename does not exits",
                ErrorCode.FILE_NOT_EXIST,
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        );

        List<UploadedFile> files = new ArrayList<>();  // list for all files  to remove 
        List<Directory> dirs = new ArrayList<>(); // list for all dirs to remove
        Queue<Directory> queue = new LinkedList<>();
        queue.add(rootDir); // keep for proper recursive deeper and deeper ordered  dir listing

        // loop over dirs in dir and add for proper safe recursive order
        while (!queue.isEmpty()) {
            Directory curDir = queue.poll();
            
            // fetched dirs from particular dir 
            List<Directory> gotDirs =
                directoryRepository.findByParentIdAndUserId(
                    curDir.getId(),
                    user.getId()
                );
            dirs.addAll(gotDirs);
            queue.addAll(gotDirs); 

            List<UploadedFile> gotFiles =
                uploadedFileRepository.findByUserIdAndDirectoryId(
                    user.getId(),
                    curDir.getId()
                );
            files.addAll(gotFiles);
        }

        for (var f : files) {
            String fullPath = f.getFullPath();
            try {
                // remove file, order here is not that important
                Files.delete(userDirPath.resolve(fullPath));
            } catch (IOException e) {
                System.out.println(
                    "Warning: failed to delete file: " +
                        userDirPath.resolve(fullPath)
                );
            }
        }
        uploadedFileRepository.deleteAll(files);

        // reverse so we removed from deepest dir
        Collections.reverse(dirs);

        for (var d : dirs) {
            String fullPath = d.getFullPath();
            try {
                Files.delete(userDirPath.resolve(fullPath));
            } catch (IOException e) {
                System.out.println(
                    "Warning: failed to delete dir: " +
                        userDirPath.resolve(fullPath)
                );
            }
        }

        // same as before remove from DB is reverse order
        for (var d : dirs) {
            directoryRepository.deleteById(d.getId());
        }

        try {
            // try to remove 
            Files.delete(userDirPath.resolve(rootDir.getFullPath()));
        } catch (IOException e) {
            System.out.println("Warning: failed to delete root dir");
        }
        directoryRepository.deleteById(rootDirId);
    }
}
