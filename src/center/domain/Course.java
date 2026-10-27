package center.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public class Course {
    private final String code;
    private final String title;
    private final LocalDate startDate;
    private final int capacity;
    private final ResultPolicy resultPolicy;
    private boolean started;
    private final List<Enrollment> enrollments = new ArrayList<>();

    public Course(String code, String title, LocalDate startDate, int capacity, ResultPolicy resultPolicy) {
        if (code == null || code.isBlank()) {
            throw new DomainException("Код курса не может быть пустым");
        }
        if (title == null || title.isBlank()) {
            throw new DomainException("Название курса не может быть пустым");
        }
        if (startDate == null) {
            throw new DomainException("Не указана дата начала курса");
        }
        if (capacity < 1) {
            throw new DomainException("Вместимость курса должна быть не меньше 1");
        }
        if (resultPolicy == null) {
            throw new DomainException("Не указано правило оценивания");
        }
        this.code = code.trim().toUpperCase();
        this.title = title.trim();
        this.startDate = startDate;
        this.capacity = capacity;
        this.resultPolicy = resultPolicy;
    }

    //записать слушателя
    public Enrollment enroll(Listener listener, LocalDate today) {
        if (started) {
            throw new DomainException("Курс " + code + " уже начался, запись закрыта");
        }
        if (findActiveEnrollment(listener.getId()).isPresent()) {
            throw new DomainException("Слушатель №" + listener.getId() + " уже записан на курс " + code);
        }
        ensureFreePlace();
        Enrollment enrollment = new Enrollment(this, listener, today);
        enrollments.add(enrollment);
        return enrollment;
    }

    //проверить свободные места
    public void ensureFreePlace() {
        if (activeCount() >= capacity) {
            throw new DomainException("На курсе " + code + " нет свободных мест (" + capacity + " из " + capacity + ")");
        }
    }

    public EnrollmentStatus determineResult(int score) {
        return resultPolicy.isPassed(score) ? EnrollmentStatus.PASSED : EnrollmentStatus.FAILED;
    }

    public int start() {
        if (started) {
            throw new DomainException("Курс " + code + " уже начался");
        }
        List<Enrollment> toStart = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED)
                .toList();
        if (toStart.isEmpty()) {
            throw new DomainException("Нельзя начать курс " + code + ": нет записанных слушателей");
        }
        toStart.forEach(Enrollment::start);
        started = true;
        return toStart.size();
    }

    public Optional<Enrollment> findActiveEnrollment(int listenerId) {
        return enrollments.stream()
                .filter(e -> e.getListener().getId() == listenerId && e.isActive())
                .findFirst();
    }

    public List<Enrollment> enrollmentsOf(int listenerId) {
        return enrollments.stream().filter(e -> e.getListener().getId() == listenerId).toList();
    }

    public long activeCount() {
        return enrollments.stream().filter(Enrollment::isActive).count();
    }

    public long countBy(EnrollmentStatus status) {
        return enrollments.stream().filter(e -> e.getStatus() == status).count();
    }

    public OptionalDouble averageScore() {
        return enrollments.stream()
                .flatMap(e -> e.getScore().stream())
                .mapToInt(Integer::intValue)
                .average();
    }

    public List<Enrollment> getEnrollments() {
        return Collections.unmodifiableList(enrollments);
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public int getCapacity() {
        return capacity;
    }

    public ResultPolicy getResultPolicy() {
        return resultPolicy;
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public String toString() {
        return code + " «" + title + "», старт " + startDate + ", мест занято " + activeCount() + "/" + capacity
                + (started ? " [идёт обучение]" : " [набор открыт]");
    }
}
