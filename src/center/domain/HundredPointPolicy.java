package center.domain;

public class HundredPointPolicy extends ScorePolicy {

    public HundredPointPolicy() {
        super(100, 60);
    }

    @Override
    public String description() {
        return "100-балльная шкала, сдано от " + passScore();
    }
}
