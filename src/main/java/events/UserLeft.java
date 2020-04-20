package events;

public class UserLeft implements EventStore.Event{
    public final int accountNumber;
    public final long time;

    public UserLeft(int accountNumber, long time) {
        this.accountNumber = accountNumber;
        this.time = time;
    }
}
