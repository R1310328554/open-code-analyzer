package org.keycloak.services.clientpolicy.executor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.keycloak.common.util.Time;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSecretConstants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.oidc.OIDCClientSecretConfigWrapper;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.ClientCRUDContext;
import org.keycloak.services.clientpolicy.context.ClientSecretRotationContext;
import org.keycloak.services.clientpolicy.context.DynamicClientUpdatedContext;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.logging.Logger;

import static org.keycloak.services.clientpolicy.executor.ClientSecretRotationExecutorFactory.DEFAULT_SECRET_EXPIRATION_PERIOD;
import static org.keycloak.services.clientpolicy.executor.ClientSecretRotationExecutorFactory.DEFAULT_SECRET_REMAINING_ROTATION_PERIOD;
import static org.keycloak.services.clientpolicy.executor.ClientSecretRotationExecutorFactory.DEFAULT_SECRET_ROTATED_EXPIRATION_PERIOD;


/**
 * 客户端密钥轮换 Executor：为机密客户端自动设置密钥过期时间并在到期前轮换 secret。
 * <p>监听 REGISTERED/UPDATED、AUTHORIZATION_REQUEST、TOKEN_REQUEST 等事件，维护主密钥与过渡密钥（rotated secret）的生命周期。</p>
 *
 * @author <a href="mailto:masales@redhat.com">Marcelo Sales</a>
 */
public class ClientSecretRotationExecutor implements
        ClientPolicyExecutorProvider<ClientSecretRotationExecutor.Configuration> {

    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(ClientSecretRotationExecutor.class);
    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** 密钥轮换策略配置（过期周期、过渡窗口等）。 */
    private Configuration configuration;

    /** @param session Keycloak 会话 */
    public ClientSecretRotationExecutor(KeycloakSession session) {
        this.session = session;
    }

    /** {@inheritDoc} @return {@link ClientSecretRotationExecutorFactory#PROVIDER_ID} */
    @Override
    public String getProviderId() {
        return ClientSecretRotationExecutorFactory.PROVIDER_ID;
    }

    /** {@inheritDoc} @return {@link Configuration} */
    @Override
    public Class<Configuration> getExecutorConfigurationClass() {
        return Configuration.class;
    }

    /** {@inheritDoc} 按事件类型触发密钥过期初始化或轮换逻辑 */
    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        switch (context.getEvent()) {
            case REGISTERED:
            case UPDATED:
                if(isClientWithSecret(session.getContext().getClient())) {
                    session.setAttribute(ClientSecretConstants.CLIENT_SECRET_ROTATION_ENABLED, Boolean.TRUE);
                    executeOnClientCreateOrUpdate((ClientCRUDContext) context);
                }
                break;
            case AUTHORIZATION_REQUEST:
            case TOKEN_REQUEST:
                if(isClientWithSecret(session.getContext().getClient())) {
                    session.setAttribute(ClientSecretConstants.CLIENT_SECRET_ROTATION_ENABLED, Boolean.TRUE);
                    executeOnAuthRequest();
                }
                break;
            default:
                return;
        }
    }

    /** {@inheritDoc} 解析配置并为空值填充默认过期周期 */
    @Override
    public void setupConfiguration(ClientSecretRotationExecutor.Configuration config) {

        if (config == null) {
            configuration = new Configuration().parseWithDefaultValues();
        } else {
            configuration = config.parseWithDefaultValues();
        }

    }

    /** @return 是否为需 client secret 的机密客户端（非 public/bearer-only） */
    private boolean isClientWithSecret(ClientModel client) {
        if (client == null) return false;
        return (!client.isPublicClient() && !client.isBearerOnly());
    }

    /** 首次授权/令牌请求时为客户端设置密钥过期时间。 */
    private void executeOnAuthRequest() {
        ClientModel client = session.getContext().getClient();
        OIDCClientSecretConfigWrapper wrapper = OIDCClientSecretConfigWrapper.fromClientModel(
                client);

        if (!wrapper.hasClientSecretExpirationTime()) {
            // 策略启用后首次登录：初始化密钥过期时间
            updatedSecretExpiration(wrapper);
        }

    }

    /** 客户端创建/更新或动态更新时评估是否需要轮换密钥。 */
    private void executeOnClientCreateOrUpdate(ClientCRUDContext adminContext) {
        OIDCClientSecretConfigWrapper clientConfigWrapper = OIDCClientSecretConfigWrapper.fromClientModel(
                adminContext.getTargetClient());
        logger.debugv("Executing policy {0} for client {1}-{2} with configuration [ expirationPeriod: {3}, rotatedExpirationPeriod: {4}, remainExpirationPeriod: {5} ]", getName(), clientConfigWrapper.getId(), clientConfigWrapper.getName(), configuration.getExpirationPeriod(), configuration.getRotatedExpirationPeriod(), configuration.getRemainExpirationPeriod());
        if (adminContext instanceof ClientSecretRotationContext
                || clientConfigWrapper.isClientSecretExpired()
                || !clientConfigWrapper.hasClientSecretExpirationTime()) {
            rotateSecret(adminContext, clientConfigWrapper);
        } else {

            if (adminContext instanceof DynamicClientUpdatedContext) {
                long startRemainingWindow = clientConfigWrapper.getClientSecretExpirationTime()
                        - configuration.remainExpirationPeriod;

                debugDynamicInfo(clientConfigWrapper, startRemainingWindow);

                if (Time.currentTimeSeconds() >= startRemainingWindow) {
                    logger.debugv("Executing rotation for the dynamic client {0} due to remaining expiration time that starts at {1}", adminContext.getTargetClient().getClientId(), new Date(TimeUnit.SECONDS.toMillis(startRemainingWindow)));
                    rotateSecret(adminContext, clientConfigWrapper);
                }
            }
        }
    }

    /** 调试输出动态客户端密钥过期与剩余窗口时间。 */
    private void debugDynamicInfo(OIDCClientSecretConfigWrapper clientConfigWrapper, long startRemainingWindow) {
        if (logger.isDebugEnabled()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            logger.debugv("client expiration time: {0}, remaining time: {1}, current time: {2}, Time offset: {3}", clientConfigWrapper.getClientSecretExpirationTime(), startRemainingWindow, Time.currentTimeSeconds(), Time.getOffset());
            logger.debugv("client expiration date: {0}, window remaining date: {1}, current date: {2}", sdf.format(new Date(TimeUnit.SECONDS.toMillis(clientConfigWrapper.getClientSecretExpirationTime()))), sdf.format(new Date(TimeUnit.SECONDS.toMillis(startRemainingWindow))), sdf.format(new Date(Time.currentTimeMillis())));
        }
    }

    /** 执行密钥轮换：保存旧 secret 为过渡密钥并生成新主密钥。 */
    private void rotateSecret(ClientCRUDContext crudContext,
                              OIDCClientSecretConfigWrapper clientConfigWrapper) {

        if (crudContext instanceof ClientSecretRotationContext secretRotationContext) {
            if (secretRotationContext.isForceRotation()) {
                logger.debugv("Force rotation for client {0}", clientConfigWrapper.getId());
                updateRotateSecret(clientConfigWrapper, secretRotationContext.getCurrentSecret());
                updateClientConfigProperties(clientConfigWrapper);
            }
        } else if (!clientConfigWrapper.hasClientSecretExpirationTime()) {
            logger.debugv("client {0} has no secret rotation expiration time configured", clientConfigWrapper.getId());
            updatedSecretExpiration(clientConfigWrapper);
        } else {
            logger.debugv("Execute typical secret rotation for client {0}", clientConfigWrapper.getId());
            updatedSecretExpiration(clientConfigWrapper);
            updateRotateSecret(clientConfigWrapper, clientConfigWrapper.getSecret());
            KeycloakModelUtils.generateSecret(crudContext.getTargetClient());
            updateClientConfigProperties(clientConfigWrapper);
        }

        if (Objects.nonNull(crudContext.getProposedClientRepresentation())) {
            clientConfigWrapper.updateClientRepresentationAttributes(
                    crudContext.getProposedClientRepresentation());
        }

        logger.debugv("Client configured: {0}", clientConfigWrapper.toJson());
    }

    /** 按配置的主密钥过期周期更新 client secret 过期时间戳。 */
    private void updatedSecretExpiration(OIDCClientSecretConfigWrapper clientConfigWrapper) {
        clientConfigWrapper.setClientSecretExpirationTime(
                Time.currentTimeSeconds() + configuration.getExpirationPeriod());
        logger.debugv("A new secret expiration is configured for client {0}. Expires at {1}", clientConfigWrapper.getId(), new Date(TimeUnit.SECONDS.toMillis(clientConfigWrapper.getClientSecretExpirationTime())));
    }

    /** 更新密钥创建时间并刷新主密钥过期时间。 */
    private void updateClientConfigProperties(OIDCClientSecretConfigWrapper clientConfigWrapper) {
        clientConfigWrapper.setClientSecretCreationTime(Time.currentTimeSeconds());
        updatedSecretExpiration(clientConfigWrapper);
    }

    /** 将旧 secret 写入过渡密钥字段并设置其独立过期时间。 */
    private void updateRotateSecret(OIDCClientSecretConfigWrapper clientConfigWrapper,
                                    String secret) {
        if (configuration.rotatedExpirationPeriod > 0) {
            clientConfigWrapper.setClientRotatedSecret(secret);
            clientConfigWrapper.setClientRotatedSecretCreationTime();
            clientConfigWrapper.setClientRotatedSecretExpirationTime(
                    Time.currentTimeSeconds() + configuration.getRotatedExpirationPeriod());
            logger.debugv("Rotating the secret for client {0}. Secret creation at {1}. Secret expiration at {2}", clientConfigWrapper.getId(), new Date(TimeUnit.SECONDS.toMillis(clientConfigWrapper.getClientRotatedSecretCreationTime())), new Date(TimeUnit.SECONDS.toMillis(clientConfigWrapper.getClientRotatedSecretExpirationTime())));
        } else {
            logger.debugv("Removing rotation for client {0}", clientConfigWrapper.getId());
            clientConfigWrapper.setClientRotatedSecret(null);
            clientConfigWrapper.setClientRotatedSecretCreationTime(null);
            clientConfigWrapper.setClientRotatedSecretExpirationTime(null);
        }
    }

    /** 客户端密钥轮换 Executor 配置。 */
    public static class Configuration extends ClientPolicyExecutorConfigurationRepresentation {

        /** 主 client secret 有效期（秒）。 */
        @JsonProperty(ClientSecretRotationExecutorFactory.SECRET_EXPIRATION_PERIOD)
        protected Long expirationPeriod;
        /** 到期前开始允许轮换的剩余时间窗口（秒）。 */
        @JsonProperty(ClientSecretRotationExecutorFactory.SECRET_REMAINING_ROTATION_PERIOD)
        protected Long remainExpirationPeriod;
        /** 过渡（旧）secret 在轮换后仍有效的时长（秒）。 */
        @JsonProperty(ClientSecretRotationExecutorFactory.SECRET_ROTATED_EXPIRATION_PERIOD)
        private Long rotatedExpirationPeriod;

        /** {@inheritDoc} 校验过期周期为正且过渡/剩余窗口不超过主密钥周期 */
        @Override
        public boolean validateConfig() {
            logger.debugv("Validating configuration: [ expirationPeriod: {0}, rotatedExpirationPeriod: {1}, remainExpirationPeriod: {2} ]", expirationPeriod, rotatedExpirationPeriod, remainExpirationPeriod);
            // 主密钥过期时间必须大于 0（秒）
            if (expirationPeriod <= 0) {
                return false;
            }

            // 过渡 secret 有效期不得长于主密钥
            if (rotatedExpirationPeriod > expirationPeriod) {
                return false;
            }

            // 剩余轮换窗口不得长于主密钥有效期
            if (remainExpirationPeriod > expirationPeriod) {
                return false;
            }

            return true;
        }

        /** @return 主密钥过期周期（秒） */
        public Long getExpirationPeriod() {
            return expirationPeriod;
        }

        /** @param expirationPeriod 主密钥过期周期（秒） */
        public void setExpirationPeriod(Long expirationPeriod) {
            this.expirationPeriod = expirationPeriod;
        }

        /** @return 剩余轮换窗口（秒） */
        public Long getRemainExpirationPeriod() {
            return remainExpirationPeriod;
        }

        /** @param remainExpirationPeriod 剩余轮换窗口（秒） */
        public void setRemainExpirationPeriod(Long remainExpirationPeriod) {
            this.remainExpirationPeriod = remainExpirationPeriod;
        }

        /** @return 过渡 secret 有效期（秒） */
        public Long getRotatedExpirationPeriod() {
            return rotatedExpirationPeriod;
        }

        /** @param rotatedExpirationPeriod 过渡 secret 有效期（秒） */
        public void setRotatedExpirationPeriod(Long rotatedExpirationPeriod) {
            this.rotatedExpirationPeriod = rotatedExpirationPeriod;
        }

        /** 为未设置的配置项填充 Factory 定义的默认值。 */
        public Configuration parseWithDefaultValues() {
            if (getExpirationPeriod() == null) {
                setExpirationPeriod(DEFAULT_SECRET_EXPIRATION_PERIOD);
            }

            if (getRemainExpirationPeriod() == null) {
                setRemainExpirationPeriod(DEFAULT_SECRET_REMAINING_ROTATION_PERIOD);
            }

            if (getRotatedExpirationPeriod() == null) {
                setRotatedExpirationPeriod(DEFAULT_SECRET_ROTATED_EXPIRATION_PERIOD);
            }

            return this;
        }
    }

}
