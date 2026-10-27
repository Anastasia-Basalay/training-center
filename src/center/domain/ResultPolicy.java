package center.domain;

public interface ResultPolicy {

    int maxScore();
    boolean isPassed(int score);
    String description();
}
