package events;

public class RenewedSubscription implements EventStore.Event {
    public final int accountNumber;
    public final long time;

    public RenewedSubscription(int accountNumber, long time) {
        this.accountNumber = accountNumber;
        this.time = time;
    }
}
