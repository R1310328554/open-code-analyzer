
package org.keycloak.admin.ui.rest.model;

import java.util.Objects;

/**
 * 客户端 ID 与会话类型（在线/离线）的二元组，用于会话查询键。
 */
public class ClientIdSessionType {

    /** 会话类型枚举：全部、常规在线或离线。 */
    public enum SessionType {
        ALL, REGULAR, OFFLINE
    }

    /** 客户端内部 ID。 */
    private final String clientId;
    /** 会话类型。 */
    private final SessionType type;

    public ClientIdSessionType(String clientId, SessionType type) {
        this.clientId = clientId;
        this.type = type;
    }

    public String getClientId() {
        return clientId;
    }

    public SessionType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientIdSessionType clientIdSessionType = (ClientIdSessionType) o;
        return Objects.equals(clientId, clientIdSessionType.clientId) && type == clientIdSessionType.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, type);
    }
}
