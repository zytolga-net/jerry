package net.zytolga.dialogue;

import java.util.*;
import java.util.regex.Pattern;

public class MessageClassifier {
    public enum Category {
        GREETING,
        FAREWELL,
        MOOD_QUESTION,
        COMPLIMENT,
        INSULT,
        THANKS,
        GENERAL
    }

    private final Map<Category, List<String>> triggers = new EnumMap<>(Category.class);
    private final Map<String, Double> sentimentWords = new HashMap<>();

    private static final List<Category> PRIORITY = List.of(
            Category.INSULT, Category.COMPLIMENT, Category.MOOD_QUESTION,
            Category.THANKS, Category.FAREWELL, Category.GREETING, Category.GENERAL
    );
    public MessageClassifier() {
        triggers.put(Category.GREETING, List.of(
                "hello", "hi", "hey", "yo", "sup", "whats up", "what up", "good morning", "good afternoon", "good evening"
        ));
        triggers.put(Category.FAREWELL, List.of(
                "bye", "goodbye", "see ya", "see you", "later", "cya", "gtg", "gotta go"
        ));
        triggers.put(Category.MOOD_QUESTION, List.of(
                "how are you", "how you doing", "how're you", "hows it going", "how's it going", "you good", "you ok", "you okay", "how do you feel"
        ));
        triggers.put(Category.COMPLIMENT, List.of(
                "good bot", "great bot", "love you", "youre the best", "you're amazing", "youre amazing", "nice bot", "smart bot", "well done", "thank you so much"
        ));
        triggers.put(Category.INSULT, List.of(
                "bad bot", "stupid bot", "dumb bot", "worst bot", "useless bot", "shut up", "i hate you", "you suck", "trash bot", "garbage bot", "kys"
        ));
        triggers.put(Category.THANKS, List.of(
                "thanks", "thank you", "thx", "ty", "appreciate it"
        ));

        for (String w : List.of(
                "love", "great", "awesome", "amazing", "good", "nice", "thanks", "thank", "cool", "best", "happy", "wonderful"
        )) {
            sentimentWords.put(w, 4.0);
        }
        for (String w : List.of(
                "hate", "stupid", "dumb", "bad", "worst", "trash", "garbage", "suck", "annoying", "useless", "shut up", "idiot"
        )) {
            sentimentWords.put(w, -6.0);
        }
    }

    public Category classifyCategory(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        for (Category cat : PRIORITY) {
            if (cat == Category.GENERAL) continue;
            for (String phrase : triggers.get(cat)) {
                if (containsPhrase(lower, phrase)) {
                    return cat;
                }
            }
        }
        return Category.GENERAL;
    }

    public double classifySentimentDelta(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        double total = 0.0;
        for (Map.Entry<String, Double> entry : sentimentWords.entrySet()) {
            if (containsPhrase(lower, entry.getKey())) {
                total += entry.getValue();
            }
        }
        return total;
    }

    private boolean containsPhrase(String lowerMessage, String phrase) {
        if (phrase.contains(" ")) {
            return lowerMessage.contains(phrase);
        }
        // single word: match on word boundaries so "assign" doesn't trigger on "ass"
        return Pattern.compile("\\b" + Pattern.quote(phrase) + "\\b").matcher(lowerMessage).find();
    }

}
