package com.alibaba.arthas.spring;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code arthas.*} 配置属性绑定类，对应 application.yml/properties 中的 Arthas 开关与网络参数。
 *
 * @author hengyunabc 2020-06-23
 */
@ConfigurationProperties(prefix = "arthas")
public class ArthasProperties {
	/** Agent 监听 IP，默认本机。 */
	private String ip;
	private int telnetPort;
	private int httpPort;

	private String tunnelServer;
	private String agentId;

	private String appName;

	/** 命令执行统计上报 URL。 */
	private String statUrl;

	/** 交互会话超时时间（秒）。 */
	private long sessionTimeout;

    private String username;
    private String password;

	private String home;

	/** 为 true 时 Agent 初始化失败不抛异常，仅记录日志。 */
	private boolean slientInit = false;
	/** 禁用的命令列表（逗号分隔），默认禁用 stop。 */
	private String disabledCommands;
	private String commandLocations;
	private static final String DEFAULT_DISABLEDCOMMANDS = "stop";

    /**
     * 因为 arthasConfigMap 只注入了用户配置的值，没有默认值，因些统一处理补全
     */
    public static void updateArthasConfigMapDefaultValue(Map<String, String> arthasConfigMap) {
        if (!arthasConfigMap.containsKey("disabledCommands")) {
            arthasConfigMap.put("disabledCommands", DEFAULT_DISABLEDCOMMANDS);
        }
    }

	public String getHome() {
		return home;
	}

	public void setHome(String home) {
		this.home = home;
	}

	public boolean isSlientInit() {
		return slientInit;
	}

	public void setSlientInit(boolean slientInit) {
		this.slientInit = slientInit;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getTelnetPort() {
		return telnetPort;
	}

	public void setTelnetPort(int telnetPort) {
		this.telnetPort = telnetPort;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public String getTunnelServer() {
		return tunnelServer;
	}

	public void setTunnelServer(String tunnelServer) {
		this.tunnelServer = tunnelServer;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getStatUrl() {
		return statUrl;
	}

	public void setStatUrl(String statUrl) {
		this.statUrl = statUrl;
	}

	public long getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(long sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

	public String getDisabledCommands() {
		return disabledCommands;
	}

	public void setDisabledCommands(String disabledCommands) {
		this.disabledCommands = disabledCommands;
	}

    public String getCommandLocations() {
        return commandLocations;
    }

    public void setCommandLocations(String commandLocations) {
        this.commandLocations = commandLocations;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
