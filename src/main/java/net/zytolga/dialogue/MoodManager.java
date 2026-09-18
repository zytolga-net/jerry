package net.zytolga.dialogue;

public class MoodManager {
    private static final double MIN = -100.0;
    private static final double MAX = 100.0;
    private static final double BASELINE = 0.0;

    public enum Tier {
        FURIOUS,
        ANNOYED,
        NEUTRAL,
        CONTENT,
        HAPPY
    }

    private double mood = BASELINE;

    public Tier getTier() {
        double m = getMood();
        if (m <= -60) return Tier.FURIOUS;
        if (m <= -20) return Tier.ANNOYED;
        if (m < 20)   return Tier.NEUTRAL;
        if (m < 60)   return Tier.CONTENT;
        return Tier.HAPPY;
    }

    public synchronized void adjust(double delta) {
        mood = Math.max(MIN, Math.min(MAX, mood + delta));
    }


    public double getMood() {
        return mood;
    }
    public void setMood(double mood) {
        this.mood = mood;
    }
}
