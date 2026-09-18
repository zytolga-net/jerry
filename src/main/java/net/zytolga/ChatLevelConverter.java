package net.zytolga;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.jetbrains.annotations.NotNull;

public class ChatLevelConverter extends ClassicConverter {
    @Override
    public String convert(@NotNull ILoggingEvent event) {
        return "Chat".equals(event.getLoggerName()) ? "CHAT" : event.getLevel().toString();
    }
}
