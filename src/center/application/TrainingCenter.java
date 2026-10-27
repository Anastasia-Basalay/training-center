package center.application;

import center.domain.Course;
import center.domain.DomainException;
import center.domain.Enrollment;
import center.domain.Listener;
import center.domain.ResultPolicy;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TrainingCenter {
    private final Map<Integer, Listener> listeners = new LinkedHashMap<>();
    private final Map<String, Course> courses = new LinkedHashMap<>();
    private final Clock clock;
    private int nextListenerId = 1;

    public TrainingCenter(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    public Listener registerListener(String fullName, String email) {
        String normalized = email == null ? "" : email.trim();
        boolean emailTaken = listeners.values().stream().anyMatch(l -> l.getEmail().equalsIgnoreCase(normalized));
        if (emailTaken) {
            throw new DomainException("Слушатель с e-mail " + email + " уже зарегистрирован");
        }
        Listener listener = new Listener(nextListenerId, fullName, email);
        listeners.put(listener.getId(), listener);
        nextListenerId++;
        return listener;
    }

    public Course createCourse(String code, String title, LocalDate startDate, int capacity, ResultPolicy policy) {
        if (startDate != null && startDate.isBefore(today())) {
            throw new DomainException("Дата начала курса не может быть в прошлом (сегодня " + today() + ")");
        }
        Course course = new Course(code, title, startDate, capacity, policy);
        if (courses.containsKey(course.getCode())) {
            throw new DomainException("Курс с кодом " + course.getCode() + " уже существует");
        }
        courses.put(course.getCode(), course);
        return course;
    }

    //записаться на курс
    public Enrollment enroll(int listenerId, String courseCode) {
        Listener listener = findListener(listenerId);
        Course course = findCourse(courseCode);
        return course.enroll(listener, today());
    }

    //отменить запись
    public Enrollment cancelEnrollment(int listenerId, String courseCode) {
        Enrollment enrollment = findActiveEnrollment(listenerId, courseCode);
        enrollment.cancel();
        return enrollment;
    }

    public int startCourse(String courseCode) {
        return findCourse(courseCode).start();
    }

    public Enrollment grade(int listenerId, String courseCode, int score) {
        Enrollment enrollment = findActiveEnrollment(listenerId, courseCode);
        enrollment.grade(score);
        return enrollment;
    }

    // назначить пересдачу
    public Enrollment retake(int listenerId, String courseCode) {
        Enrollment enrollment = findActiveEnrollment(listenerId, courseCode);
        enrollment.retake();
        return enrollment;
    }

    //посмотреть свои результаты
    public List<Enrollment> enrollmentsOf(int listenerId) {
        findListener(listenerId);
        List<Enrollment> result = new ArrayList<>();
        for (Course course : courses.values()) {
            result.addAll(course.enrollmentsOf(listenerId));
        }
        return result;
    }
    //посмотреть рейтинг слушателей курса
    public List<Enrollment> ranking(String courseCode) {
        return findCourse(courseCode).ranking();
    }

    public Listener findListener(int listenerId) {
        Listener listener = listeners.get(listenerId);
        if (listener == null) {
            throw new DomainException("Слушатель №" + listenerId + " не найден");
        }
        return listener;
    }

    public Course findCourse(String courseCode) {
        String code = courseCode == null ? "" : courseCode.trim().toUpperCase();
        Course course = courses.get(code);
        if (course == null) {
            throw new DomainException("Курс «" + code + "» не найден");
        }
        return course;
    }

    public List<Listener> getListeners() {
        return List.copyOf(listeners.values());
    }

    public List<Course> getCourses() {
        return List.copyOf(courses.values());
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    private Enrollment findActiveEnrollment(int listenerId, String courseCode) {
        findListener(listenerId);
        Course course = findCourse(courseCode);
        return course.findActiveEnrollment(listenerId).orElseThrow(() -> new DomainException("Слушатель №" + listenerId + " не записан на курс " + course.getCode()));
    }
}
