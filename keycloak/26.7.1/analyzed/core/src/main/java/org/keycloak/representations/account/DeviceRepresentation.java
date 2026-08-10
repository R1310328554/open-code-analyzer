/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.representations.account;

import java.util.ArrayList;
import java.util.List;

/**
 * 账户控制台中用户登录设备的聚合表示，可包含多个关联会话及操作系统/浏览器信息。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class DeviceRepresentation {

    /** 未知操作系统或浏览器版本时的占位字符串。 */
    public static final String UNKNOWN = "Unknown";
    /** 无法识别设备类型时的占位值。 */
    private static final String OTHER = "Other";
    /** 浏览器名称与版本之间的分隔符。 */
    private static final String BROWSER_VERSION_SEPARATOR = "/";

    /** 构造表示未知设备的实例（操作系统与设备类型均为 {@link #OTHER}）。 */
    public static DeviceRepresentation unknown() {
        DeviceRepresentation device = new DeviceRepresentation();

        device.setOs(OTHER);
        device.setDevice(OTHER);

        return device;
    }

    /** 设备唯一标识。 */
    private String id;
    /** 最近访问时的 IP 地址。 */
    private String ipAddress;
    /** 操作系统名称。 */
    private String os;
    /** 操作系统版本；未设置时 {@link #getOsVersion()} 返回 {@link #UNKNOWN}。 */
    private String osVersion;
    /** 浏览器名称（可含版本，格式 {@code name/version}）。 */
    private String browser;
    /** 设备类型描述（如 Desktop、Mobile）。 */
    private String device;
    /** 最后访问时间（Unix 秒级时间戳）。 */
    private int lastAccess;
    /** 是否为当前正在使用的设备/会话。 */
    private Boolean current;
    /** 该设备上关联的用户会话列表。 */
    private List<SessionRepresentation> sessions;
    /** 是否为移动设备。 */
    private boolean mobile;

    /** @return 设备 ID */
    public String getId() {
        return id;
    }

    /** @param id 设备 ID */
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

    /** @param ip IP 地址 */
    public void setIpAddress(String ip) {
        this.ipAddress = ip;
    }

    /** @return 操作系统名称 */
    public String getOs() {
        return os;
    }

    /** @param os 操作系统名称 */
    public void setOs(String os) {
        this.os = os;
    }

    /** @return 操作系统版本，未设置时返回 {@link #UNKNOWN} */
    public String getOsVersion() {
        if (osVersion == null) {
            return UNKNOWN;
        }
        return osVersion;
    }

    /** @param osVersion 操作系统版本 */
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    /** @return 浏览器标识（可能含版本） */
    public String getBrowser() {
        return browser;
    }

    /** @param browser 浏览器标识 */
    public void setBrowser(String browser) {
        this.browser = browser;
    }

    /**
     * 将浏览器名称与版本合并为 {@code name/version} 格式；{@code browser} 为 null 时使用 {@link #OTHER}。
     *
     * @param browser 浏览器名称
     * @param version 浏览器版本，null 时使用 {@link #UNKNOWN}
     */
    public void setBrowser(String browser, String version) {
        if (browser == null) {
            this.browser = OTHER;
        } else {
            this.browser = new StringBuilder(browser).append(BROWSER_VERSION_SEPARATOR).append(version == null ? UNKNOWN : version).toString();
        }
    }

    /** @return 设备类型 */
    public String getDevice() {
        return device;
    }

    /** @param device 设备类型 */
    public void setDevice(String device) {
        this.device = device;
    }

    /** @return 最后访问时间戳（秒） */
    public int getLastAccess() {
        return lastAccess;
    }

    /** @param lastAccess 最后访问时间戳（秒） */
    public void setLastAccess(int lastAccess) {
        this.lastAccess = lastAccess;
    }

    /** @return 是否为当前设备 */
    public Boolean getCurrent() {
        return current;
    }

    /** @param current 是否为当前设备 */
    public void setCurrent(Boolean current) {
        this.current = current;
    }

    /** 向会话列表追加一条会话表示。 */
    public void addSession(SessionRepresentation sessionRep) {
        if (this.sessions == null) {
            this.sessions = new ArrayList<>();
        }
        this.sessions.add(sessionRep);
    }

    /** @return 关联会话列表 */
    public List<SessionRepresentation> getSessions() {
        return sessions;
    }

    /** @param mobile 是否为移动设备 */
    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    /** @return 是否为移动设备 */
    public boolean isMobile() {
        return mobile;
    }
}
