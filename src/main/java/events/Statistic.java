package events;

public class Statistic {
    public int accountNumber;
    public int renewNumber;
    public int attendanceNumber;
    public Long lastVisit;

    public Statistic(int accountNumber, int renewNumber, int attendanceNumber, Long lastVisit) {
        this.accountNumber = accountNumber;
        this.renewNumber = renewNumber;
        this.attendanceNumber = attendanceNumber;
        this.lastVisit = lastVisit;
    }
}
