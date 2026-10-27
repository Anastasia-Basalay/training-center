package center.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

public class ConsoleInput {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern CODE = Pattern.compile("^[A-Za-zА-Яа-я0-9-]{2,15}$");

    private final BufferedReader reader;
    private final PrintStream out;
    private final boolean echo;

    public ConsoleInput(BufferedReader reader, PrintStream out, boolean echo) {
        this.reader = reader;
        this.out = out;
        this.echo = echo;
    }

    public String readLine(String prompt) {
        out.print(prompt + ": ");
        out.flush();
        try {
            String line = reader.readLine();
            if (line == null) {
                throw new InputClosedException();
            }
            if (echo) {
                out.println(line);
            }
            return line.trim();
        } catch (IOException e) {
            throw new InputClosedException();
        }
    }

    public String readNonEmpty(String prompt, int maxLength) {
        while (true) {
            String value = readLine(prompt);
            if (value.isEmpty()) {
                out.println("  ! Ввод не может быть пустым. Повторите.");
            } else if (value.length() > maxLength) {
                out.println("  ! Слишком длинное значение (максимум " + maxLength + " символов). Повторите.");
            } else {
                return value;
            }
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readLine(prompt + " [" + min + ".." + max + "]");
            if (value.isEmpty()) {
                out.println("  ! Ввод не может быть пустым. Повторите.");
                continue;
            }
            try {
                int number = Integer.parseInt(value);
                if (number < min || number > max) {
                    out.println("  ! Число вне допустимого диапазона " + min + ".." + max + ". Повторите.");
                } else {
                    return number;
                }
            } catch (NumberFormatException e) {
                out.println("  ! «" + value + "» — не целое число. Повторите.");
            }
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            String value = readLine(prompt + " (дд.мм.гггг)");
            try {
                return LocalDate.parse(value, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                out.println("  ! Неверная дата «" + value + "». Формат: дд.мм.гггг, дата должна существовать.");
            }
        }
    }

    public String readEmail(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (EMAIL.matcher(value).matches()) {
                return value;
            }
            out.println("  ! Некорректный e-mail (пример: ivan@mail.by). Повторите.");
        }
    }

    public String readCourseCode(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (CODE.matcher(value).matches()) {
                return value;
            }
            out.println("  ! Код курса: 2-15 символов (буквы, цифры, дефис), например JAVA-101. Повторите.");
        }
    }

    public boolean confirm(String prompt) {
        while (true) {
            String value = readLine(prompt + " (д/н)").toLowerCase();
            if (value.equals("д") || value.equals("да") || value.equals("y") || value.equals("yes")) {
                return true;
            }
            if (value.equals("н") || value.equals("нет") || value.equals("n") || value.equals("no")) {
                return false;
            }
            out.println("  ! Введите «д» или «н».");
        }
    }
}
