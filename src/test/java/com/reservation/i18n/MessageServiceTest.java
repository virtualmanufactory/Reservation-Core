package com.reservation.i18n;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import static org.assertj.core.api.Assertions.assertThat;

class MessageServiceTest {

    private final MessageService messageService;

    MessageServiceTest() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        this.messageService = new MessageService(source);
    }

    @Test
    void resolvesPolishMessage() {
        String msg = messageService.get("api.cancel.success", "pl");
        assertThat(msg).contains("odwołana");
    }

    @Test
    void resolvesEnglishMessage() {
        String msg = messageService.get("api.cancel.success", "en");
        assertThat(msg).contains("cancelled");
    }

    @Test
    void formatsConfirmationSubjectWithCode() {
        String subject = messageService.get("email.confirmation.subject", "en", "RES-1");
        assertThat(subject).contains("RES-1");
    }
}
