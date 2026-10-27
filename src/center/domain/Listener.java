package center.domain;

import java.util.regex.Pattern;

public class Listener {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final int id;
    private final String fullName;
    private final String email;

    public Listener(int id, String fullName, String email) {
        if (fullName == null || fullName.isBlank()) {
            throw new DomainException("ФИО слушателя не может быть пустым");
        }
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new DomainException("Некорректный e-mail: " + email);
        }
        this.id = id;
        this.fullName = fullName.trim();
        this.email = email.trim().toLowerCase();
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "№" + id + " " + fullName + " <" + email + ">";
    }
}
