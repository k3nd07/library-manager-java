import java.sql.*;
import java.util.*;

/** Library Manager: Java OOP + SQLite (JDBC). Меню в консоли. */
public class Main {
    public static void main(String[] a) throws Exception {
        LibraryService lib = new LibraryService(new Database("library.db"));
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("\n1 Добавить книгу  2 Добавить читателя  3 Книги  4 Выдать  5 Вернуть  6 Должники  7 Поиск  0 Выход");
            System.out.print("> ");
            String c = in.nextLine().trim();
            try {
                switch (c) {
                    case "1" -> { System.out.print("Название: "); String t = in.nextLine(); System.out.print("Автор: "); String au = in.nextLine();
                                  System.out.print("Копий: "); lib.addBook(new Book(0, t, au, Integer.parseInt(in.nextLine()))); System.out.println("Добавлено"); }
                    case "2" -> { System.out.print("Имя: "); lib.addMember(in.nextLine()); System.out.println("Добавлен"); }
                    case "3" -> lib.books("").forEach(System.out::println);
                    case "4" -> { System.out.print("ID книги: "); int b = Integer.parseInt(in.nextLine()); System.out.print("ID читателя: ");
                                  lib.borrow(b, Integer.parseInt(in.nextLine())); System.out.println("Выдано на 14 дней"); }
                    case "5" -> { System.out.print("ID выдачи: "); System.out.println("Штраф: " + lib.giveBack(Integer.parseInt(in.nextLine())) + " у.е."); }
                    case "6" -> lib.overdue().forEach(System.out::println);
                    case "7" -> { System.out.print("Поиск: "); lib.books(in.nextLine()).forEach(System.out::println); }
                    case "0" -> { return; }
                    default -> System.out.println("?");
                }
            } catch (LibraryException e) { System.out.println("Ошибка: " + e.getMessage()); }
              catch (NumberFormatException e) { System.out.println("Нужно число"); }
        }
    }
}

class LibraryException extends Exception { LibraryException(String m) { super(m); } }

class Book {
    final int id; final String title, author; final int copies;
    Book(int id, String title, String author, int copies) { this.id = id; this.title = title; this.author = author; this.copies = copies; }
    @Override public String toString() { return String.format("#%d %s — %s (в наличии: %d)", id, title, author, copies); }
}

class Database {
    private final Connection conn;
    Database(String file) throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + file);
        try (Statement s = conn.createStatement()) {
            s.executeUpdate("create table if not exists books(id integer primary key, title text, author text, copies int)");
            s.executeUpdate("create table if not exists members(id integer primary key, name text)");
            s.executeUpdate("create table if not exists loans(id integer primary key, book_id int, member_id int, due text, returned int default 0)");
        }
    }
    Connection get() { return conn; }
}

class LibraryService {
    static final double FINE_PER_DAY = 0.5;
    private final Connection c;
    LibraryService(Database db) { c = db.get(); }

    void addBook(Book b) throws LibraryException {
        if (b.title.isBlank() || b.copies < 1) throw new LibraryException("Пустое название или копий < 1");
        run("insert into books(title,author,copies) values(?,?,?)", b.title, b.author, b.copies);
    }
    void addMember(String name) throws LibraryException {
        if (name.isBlank()) throw new LibraryException("Пустое имя");
        run("insert into members(name) values(?)", name);
    }
    List<Book> books(String q) throws LibraryException {
        List<Book> r = new ArrayList<>();
        try (PreparedStatement p = c.prepareStatement("select * from books where title like ? or author like ?")) {
            p.setString(1, "%" + q + "%"); p.setString(2, "%" + q + "%");
            ResultSet rs = p.executeQuery();
            while (rs.next()) r.add(new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getInt("copies")));
        } catch (SQLException e) { throw new LibraryException(e.getMessage()); }
        return r;
    }
    void borrow(int bookId, int memberId) throws LibraryException {
        int copies = scalar("select copies from books where id=?", bookId);
        if (copies < 0) throw new LibraryException("Книга не найдена");
        if (scalar("select count(*) from members where id=?", memberId) == 0) throw new LibraryException("Читатель не найден");
        if (copies == 0) throw new LibraryException("Нет свободных копий");
        if (scalar("select count(*) from loans where member_id=? and returned=0", memberId) >= 3) throw new LibraryException("Лимит: 3 книги");
        run("update books set copies=copies-1 where id=?", bookId);
        run("insert into loans(book_id,member_id,due) values(?,?,date('now','+14 day'))", bookId, memberId);
    }
    double giveBack(int loanId) throws LibraryException {
        int overdueDays = scalar("select cast(julianday('now')-julianday(due) as int) from loans where id=? and returned=0", loanId);
        if (overdueDays == -1 && scalar("select count(*) from loans where id=? and returned=0", loanId) == 0) throw new LibraryException("Выдача не найдена или уже возвращена");
        run("update books set copies=copies+1 where id=(select book_id from loans where id=?)", loanId);
        run("update loans set returned=1 where id=?", loanId);
        return Math.max(0, overdueDays) * FINE_PER_DAY;
    }
    List<String> overdue() throws LibraryException {
        List<String> r = new ArrayList<>();
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(
            "select l.id, m.name, b.title, l.due from loans l join members m on m.id=l.member_id join books b on b.id=l.book_id where l.returned=0 and l.due<date('now')")) {
            while (rs.next()) r.add("Выдача #" + rs.getInt(1) + ": " + rs.getString(2) + " — " + rs.getString(3) + " (срок " + rs.getString(4) + ")");
        } catch (SQLException e) { throw new LibraryException(e.getMessage()); }
        if (r.isEmpty()) r.add("Должников нет");
        return r;
    }
    private void run(String sql, Object... args) throws LibraryException {
        try (PreparedStatement p = c.prepareStatement(sql)) { for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]); p.executeUpdate(); }
        catch (SQLException e) { throw new LibraryException(e.getMessage()); }
    }
    private int scalar(String sql, Object... args) throws LibraryException {
        try (PreparedStatement p = c.prepareStatement(sql)) { for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]);
            ResultSet rs = p.executeQuery(); return rs.next() ? rs.getInt(1) : -1; }
        catch (SQLException e) { throw new LibraryException(e.getMessage()); }
    }
}
