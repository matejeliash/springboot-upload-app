.
├── \
├── check.md
├── docker-compose.yaml
├── Dockerfile
├── HELP.md
├── login.sh
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── dev
│   │   │       └── matejeliash
│   │   │           └── springbootbackend
│   │   │               ├── config [x]
│   │   │               │   ├── ApplicationConfiguration.java [x]
│   │   │               │   ├── EmailConfiguration.java [x]
│   │   │               │   ├── JwtAuthFilter.java [x]
│   │   │               │   ├── LangSuppportConfiguration.java [x]
│   │   │               │   └── SecurityConfiguration.java [x]
│   │   │               ├── controller
│   │   │               │   ├── AuthController.java
│   │   │               │   ├── FileController.java
│   │   │               │   ├── MessageController.java [x]
│   │   │               │   ├── PageController.java [x]
│   │   │               │   └── UserController.java [x]
│   │   │               ├── dto
│   │   │               │   ├── FileDto.java
│   │   │               │   ├── LoginUserDto.java
│   │   │               │   ├── RegisterUserDto.java
│   │   │               │   ├── ResponseUserDto.java
│   │   │               │   ├── UploadedFileDto.java
│   │   │               │   └── VerifyUserDto.java
│   │   │               ├── exception [x]
│   │   │               │   ├── EmailUsedException.java [x]
│   │   │               │   ├── GlobalExceptionHandler.java [x]
│   │   │               │   └── UsernameUsedException.java [x]
│   │   │               ├── model 
│   │   │               │   ├── UploadedFile.java  [x]
│   │   │               │   └── User.java [x]
│   │   │               ├── repository [x]
│   │   │               │   ├── UploadedFileRepository.java [x]
│   │   │               │   └── UserRepository.java [x]
│   │   │               ├── response [x]
│   │   │               │   └── LoginResponse.java [x]
│   │   │               ├── service
│   │   │               │   ├── AuthentificationService.java
│   │   │               │   ├── EmailService.java [x]
│   │   │               │   ├── FileUploadService.java
│   │   │               │   ├── JwtService.java
│   │   │               │   └── UserService.java [x]
│   │   │               └── SpringbootbackendApplication.java
│   │   └── resources
│   │       ├── application.properties
│   │       ├── messages
│   │       │   ├── messages.properties
│   │       │   └── messages_sk.properties
│   │       ├── static
│   │       │   ├── common.js
│   │       │   ├── login.js
│   │       │   ├── register.js
│   │       │   ├── styles.css
│   │       │   ├── upload.js
│   │       │   └── verify.js
│   │       └── templates
│   │           ├── fragments
│   │           ├── fragments.html
│   │           ├── login.html
│   │           ├── register.html
│   │           ├── upload.html
│   │           └── verify.html
│   └── test
│       └── java
│           └── dev
│               └── matejeliash
│                   └── springbootbackend
│                       ├── AuthTests.java
│                       └── SpringbootbackendApplicationTests.java
├── test_file.txt
├── test.sh
└── tmp

26 directories, 57 files
