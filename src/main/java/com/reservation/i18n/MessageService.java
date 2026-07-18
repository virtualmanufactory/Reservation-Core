package com.reservation.i18n;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(String key, String localeTag, Object... args) {
        Locale locale = resolveLocale(localeTag);
        return messageSource.getMessage(key, args, key, locale);
    }

    public Locale resolveLocale(String localeTag) {
        if (localeTag == null || localeTag.isBlank()) {
            return Locale.forLanguageTag("pl");
        }
        String normalized = localeTag.toLowerCase();
        if (normalized.startsWith("en")) {
            return Locale.ENGLISH;
        }
        return Locale.forLanguageTag("pl");
    }
}
