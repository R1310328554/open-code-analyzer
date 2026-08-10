package org.keycloak.protocol.oidc;

import java.io.InvalidObjectException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.keycloak.common.util.Time;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSecretConstants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.delegate.ClientModelLazyDelegate;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.utils.StringUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.keycloak.models.ClientSecretConstants.CLIENT_ROTATED_SECRET;
import static org.keycloak.models.ClientSecretConstants.CLIENT_ROTATED_SECRET_CREATION_TIME;
import static org.keycloak.models.ClientSecretConstants.CLIENT_ROTATED_SECRET_EXPIRATION_TIME;
import static org.keycloak.models.ClientSecretConstants.CLIENT_SECRET_AUTHENTICATION_ALLOWED_METHOD;
import static org.keycloak.models.ClientSecretConstants.CLIENT_SECRET_CREATION_TIME;
import static org.keycloak.models.ClientSecretConstants.CLIENT_SECRET_EXPIRATION;
import static org.keycloak.models.ClientSecretConstants.CLIENT_SECRET_REMAINING_EXPIRATION_TIME;

/**
 * OIDC 客户端密钥与轮换配置包装器。
 * <p>管理密钥创建/过期时间、轮换密钥、Vault 解析及校验逻辑。</p>
 *
 * @author <a href="mailto:masales@redhat.com">Marcelo Sales</a>
 */
public class OIDCClientSecretConfigWrapper extends AbstractClientConfigWrapper {

    private OIDCClientSecretConfigWrapper(ClientModel client, ClientRepresentation clientRep) {
        super(client, clientRep);
    }

    /** @param client 客户端模型 @return 密钥配置包装器 */
    public static OIDCClientSecretConfigWrapper fromClientModel(ClientModel client) {
        return new OIDCClientSecretConfigWrapper(client, null);
    }

    /** @param clientRep 客户端表示 @return 密钥配置包装器 */
    public static OIDCClientSecretConfigWrapper fromClientRepresentation(ClientRepresentation clientRep) {
        return new OIDCClientSecretConfigWrapper(null, clientRep);
    }

    /** 获取Secret 配置值。 */
    public String getSecret() {
        if (clientModel != null) {
            return clientModel.getSecret();
        } else {
            return clientRep.getSecret();
        }
    }

    /** 获取Id 配置值。 */
    public String getId() {
        if (clientModel != null) {
            return clientModel.getId();
        } else {
            return clientRep.getId();
        }
    }

    /** 获取Name 配置值。 */
    public String getName() {
        if (clientModel != null) {
            return clientModel.getName();
        } else {
            return clientRep.getName();
        }
    }

    /** 获取ClientSecretAuthenticationAllowedMethod 配置值。 */
    public String getClientSecretAuthenticationAllowedMethod() {
        return getAttribute(CLIENT_SECRET_AUTHENTICATION_ALLOWED_METHOD);
    }

    /** 设置ClientSecretAuthenticationAllowedMethod 配置。 */
    public void setClientSecretAuthenticationAllowedMethod(String clientSecretAuthenticationAllowedMethod) {
        setAttribute(CLIENT_SECRET_AUTHENTICATION_ALLOWED_METHOD, clientSecretAuthenticationAllowedMethod);
    }

    /** removeClientSecretRotationInfo 相关操作。 */
    public void removeClientSecretRotationInfo() {
        setAttribute(CLIENT_SECRET_EXPIRATION, null);
        setAttribute(CLIENT_SECRET_REMAINING_EXPIRATION_TIME, null);
        removeClientSecretRotated();
    }

    /** removeClientSecretRotated 相关操作。 */
    public void removeClientSecretRotated() {
        if (hasRotatedSecret()) {
            setAttribute(CLIENT_ROTATED_SECRET, null);
            setAttribute(CLIENT_ROTATED_SECRET_CREATION_TIME, null);
            setAttribute(CLIENT_ROTATED_SECRET_EXPIRATION_TIME, null);
        }
    }

    /** 获取ClientSecretCreationTime 配置值。 */
    public long getClientSecretCreationTime() {
        String creationTime = getAttribute(CLIENT_SECRET_CREATION_TIME);
        return StringUtil.isBlank(creationTime) ? 0 : Long.parseLong(creationTime);
    }

    /** 设置ClientSecretCreationTime 配置。 */
    public void setClientSecretCreationTime(long creationTime) {
        setAttribute(CLIENT_SECRET_CREATION_TIME, String.valueOf(creationTime));
    }

    /** hasRotatedSecret 相关操作。 */
    public boolean hasRotatedSecret() {
        return StringUtil.isNotBlank(getAttribute(CLIENT_ROTATED_SECRET)) && StringUtil.isNotBlank(getAttribute(CLIENT_ROTATED_SECRET_CREATION_TIME));
    }

    /** 获取ClientRotatedSecret 配置值。 */
    public String getClientRotatedSecret(KeycloakSession session) {
        String secret = getAttribute(CLIENT_ROTATED_SECRET);
        return session == null ? getAttribute(CLIENT_ROTATED_SECRET) : session.vault().getStringSecret(secret).get().orElse(secret);
    }

    /** 设置ClientRotatedSecret 配置。 */
    public void setClientRotatedSecret(String secret) {
        setAttribute(CLIENT_ROTATED_SECRET, secret);
    }

    /** 获取ClientRotatedSecretCreationTime 配置值。 */
    public long getClientRotatedSecretCreationTime() {
        String rotatedCreationTime = getAttribute(CLIENT_ROTATED_SECRET_CREATION_TIME);
        if (StringUtil.isNotBlank(rotatedCreationTime)) return Long.parseLong(rotatedCreationTime);
        return 0;
    }

    /** 设置ClientRotatedSecretCreationTime 配置。 */
    public void setClientRotatedSecretCreationTime(Long rotatedTime) {
        setAttribute(CLIENT_ROTATED_SECRET_CREATION_TIME, rotatedTime != null ? String.valueOf(rotatedTime) : null);
    }

    /** 将密钥创建时间设为当前秒级时间戳。 */
    public void setClientSecretCreationTime() {
        setClientSecretCreationTime(Time.currentTimeSeconds());
    }

    /** 设置ClientRotatedSecretCreationTime 配置。 */
    public void setClientRotatedSecretCreationTime() {
        setClientRotatedSecretCreationTime(Time.currentTimeSeconds());
    }

    /** updateClientRepresentationAttributes 相关操作。 */
    public void updateClientRepresentationAttributes(ClientRepresentation rep) {
        rep.getAttributes().put(CLIENT_ROTATED_SECRET, getAttribute(CLIENT_ROTATED_SECRET));
        rep.getAttributes().put(CLIENT_SECRET_CREATION_TIME, getAttribute(CLIENT_SECRET_CREATION_TIME));
        rep.getAttributes().put(CLIENT_SECRET_EXPIRATION, getAttribute(CLIENT_SECRET_EXPIRATION));
        rep.getAttributes().put(CLIENT_ROTATED_SECRET_CREATION_TIME, getAttribute(CLIENT_ROTATED_SECRET_CREATION_TIME));
        rep.getAttributes().put(CLIENT_ROTATED_SECRET_EXPIRATION_TIME, getAttribute(CLIENT_ROTATED_SECRET_EXPIRATION_TIME));
    }

    /** hasClientSecretExpirationTime 相关操作。 */
    public boolean hasClientSecretExpirationTime() {
        return getClientSecretExpirationTime() > 0;
    }

    /** 获取ClientSecretExpirationTime 配置值。 */
    public long getClientSecretExpirationTime() {
        String expiration = getAttribute(CLIENT_SECRET_EXPIRATION);
        return expiration == null ? 0 : Long.parseLong(expiration);
    }

    /** 设置ClientSecretExpirationTime 配置。 */
    public void setClientSecretExpirationTime(Long expiration) {
        setAttribute(ClientSecretConstants.CLIENT_SECRET_EXPIRATION, expiration != null ? String.valueOf(expiration) : null);
    }

    /** 是否ClientSecretExpired。 */
    public boolean isClientSecretExpired() {
        if (hasClientSecretExpirationTime()) {
            return getClientSecretExpirationTime() < Time.currentTimeSeconds();
        }
        return false;
    }

    /** 获取ClientRotatedSecretExpirationTime 配置值。 */
    public long getClientRotatedSecretExpirationTime() {
        if (hasClientRotatedSecretExpirationTime()) {
            return Long.parseLong(getAttribute(ClientSecretConstants.CLIENT_ROTATED_SECRET_EXPIRATION_TIME));
        }
        return 0;
    }

    /** 设置ClientRotatedSecretExpirationTime 配置。 */
    public void setClientRotatedSecretExpirationTime(Long expiration) {
        setAttribute(ClientSecretConstants.CLIENT_ROTATED_SECRET_EXPIRATION_TIME, expiration != null ? String.valueOf(expiration) : null);
    }

    /** hasClientRotatedSecretExpirationTime 相关操作。 */
    public boolean hasClientRotatedSecretExpirationTime() {
        return StringUtil.isNotBlank(getAttribute(ClientSecretConstants.CLIENT_ROTATED_SECRET_EXPIRATION_TIME));
    }

    /** 是否ClientRotatedSecretExpired。 */
    public boolean isClientRotatedSecretExpired() {
        if (hasClientRotatedSecretExpirationTime()) {
            return getClientRotatedSecretExpirationTime() < Time.currentTimeSeconds();
        }
        return true;
    }

    /** validateSecret 相关操作。 */
    public boolean validateSecret(KeycloakSession session, String secret) {
        if (isClientSecretExpired()) {
            return false;
        }

        ClientModel wrapper = new ClientModelLazyDelegate(() -> clientModel) {
            @Override
            /** 获取Secret 配置值。 */
            public String getSecret() {
                final String secret = clientModel.getSecret();
                final String result = session.vault().getStringSecret(secret).get().orElse(secret);
                return result;
            }

            @Override
            /** validateSecret 相关操作。 */
            public boolean validateSecret(String secret) {
                return MessageDigest.isEqual(secret.getBytes(), getSecret().getBytes());
            }
        };

        return wrapper.validateSecret(secret);
    }

    // 校验轮换密钥的值与有效期
    /** validateRotatedSecret 相关操作。 */
    public boolean validateRotatedSecret(KeycloakSession session, String secret) {

        // 必须存在轮换密钥
        if (hasRotatedSecret()) {
            // 轮换密钥未过期
            if (isClientRotatedSecretExpired()) {
                return false;
            }
        } else {
            return false;
        }

        return MessageDigest.isEqual(secret.getBytes(), getClientRotatedSecret(session).getBytes());

    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            map.put("clientId", getId());
            map.put("clientName", getName());
            map.put("secretCreationTimeSeconds", getClientSecretCreationTime());
            map.put("secretCreationTime", sdf.format(new Date(TimeUnit.SECONDS.toMillis(getClientSecretCreationTime()))));
            map.put("secretExpirationTimeSeconds", getClientSecretExpirationTime());
            map.put("secretExpirationTime", sdf.format(new Date(TimeUnit.SECONDS.toMillis(getClientSecretExpirationTime()))));
            map.put("rotatedSecretCreationTimeSeconds", getClientRotatedSecretCreationTime());
            map.put("rotatedSecretCreationTime", sdf.format(new Date(TimeUnit.SECONDS.toMillis(getClientRotatedSecretCreationTime()))));
            map.put("rotatedSecretExpirationTimeSeconds", getClientRotatedSecretExpirationTime());
            map.put("rotatedSecretExpirationTime", sdf.format(new Date(TimeUnit.SECONDS.toMillis(getClientRotatedSecretExpirationTime()))));
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    /** toRotatedClientModel 相关操作。 */
    public ReadOnlyRotatedSecretClientModel toRotatedClientModel(KeycloakSession session) throws InvalidObjectException {
        if (Objects.isNull(this.clientModel))
            throw new InvalidObjectException(getClass().getCanonicalName() + " does not have an attribute of type " + ClientModel.class.getCanonicalName());
        return new ReadOnlyRotatedSecretClientModel(session, clientModel);
    }

    /**
     * 只读装饰客户端模型，对外暴露轮换密钥值而不持久化修改。
     */
    public class ReadOnlyRotatedSecretClientModel extends ClientModelLazyDelegate {

        private final KeycloakSession session;

        private ReadOnlyRotatedSecretClientModel(KeycloakSession session, ClientModel clientModel) {
            super(() -> clientModel);
            this.session = session;
        }

        @Override
        /** 获取Secret 配置值。 */
        public String getSecret() {
            return OIDCClientSecretConfigWrapper.this.getClientRotatedSecret(session);
        }

    }
}
