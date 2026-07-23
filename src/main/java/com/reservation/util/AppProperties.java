package com.reservation.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String baseUrl = "http://localhost:8080";
    private String defaultLocale = "pl";
    private Cleanup cleanup = new Cleanup();
    // SAP sync settings live in SapSyncProperties (prefix app.sap)

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public Cleanup getCleanup() {
        return cleanup;
    }

    public void setCleanup(Cleanup cleanup) {
        this.cleanup = cleanup;
    }

    public static class Cleanup {
        private boolean enabled = true;
        private int unconfirmedHours = 24;
        private int pastSlotsDays = 30;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getUnconfirmedHours() {
            return unconfirmedHours;
        }

        public void setUnconfirmedHours(int unconfirmedHours) {
            this.unconfirmedHours = unconfirmedHours;
        }

        public int getPastSlotsDays() {
            return pastSlotsDays;
        }

        public void setPastSlotsDays(int pastSlotsDays) {
            this.pastSlotsDays = pastSlotsDays;
        }
    }
}
