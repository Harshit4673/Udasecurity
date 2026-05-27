# Udasecurity Home Security Application

Udasecurity is a modular Java-based home security application built using Maven.  
The application monitors sensors, processes image input for cat detection, and updates the alarm state depending on system activity.

This project was refactored into a multi-module Maven architecture to improve maintainability, scalability, and testing support.

---

# Project Overview

The application supports:

- Sensor monitoring
- Alarm state management
- Home and Away arming modes
- Cat detection using image analysis
- Unit testing for security logic
- Maven-based dependency management
- Modular Java project structure

The project separates the image processing functionality into its own independent Maven module so that it can be reused in other applications.

---

# Project Structure

```text
Udasecurity/
│
├── image-service/
│
├── security-service/
│
├── pom.xml
│
├── .gitignore
│
├── sample-cat.jpg
├── sample-not-a-cat-fail.jpg
└── sample-not-cat.jpg
```

---

# Modules

## 1. image-service

The image-service module handles image processing functionality.

### Responsibilities
- Cat detection
- Image analysis
- AWS Rekognition integration

### Main Technologies
- AWS Rekognition SDK
- SLF4J Logging

---

## 2. security-service

The security-service module contains the main application logic and GUI.

### Responsibilities
- Sensor management
- Alarm state updates
- User interface
- Arming/disarming system
- Unit testing

### Main Technologies
- Java Swing
- Guava
- Gson
- JUnit 5
- Mockito

---

# Features

- Multi-module Maven project
- Java modularization using module-info.java
- Sensor activation handling
- Alarm state transitions
- Home and Away arming support
- Cat detection using image analysis
- Unit testing using JUnit 5 and Mockito
- JaCoCo test coverage support
- SpotBugs static analysis configuration
- Executable JAR generation using Maven Shade Plugin

---

# Technologies Used

| Technology | Purpose |
|---|---|
| Java 14 | Core application development |
| Maven | Build and dependency management |
| JUnit 5 | Unit testing |
| Mockito | Mocking framework |
| AWS Rekognition SDK | Image analysis |
| Guava | Utility library |
| Gson | JSON processing |
| SLF4J | Logging |
| JaCoCo | Code coverage |
| SpotBugs | Static code analysis |

---

# Build Instructions

Open terminal inside the project root directory and run:

```bash
mvn clean test
```

This command:
- compiles the project
- runs all unit tests
- verifies project structure

---

# Package Instructions

To generate the executable JAR file:

```bash
mvn clean package
```

The generated executable JAR will be available inside:

```text
security-service/target/
```

---

# Running the Application

After packaging, run the application using:

```bash
java -jar security-service/target/security-service-1.0-SNAPSHOT.jar
```

---

# Testing

The project includes unit tests for the security service logic.

Run tests using:

```bash
mvn test
```

The tests validate:
- sensor activation behavior
- alarm transitions
- arming/disarming logic
- cat detection handling

---

# Static Analysis and Coverage

The project includes:

- JaCoCo for code coverage
- SpotBugs for static code analysis

These tools are configured through Maven plugins.

---

# Sample Images

The repository contains sample images used for testing image analysis functionality:

- sample-cat.jpg
- sample-not-a-cat-fail.jpg
- sample-not-cat.jpg

---

# Author

Harshit Chaurasia
