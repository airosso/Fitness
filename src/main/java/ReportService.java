import events.EventStore;
import events.UserEntered;
import events.UserLeft;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

class ReportService {
    private EventStore eventStore;
    private Map<Integer, Long> entered = new HashMap<>();
    private Map<Integer, AverageStatistic> averageStatistics = new HashMap<>();
    private Map<Integer, Map<String, Integer>> dailyStat = new HashMap<>();
    private DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    ReportService(EventStore eventStore) throws IOException {
        this.eventStore = eventStore;
        this.eventStore.subscribe(this::consumeEvent);
    }

    private void consumeEvent(EventStore.Event event) {
        if (event instanceof UserEntered) {
            UserEntered cast = (UserEntered) event;
            String day = format.format(LocalDateTime.ofEpochSecond(cast.time, 0, ZoneOffset.UTC));
            dailyStat.computeIfAbsent(cast.accountNumber, s -> new HashMap<>())
                    .compute(day, (key, oldValue) -> 1 + (oldValue == null ? 0 : oldValue));
            entered.put(cast.accountNumber, cast.time);
        } else if (event instanceof UserLeft) {
            UserLeft cast = (UserLeft) event;
            Long start = entered.remove(cast.accountNumber);
            if (start != null) {
                AverageStatistic st = averageStatistics.get(cast.accountNumber);
                if (st == null) {
                    st = new AverageStatistic(0, 0.0);
                } else {
                    long duration = cast.time - start;
                    st.duration = ((st.attendanceNumber * st.duration) + duration) / (st.attendanceNumber + 1);
                    st.attendanceNumber = st.attendanceNumber + 1;
                }
                averageStatistics.put(cast.accountNumber, st);
            }
        }
    }

    double getAverageDuration(int accountNumber) {
        return averageStatistics.getOrDefault(accountNumber, new AverageStatistic(0, 0.0)).duration;
    }

    Map<String, Integer> getStatistics(int accountNumber) {
        return dailyStat.getOrDefault(accountNumber, new HashMap<>());
    }

    public class AverageStatistic {
        int attendanceNumber;
        Double duration;

        AverageStatistic(int attendanceNumber, Double duration) {
            this.attendanceNumber = attendanceNumber;
            this.duration = duration;
        }
    }
}
