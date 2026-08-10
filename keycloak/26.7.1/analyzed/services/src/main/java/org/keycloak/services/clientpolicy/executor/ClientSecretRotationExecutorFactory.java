package org.keycloak.services.clientpolicy.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.keycloak.Config;
import org.keycloak.Config.Scope;
import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 客户端密钥轮换执行器工厂。
 * <p>创建 {@link ClientSecretRotationExecutor}，并暴露密钥过期、轮换窗口等可配置项；需启用 {@link Feature#CLIENT_SECRET_ROTATION} 特性。</p>
 *
 * @author <a href="mailto:masales@redhat.com">Marcelo Sales</a>
 */
public class ClientSecretRotationExecutorFactory implements ClientPolicyExecutorProviderFactory,
    EnvironmentDependentProviderFactory {

    /** 执行器 Provider 标识符 */
    public static final String PROVIDER_ID = "secret-rotation";

    /** 配置键：密钥过期/轮换周期（秒） */
    public static final String SECRET_EXPIRATION_PERIOD = "expiration-period";
    /** 默认密钥过期周期：29 天（秒） */
    public static final Long DEFAULT_SECRET_EXPIRATION_PERIOD = TimeUnit.DAYS.toSeconds(29);

    /** 配置键：动态更新时触发自动轮换的剩余有效期阈值（秒） */
    public static final String SECRET_REMAINING_ROTATION_PERIOD = "remaining-rotation-period";
    /** 默认剩余轮换阈值：10 天（秒） */
    public static final Long DEFAULT_SECRET_REMAINING_ROTATION_PERIOD = TimeUnit.DAYS.toSeconds(10);

    /** 配置键：旧密钥在轮换后的保留有效期（秒） */
    public static final String SECRET_ROTATED_EXPIRATION_PERIOD = "rotated-expiration-period";
    /** 默认旧密钥保留期：2 天（秒） */
    public static final Long DEFAULT_SECRET_ROTATED_EXPIRATION_PERIOD = TimeUnit.DAYS.toSeconds(2);
    /** 管理控制台可配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    // 初始化密钥轮换相关配置项
    static {
        ProviderConfigProperty secretExpirationPeriod = new ProviderConfigProperty(
            SECRET_EXPIRATION_PERIOD, "Secret expiration",
            "When the secret is rotated. The time frequency for generating a new secret. (In seconds)",
            ProviderConfigProperty.STRING_TYPE, DEFAULT_SECRET_EXPIRATION_PERIOD);
        configProperties.add(secretExpirationPeriod);

        ProviderConfigProperty secretRotatedPeriod = new ProviderConfigProperty(
            SECRET_ROTATED_EXPIRATION_PERIOD, "Rotated Secret expiration",
            "When secret is rotated, this is the remaining expiration time for the old secret. This value should be always smaller than Secret expiration. When this is set to 0, the old secret will be immediately removed during client rotation (In seconds)",
            ProviderConfigProperty.STRING_TYPE, DEFAULT_SECRET_ROTATED_EXPIRATION_PERIOD);
        configProperties.add(secretRotatedPeriod);

        ProviderConfigProperty secretRemainingExpirationPeriod = new ProviderConfigProperty(
            SECRET_REMAINING_ROTATION_PERIOD, "Remain Expiration Time",
            "During dynamic client registration client-update request, the client secret will be automatically rotated if the remaining expiration time of the current secret is smaller than the value specified by this option. This configuration option is relevant only for dynamic client update requests (In seconds)",
            ProviderConfigProperty.STRING_TYPE, DEFAULT_SECRET_REMAINING_ROTATION_PERIOD);
        configProperties.add(secretRemainingExpirationPeriod);

    }

    /** @return 执行器说明（英文原文保留） */
    @Override
    public String getHelpText() {
        return "The executor verifies that secret rotation is enabled for the client. If rotation is enabled, it provides validation of secrets and performs rotation if necessary.";
    }

    /** @return 可配置属性列表 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /** @param session Keycloak 会话 @return 新的密钥轮换执行器实例 */
    @Override
    public ClientPolicyExecutorProvider create(KeycloakSession session) {
        return new ClientSecretRotationExecutor(session);
    }

    /** 工厂初始化（无全局配置） */
    @Override
    public void init(Scope config) {

    }

    /** 会话工厂就绪回调 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** 工厂关闭钩子 */
    @Override
    public void close() {

    }

    /** @return 执行器标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 是否启用 CLIENT_SECRET_ROTATION 特性 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Feature.CLIENT_SECRET_ROTATION);
    }
}
