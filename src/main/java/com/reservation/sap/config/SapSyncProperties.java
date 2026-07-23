package com.reservation.sap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.sap")
public class SapSyncProperties {

    private boolean enabled = true;

    /** Cron zasilenia (domyślnie co godzinę). */
    private String cron = "0 0 * * * *";

    /**
     * UPSERT — insert/update w tabeli docelowej + oznaczanie nieaktualnych.
     * STAGING_SWAP — najpierw staging, potem promocja w jednej transakcji.
     */
    private Strategy strategy = Strategy.UPSERT;

    private ObsoleteMode obsoleteMode = ObsoleteMode.SOFT_DELETE;

    /** Wartość {@code status_na_stronie} dla rekordów nieobecnych w snapshocie SAP. */
    private String obsoleteStatus = "NIEAKTUALNY";

    private Source source = new Source();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public ObsoleteMode getObsoleteMode() {
        return obsoleteMode;
    }

    public void setObsoleteMode(ObsoleteMode obsoleteMode) {
        this.obsoleteMode = obsoleteMode;
    }

    public String getObsoleteStatus() {
        return obsoleteStatus;
    }

    public void setObsoleteStatus(String obsoleteStatus) {
        this.obsoleteStatus = obsoleteStatus;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public enum Strategy {
        UPSERT,
        STAGING_SWAP
    }

    public enum ObsoleteMode {
        /** Ustawia status_na_stronie na wartość obsolete-status. */
        SOFT_DELETE,
        /** Fizycznie usuwa rekordy spoza snapshotu. */
        HARD_DELETE
    }

    public static class Source {
        private Type type = Type.FILE;
        private String filePath = "./config/sap-feed.json";
        private String url = "";
        private String username = "";
        private String password = "";
        private int connectTimeoutMs = 5000;
        private int readTimeoutMs = 30000;

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        public enum Type {
            FILE,
            HTTP
        }
    }
}
