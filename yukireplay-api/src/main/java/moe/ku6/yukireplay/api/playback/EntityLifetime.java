package moe.ku6.yukireplay.api.playback;

import moe.ku6.yukireplay.api.util.Vec2i;

import java.util.TreeMap;

/**
 * Represents the lifetime of an entity in a playback session. Entities in a playback are created upon playback load and are never removed. An entity's lifetime controls when the entity is visible to players and when it is spawned or despawned on the client. An entity's lifetime may contain multiple spawn and despawn events, allowing for complex behaviors such as re-spawning after being killed or disappearing after a certain event.
 */
public class EntityLifetime {
    private final TreeMap<Integer, Vec2i> periods = new TreeMap<>();

    public void AddPeriod(int start, int end) {
        if (start >= end) {
            throw new IllegalArgumentException("Start time must be less than end time.");
        }
        if (periods.containsKey(start)) {
            throw new IllegalArgumentException("A period already exists starting at this time.");
        }

        // Check for overlap with any period where start < newEnd and end > newStart
        var lower = periods.floorEntry(start);
        if (lower != null) {
            Vec2i existing = lower.getValue();
            if (existing.getY() > start) {
                throw new IllegalArgumentException("New period overlaps with an existing period.");
            }
        }

        var higher = periods.ceilingEntry(start);
        if (higher != null) {
            Vec2i existing = higher.getValue();
            if (existing.getX() < end) {
                throw new IllegalArgumentException("New period overlaps with an existing period.");
            }
        }

        periods.put(start, new Vec2i(start, end));
    }

    /**
     * Checks whether the entity is alive at a specific time.
     * The entity is alive if there is a period that contains the time t.
     * @param t The time to check, in ticks.
     * @return true if the entity is alive at time t, false otherwise.
     */
    public boolean IsAlive(int t) {
        if (periods.isEmpty()) {
            return false;
        }
        // Find the period that contains the time t
        var entry = periods.floorEntry(t);
        if (entry == null) {
            return false; // No period starts before or at time t
        }
        Vec2i period = entry.getValue();
        return period.Contains(t);
    }
}
