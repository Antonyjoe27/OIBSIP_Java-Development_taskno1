#  Task 1 — Online Train Reservation System

## Description
A GUI-based train reservation system built with Java Swing and SQLite. Users can log in, book tickets with auto-generated PNR numbers, and cancel bookings.

## Tech Stack
- Java Swing (GUI)
- JDBC + SQLite

## Prerequisites
- JDK 17 or 21 — download from [https://adoptium.net](https://adoptium.net)
- SQLite JDBC jar
- SLF4J jars

## Required JARs
Download and place all 3 jars inside the `task1-reservation/` folder:
- [sqlite-jdbc-3.45.3.0.jar](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.3.0/sqlite-jdbc-3.45.3.0.jar)
- [slf4j-api-2.0.9.jar](https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar)
- [slf4j-simple-2.0.9.jar](https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.9/slf4j-simple-2.0.9.jar)

## How to Run

**Step 1: Navigate to the project folder**
```bash
cd task1-reservation
```
**Step 2: Compile**
```bash
javac -cp ".;sqlite-jdbc-3.45.3.0.jar;slf4j-api-2.0.9.jar;slf4j-simple-2.0.9.jar" src\ReservationSystem.java -d out
```
**Step 3: Run**
```bash
java -cp "out;sqlite-jdbc-3.45.3.0.jar;slf4j-api-2.0.9.jar;slf4j-simple-2.0.9.jar" ReservationSystem
```

## Login Credentials
| Username | Password |
|----------|----------|
| admin    | admin123 |
| user1    | pass1    |

## Features
- Login with credential validation
- Book tickets with passenger name, train number, class, date, source, destination
- Train name auto-populates from train number
- Auto-generated unique PNR number
- Confirmation dialog after booking
- Cancel booking by PNR with "Are you sure?" prompt
- Input validation for all fields
- SQLite database stores all bookings

## Project Structure
```
task1-reservation/
├── src/
│   └── ReservationSystem.java
├── sqlite-jdbc-3.45.3.0.jar
├── slf4j-api-2.0.9.jar
└── slf4j-simple-2.0.9.jar
```
