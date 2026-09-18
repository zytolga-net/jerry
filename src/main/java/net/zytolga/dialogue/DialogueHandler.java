package net.zytolga.dialogue;

import java.io.IOException;
import java.io.InputStream;

public class DialogueHandler {
    private final MoodManager mood = new MoodManager();
    private final MessageClassifier classifier = new MessageClassifier();

    public DialogueHandler() throws IOException {
        try (InputStream in = DialogueHandler.class.getResourceAsStream("responses.json")) {
            if (in == null) {
                throw new IOException("responses.json not found on classpath");
            }
        }
    }

}
