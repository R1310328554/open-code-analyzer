package com.alibaba.arthas.tunnel.server.app.web;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.arthas.tunnel.server.AgentClusterInfo;
import com.alibaba.arthas.tunnel.server.app.configuration.ArthasProperties;
import com.alibaba.arthas.tunnel.server.cluster.TunnelClusterStore;

/**
 * 详情页 REST API：列出集群中的应用与 agent 信息（需开启 {@code enableDetailPages}）。
 *
 * @author hengyunabc 2020-11-03
 *
 */
@Controller
public class DetailAPIController {

    private final static Logger logger = LoggerFactory.getLogger(DetailAPIController.class);

    @Autowired
    ArthasProperties arthasProperties;

    @Autowired(required = false)
    private TunnelClusterStore tunnelClusterStore;

    /**
     * 返回集群中所有应用名称（从 agentId 前缀解析）。
     */
    @RequestMapping("/api/tunnelApps")
    @ResponseBody
    public Set<String> tunnelApps(HttpServletRequest request, Model model) {
        if (!arthasProperties.isEnableDetailPages()) {
            throw new IllegalAccessError("not allow");
        }

        Set<String> result = new HashSet<String>();

        if (tunnelClusterStore != null) {
            Collection<String> agentIds = tunnelClusterStore.allAgentIds();

            for (String id : agentIds) {
                String appName = findAppNameFromAgentId(id);
                if (appName != null) {
                    result.add(appName);
                } else {
                    logger.warn("illegal agentId: " + id);
                }
            }

        }

        return result;
    }

    /**
     * 按应用名查询该应用下所有 agent 的集群信息。
     *
     * @param appName 应用名称
     */
    @RequestMapping("/api/tunnelAgentInfo")
    @ResponseBody
    public Map<String, AgentClusterInfo> tunnelAgentIds(@RequestParam(value = "app", required = true) String appName,
            HttpServletRequest request, Model model) {
        if (!arthasProperties.isEnableDetailPages()) {
            throw new IllegalAccessError("not allow");
        }

        if (tunnelClusterStore != null) {
            Map<String, AgentClusterInfo> agentInfos = tunnelClusterStore.agentInfo(appName);

            return agentInfos;
        }

        return Collections.emptyMap();
    }

    /**
     * check if agentId exists
     * @param agentId
     * @return
     */
    @RequestMapping("/api/tunnelAgents")
    @ResponseBody
    public Map<String, Object> tunnelAgentIds(@RequestParam(value = "agentId", required = true) String agentId) {
        Map<String, Object> result = new HashMap<String, Object>();
        boolean success = false;
        try {
            AgentClusterInfo info = tunnelClusterStore.findAgent(agentId);
            if (info != null) {
                success = true;
            }
        } catch (Throwable e) {
            logger.error("try to find agentId error, id: {}", agentId, e);
        }
        result.put("success", success);
        return result;
    }

    /** 从 agentId（格式 appName_host）中解析应用名 */
    private static String findAppNameFromAgentId(String id) {
        int index = id.indexOf('_');
        if (index < 0 || index >= id.length()) {
            return null;
        }

        return id.substring(0, index);
    }
}
