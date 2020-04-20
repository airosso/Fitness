package clock;

import java.time.Instant;

public class TimePeriod {
    private final Instant startTime;
    private final Instant endTime;

    public TimePeriod(Instant startTime, Instant endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isIn(Instant instant) {
        return !startTime.isAfter(instant) && endTime.isAfter(instant);
    }
}
