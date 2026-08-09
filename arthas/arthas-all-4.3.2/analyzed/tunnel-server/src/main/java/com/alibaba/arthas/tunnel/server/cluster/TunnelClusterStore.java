package com.alibaba.arthas.tunnel.server.cluster;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alibaba.arthas.tunnel.server.AgentClusterInfo;

/**
 * 保存agentId连接到哪个具体的 tunnel server，集群部署时使用
 * 
 * @author hengyunabc 2020-10-27
 *
 */
public interface TunnelClusterStore {

    /** 注册 agent 及其所在 Tunnel Server 信息，并设置过期时间 */
    public void addAgent(String agentId, AgentClusterInfo info, long expire, TimeUnit timeUnit);

    /** 根据 agentId 查询集群路由信息 */
    public AgentClusterInfo findAgent(String agentId);

    /** agent 下线时移除路由记录 */
    public void removeAgent(String agentId);

    /** 返回当前所有已注册的 agentId */
    public Collection<String> allAgentIds();

    /** 按应用名查询该应用下所有 agent 的集群信息 */
    public Map<String, AgentClusterInfo> agentInfo(String appName);
}
