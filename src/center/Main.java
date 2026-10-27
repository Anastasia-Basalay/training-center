package center;

import center.application.TrainingCenter;
import center.ui.ConsoleInput;
import center.ui.ConsoleMenu;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        boolean echo = args.length > 0 && args[0].equals("--echo");
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        TrainingCenter center = new TrainingCenter(Clock.systemDefaultZone());
        new ConsoleMenu(center, new ConsoleInput(reader, out, echo), out).run();
    }
}
