package com.ptsecurity.appsec.ai.ee.utils.ci.integration.api.v470.converters;

import com.ptsecurity.appsec.ai.ee.scan.result.ScanBrief.ScanSettings.Language;
import com.ptsecurity.appsec.ai.ee.scan.settings.Policy;
import com.ptsecurity.appsec.ai.ee.scan.settings.UnifiedAiProjScanSettings;
import com.ptsecurity.appsec.ai.ee.scan.settings.UnifiedAiProjScanSettings.BlackBoxSettings;
import com.ptsecurity.appsec.ai.ee.scan.settings.UnifiedAiProjScanSettings.BlackBoxSettings.AddressListItem;
import com.ptsecurity.appsec.ai.ee.scan.settings.UnifiedAiProjScanSettings.BlackBoxSettings.Authentication;
import com.ptsecurity.appsec.ai.ee.scan.settings.UnifiedAiProjScanSettings.BlackBoxSettings.FormAuthentication.DetectionType;
import com.ptsecurity.appsec.ai.ee.scan.settings.UnifiedAiProjScanSettings.BlackBoxSettings.ProxySettings;
import com.ptsecurity.appsec.ai.ee.scan.settings.UnifiedAiProjScanSettings.DotNetSettings;
import com.ptsecurity.appsec.ai.ee.scan.settings.UnifiedAiProjScanSettings.JavaSettings;
import com.ptsecurity.appsec.ai.ee.server.v470.api.model.*;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.utils.json.JsonPolicyHelper;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static com.ptsecurity.appsec.ai.ee.scan.settings.UnifiedAiProjScanSettings.ScanModule.BLACKBOX;
import static com.ptsecurity.misc.tools.helpers.CollectionsHelper.isNotEmpty;

@Slf4j
public class AiProjConverter {
    private static final Map<BlackBoxSettings.ScanLevel, BlackBoxScanLevel> BLACKBOX_SCAN_LEVEL_MAP = new HashMap<>();
    private static final Map<BlackBoxSettings.ScanScope, ScanScope> BLACKBOX_SCAN_SCOPE_MAP = new HashMap<>();
    private static final Map<Authentication.Type, AuthType> BLACKBOX_AUTH_TYPE_MAP = new HashMap<>();
    private static final Map<ProxySettings.Type, ProxyType> BLACKBOX_PROXY_TYPE_MAP = new HashMap<>();
    private static final Map<DetectionType, BlackBoxFormDetection> BLACKBOX_FORM_DETECTION_TYPE_MAP = new HashMap<>();
    private static final Map<AddressListItem.Format, BlackBoxFormat> BLACKBOX_ADDRESSLIST_ITEM_FORMAT_MAP = new HashMap<>();

    private static final Map<Language, ProgrammingLanguageGroup> REVERSE_LANGUAGE_GROUP_MAP = new HashMap<>();
    private static final Map<DotNetSettings.ProjectType, DotNetProjectType> DOTNET_PROJECT_TYPE_MAP = new HashMap<>();
    private static final Map<JavaSettings.JavaVersion, JavaVersions> JAVA_VERSION_MAP = new HashMap<>();

    static {
        BLACKBOX_SCAN_LEVEL_MAP.put(BlackBoxSettings.ScanLevel.NONE, BlackBoxScanLevel.NONE);
        BLACKBOX_SCAN_LEVEL_MAP.put(BlackBoxSettings.ScanLevel.FAST, BlackBoxScanLevel.FAST);
        BLACKBOX_SCAN_LEVEL_MAP.put(BlackBoxSettings.ScanLevel.NORMAL, BlackBoxScanLevel.NORMAL);
        BLACKBOX_SCAN_LEVEL_MAP.put(BlackBoxSettings.ScanLevel.FULL, BlackBoxScanLevel.FULL);

        BLACKBOX_SCAN_SCOPE_MAP.put(BlackBoxSettings.ScanScope.DOMAIN, ScanScope.DOMAIN);
        BLACKBOX_SCAN_SCOPE_MAP.put(BlackBoxSettings.ScanScope.FOLDER, ScanScope.FOLDER);
        BLACKBOX_SCAN_SCOPE_MAP.put(BlackBoxSettings.ScanScope.PATH, ScanScope.PATH);

        BLACKBOX_AUTH_TYPE_MAP.put(Authentication.Type.FORM, AuthType.FORM);
        BLACKBOX_AUTH_TYPE_MAP.put(Authentication.Type.HTTP, AuthType.HTTP);
        BLACKBOX_AUTH_TYPE_MAP.put(Authentication.Type.NONE, AuthType.NONE);
        BLACKBOX_AUTH_TYPE_MAP.put(Authentication.Type.COOKIE, AuthType.RAWCOOKIE);

        BLACKBOX_PROXY_TYPE_MAP.put(ProxySettings.Type.HTTP, ProxyType.HTTP);
        BLACKBOX_PROXY_TYPE_MAP.put(ProxySettings.Type.HTTPNOCONNECT, ProxyType.HTTPNOCONNECT);
        BLACKBOX_PROXY_TYPE_MAP.put(ProxySettings.Type.SOCKS4, ProxyType.SOCKS4);
        BLACKBOX_PROXY_TYPE_MAP.put(ProxySettings.Type.SOCKS5, ProxyType.SOCKS5);

        BLACKBOX_FORM_DETECTION_TYPE_MAP.put(DetectionType.AUTO, BlackBoxFormDetection.AUTO);
        BLACKBOX_FORM_DETECTION_TYPE_MAP.put(DetectionType.MANUAL, BlackBoxFormDetection.MANUAL);

        BLACKBOX_ADDRESSLIST_ITEM_FORMAT_MAP.put(AddressListItem.Format.WILDCARD, BlackBoxFormat.WILDCARD);
        BLACKBOX_ADDRESSLIST_ITEM_FORMAT_MAP.put(AddressListItem.Format.EXACTMATCH, BlackBoxFormat.EXACTMATCH);
        BLACKBOX_ADDRESSLIST_ITEM_FORMAT_MAP.put(AddressListItem.Format.REGEXP, BlackBoxFormat.REGEXP);

        REVERSE_LANGUAGE_GROUP_MAP.put(Language.CPP, ProgrammingLanguageGroup.CANDCPLUSPLUS);
        REVERSE_LANGUAGE_GROUP_MAP.put(Language.GO, ProgrammingLanguageGroup.GO);
        REVERSE_LANGUAGE_GROUP_MAP.put(Language.JAVASCRIPT, ProgrammingLanguageGroup.JAVASCRIPT);
        REVERSE_LANGUAGE_GROUP_MAP.put(Language.CSHARPWINONLY, ProgrammingLanguageGroup.CSHARPWINONLY);
        REVERSE_LANGUAGE_GROUP_MAP.put(Language.CSHARP, ProgrammingLanguageGroup.CSHARP);
        REVERSE_LANGUAGE_GROUP_MAP.put(Language.JAVA, ProgrammingLanguageGroup.JAVA);
        REVERSE_LANGUAGE_GROUP_MAP.put(Language.KOTLIN, ProgrammingLanguageGroup.KOTLIN);
        REVERSE_LANGUAGE_GROUP_MAP.put(Language.SQL, ProgrammingLanguageGroup.SQL);
        REVERSE_LANGUAGE_GROUP_MAP.put(Language.PYTHON, ProgrammingLanguageGroup.PYTHON);
        REVERSE_LANGUAGE_GROUP_MAP.put(Language.SWIFT, ProgrammingLanguageGroup.SWIFT);
        REVERSE_LANGUAGE_GROUP_MAP.put(Language.VB, ProgrammingLanguageGroup.VB);
        REVERSE_LANGUAGE_GROUP_MAP.put(Language.PHP, ProgrammingLanguageGroup.PHP);
        REVERSE_LANGUAGE_GROUP_MAP.put(Language.OBJECTIVEC, ProgrammingLanguageGroup.OBJECTIVEC);
        REVERSE_LANGUAGE_GROUP_MAP.put(Language.RUBY, ProgrammingLanguageGroup.RUBY);

        DOTNET_PROJECT_TYPE_MAP.put(DotNetSettings.ProjectType.NONE, DotNetProjectType.NONE);
        DOTNET_PROJECT_TYPE_MAP.put(DotNetSettings.ProjectType.SOLUTION, DotNetProjectType.SOLUTION);
        DOTNET_PROJECT_TYPE_MAP.put(DotNetSettings.ProjectType.WEBSITE, DotNetProjectType.WEBSITE);

        JAVA_VERSION_MAP.put(JavaSettings.JavaVersion.v1_8, JavaVersions._8);
        JAVA_VERSION_MAP.put(JavaSettings.JavaVersion.v1_11, JavaVersions._11);
        JAVA_VERSION_MAP.put(JavaSettings.JavaVersion.v1_17, JavaVersions._17);

    }

    protected static WhiteBoxSettingsModel apply(@NonNull final UnifiedAiProjScanSettings settings, @NonNull WhiteBoxSettingsModel model) {
        model.setStaticCodeAnalysisEnabled(
                settings.getScanModules().contains(UnifiedAiProjScanSettings.ScanModule.VULNERABLESOURCECODE)
                        || settings.getScanModules().contains(UnifiedAiProjScanSettings.ScanModule.DATAFLOWANALYSIS)
                        || settings.getScanModules().contains(UnifiedAiProjScanSettings.ScanModule.STATICCODEANALYSIS)
        );
        model.setPatternMatchingEnabled(settings.getScanModules().contains(UnifiedAiProjScanSettings.ScanModule.PATTERNMATCHING));
        model.setSearchForConfigurationFlawsEnabled(settings.getScanModules().contains(UnifiedAiProjScanSettings.ScanModule.CONFIGURATION));
        model.setSearchForVulnerableComponentsEnabled(settings.getScanModules().contains(UnifiedAiProjScanSettings.ScanModule.COMPONENTS));

        return model;
    }

    /**
     * PT AI project creation is to be started with POST API call with base project settings. This method
     * uses default base settings as a template and applies AIPROJ scan settings to them
     *
     * @param settings        AIPROJ settings to be applied to default settings
     * @param defaultSettings Default settings that PT AI API provides
     */
    @SneakyThrows
    public static CreateProjectSettingsModel convert(
            @NonNull final UnifiedAiProjScanSettings settings,
            @NonNull final DefaultProjectSettingsModel defaultSettings) {

        CreateProjectSettingsModel result = new CreateProjectSettingsModel();
        result.setId(defaultSettings.getId());
        result.setName(defaultSettings.getName());
        result.setLanguages(defaultSettings.getLanguages());
        result.setProjectUrl(defaultSettings.getProjectUrl());
        result.setBlackBox(defaultSettings.getBlackBox());
        result.setBlackBoxEnabled(defaultSettings.getBlackBoxEnabled());
        result.setWhiteBox(defaultSettings.getWhiteBox());

        log.trace("Set base project settings");
        result.setName(settings.getProjectName());
        result.setLanguages(convertLanguagesGroup(settings.getProgrammingLanguages()));
        if (null != settings.getBlackBoxSettings())
            result.setProjectUrl(settings.getBlackBoxSettings().getSite());

        result.setWhiteBox(apply(settings, new WhiteBoxSettingsModel()));

        result.setBlackBoxEnabled(settings.getScanModules().contains(BLACKBOX));
        if (Boolean.TRUE.equals(result.getBlackBoxEnabled())) {
            log.trace("Set base project blackbox settings");
            result.setBlackBox(apply(settings, new BlackBoxSettingsBaseModel()));
        }
        return result;
    }

    /**
     * Method converts PT AI API version independent language to PT AI v.4.5 API programming language group
     *
     * @param languages PT AI API version independent language
     * @return PT AI v.4.5 API programming language group
     */
    @NonNull
    public static List<ProgrammingLanguageGroup> convertLanguagesGroup(@NonNull final Set<Language> languages) {
        return languages.stream().map(it -> REVERSE_LANGUAGE_GROUP_MAP.getOrDefault(it, ProgrammingLanguageGroup.NONE)).collect(Collectors.toList());
    }

    @SneakyThrows
    public static JavaSettingsModel apply(
            @NonNull final UnifiedAiProjScanSettings settings,
            @NonNull final JavaSettingsModel model) {
        if (null == settings.getJavaSettings()) return model;
        JavaSettings javaSettings = settings.getJavaSettings();
        // Set isUnpackUserJarFiles
        model.setUnpackUserPackages(javaSettings.getUnpackUserPackages());
        model.setDownloadDependencies(javaSettings.getDownloadDependencies());
        model.setUseAvailablePublicAndProtectedMethods(model.getUseAvailablePublicAndProtectedMethods());
        model.setVersion(JAVA_VERSION_MAP.getOrDefault(javaSettings.getJavaVersion(), JavaVersions._11));
        // Set userPackagePrefixes and launchJvmParameters
        model.setUserPackagePrefixes(javaSettings.getUserPackagePrefixes());
        model.setParameters(javaSettings.getParameters());
        model.setLaunchParameters(javaSettings.getCustomParameters());
        return model;
    }

    @SneakyThrows
    public static DotNetSettingsModel apply(
            @NonNull final UnifiedAiProjScanSettings settings,
            @NonNull final DotNetSettingsModel model) {
        if (null == settings.getWindowsDotNetSettings()) return model;
        UnifiedAiProjScanSettings.WindowsDotNetSettings dotNetSettings = settings.getWindowsDotNetSettings();
        // Set projectType
        model.setProjectType(DOTNET_PROJECT_TYPE_MAP.getOrDefault(dotNetSettings.getProjectType(), DotNetProjectType.NONE));
        model.setSolutionFile(dotNetSettings.getSolutionFile());
        model.setLaunchParameters(dotNetSettings.getCustomParameters());
        model.setDownloadDependencies(dotNetSettings.getDownloadDependencies());
        model.setUseAvailablePublicAndProtectedMethods(dotNetSettings.getUsePublicAnalysisMethod());
        return model;
    }

    @SneakyThrows
    public static JsaDotNetSettingsModel apply(
            @NonNull final UnifiedAiProjScanSettings settings,
            @NonNull final JsaDotNetSettingsModel model) {
        if (null == settings.getDotNetSettings()) return model;
        DotNetSettings dotNetSettings = settings.getDotNetSettings();
        // Set projectType
        model.setProjectType(DOTNET_PROJECT_TYPE_MAP.getOrDefault(dotNetSettings.getProjectType(), DotNetProjectType.NONE));
        model.setSolutionFile(dotNetSettings.getSolutionFile());
        model.setLaunchParameters(dotNetSettings.getCustomParameters());
        model.setDownloadDependencies(dotNetSettings.getDownloadDependencies());
        model.setUseAvailablePublicAndProtectedMethods(dotNetSettings.getUsePublicAnalysisMethod());
        return model;
    }

    @SneakyThrows
    public static GoSettingsModel apply(
            @NonNull final UnifiedAiProjScanSettings settings,
            @NonNull final GoSettingsModel model) {
        if (null == settings.getGoSettings()) return model;
        UnifiedAiProjScanSettings.GoSettings goSettings = settings.getGoSettings();

        model.setUseAvailablePublicAndProtectedMethods(goSettings.getUsePublicAnalysisMethod());
        model.setLaunchParameters(goSettings.getCustomParameters());
        return model;
    }

    @SneakyThrows
    public static JavaScriptSettingsModel apply(
            @NonNull final UnifiedAiProjScanSettings settings,
            @NonNull final JavaScriptSettingsModel model) {
        if (null == settings.getJavaScriptSettings()) return model;
        UnifiedAiProjScanSettings.JavaScriptSettings javaScriptSettings = settings.getJavaScriptSettings();

        model.setUseAvailablePublicAndProtectedMethods(javaScriptSettings.getUsePublicAnalysisMethod());
        model.setLaunchParameters(javaScriptSettings.getCustomParameters());
        model.setDownloadDependencies(javaScriptSettings.getDownloadDependencies());
        return model;
    }


    @SneakyThrows
    public static PhpSettingsModel apply(
            @NonNull final UnifiedAiProjScanSettings settings,
            @NonNull final PhpSettingsModel model) {
        if (null == settings.getPhpSettings()) return model;
        UnifiedAiProjScanSettings.PhpSettings phpSettings = settings.getPhpSettings();

        model.setUseAvailablePublicAndProtectedMethods(phpSettings.getUsePublicAnalysisMethod());
        model.setLaunchParameters(phpSettings.getCustomParameters());
        model.setDownloadDependencies(phpSettings.getDownloadDependencies());
        return model;
    }

    @SneakyThrows
    public static PythonSettingsModel apply(
            @NonNull final UnifiedAiProjScanSettings settings,
            @NonNull final PythonSettingsModel model) {
        if (null == settings.getPhpSettings()) return model;
        UnifiedAiProjScanSettings.PythonSettings pythonSettings = settings.getPythonSettings();

        model.setUseAvailablePublicAndProtectedMethods(pythonSettings.getUsePublicAnalysisMethod());
        model.setLaunchParameters(pythonSettings.getCustomParameters());
        model.setDownloadDependencies(pythonSettings.getDownloadDependencies());
        return model;
    }

    @SneakyThrows
    public static RubySettingsModel apply(
            @NonNull final UnifiedAiProjScanSettings settings,
            @NonNull final RubySettingsModel model) {
        if (null == settings.getRubySettings()) return model;
        UnifiedAiProjScanSettings.RubySettings rubySettings = settings.getRubySettings();

        model.setUseAvailablePublicAndProtectedMethods(rubySettings.getUsePublicAnalysisMethod());
        model.setLaunchParameters(rubySettings.getCustomParameters());
        return model;
    }

    @SneakyThrows
    public static PmTaintBaseSettingsModel apply(
            @NonNull final UnifiedAiProjScanSettings settings,
            @NonNull final PmTaintBaseSettingsModel model) {
        if (null == settings.getPmTaintSettings()) return model;
        UnifiedAiProjScanSettings.PmTaintSettings pmTaintSettings = settings.getPmTaintSettings();

        model.setUseAvailablePublicAndProtectedMethods(pmTaintSettings.getUsePublicAnalysisMethod());
        model.setLaunchParameters(pmTaintSettings.getCustomParameters());
        return model;
    }

    @SneakyThrows
    public static AnalysisRulesBaseModel apply(
            @NonNull final UnifiedAiProjScanSettings settings) {
        return new AnalysisRulesBaseModel()
                .pmRules(new PmRulesBaseModel().useRules(settings.isUseCustomPmRules()))
                .sastRules(new SastRulesBaseModel().useRules(settings.isUseSastRules()));
    }

    /**
     * Method sets project settings attributes using AIPROJ-defined ones
     */
    @SneakyThrows
    public static ProjectSettingsModel apply(
            @NonNull final UnifiedAiProjScanSettings settings,
            @NonNull final ProjectSettingsModel model) {
        log.trace("Set base project settings");

        model.setSourceType(SourceType.EMPTY);
        model.setProjectName(settings.getProjectName());
        model.setWhiteBoxSettings(apply(settings, new WhiteBoxSettingsModel()));
        model.setDotNetSettings(apply(settings, new DotNetSettingsModel()));
        model.setJsaDotNetSettings(apply(settings, new JsaDotNetSettingsModel()));
        model.setJavaSettings(apply(settings, new JavaSettingsModel()));
        model.setGoSettings(apply(settings, new GoSettingsModel()));
        model.setJavaScriptSettings(apply(settings, new JavaScriptSettingsModel()));
        model.setPhpSettings(apply(settings, new PhpSettingsModel()));
        model.setPythonSettings(apply(settings, new PythonSettingsModel()));
        model.setRubySettings(apply(settings, new RubySettingsModel()));
        model.setPmTaintSettings(apply(settings, new PmTaintBaseSettingsModel()));

        return model;
    }

    @SneakyThrows
    public static BlackBoxAuthenticationFullModel apply(
            @NonNull final BlackBoxSettings blackBoxSettings,
            @NonNull final BlackBoxAuthenticationFullModel destination) {
        destination.setType(AuthType.NONE);
        log.trace("Check if AIPROJ authentication field is defined");
        Authentication auth = blackBoxSettings.getAuthentication();
        if (null == auth || Authentication.Type.NONE == auth.getType()) return destination;
        destination.setType(BLACKBOX_AUTH_TYPE_MAP.getOrDefault(auth.getType(), AuthType.NONE));

        if (AuthType.FORM == destination.getType()) {
            BlackBoxFormAuthenticationModel formAuthModel;
            BlackBoxSettings.FormAuthentication formAuth;
            formAuth = (BlackBoxSettings.FormAuthentication) auth;
            if (DetectionType.AUTO == formAuth.getDetectionType())
                formAuthModel = new BlackBoxFormAuthenticationModel()
                        .formDetection(BLACKBOX_FORM_DETECTION_TYPE_MAP.get(formAuth.getDetectionType()))
                        .login(formAuth.getLogin())
                        .password(formAuth.getPassword())
                        .formAddress(formAuth.getFormAddress())
                        .validationTemplate(formAuth.getValidationTemplate());
            else
                formAuthModel = new BlackBoxFormAuthenticationModel()
                        .formDetection(BLACKBOX_FORM_DETECTION_TYPE_MAP.get(formAuth.getDetectionType()))
                        .loginKey(formAuth.getLoginKey())
                        .passwordKey(formAuth.getPasswordKey())
                        .login(formAuth.getLogin())
                        .password(formAuth.getPassword())
                        .formAddress(formAuth.getFormAddress())
                        .formXPath(formAuth.getXPath())
                        .validationTemplate(formAuth.getValidationTemplate());
            destination.setForm(formAuthModel);
        } else if (AuthType.HTTP == destination.getType()) {
            BlackBoxSettings.HttpAuthentication httpAuth;
            httpAuth = (BlackBoxSettings.HttpAuthentication) auth;
            BlackBoxHttpAuthenticationModel httpAuthModel = new BlackBoxHttpAuthenticationModel()
                    .login(httpAuth.getLogin())
                    .password(httpAuth.getPassword())
                    .validationAddress(httpAuth.getValidationAddress());
            destination.setHttp(httpAuthModel);
        } else if (AuthType.RAWCOOKIE == destination.getType()) {
            BlackBoxSettings.CookieAuthentication cookieAuth;
            cookieAuth = (BlackBoxSettings.CookieAuthentication) auth;
            BlackBoxRawCookieAuthenticationModel cookieAuthModel = new BlackBoxRawCookieAuthenticationModel()
                    .cookie(cookieAuth.getCookie())
                    .validationAddress(cookieAuth.getValidationAddress())
                    .validationTemplate(cookieAuth.getValidationTemplate());
            destination.setCookie(cookieAuthModel);
        }
        return destination;
    }

    @SneakyThrows
    protected static BlackBoxProxySettingsModel apply(
            @NonNull final ProxySettings source,
            @NonNull final BlackBoxProxySettingsModel destination) {
        destination.setIsActive(source.getEnabled());
        if (Boolean.FALSE.equals(destination.getIsActive())) return destination;
        destination.setType(BLACKBOX_PROXY_TYPE_MAP.get(source.getType()));
        destination.setHost(source.getHost());
        destination.setPort(source.getPort());
        destination.setLogin(source.getLogin());
        destination.setPassword(source.getPassword());
        return destination;
    }

    @SneakyThrows
    protected static BlackBoxProxySettingsModel apply(final ProxySettings source) {
        return null == source ? null : apply(source, new BlackBoxProxySettingsModel());
    }

    @SneakyThrows
    public static BlackBoxSettingsModel apply(
            @NonNull final UnifiedAiProjScanSettings settings,
            @NonNull final BlackBoxSettingsModel model) {
        BlackBoxSettings blackBoxSettings = settings.getBlackBoxSettings();
        if (null == blackBoxSettings || !settings.getScanModules().contains(BLACKBOX))
            return model;

        model.setRunAutocheckAfterScan(blackBoxSettings.getRunAutocheckAfterScan());
        model.setSite(blackBoxSettings.getSite());
        model.setIsActive(true);
        model.setLevel(BLACKBOX_SCAN_LEVEL_MAP.get(blackBoxSettings.getScanLevel()));
        model.setScanScope(BLACKBOX_SCAN_SCOPE_MAP.get(blackBoxSettings.getScanScope()));
        model.setSslCheck(blackBoxSettings.getSslCheck());
        if (isNotEmpty(blackBoxSettings.getHttpHeaders())) {
            log.trace("Set additional HTTP headers");
            List<HttpHeaderModel> headers = new ArrayList<>();
            for (Pair<String, String> header : settings.getBlackBoxSettings().getHttpHeaders())
                headers.add(new HttpHeaderModel().key(header.getKey()).value(header.getValue()));
            model.setAdditionalHttpHeaders(headers);
        }
        if (isNotEmpty(blackBoxSettings.getBlackListedAddresses())) {
            log.trace("Set blacklisted addresses");
            List<BlackBoxAddressModel> blackboxList = new ArrayList<>();

            for (AddressListItem address : blackBoxSettings.getBlackListedAddresses()) {
                blackboxList.add(new BlackBoxAddressModel()
                        .address(address.getAddress())
                        .format(BLACKBOX_ADDRESSLIST_ITEM_FORMAT_MAP.get(address.getFormat())));
            }
            model.setBlackListedAddresses(blackboxList);
        }
        if (isNotEmpty(blackBoxSettings.getBlackListedAddresses())) {
            log.trace("Set whitelisted addresses");
            List<BlackBoxAddressModel> blackboxList = new ArrayList<>();
            for (AddressListItem address : blackBoxSettings.getWhiteListedAddresses()) {
                blackboxList.add(new BlackBoxAddressModel()
                        .address(address.getAddress())
                        .format(BLACKBOX_ADDRESSLIST_ITEM_FORMAT_MAP.get(address.getFormat())));
            }
            model.setWhiteListedAddresses(blackboxList);
        }
        model.setAuthentication(apply(blackBoxSettings, new BlackBoxAuthenticationFullModel()));
        model.setProxySettings(null == blackBoxSettings.getProxySettings() ? null : apply(blackBoxSettings.getProxySettings()));
        return model;
    }

    @SneakyThrows
    public static BlackBoxSettingsBaseModel apply(
            @NonNull final UnifiedAiProjScanSettings settings,
            @NonNull final BlackBoxSettingsBaseModel model) {
        BlackBoxSettings blackBoxSettings = settings.getBlackBoxSettings();
        if (null == blackBoxSettings || !settings.getScanModules().contains(BLACKBOX))
            return model;

        model.setRunAutocheckAfterScan(blackBoxSettings.getRunAutocheckAfterScan());
        model.setSite(blackBoxSettings.getSite());
        model.setLevel(BLACKBOX_SCAN_LEVEL_MAP.get(blackBoxSettings.getScanLevel()));
        model.setScanScope(BLACKBOX_SCAN_SCOPE_MAP.get(blackBoxSettings.getScanScope()));
        model.setSslCheck(blackBoxSettings.getSslCheck());
        if (isNotEmpty(blackBoxSettings.getHttpHeaders())) {
            log.trace("Set additional HTTP headers");
            List<HttpHeaderModel> headers = new ArrayList<>();
            for (Pair<String, String> header : settings.getBlackBoxSettings().getHttpHeaders())
                headers.add(new HttpHeaderModel().key(header.getKey()).value(header.getValue()));
            model.setAdditionalHttpHeaders(headers);
        }
        if (isNotEmpty(blackBoxSettings.getBlackListedAddresses())) {
            log.trace("Set blacklisted addresses");
            List<BlackBoxAddressModel> blackboxList = new ArrayList<>();

            for (AddressListItem address : blackBoxSettings.getBlackListedAddresses()) {
                blackboxList.add(new BlackBoxAddressModel()
                        .address(address.getAddress())
                        .format(BLACKBOX_ADDRESSLIST_ITEM_FORMAT_MAP.get(address.getFormat())));
            }
            model.setBlackListedAddresses(blackboxList);
        }
        if (isNotEmpty(blackBoxSettings.getBlackListedAddresses())) {
            log.trace("Set whitelisted addresses");
            List<BlackBoxAddressModel> blackboxList = new ArrayList<>();
            for (AddressListItem address : blackBoxSettings.getWhiteListedAddresses()) {
                blackboxList.add(new BlackBoxAddressModel()
                        .address(address.getAddress())
                        .format(BLACKBOX_ADDRESSLIST_ITEM_FORMAT_MAP.get(address.getFormat())));
            }
            model.setWhiteListedAddresses(blackboxList);
        }
        model.setAuthentication(apply(blackBoxSettings, new BlackBoxAuthenticationFullModel()));
        model.setProxySettings(null == blackBoxSettings.getProxySettings() ? null : apply(blackBoxSettings.getProxySettings()));
        return model;
    }

    @SneakyThrows
    public static SecurityPoliciesModel apply(
            final Policy[] policy,
            @NonNull final SecurityPoliciesModel model) {
        model.setCheckSecurityPoliciesAccordance(null != policy && 0 != policy.length);
        model.setSecurityPolicies(Boolean.TRUE.equals(model.getCheckSecurityPoliciesAccordance()) ? JsonPolicyHelper.serialize(policy) : "");
        return model;
    }
}