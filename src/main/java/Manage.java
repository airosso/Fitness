import clock.Clock;
import events.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class Manage {
    private Clock clock;
    private EventStore eventStore;
    private Map<Integer, Statistic> stat = new HashMap<>();

    Manage(Clock clock, EventStore eventStore) throws IOException {
        this.clock = clock;
        this.eventStore = eventStore;
        this.eventStore.subscribe(this::consumeEvent);
    }

    private void consumeEvent(EventStore.Event event) {
        if (event instanceof BoughtSubscription) {
            stat.put(((BoughtSubscription) event).accountNumber, new Statistic(
                    ((BoughtSubscription) event).accountNumber,
                    0,
                    0,
                    null
            ));
        } else if (event instanceof RenewedSubscription) {
            Statistic st = stat.get(((RenewedSubscription) event).accountNumber);
            if (st != null) {
                st.renewNumber = st.renewNumber + 1;
                stat.put(((RenewedSubscription) event).accountNumber, st);
            }
        } else if (event instanceof UserEntered) {
            Statistic st = stat.get(((UserEntered) event).accountNumber);
            if (st != null) {
                st.attendanceNumber = st.attendanceNumber + 1;
                st.lastVisit = ((UserEntered) event).time;
                stat.put(((UserEntered) event).accountNumber, st);
            }
        }
    }

    int buySubscription() throws IOException {
        int accountNumber = new Random().nextInt();
        while (stat.containsKey(accountNumber)) {
            accountNumber = new Random().nextInt();
        }
        eventStore.post(new BoughtSubscription(accountNumber, clock.now().getEpochSecond()));
        return accountNumber;
    }

    void renewSubscription(int accountNumber) throws Exception {
        if (stat.containsKey(accountNumber)) {
            eventStore.post(new RenewedSubscription(accountNumber, clock.now().getEpochSecond()));
        } else
            throw new Exception("No such subscription");
    }


}
