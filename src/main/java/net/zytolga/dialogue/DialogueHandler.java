package net.zytolga.dialogue;

import java.io.IOException;
import java.io.InputStream;

public class DialogueHandler {
    private final MoodManager mood = new MoodManager();
    private final MessageClassifier classifier = new MessageClassifier();
    private final ResponseBank responseBank;


    public DialogueHandler() throws IOException {
        try (InputStream in = DialogueHandler.class.getResourceAsStream("/responses.json")) {
            if (in == null) {
                throw new IOException("responses.json not found on classpath");
            }
            responseBank = new ResponseBank(in);
        }
    }

    public String handleMessage(String userId, String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        double sentimentDelta = classifier.classifySentimentDelta(content);
        if (sentimentDelta != 0.0) {
            mood.adjust(sentimentDelta);
        }

        MessageClassifier.Category category = classifier.classifyCategory(content);
        return responseBank.getResponse(category.name(), mood.getTier());
    }

    public MoodManager.Tier getCurrentMoodTier() {
        return mood.getTier();
    }
    public double getCurrentMoodScore() {
        return mood.getMood();
    }
    public void resetMood() {
        mood.setMood(0.0);
    }

}
