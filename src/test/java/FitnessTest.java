import clock.SettableClock;
import events.EventStore;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.StrictMath.abs;

public class FitnessTest {
    private EventStore eventStore;
    private Instant begin;
    private SettableClock clock;
    private Login login;
    private Manage manager;

    @Before
    public void before() throws IOException {
        eventStore = new EventStore("db.txt", true);
        begin = LocalDateTime.of(2020, 4, 20, 10, 0).toInstant(ZoneOffset.UTC);
        clock = new SettableClock(begin);
        login = new Login(clock, 200L, eventStore);
        manager = new Manage(clock, eventStore);
    }

    @Test
    public void login() throws Exception {
        Assertions.assertThatThrownBy(() -> login.enter(1));
        Assertions.assertThatThrownBy(() -> login.exit(1));

        int accountNumber = manager.buySubscription();

        login.enter(accountNumber);

        login.exit(accountNumber);
    }

    @Test
    public void expiredSubscription() throws IOException {
        int accountNumber = manager.buySubscription();

        clock.setNow(begin.plus(200L, ChronoUnit.SECONDS));

        Assertions.assertThatThrownBy(() -> login.enter(accountNumber));
    }

    @Test
    public void renewSubscription() throws Exception {
        int accountNumber = manager.buySubscription();

        clock.setNow(begin.plus(400L, ChronoUnit.SECONDS));

        manager.renewSubscription(accountNumber);

        login.enter(accountNumber);
    }

    @Test
    public void reportStatistics() throws Exception {
        ReportService reporter = new ReportService(eventStore);

        int accountNumber1 = manager.buySubscription();
        int accountNumber2 = manager.buySubscription();

        for (int i = 0; i < 5; i++) {
            login.enter(accountNumber1);
            begin = begin.plus(1, ChronoUnit.SECONDS);
            clock.setNow(begin);
            login.exit(accountNumber1);
            begin = begin.plus(1, ChronoUnit.SECONDS);
            clock.setNow(begin);
        }

        begin = begin.plus(1, ChronoUnit.DAYS);
        clock.setNow(begin);
        manager.renewSubscription(accountNumber2);
        for (int i = 0; i < 10; i++) {
            login.enter(accountNumber2);
            begin = begin.plus(2, ChronoUnit.SECONDS);
            clock.setNow(begin);
            login.exit(accountNumber2);
            begin = begin.plus(2, ChronoUnit.SECONDS);
            clock.setNow(begin);
        }

        Assert.assertTrue(abs(reporter.getAverageDuration(accountNumber1) - 1.0) < 1e-6);
        Assert.assertTrue(abs(reporter.getAverageDuration(accountNumber2) - 2.0) < 1e-6);
        Map<String, Integer> expected = reporter.getStatistics(accountNumber1);
        Map<String, Integer> actual = new HashMap<>();
        actual.put("20-04-2020", 5);
        Assert.assertEquals(actual, expected);
    }
}
