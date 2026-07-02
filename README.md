# OIBSIP_Java-Development_taskno1

 Task 1 — Online Train Reservation System
.>Description
A GUI-based train reservation system built with Java Swing and SQLite. Users can log in, book tickets with auto-generated PNR numbers, and cancel bookings.
Tech Stack

Java Swing (GUI)
JDBC + SQLite

.>Prerequisites

JDK 17 or 21
SQLite JDBC jar (from Maven Central)
SLF4J jars

.>Required JARs (download and place in task1-reservation/)

https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.3.0/sqlite-jdbc-3.45.3.0.jar
https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar
https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.9/slf4j-simple-2.0.9.jar

.>How to Run
bashcd task1-reservation

javac -cp ".;sqlite-jdbc-3.45.3.0.jar;slf4j-api-2.0.9.jar;slf4j-simple-2.0.9.jar" src\ReservationSystem.java -d out

java -cp "out;sqlite-jdbc-3.45.3.0.jar;slf4j-api-2.0.9.jar;slf4j-simple-2.0.9.jar" ReservationSystem
Login Credentials
UsernamePasswordadminadmin123user1pass1

.>Features

Login with credential validation
Book tickets with passenger name, train number, class, date, source, destination
Train name auto-populates from train number
Auto-generated unique PNR number
Confirmation dialog after booking
Cancel booking by PNR with "Are you sure?" prompt
Input validation for all fields
SQLite database stores all bookings
