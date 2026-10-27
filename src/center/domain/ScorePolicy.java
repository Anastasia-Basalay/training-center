package center.domain;

public abstract class ScorePolicy implements ResultPolicy {
    private final int maxScore;
    private final int passScore;

    protected ScorePolicy(int maxScore, int passScore) {
        if (passScore < 1 || passScore > maxScore) {
            throw new DomainException("Порог сдачи должен быть в диапазоне 1.." + maxScore);
        }
        this.maxScore = maxScore;
        this.passScore = passScore;
    }

    @Override
    public int maxScore() {
        return maxScore;
    }

    @Override
    public boolean isPassed(int score) {
        return score >= passScore;
    }

    protected int passScore() {
        return passScore;
    }

    @Override
    public abstract String description();
}
