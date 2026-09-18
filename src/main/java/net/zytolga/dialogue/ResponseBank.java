package net.zytolga.dialogue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.*;

/**
 * Holds the big pool of canned lines, organized as:
 * category -> moodTier -> [ possible lines ]
 * <p>
 * Loaded from responses.json (put it on the classpath, e.g. src/main/resources).
 * Keeps a short "recently used" memory per (category, tier) so it doesn't
 * repeat the same line twice in a row — this alone does a lot of work to
 * make canned responses feel less robotic.
 */
public class ResponseBank {

    private final Map<String, Map<String, List<String>>> data = new HashMap<>();
    private final Map<String, Deque<String>> recentlyUsed = new HashMap<>();
    private static final int NO_REPEAT_MEMORY = 2; // avoid repeating last N lines per bucket
    private final Random random = new Random();

    public ResponseBank(InputStream jsonStream) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonStream);

        for (String category : root.propertyNames()) {
            JsonNode tiersNode = root.get(category);
            Map<String, List<String>> tierMap = new HashMap<>();

            for (String tier : tiersNode.propertyNames()) {
                List<String> lines = new ArrayList<>();
                for (JsonNode lineNode : tiersNode.get(tier)) {
                    lines.add(lineNode.asString());
                }
                tierMap.put(tier, lines);
            }
            data.put(category, tierMap);
        }
    }

    /**
     * Gets a response for the given category + mood tier.
     * Falls back to NEUTRAL tier, then to GENERAL category, if the exact
     * bucket is missing or empty, so a sparsely-filled responses.json never
     * throws — it just degrades gracefully.
     */
    public String getResponse(String category, @NotNull MoodManager.Tier tier) {
        List<String> pool = lookup(category, tier.name());
        if (pool == null || pool.isEmpty()) {
            pool = lookup(category, "NEUTRAL");
        }
        if (pool == null || pool.isEmpty()) {
            pool = lookup("GENERAL", tier.name());
        }
        if (pool == null || pool.isEmpty()) {
            return "..."; // absolute last-resort fallback
        }

        String bucketKey = category + ":" + tier.name();
        Deque<String> recent = recentlyUsed.computeIfAbsent(bucketKey, k -> new ArrayDeque<>());

        List<String> candidates = new ArrayList<>(pool);
        if (candidates.size() > NO_REPEAT_MEMORY) {
            candidates.removeAll(recent);
        }
        if (candidates.isEmpty()) {
            candidates = pool; // pool too small to avoid repeats, just use it all
        }

        String chosen = candidates.get(random.nextInt(candidates.size()));

        recent.addLast(chosen);
        while (recent.size() > NO_REPEAT_MEMORY) {
            recent.pollFirst();
        }

        return chosen;
    }

    @Nullable
    private List<String> lookup(String category, String tier) {
        Map<String, List<String>> tierMap = data.get(category);
        return tierMap == null ? null : tierMap.get(tier);
    }
}