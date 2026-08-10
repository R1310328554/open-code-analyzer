package org.keycloak.representations.account;

import java.util.List;

/**
 * 账户控制台中单个用户登录会话的摘要，含 IP、时间戳及关联客户端列表。
 *
 * Created by st on 29/03/17.
 */
public class SessionRepresentation {

    /** 会话唯一标识。 */
    private String id;
    /** 会话建立时的 IP 地址。 */
    private String ipAddress;
    /** 会话开始时间（Unix 秒级时间戳）。 */
    private int started;
    /** 最后访问时间（Unix 秒级时间戳）。 */
    private int lastAccess;
    /** 会话过期时间（Unix 秒级时间戳）。 */
    private int expires;
    /** 该会话下活跃的客户端列表。 */
    private List<ClientRepresentation> clients;
    /** 浏览器标识字符串。 */
    private String browser;
    /** 是否为当前会话。 */
    private Boolean current;

    /** @return 会话 ID */
    public String getId() {
        return id;
    }

    /** @param id 会话 ID */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 注意：当代理未提供有效地址时，返回值可能不是真实 IP。
     *
     * @return IP 地址
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /** @param ipAddress IP 地址 */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /** @return 会话开始时间戳（秒） */
    public int getStarted() {
        return started;
    }

    /** @param started 会话开始时间戳（秒） */
    public void setStarted(int started) {
        this.started = started;
    }

    /** @return 最后访问时间戳（秒） */
    public int getLastAccess() {
        return lastAccess;
    }

    /** @param lastAccess 最后访问时间戳（秒） */
    public void setLastAccess(int lastAccess) {
        this.lastAccess = lastAccess;
    }

    /** @return 过期时间戳（秒） */
    public int getExpires() {
        return expires;
    }

    /** @param expires 过期时间戳（秒） */
    public void setExpires(int expires) {
        this.expires = expires;
    }

    /** @return 关联客户端列表 */
    public List<ClientRepresentation> getClients() {
        return clients;
    }

    /** @param clients 关联客户端列表 */
    public void setClients(List<ClientRepresentation> clients) {
        this.clients = clients;
    }

    /** @param browser 浏览器标识 */
    public void setBrowser(String browser) {
        this.browser = browser;
    }

    /** @return 浏览器标识 */
    public String getBrowser() {
        return browser;
    }

    /** @return 是否为当前会话 */
    public Boolean getCurrent() {
        return current;
    }

    /** @param current 是否为当前会话 */
    public void setCurrent(Boolean current) {
        this.current = current;
    }
}
