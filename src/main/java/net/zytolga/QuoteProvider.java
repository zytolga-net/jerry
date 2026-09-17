package net.zytolga;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;

record Quote(String quote, String author) {
}

public class QuoteProvider {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final List<Quote> quotes;
    private final Random random = new Random();

    public QuoteProvider() {
        try (InputStream is = QuoteProvider.class.getResourceAsStream("/quotes.json")) {
            if (is == null) {
                throw new IllegalStateException("quotes.json not found on classpath");
            }
            quotes = MAPPER.readValue(is, MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, Quote.class));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load quotes.json", e);
        }
    }

    public MessageEmbed randomQuote() {
        Quote quote = quotes.get(random.nextInt(quotes.size()));
        return new EmbedBuilder()
                .setDescription("\"" + quote.quote() + "\"")
                .setFooter(quote.author())
                .setColor(Color.decode("#5865F2")) // Discord blurple, or whatever you like
                .build();
    }
}