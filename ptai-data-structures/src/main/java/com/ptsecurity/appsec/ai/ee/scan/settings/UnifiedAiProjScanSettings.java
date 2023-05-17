package com.ptsecurity.appsec.ai.ee.scan.settings;

import com.ptsecurity.appsec.ai.ee.scan.result.ScanBrief;
import com.ptsecurity.misc.tools.exceptions.GenericException;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface UnifiedAiProjScanSettings {
    void load(@NonNull final String data) throws GenericException;

    enum Version { LEGACY, V10, V11 }
    Version getVersion();

    /**
     * Project name i.e. how it will be shown in PT AI viewer interface
     */
    @NonNull String getProjectName();

    @NonNull ScanBrief.ScanSettings.Language getProgrammingLanguage();

    @RequiredArgsConstructor
    enum ScanModule {
        CONFIGURATION("Configuration"),
        COMPONENTS("Components"),
        BLACKBOX("BlackBox"),
        DATAFLOWANALYSIS("DataFlowAnalysis"),
        PATTERNMATCHING("PatternMatching"),
        VULNERABLESOURCECODE("VulnerableSourceCode");

        @Getter
        private final String value;
    }
    Set<ScanModule> getScanModules();

    String getCustomParameters();

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    class DotNetSettings {
        public enum ProjectType {
            NONE, SOLUTION, WEBSITE
        }
        protected ProjectType projectType;
        protected String solutionFile;
    }
    DotNetSettings getDotNetSettings();

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    class JavaSettings {
        protected String parameters;
        protected Boolean unpackUserPackages;
        protected String userPackagePrefixes;
        public enum JavaVersion {
            v1_8, v1_11
        }
        protected JavaSettings.JavaVersion javaVersion;
    }
    JavaSettings getJavaSettings();

    @NonNull Boolean isSkipGitIgnoreFiles();
    @NonNull Boolean isUsePublicAnalysisMethod();
    @NonNull Boolean isUseSastRules();
    @NonNull Boolean isUseCustomPmRules();

    @NonNull
    @Deprecated
    Boolean isUseCustomYaraRules();

    @NonNull Boolean isUseSecurityPolicies();
    @NonNull Boolean isDownloadDependencies();

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    class MailingProjectSettings {
        @NonNull protected Boolean enabled;
        protected String mailProfileName;
        protected List<String> emailRecipients = new ArrayList<>();
    }
    MailingProjectSettings getMailingProjectSettings();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class BlackBoxSettings {
        protected List<Pair<String, String>> additionalHttpHeaders;

        @Getter
        @Setter
        @Builder
        @AllArgsConstructor
        public static class ListItem {
            enum Format {
                WILDCARD, EXACTMATCH, REGEXP
            }
            protected Format format;
            protected String address;
        }
        protected List<ListItem> whiteListedAddresses;
        protected List<ListItem> blackListedAddresses;

        public enum ScanLevel {
            NONE,
            FAST,
            FULL,
            NORMAL
        }
        protected ScanLevel scanLevel;

        public enum ScanScope {
            FOLDER,
            DOMAIN,
            PATH
        }
        protected ScanScope scanScope;

        protected String site;

        protected Boolean sslCheck;

        protected Boolean runAutocheckAfterScan;

        @Getter
        @Setter
        @Builder
        @AllArgsConstructor
        public static class ProxySettings {
            Boolean enabled;
            String host;
            String login;
            String password;
            Integer port;

            public enum Type {
                HTTP, HTTPNOCONNECT, SOCKS4, SOCKS5
            }
            Type type;
        }
        protected ProxySettings proxySettings;

        @Getter
        @Setter
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Authentication {
            public enum Type {
                FORM,
                HTTP,
                NONE,
                COOKIE;
            }
            protected Type type;
        }
        @Getter
        @Setter
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CookieAuthentication extends Authentication {
            protected String cookie;
            protected String validationAddress;
            protected String validationTemplate;
        }
        @Getter
        @Setter
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class HttpAuthentication extends Authentication {
            protected String login;
            protected String password;
            protected String validationAddress;
        }
        @Getter
        @Setter
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FormAuthentication extends Authentication {
            public enum Detection { AUTO, MANUAL }
            protected Detection detection;
            protected String formAddress;
            protected String login;
            protected String password;
            protected String validationAddress;
            protected String validationTemplate;
        }
        @Getter
        @Setter
        @SuperBuilder
        @AllArgsConstructor
        public static class FormAuthenticationAuto extends FormAuthentication {}
        @Getter
        @Setter
        @SuperBuilder
        @AllArgsConstructor
        public static class FormAuthenticationManual extends FormAuthentication {
            protected String loginKey;
            protected String passwordKey;
            protected String xPath;
        }
        protected Authentication authentication;
    }
    BlackBoxSettings getBlackBoxSettings();
}