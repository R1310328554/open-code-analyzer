/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.services.resources.admin.info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.IdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.Profile;
import org.keycloak.common.Version;
import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.crypto.CryptoProvider;
import org.keycloak.common.util.KeystoreUtil;
import org.keycloak.component.ComponentFactory;
import org.keycloak.crypto.ClientSignatureVerifierProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.policy.PasswordPolicyProvider;
import org.keycloak.policy.PasswordPolicyProviderFactory;
import org.keycloak.protocol.ClientInstallationProvider;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.LoginProtocolFactory;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.scope.ParameterizedScopeTypeProvider;
import org.keycloak.provider.ConfiguredPerClientProvider;
import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.ServerInfoAwareProviderFactory;
import org.keycloak.provider.Spi;
import org.keycloak.representations.idm.ComponentTypeRepresentation;
import org.keycloak.representations.idm.PasswordPolicyTypeRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.ProtocolMapperTypeRepresentation;
import org.keycloak.representations.info.ClientInstallationRepresentation;
import org.keycloak.representations.info.CpuInfoRepresentation;
import org.keycloak.representations.info.CryptoInfoRepresentation;
import org.keycloak.representations.info.FeatureRepresentation;
import org.keycloak.representations.info.FeatureType;
import org.keycloak.representations.info.MemoryInfoRepresentation;
import org.keycloak.representations.info.ParameterizedScopeTypeRepresentation;
import org.keycloak.representations.info.ProfileInfoRepresentation;
import org.keycloak.representations.info.ProviderRepresentation;
import org.keycloak.representations.info.ServerInfoRepresentation;
import org.keycloak.representations.info.SpiInfoRepresentation;
import org.keycloak.representations.info.SystemInfoRepresentation;
import org.keycloak.representations.info.ThemeInfoRepresentation;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.fgap.AdminPermissions;
import org.keycloak.theme.Theme;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.NoCache;

/**
 * 管理端服务器信息 REST 资源。
 * <p>暴露主题、身份提供者、协议映射器、密码策略、特性开关、加密算法及 SPI 提供者等运行时元数据，
 * 供 Admin Console 与客户端发现可用组件。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN , value = "")
public class ServerInfoAdminResource {

    /** 预构建的枚举值映射（事件类型、操作类型、资源类型、组类型） */
    private static final Map<String, List<String>> ENUMS = createEnumsMap(EventType.class, OperationType.class, ResourceType.class, GroupModel.Type.class);

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 当前管理认证上下文 */
    private final AdminAuth auth;

    /** 构造服务器信息资源。 */
    public ServerInfoAdminResource(KeycloakSession session, AdminAuth auth) {
        this.session = session;
        this.auth = auth;
    }

    /**
     * 获取服务器可用主题、社交/身份提供者、协议映射器及密码策略等元信息。
     * <p>拥有 realm 管理权限时可附加系统/CPU/内存信息。</p>
     *
     * @return 服务器信息表示
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROOT)
    @Operation( summary = "Get themes, social providers, auth providers, and event listeners available on this server")
    public ServerInfoRepresentation getInfo() {
        ServerInfoRepresentation info = new ServerInfoRepresentation();
        RealmModel userRealm = session.getContext().getRealm();
        AdminPermissionEvaluator adminEvaluator = AdminPermissions.evaluator(session, userRealm, auth);

        if (adminEvaluator.realm().canManageRealm()) {
            if (RealmManager.isAdministrationRealm(userRealm)) {
                info.setSystemInfo(SystemInfoRepresentation.create(session.getKeycloakSessionFactory().getServerStartupTimestamp(), Version.VERSION));
                info.setCpuInfo(CpuInfoRepresentation.create());
                info.setMemoryInfo(MemoryInfoRepresentation.create());
            } else {
                // 非管理 realm 时仅返回版本信息
                SystemInfoRepresentation systemInfo = new SystemInfoRepresentation();
                systemInfo.setVersion(Version.VERSION);
                info.setSystemInfo(systemInfo);
            }
        }

        info.setProfileInfo(createProfileInfo());
        info.setFeatures(createFeatureRepresentations());

        // true 表示非对称算法，false 表示对称算法
        Map<Boolean, List<String>> algorithms = session.getAllProviders(ClientSignatureVerifierProvider.class).stream()
                        .collect(
                                Collectors.toMap(
                                        ClientSignatureVerifierProvider::isAsymmetricAlgorithm,
                                        clientSignatureVerifier -> Collections.singletonList(clientSignatureVerifier.getAlgorithm()),
                                        (l1, l2) -> {
                                            List<String> result = listCombiner(l1, l2);
                                            return result.stream()
                                                    .sorted()
                                                    .collect(Collectors.toList());
                                        },
                                        HashMap::new
                                )
                        );
        info.setCryptoInfo(createCryptoInfo(algorithms.get(false), algorithms.get(true)));

        setSocialProviders(info);
        setIdentityProviders(info);
        setThemes(info);
        setProviders(info);
        setProtocolMapperTypes(info);
        setBuiltinProtocolMappers(info);
        setClientInstallations(info);
        setPasswordPolicies(info);
        info.setEnums(ENUMS);
        info.setParameterizedScopeTypes(buildParameterizedScopeTypesList());
        return info;
    }

    /** 构建参数化 scope 类型列表。 */
    private List<ParameterizedScopeTypeRepresentation> buildParameterizedScopeTypesList() {
        return session.getKeycloakSessionFactory()
                .getProviderFactoriesStream(ParameterizedScopeTypeProvider.class)
                .map(f -> {
                    ParameterizedScopeTypeProvider provider = session.getProvider(ParameterizedScopeTypeProvider.class, f.getId());
                    return new ParameterizedScopeTypeRepresentation(provider.getTypeName(), provider.isRepeatable());
                })
                .collect(Collectors.toList());
    }

    /** 收集全部 SPI 及其提供者元数据。 */
    private void setProviders(ServerInfoRepresentation info) {
        info.setComponentTypes(new HashMap<>());
        LinkedHashMap<String, SpiInfoRepresentation> spiReps = new LinkedHashMap<>();

        List<Spi> spis = new LinkedList<>(session.getKeycloakSessionFactory().getSpis());
        Collections.sort(spis, new Comparator<Spi>() {
            @Override
            public int compare(Spi s1, Spi s2) {
                return s1.getName().compareTo(s2.getName());
            }
        });

        for (Spi spi : spis) {
            SpiInfoRepresentation spiRep = new SpiInfoRepresentation();
            spiRep.setInternal(spi.isInternal());

            List<String> providerIds = new LinkedList<>(session.listProviderIds(spi.getProviderClass()));
            Collections.sort(providerIds);

            Map<String, ProviderRepresentation> providers = new HashMap<>();

            for (String name : providerIds) {
                ProviderRepresentation provider = new ProviderRepresentation();
                ProviderFactory<?> pi = session.getKeycloakSessionFactory().getProviderFactory(spi.getProviderClass(), name);
                provider.setOrder(pi.order());
                if (ServerInfoAwareProviderFactory.class.isAssignableFrom(pi.getClass())) {
                    provider.setOperationalInfo(((ServerInfoAwareProviderFactory) pi).getOperationalInfo());
                }
                if (pi instanceof ConfiguredProvider) {
                    ComponentTypeRepresentation rep = new ComponentTypeRepresentation();
                    rep.setId(pi.getId());
                    ConfiguredProvider configured = (ConfiguredProvider)pi;
                    rep.setHelpText(configured.getHelpText());
                    List<ProviderConfigProperty> configProperties = configured.getConfigProperties();
                    if (configProperties == null) configProperties = Collections.EMPTY_LIST;
                    rep.setProperties(ModelToRepresentation.toRepresentation(configProperties));
                    if (pi instanceof ComponentFactory) {
                        rep.setMetadata(((ComponentFactory)pi).getTypeMetadata());
                    }
                    if (pi instanceof ConfiguredPerClientProvider) {
                        List<ProviderConfigProperty> configClientProperties = ((ConfiguredPerClientProvider) pi).getConfigPropertiesPerClient();
                        rep.setClientProperties(ModelToRepresentation.toRepresentation(configClientProperties));
                    }
                    List<ComponentTypeRepresentation> reps = info.getComponentTypes().get(spi.getProviderClass().getName());
                    if (reps == null) {
                        reps = new LinkedList<>();
                        info.getComponentTypes().put(spi.getProviderClass().getName(), reps);
                    }
                    reps.add(rep);
                }
                providers.put(name, provider);
            }
            spiRep.setProviders(providers);

            spiReps.put(spi.getName(), spiRep);
        }
        info.setProviders(spiReps);
    }

    /** 收集各类型可用主题及其语言区域。 */
    private void setThemes(ServerInfoRepresentation info) {
        info.setThemes(new HashMap<>());

        for (Theme.Type type : Theme.Type.values()) {
            List<String> themeNames = filterThemes(type, new LinkedList<>(session.theme().nameSet(type)));
            Collections.sort(themeNames);

            List<ThemeInfoRepresentation> themes = new LinkedList<>();
            info.getThemes().put(type.toString().toLowerCase(), themes);

            for (String name : themeNames) {
                try {
                    Theme theme = session.theme().getTheme(name, type);
                    // 名称不一致表示主题未找到并已回退至默认主题
                    // 排除仅可继承的抽象主题（如 base）
                    if (theme != null && name.equals(theme.getName()) && !theme.isAbstract()) {
                        ThemeInfoRepresentation ti = new ThemeInfoRepresentation();
                        ti.setName(name);

                        String locales = theme.getProperties().getProperty("locales");
                        if (locales != null) {
                            ti.setLocales(locales.replaceAll(" ", "").split(","));
                        }

                        ti.setDescription(getThemeDescription(theme));

                        themes.add(ti);
                    }
                } catch (IOException e) {
                    throw new WebApplicationException("Failed to load themes", e);
                }
            }
        }
    }

    /** 从主题消息中获取本地化描述。 */
    private String getThemeDescription(Theme theme) throws IOException {
        Locale locale = session.getContext().resolveLocale(null);

        Properties enhancedMessages = theme.getEnhancedMessages(session.getContext().getRealm(), locale);
        if (enhancedMessages == null) {
            return null;
        }

        String descriptionKey = "theme." + theme.getName() + "." + theme.getType().name().toLowerCase(Locale.ROOT) + ".description";
        return enhancedMessages.getProperty(descriptionKey);
    }

    /** 按已启用特性过滤 v2/v3 主题名称。 */
    private LinkedList<String> filterThemes(Theme.Type type, LinkedList<String> themeNames) {
        LinkedList<String> filteredNames = new LinkedList<>(themeNames);
        boolean filterAdminV2 = (type == Theme.Type.ADMIN) &&
                !Profile.isFeatureEnabled(Profile.Feature.ADMIN_V2);
        boolean filterLoginV2 = (type == Theme.Type.LOGIN) &&
                !Profile.isFeatureEnabled(Profile.Feature.LOGIN_V2);

        if (filterAdminV2 || filterLoginV2) {
            filteredNames.remove("keycloak.v2");
            filteredNames.remove("rh-sso.v2");
        }

        boolean filterAccountV3 = (type == Theme.Type.ACCOUNT) &&
            !Profile.isFeatureEnabled(Profile.Feature.ACCOUNT_V3);

        if (filterAccountV3) {
            filteredNames.remove("keycloak.v3");
        }

        return filteredNames;
    }

    /** 收集社交身份提供者列表。 */
    private void setSocialProviders(ServerInfoRepresentation info) {
        info.setSocialProviders(new LinkedList<>());
        Stream<ProviderFactory> providerFactories = session.getKeycloakSessionFactory().getProviderFactoriesStream(SocialIdentityProvider.class);
        setIdentityProviders(providerFactories, info.getSocialProviders(), "Social");
    }

    /** 收集用户定义与社交身份提供者列表。 */
    private void setIdentityProviders(ServerInfoRepresentation info) {
        info.setIdentityProviders(new LinkedList<>());
        Stream<ProviderFactory> providerFactories = session.getKeycloakSessionFactory().getProviderFactoriesStream(IdentityProvider.class);
        setIdentityProviders(providerFactories, info.getIdentityProviders(), "User-defined");

        providerFactories = session.getKeycloakSessionFactory().getProviderFactoriesStream(SocialIdentityProvider.class);
        setIdentityProviders(providerFactories, info.getIdentityProviders(), "Social");
    }

    /** 将提供者工厂流转换为带分组名的 IdP 映射列表。 */
    public void setIdentityProviders(Stream<ProviderFactory> factories, List<Map<String, String>> providers, String groupName) {
        List<Map<String, String>> providerMaps = factories
                .map(IdentityProviderFactory.class::cast)
                .map(factory -> {
                    Map<String, String> data = new HashMap<>();
                    data.put("groupName", groupName);
                    data.put("name", factory.getName());
                    data.put("id", factory.getId());
                    return data;
                })
                .collect(Collectors.toList());

        providers.addAll(providerMaps);
    }

    /** 收集各协议的客户端安装适配器信息。 */
    private void setClientInstallations(ServerInfoRepresentation info) {
        HashMap<String, List<ClientInstallationRepresentation>> clientInstallations = session.getKeycloakSessionFactory()
                .getProviderFactoriesStream(ClientInstallationProvider.class)
                .map(ClientInstallationProvider.class::cast)
                .collect(
                        Collectors.toMap(
                                ClientInstallationProvider::getProtocol,
                                this::toClientInstallationRepresentation,
                                (l1, l2) -> listCombiner(l1, l2),
                                HashMap::new
                        )
                );
        info.setClientInstallations(clientInstallations);

    }

    /** 收集各协议可用的协议映射器类型。 */
    private void setProtocolMapperTypes(ServerInfoRepresentation info) {
        HashMap<String, List<ProtocolMapperTypeRepresentation>> protocolMappers = session.getKeycloakSessionFactory()
                .getProviderFactoriesStream(ProtocolMapper.class)
                .map(ProtocolMapper.class::cast)
                .collect(
                        Collectors.toMap(
                                ProtocolMapper::getProtocol,
                                this::toProtocolMapperTypeRepresentation,
                                (l1, l2) -> listCombiner(l1, l2),
                                HashMap::new
                        )
                );
        info.setProtocolMapperTypes(protocolMappers);
    }

    /** 收集各登录协议的内置协议映射器。 */
    private void setBuiltinProtocolMappers(ServerInfoRepresentation info) {
        Map<String, List<ProtocolMapperRepresentation>> protocolMappers = session.getKeycloakSessionFactory()
                .getProviderFactoriesStream(LoginProtocol.class)
                .collect(Collectors.toMap(
                        p -> p.getId(),
                        p -> {
                            LoginProtocolFactory factory = (LoginProtocolFactory) p;
                            return factory.getBuiltinMappers().values().stream()
                                    .map(ModelToRepresentation::toRepresentation)
                                    .collect(Collectors.toList());
                        })
                );
        info.setBuiltinProtocolMappers(protocolMappers);
    }

    /** 收集可用密码策略类型及其配置元数据。 */
    private void setPasswordPolicies(ServerInfoRepresentation info) {
        List<PasswordPolicyTypeRepresentation> passwordPolicyTypes= session.getKeycloakSessionFactory().getProviderFactoriesStream(PasswordPolicyProvider.class)
                .map(PasswordPolicyProviderFactory.class::cast)
                .map(factory -> {
                    PasswordPolicyTypeRepresentation rep = new PasswordPolicyTypeRepresentation();
                    rep.setId(factory.getId());
                    rep.setDisplayName(factory.getDisplayName());
                    rep.setConfigType(factory.getConfigType());
                    rep.setDefaultValue(factory.getDefaultConfigValue());
                    rep.setMultipleSupported(factory.isMultiplSupported());
                    return rep;
                })
                .collect(Collectors.toList());
        info.setPasswordPolicies(passwordPolicyTypes);
    }

    private List<ClientInstallationRepresentation> toClientInstallationRepresentation(ClientInstallationProvider provider) {
        ClientInstallationRepresentation rep = new ClientInstallationRepresentation();
        rep.setId(provider.getId());
        rep.setHelpText(provider.getHelpText());
        rep.setDisplayType( provider.getDisplayType());
        rep.setProtocol( provider.getProtocol());
        rep.setDownloadOnly( provider.isDownloadOnly());
        rep.setFilename(provider.getFilename());
        rep.setMediaType(provider.getMediaType());
        return Arrays.asList(rep);
    }

    private List<ProtocolMapperTypeRepresentation> toProtocolMapperTypeRepresentation(ProtocolMapper mapper) {
        ProtocolMapperTypeRepresentation rep = new ProtocolMapperTypeRepresentation();
        rep.setId(mapper.getId());
        rep.setName(mapper.getDisplayType());
        rep.setHelpText(mapper.getHelpText());
        rep.setCategory(mapper.getDisplayCategory());
        rep.setPriority(mapper.getPriority());
        List<ProviderConfigProperty> configProperties = mapper.getConfigProperties();
        rep.setProperties(ModelToRepresentation.toRepresentation(configProperties));
        return Arrays.asList(rep);
    }

    private static <T> List<T> listCombiner(List<T> list1, List<T> list2) {
        return Stream.concat(list1.stream(), list2.stream()).collect(Collectors.toList());
    }

    /** 将枚举类转换为 camelCase 键名到枚举值列表的映射。 */
    private static Map<String, List<String>> createEnumsMap(Class... enums) {
        Map<String, List<String>> m = new HashMap<>();
        for (Class e : enums) {
            String n = e.getSimpleName();
            n = Character.toLowerCase(n.charAt(0)) + n.substring(1);

            List<String> l = new LinkedList<>();
            for (Object c :  e.getEnumConstants()) {
                l.add(c.toString());
            }
            Collections.sort(l);

            m.put(n, l);
        }
        return m;
    }

    /** 构建当前 Profile 信息（名称及禁用/预览/实验特性）。 */
    private ProfileInfoRepresentation createProfileInfo() {
        ProfileInfoRepresentation info = new ProfileInfoRepresentation();

        Profile profile = Profile.getInstance();

        info.setName(profile.getName().name().toLowerCase());
        info.setDisabledFeatures(names(profile.getDisabledFeatures()));
        info.setPreviewFeatures(names(profile.getPreviewFeatures()));
        info.setExperimentalFeatures(names(profile.getExperimentalFeatures()));

        return info;
    }

    private static List<String> names(Set<Profile.Feature> featureSet) {
        List<String> l = new LinkedList();
        for (Profile.Feature f : featureSet) {
            l.add(f.name());
        }
        return l;
    }


    private static FeatureRepresentation getFeatureRep(Profile.Feature feature, boolean isEnabled) {
        FeatureRepresentation featureRep = new FeatureRepresentation();
        featureRep.setName(feature.name());
        featureRep.setLabel(feature.getLabel());
        featureRep.setType(FeatureType.valueOf(feature.getType().name()));
        featureRep.setEnabled(isEnabled);
        featureRep.setDeprecated(feature.isDeprecated());
        featureRep.setDependencies(feature.getDependencies() != null ?
                feature.getDependencies().stream().map(Enum::name).collect(Collectors.toSet()) : Collections.emptySet());
        return featureRep;
    }

    /** 构建全部特性开关表示列表。 */
    private static List<FeatureRepresentation> createFeatureRepresentations() {
        List<FeatureRepresentation> featureRepresentationList = new ArrayList<>();
        Profile profile = Profile.getInstance();
        final Map<Profile.Feature, Boolean> features = profile.getFeatures();
        features.forEach((f, enabled) -> featureRepresentationList.add(getFeatureRep(f, enabled)));
        return featureRepresentationList;
    }

    /** 构建加密提供者及客户端签名算法信息。 */
    private static CryptoInfoRepresentation createCryptoInfo(List<String> clientSignatureSymmetricAlgorithms, List<String> clientSignatureAsymmetricAlgorithms) {
        CryptoInfoRepresentation info = new CryptoInfoRepresentation();

        CryptoProvider cryptoProvider = CryptoIntegration.getProvider();
        info.setCryptoProvider(cryptoProvider.getClass().getSimpleName());
        info.setSupportedKeystoreTypes(CryptoIntegration.getProvider().getSupportedKeyStoreTypes()
                .map(KeystoreUtil.KeystoreFormat::toString)
                .collect(Collectors.toList()));
        info.setClientSignatureSymmetricAlgorithms(clientSignatureSymmetricAlgorithms);
        info.setClientSignatureAsymmetricAlgorithms(clientSignatureAsymmetricAlgorithms);

        return info;
    }

}
