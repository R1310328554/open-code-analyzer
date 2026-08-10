package org.keycloak.admin.ui.rest.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.keycloak.admin.ui.rest.model.ClientIdSessionType.SessionType;

/**
 * 用户会话在管理 UI 中的 REST 表示。
 * <p>
 * 包含会话标识、用户信息、活动时间、关联客户端及会话类型（在线/离线等）。
 */
public class SessionRepresentation {
    /** 用户会话 ID，与 {@link SessionType} 共同构成实体主键。 */
    private String id;
    /** 会话所属用户名。 */
    private String username;
    /** 会话所属用户内部 ID。 */
    private String userId;
    /** 客户端 IP 地址。 */
    private String ipAddress;
    /** 会话开始时间（Unix 毫秒时间戳）。 */
    private long start;
    /** 最后访问时间（Unix 毫秒时间戳）。 */
    private long lastAccess;
    /** 是否为临时（transient）用户会话。 */
    private boolean transientUser;

    /** 会话类型（如普通在线会话或离线令牌会话）。 */
    private SessionType type;
    /** 会话关联的客户端映射：键为 clientId，值为客户端内部 ID。 */
    private Map<String, String> clients = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public SessionType getType() {
        return type;
    }

    public void setType(SessionType type) {
        this.type = type;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(long lastAccess) {
        this.lastAccess = lastAccess;
    }

    public Map<String, String> getClients() {
        return clients;
    }

    public void setClients(Map<String, String> clients) {
        this.clients = clients;
    }

    public boolean isTransientUser() {
        return transientUser;
    }

    public void setTransientUser(boolean transientUser) {
        this.transientUser = transientUser;
    }

    /**
     * 相等性由用户会话 ID 与会话类型决定，二者也是该实体的主键。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionRepresentation)) return false;
        SessionRepresentation that = (SessionRepresentation) o;
        return Objects.equals(id, that.id) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }
}
