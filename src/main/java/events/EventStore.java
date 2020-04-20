package events;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Consumer;

public class EventStore {
    private String fileName;
    private List<Consumer<Event>> consumers = new ArrayList<>();

    public EventStore(String fileName, Boolean clean) throws IOException {
        this.fileName = fileName;
        Path path = Paths.get(fileName);
        if (clean && Files.exists(path)) {
            Files.delete(path);
        }
        if (Files.notExists(path)) {
            Files.createFile(path);
        }
    }

    public void post(Event event) throws IOException {
        String serialized = new ObjectMapper().writeValueAsString(event);
        String className = event.getClass().getName();
        String payload = className + " \"" + serialized + "\"\n";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(payload);
        }
        consumers.forEach(s -> s.accept(event));
    }

    public void subscribe(Consumer<Event> consumer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            reader.lines().forEach(line -> {
                StringTokenizer tokenizer = new StringTokenizer(line);
                String className = tokenizer.nextToken();
                String payload = tokenizer.nextToken();
                payload = payload.substring(1, payload.length() - 1);
                try {
                    Event event = (Event) (new ObjectMapper().readValue(payload, Class.forName(className)));
                    consumer.accept(event);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
        consumers.add(consumer);
    }

    public interface Event {
    }
}

