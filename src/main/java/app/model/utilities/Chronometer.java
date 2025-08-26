package app.model.utilities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class Chronometer {
    private final LocalDateTime end;
    private final DateTimeFormatter format;
    private final Timer timer;
    private int minutes;
    private int seconds;
    private ChronometerListener listener;

    public Chronometer(int seconds) {
        LocalDateTime start = LocalDateTime.now();
        this.end = start.plusSeconds(seconds);
        this.format = DateTimeFormatter.ofPattern("mm:ss");
        this.minutes = 0;
        this.seconds = 0;
        this.timer = createTimer(seconds);
    }

    public void registerListener(ChronometerListener listener){
        this.listener = listener;
    }

    private Timer createTimer(int seconds) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int time = seconds;

            @Override
            public void run() {
                if (time <= 0) {
                    timer.cancel();
                } else {
                    Chronometer.this.minutes = (time % 3600) / 60;
                    Chronometer.this.seconds = time % 60;
                    System.out.printf("Time left - %02d:%02d%n", Chronometer.this.minutes, Chronometer.this.seconds);
                    time--;
                }
            }
        }, 0, 1000);
        return timer;
    }

    public LocalDateTime getEnd() { return end;}
    public int getMinutes() { return minutes;}
    public int getSeconds() { return seconds;}
}
