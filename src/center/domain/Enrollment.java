package center.domain;

import java.time.LocalDate;
import java.util.Optional;

public class Enrollment {
    private final Course course;
    private final Listener listener;
    private final LocalDate enrolledOn;
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;
    private Integer score;
    private int attempts;

    Enrollment(Course course, Listener listener, LocalDate enrolledOn) {
        this.course = course;
        this.listener = listener;
        this.enrolledOn = enrolledOn;
    }

    void start() {
        requireStatus(EnrollmentStatus.ENROLLED, "начать обучение");
        status = EnrollmentStatus.IN_PROGRESS;
    }

    public void cancel() {
        if (status != EnrollmentStatus.ENROLLED) {
            throw new DomainException("Отменить можно только запись до начала курса (текущий статус: "
                    + status.getLabel() + ")");
        }
        status = EnrollmentStatus.CANCELLED;
    }

    public void grade(int newScore) {
        requireStatus(EnrollmentStatus.IN_PROGRESS, "выставить оценку");
        int max = course.getResultPolicy().maxScore();
        if (newScore < 0 || newScore > max) {
            throw new DomainException("Балл должен быть в диапазоне 0.." + max);
        }
        score = newScore;
        attempts++;
        status = course.determineResult(newScore);
    }

    public void retake() {
        requireStatus(EnrollmentStatus.FAILED, "назначить пересдачу");
        status = EnrollmentStatus.IN_PROGRESS;
    }

    public boolean isActive() {
        return status != EnrollmentStatus.CANCELLED;
    }

    private void requireStatus(EnrollmentStatus expected, String action) {
        if (status != expected) {
            throw new DomainException("Нельзя " + action + ": статус записи «" + status.getLabel()
                    + "», требуется «" + expected.getLabel() + "»");
        }
    }

    public Course getCourse() {
        return course;
    }

    public Listener getListener() {
        return listener;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public Optional<Integer> getScore() {
        return Optional.ofNullable(score);
    }

    public int getAttempts() {
        return attempts;
    }

    @Override
    public String toString() {
        return course.getCode() + " | " + listener.getFullName() + " | " + status.getLabel() + getScore().map(s -> " | балл " + s + " (попыток: " + attempts + ")").orElse("");
    }
}
