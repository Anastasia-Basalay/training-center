package center.domain;

public enum EnrollmentStatus {
    ENROLLED("Записан"),
    IN_PROGRESS("Обучается"),
    PASSED("Сдал"),
    FAILED("Не сдал"),
    CANCELLED("Запись отменена");

    private final String label;

    EnrollmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
