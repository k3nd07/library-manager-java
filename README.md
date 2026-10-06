# Library Manager (Java + SQLite)
Нужен JDK 17+ и драйвер sqlite-jdbc (https://github.com/xerial/sqlite-jdbc/releases) -> положите `sqlite-jdbc.jar` рядом.

    javac Main.java
    java -cp .:sqlite-jdbc.jar Main        # Windows: java -cp .;sqlite-jdbc.jar Main

ООП: Book (модель), Database (соединение), LibraryService (бизнес-логика, лимит 3 книги, штраф 0.5/день), LibraryException.


📚 Library Manager

A simple, lightweight console-based Library Management Application built with Java and SQLite (JDBC).

🌟 Key Features

Book Management: Add new books, update stock counts, and search by title or author.

Member Registration: Add library members with input validation.

Borrowing System: Issued for 14 days, with automatic check for a maximum limit of 3 books per member.

Overdue & Fine Calculation: Tracks overdue books and automatically calculates fines ($0.50 per day).

SQLite Integration: Auto-creates database schema and persists data locally in library.db.

🛠️ Tech Stack

Language: Java 17+

Database: SQLite

Driver: sqlite-jdbc

🚀 How to Run

Download the SQLite JDBC Driver
Download the latest .jar file from xerial/sqlite-jdbc releases and place it in the project root directory as sqlite-jdbc.jar.

Compile the Code

javac Main.java


Run the Application

Linux / macOS:

java -cp .:sqlite-jdbc.jar Main


Windows:

java -cp .;sqlite-jdbc.jar Main


🏗️ Architecture Overview

Book — Domain model representing book entity details.

Database — Manages connection and initializes database tables (books, members, loans).

LibraryService — Handles core business logic, SQL operations, and validation.

LibraryException — Custom exception handling for business rules violation.

Main — Interactive CLI menu driver.

📝 License

This project is open-source under the MIT License.
