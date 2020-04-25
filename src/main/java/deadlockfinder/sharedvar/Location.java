package deadlockfinder.sharedvar;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Location {
    public final String name;

    private static final Map<String, Location> pool = new HashMap<String, Location>();

    public static Location of(String name) {
        if (pool.containsKey(name)) {
            return pool.get(name);
        }

        final Location newInstance = new Location(name);
        pool.put(name, newInstance);
        return newInstance;
    }

    public static Location loc(String name) {
        return Location.of(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
