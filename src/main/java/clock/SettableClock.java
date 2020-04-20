package clock;

import java.time.Instant;

public class SettableClock implements Clock {
    private Instant now;

    @Override
    public Instant now() {
        return this.now;
    }

    public SettableClock(Instant now) {
        this.now = now;
    }

    public void setNow(Instant now) {
        this.now = now;
    }
}
