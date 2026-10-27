package center.domain;

public class TenPointPolicy extends ScorePolicy {

    public TenPointPolicy() {
        super(10, 4);
    }

    @Override
    public String description() {
        return "10-балльная шкала, сдано от " + passScore();
    }
}
