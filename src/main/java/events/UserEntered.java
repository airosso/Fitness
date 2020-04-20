package events;

public class UserEntered implements EventStore.Event {
    public final int accountNumber;
    public final long time;

    public UserEntered(int accountNumber, long time) {
        this.accountNumber = accountNumber;
        this.time = time;
    }
}
