package center.ui;

import center.application.TrainingCenter;
import center.domain.Course;
import center.domain.DomainException;
import center.domain.Enrollment;
import center.domain.EnrollmentStatus;
import center.domain.HundredPointPolicy;
import center.domain.Listener;
import center.domain.ResultPolicy;
import center.domain.TenPointPolicy;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;

public class ConsoleMenu {
    private static final int MAX_ID = 1_000_000;

    private final TrainingCenter center;
    private final ConsoleInput in;
    private final PrintStream out;

    public ConsoleMenu(TrainingCenter center, ConsoleInput in, PrintStream out) {
        this.center = center;
        this.in = in;
        this.out = out;
    }

    public void run() {
        try {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = in.readInt("Выберите пункт меню", 0, 11);
                running = handle(choice);
            }
        } catch (InputClosedException e) {
            out.println();
        }
        out.println("Работа завершена. До свидания!");
    }

    private boolean handle(int choice) {
        switch (choice) {
            case 1 -> execute(this::registerListener);
            case 2 -> execute(this::enroll);
            case 3 -> execute(this::cancelEnrollment);
            case 4 -> execute(this::showMyEnrollments);
            case 5 -> execute(this::createCourse);
            case 6 -> execute(this::startCourse);
            case 7 -> execute(this::grade);
            case 8 -> execute(this::retake);
            case 9 -> execute(this::showCourseReport);
            case 10 -> showListenersAndCourses();
            case 11 -> loadDemoData();
            default -> {
                return false;
            }
        }
        return true;
    }

    private void execute(Runnable scenario) {
        while (true) {
            try {
                scenario.run();
                return;
            } catch (InputClosedException e) {
                throw e;
            } catch (DomainException e) {
                out.println("  ✗ Отказ: " + e.getMessage());
                if (!in.confirm("Повторить ввод?")) {
                    return;
                }
            }
        }
    }

    private void printMenu() {
        out.println();
        out.println("========== УЧЕБНЫЙ ЦЕНТР ==========");
        out.println("Слушатель:");
        out.println("  1. Зарегистрироваться");
        out.println("  2. Записаться на курс");
        out.println("  3. Отменить запись");
        out.println("  4. Посмотреть свои результаты");
        out.println("Преподаватель:");
        out.println("  5. Создать курс");
        out.println("  6. Начать обучение на курсе");
        out.println("  7. Выставить оценку");
        out.println("  8. Назначить пересдачу");
        out.println("  9. Сформировать отчёт по курсу");
        out.println("Прочее:");
        out.println("  10. Показать слушателей и курсы");
        out.println("  11. Загрузить демо-данные");
        out.println("  0. Выход");
    }

    private void registerListener() {
        String name = in.readNonEmpty("ФИО слушателя", 80);
        String email = in.readEmail("E-mail");
        Listener listener = center.registerListener(name, email);
        out.println("  ✓ Слушатель зарегистрирован: " + listener);
    }

    private void enroll() {
        int listenerId = in.readInt("Номер слушателя", 1, MAX_ID);
        String code = in.readCourseCode("Код курса");
        Enrollment enrollment = center.enroll(listenerId, code);
        out.println("  ✓ Запись оформлена: " + enrollment);
    }

    private void cancelEnrollment() {
        int listenerId = in.readInt("Номер слушателя", 1, MAX_ID);
        String code = in.readCourseCode("Код курса");
        Enrollment enrollment = center.cancelEnrollment(listenerId, code);
        out.println("  ✓ Запись отменена: " + enrollment);
    }

    private void showMyEnrollments() {
        int listenerId = in.readInt("Номер слушателя", 1, MAX_ID);
        List<Enrollment> enrollments = center.enrollmentsOf(listenerId);
        out.println("Слушатель: " + center.findListener(listenerId));
        if (enrollments.isEmpty()) {
            out.println("  Записей пока нет.");
        }
        enrollments.forEach(e -> out.println("  - " + e));
    }

    private void createCourse() {
        String code = in.readCourseCode("Код курса");
        String title = in.readNonEmpty("Название курса", 80);
        LocalDate start = in.readDate("Дата начала");
        int capacity = in.readInt("Вместимость (мест)", 1, 100);
        out.println("Шкала оценивания: 1 — 100-балльная (сдано от 60), 2 — 10-балльная (сдано от 4)");
        int scale = in.readInt("Шкала", 1, 2);
        ResultPolicy policy = scale == 1 ? new HundredPointPolicy() : new TenPointPolicy();
        Course course = center.createCourse(code, title, start, capacity, policy);
        out.println("  ✓ Курс создан: " + course);
    }

    private void startCourse() {
        String code = in.readCourseCode("Код курса");
        int count = center.startCourse(code);
        out.println("  ✓ Курс " + code.toUpperCase() + " начат, приступили к обучению: " + count);
    }

    /** «Выставить оценку»; если слушатель не сдал — предлагается расширение «Назначить пересдачу». */
    private void grade() {
        int listenerId = in.readInt("Номер слушателя", 1, MAX_ID);
        String code = in.readCourseCode("Код курса");
        ResultPolicy policy = center.findCourse(code).getResultPolicy();
        int score = in.readInt("Балл (" + policy.description() + ")", 0, policy.maxScore());
        Enrollment enrollment = center.grade(listenerId, code, score);
        out.println("  ✓ Результат определён: " + enrollment);
        if (enrollment.getStatus() == EnrollmentStatus.FAILED && in.confirm("Слушатель не сдал. Назначить пересдачу сейчас?")) {
            center.retake(listenerId, code);
            out.println("  ✓ Пересдача назначена: " + enrollment);
        }
    }

    private void retake() {
        int listenerId = in.readInt("Номер слушателя", 1, MAX_ID);
        String code = in.readCourseCode("Код курса");
        Enrollment enrollment = center.retake(listenerId, code);
        out.println("  ✓ Пересдача назначена: " + enrollment);
    }

    private void showCourseReport() {
        String code = in.readCourseCode("Код курса");
        Course course = center.findCourse(code);
        out.println("Отчёт по курсу " + course);
        for (EnrollmentStatus status : EnrollmentStatus.values()) {
            out.println("  " + status.getLabel() + ": " + course.countBy(status));
        }
        OptionalDouble average = course.averageScore();
        out.println("  Средний балл: " + (average.isPresent() ? String.format("%.1f", average.getAsDouble()) : "—"));
        course.getEnrollments().forEach(e -> out.println("  * " + e));
    }

    private void showListenersAndCourses() {
        out.println("Слушатели:");
        if (center.getListeners().isEmpty()) {
            out.println("  (нет)");
        }
        center.getListeners().forEach(l -> out.println("  " + l));
        out.println("Курсы:");
        if (center.getCourses().isEmpty()) {
            out.println("  (нет)");
        }
        center.getCourses().forEach(c -> out.println("  " + c));
    }

    private void loadDemoData() {
        if (!center.getListeners().isEmpty() || !center.getCourses().isEmpty()) {
            out.println("  ! Демо-данные можно загрузить только в пустую систему.");
            return;
        }
        LocalDate start = center.today().plusDays(7);
        center.registerListener("Иванов Иван Иванович", "ivanov@mail.by");
        center.registerListener("Петрова Анна Сергеевна", "petrova@mail.by");
        center.registerListener("Сидоров Пётр Олегович", "sidorov@mail.by");
        center.createCourse("JAVA-101", "Основы Java", start, 2, new HundredPointPolicy());
        center.createCourse("UML-201", "Проектирование на UML", start.plusDays(14), 10, new TenPointPolicy());
        out.println("  ✓ Загружено: 3 слушателя, 2 курса (JAVA-101 — 100-балльная, 2 места; UML-201 — 10-балльная).");
    }
}
