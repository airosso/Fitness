package events;

public class BoughtSubscription implements EventStore.Event {
    public final int accountNumber;
    public final long time;

    public BoughtSubscription(int accountNumber, long time) {
        this.accountNumber = accountNumber;
        this.time = time;
    }
}
