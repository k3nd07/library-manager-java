# Library Manager (Java + SQLite)
Нужен JDK 17+ и драйвер sqlite-jdbc (https://github.com/xerial/sqlite-jdbc/releases) -> положите `sqlite-jdbc.jar` рядом.

    javac Main.java
    java -cp .:sqlite-jdbc.jar Main        # Windows: java -cp .;sqlite-jdbc.jar Main

ООП: Book (модель), Database (соединение), LibraryService (бизнес-логика, лимит 3 книги, штраф 0.5/день), LibraryException.
