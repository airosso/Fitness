import clock.Clock;
import clock.TimePeriod;
import events.*;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Login {
    private Clock clock;
    private Long period;
    private EventStore eventStore;

    private Map<Integer, TimePeriod> stat = new HashMap<>();
    private Set<Integer> entered = new HashSet<>();

    Login(Clock clock, Long period, EventStore eventStore) throws IOException {
        this.clock = clock;
        this.period = period;
        this.eventStore = eventStore;
        this.eventStore.subscribe(this::consumeEvent);
    }

    private void consumeEvent(EventStore.Event event) {
        if (event instanceof BoughtSubscription) {
            BoughtSubscription cast = (BoughtSubscription) event;
            Instant start = Instant.ofEpochSecond(cast.time);
            Instant end = start.plusSeconds(period);
            stat.put(cast.accountNumber, new TimePeriod(start, end));
        } else if (event instanceof RenewedSubscription) {
            RenewedSubscription cast = (RenewedSubscription) event;
            Instant start = Instant.ofEpochSecond(cast.time);
            Instant end = start.plusSeconds(period);
            stat.put(cast.accountNumber, new TimePeriod(start, end));
        } else if (event instanceof UserEntered) {
            entered.add(((UserEntered) event).accountNumber);
        } else if (event instanceof UserLeft) {
            entered.remove(((UserLeft) event).accountNumber);
        }
    }

    void enter(int accountNumber) throws Exception {
        if (entered.contains(accountNumber)) {
            throw new Exception("User is already entered");
        }
        TimePeriod accountTime = stat.get(accountNumber);
        if (accountTime == null) {
            throw new Exception("No such accountNumber");
        }
        if (!accountTime.isIn(clock.now())) {
            throw new Exception("Subscription was expired");
        }
        eventStore.post(new UserEntered(accountNumber, clock.now().getEpochSecond()));
    }

    void exit(int accountNumber) throws Exception {
        if (!entered.contains(accountNumber)) {
            throw new Exception("User did not enter");
        }
        eventStore.post(new UserLeft(accountNumber, clock.now().getEpochSecond()));
    }

}
