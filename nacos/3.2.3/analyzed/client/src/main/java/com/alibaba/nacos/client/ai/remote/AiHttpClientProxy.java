/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.client.ai.remote;

import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpec;
import com.alibaba.nacos.api.ai.model.prompt.Prompt;
import com.alibaba.nacos.api.ai.model.skills.SkillUtils;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.naming.core.NamingServerListManager;
import com.alibaba.nacos.client.naming.remote.http.NamingHttpClientManager;
import com.alibaba.nacos.client.security.SecurityProxy;
import com.alibaba.nacos.client.utils.ContextPathUtil;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.common.tls.TlsSystemConfig;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.ThreadUtils;
import com.alibaba.nacos.plugin.auth.api.RequestResource;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.client.constant.Constants.Security.SECURITY_INFO_REFRESH_INTERVAL_MILLS;
import static com.alibaba.nacos.common.constant.RequestUrlConstants.HTTPS_PREFIX;
import static com.alibaba.nacos.common.constant.RequestUrlConstants.HTTP_PREFIX;

/**
 * AI HTTP 传输层客户端代理。
 *
 * <p>实现 {@link AiClientProxy}，通过 HTTP REST 与 Nacos 服务端通信。适用于客户端与服务端之间存在无法转发 gRPC 流量的网关场景。</p>
 *
 * <p>当前支持 Prompt、Skill、AgentSpec 查询；内置服务端列表轮询、安全认证及 304 条件查询处理。</p>
 *
 * @author nacos
 */
public class AiHttpClientProxy implements AiClientProxy {
    
    /** 日志记录器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(AiHttpClientProxy.class);
    
    /** Prompt 查询 REST 路径。 */
    private static final String PROMPT_CLIENT_PATH = "/v3/client/ai/prompt";
    
    /** Skill 下载 REST 路径。 */
    private static final String SKILL_DOWNLOAD_PATH = "/v3/client/ai/skills";
    
    /** AgentSpec 查询 REST 路径。 */
    private static final String AGENTSPEC_CLIENT_PATH = "/v3/client/ai/agentspecs";
    
    /** 单请求最大重试次数。 */
    private static final int MAX_RETRY = 3;
    
    /** 是否启用 HTTPS 协议前缀。 */
    private static final boolean ENABLE_HTTPS = Boolean.getBoolean(TlsSystemConfig.TLS_ENABLE);
    
    /** 命名空间 ID。 */
    private final String namespaceId;
    
    /** HTTP REST 请求模板。 */
    private final NacosRestTemplate nacosRestTemplate;
    
    /** 服务端地址列表管理器。 */
    private final NamingServerListManager serverListManager;
    
    /** 安全认证代理。 */
    private final SecurityProxy securityProxy;
    
    /** 安全令牌定时刷新线程池。 */
    private final ScheduledThreadPoolExecutor executorService;
    
    /** 包级私有默认构造，供测试或框架使用。 */
    AiHttpClientProxy() {
        this.namespaceId = null;
        this.nacosRestTemplate = null;
        this.serverListManager = null;
        this.securityProxy = null;
        this.executorService = null;
    }
    
    /**
     * 构造 AI HTTP 客户端代理并初始化服务端列表与安全认证。
     *
     * @param namespaceId 命名空间 ID
     * @param properties  客户端配置
     * @throws NacosException 初始化失败时抛出
     */
        this.namespaceId = namespaceId;
        this.nacosRestTemplate = NamingHttpClientManager.getInstance().getNacosRestTemplate();
        this.serverListManager = new NamingServerListManager(properties, namespaceId);
        this.serverListManager.start();
        this.securityProxy = new SecurityProxy(this.serverListManager, this.nacosRestTemplate);
        this.executorService = new ScheduledThreadPoolExecutor(1,
            new NameThreadFactory("com.alibaba.nacos.client.ai.http.security"));
        final Properties nacosClientPropertiesView = properties.asProperties();
        this.securityProxy.login(nacosClientPropertiesView);
        this.executorService.scheduleWithFixedDelay(
            () -> securityProxy.login(nacosClientPropertiesView), 0,
            SECURITY_INFO_REFRESH_INTERVAL_MILLS, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public Prompt queryPrompt(String promptKey, String version, String label, String md5)
        throws NacosException {
        Map<String, String> params = new HashMap<>(8);
        params.put("namespaceId", namespaceId);
        params.put("promptKey", promptKey);
        if (StringUtils.isNotBlank(version)) {
            params.put("version", version);
        }
        if (StringUtils.isNotBlank(label)) {
            params.put("label", label);
        }
        if (StringUtils.isNotBlank(md5)) {
            params.put("md5", md5);
        }
        
        RequestResource resource = RequestResource.aiBuilder().setNamespace(namespaceId)
            .setGroup(com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP)
            .setResource(null == promptKey ? StringUtils.EMPTY : promptKey).build();
        
        String responseBody = reqApi(PROMPT_CLIENT_PATH, params, resource);
        Result<Prompt> result =
            JacksonUtils.toObj(responseBody, new TypeReference<Result<Prompt>>() {
            });
        return result.getData();
    }
    
    /**
     * Download skill as ZIP byte array via HTTP REST API.
     *
     * @param skillName skill name
     * @param version   explicit version (optional)
     * @param label     route label, e.g. latest/stable (optional)
     * @return ZIP file as byte array
     * @throws NacosException if request fails
      * <p>Nacos AI HTTP 客户端 REST 操作；详见上方说明。</p>
     */
    public byte[] downloadSkillZip(String skillName, String version, String label)
        throws NacosException {
        Map<String, String> params = new HashMap<>(8);
        params.put("namespaceId", namespaceId);
        params.put("name", skillName);
        if (StringUtils.isNotBlank(version)) {
            params.put("version", version);
        }
        if (StringUtils.isNotBlank(label)) {
            params.put("label", label);
        }
        
        RequestResource resource = RequestResource.aiBuilder().setNamespace(namespaceId)
            .setGroup(com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP)
            .setResource(null == skillName ? StringUtils.EMPTY : skillName).build();
        
        byte[] zipBytes = reqApiBytes(SKILL_DOWNLOAD_PATH, params, resource);
        SkillUtils.validateZipBytes(zipBytes);
        try {
            SkillUtils.validateZipEntryPaths(zipBytes);
        } catch (Exception e) {
            throw new NacosException(NacosException.SERVER_ERROR,
                "Downloaded ZIP contains unsafe entry paths: " + e.getMessage(), e);
        }
        return zipBytes;
    }
    
    @Override
    public SkillQueryResponse querySkill(String skillName, String version, String label, String md5)
        throws NacosException {
        Map<String, String> params = new HashMap<>(8);
        params.put("namespaceId", namespaceId);
        params.put("name", skillName);
        if (StringUtils.isNotBlank(version)) {
            params.put("version", version);
        }
        if (StringUtils.isNotBlank(label)) {
            params.put("label", label);
        }
        if (StringUtils.isNotBlank(md5)) {
            params.put("md5", md5);
        }
        
        RequestResource resource = RequestResource.aiBuilder().setNamespace(namespaceId)
            .setGroup(com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP)
            .setResource(null == skillName ? StringUtils.EMPTY : skillName).build();
        
        HttpRestResult<byte[]> restResult = reqApiBytesWithHeader(SKILL_DOWNLOAD_PATH, params,
            resource);
        byte[] zipBytes = restResult.getData();
        SkillUtils.validateZipBytes(zipBytes);
        try {
            SkillUtils.validateZipEntryPaths(zipBytes);
        } catch (Exception e) {
            throw new NacosException(NacosException.SERVER_ERROR,
                "Downloaded ZIP contains unsafe entry paths: " + e.getMessage(), e);
        }
        String publishedMd5 = restResult.getHeader().getValue("X-Nacos-Skill-Md5");
        String resolvedVersion = restResult.getHeader()
            .getValue("X-Nacos-Skill-Resolved-Version");
        return new SkillQueryResponse(zipBytes, publishedMd5, resolvedVersion);
    }
    
    @Override
    public AgentSpecQueryResponse queryAgentSpec(String agentSpecName, String version,
        String label, String md5) throws NacosException {
        Map<String, String> params = new HashMap<>(8);
        params.put("namespaceId", namespaceId);
        params.put("name", agentSpecName);
        if (StringUtils.isNotBlank(version)) {
            params.put("version", version);
        }
        if (StringUtils.isNotBlank(label)) {
            params.put("label", label);
        }
        if (StringUtils.isNotBlank(md5)) {
            params.put("md5", md5);
        }
        
        RequestResource resource = RequestResource.aiBuilder().setNamespace(namespaceId)
            .setGroup(com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP)
            .setResource(
                null == agentSpecName ? StringUtils.EMPTY : agentSpecName)
            .build();
        
        HttpRestResult<String> restResult = reqApiStringWithHeader(
            AGENTSPEC_CLIENT_PATH, params, resource);
        String responseBody = restResult.getData();
        Result<AgentSpec> result =
            JacksonUtils.toObj(responseBody, new TypeReference<Result<AgentSpec>>() {
            });
        String publishedMd5 = restResult.getHeader().getValue("X-Nacos-AgentSpec-Md5");
        String resolvedVersion = restResult.getHeader()
            .getValue("X-Nacos-AgentSpec-Resolved-Version");
        return new AgentSpecQueryResponse(result.getData(), publishedMd5,
            resolvedVersion);
    }
    
    /** 向服务端列表轮询发送 GET 请求并返回字符串响应体。 */
    private String reqApi(String api, Map<String, String> params, RequestResource resource)
        throws NacosException {
        List<String> servers = serverListManager.getServerList();
        if (servers.isEmpty()) {
            throw new NacosException(NacosException.INVALID_PARAM, "no server available");
        }
        
        NacosException exception = new NacosException();
        int index = ThreadLocalRandom.current().nextInt(servers.size());
        
        for (int i = 0; i < Math.max(servers.size(), MAX_RETRY); i++) {
            String server = servers.get(index % servers.size());
            try {
                return callServer(api, params, server, resource);
            } catch (NacosException e) {
                exception = e;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Request {} to server {} failed.", api, server, e);
                }
            }
            index = (index + 1) % servers.size();
        }
        
        LOGGER.error("Request: {} failed, servers: {}, code: {}, msg: {}", api, servers,
            exception.getErrCode(),
            exception.getErrMsg());
        throw new NacosException(exception.getErrCode(),
            "Failed to request API: " + api + " after all servers(" + servers + ") tried: "
                + exception.getMessage());
    }
    
    /** 向服务端列表轮询发送 GET 请求并返回字节响应体。 */
    private byte[] reqApiBytes(String api, Map<String, String> params, RequestResource resource)
        throws NacosException {
        List<String> servers = serverListManager.getServerList();
        if (servers.isEmpty()) {
            throw new NacosException(NacosException.INVALID_PARAM, "no server available");
        }
        
        NacosException exception = new NacosException();
        int index = ThreadLocalRandom.current().nextInt(servers.size());
        
        for (int i = 0; i < Math.max(servers.size(), MAX_RETRY); i++) {
            String server = servers.get(index % servers.size());
            try {
                return callServerBytes(api, params, server, resource);
            } catch (NacosException e) {
                exception = e;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Request {} to server {} failed.", api, server, e);
                }
            }
            index = (index + 1) % servers.size();
        }
        
        LOGGER.error("Request: {} failed, servers: {}, code: {}, msg: {}", api, servers,
            exception.getErrCode(),
            exception.getErrMsg());
        throw new NacosException(exception.getErrCode(),
            "Failed to request API: " + api + " after all servers(" + servers + ") tried: "
                + exception.getMessage());
    }
    
    /** 发送 GET 请求并返回含响应头的字节结果（304 立即传播）。 */
    private HttpRestResult<byte[]> reqApiBytesWithHeader(String api, Map<String, String> params,
        RequestResource resource) throws NacosException {
        List<String> servers = serverListManager.getServerList();
        if (servers.isEmpty()) {
            throw new NacosException(NacosException.INVALID_PARAM, "no server available");
        }
        
        NacosException exception = new NacosException();
        int index = ThreadLocalRandom.current().nextInt(servers.size());
        
        for (int i = 0; i < Math.max(servers.size(), MAX_RETRY); i++) {
            String server = servers.get(index % servers.size());
            try {
                return callServerBytesWithHeader(api, params, server, resource);
            } catch (NacosException e) {
                if (NacosException.NOT_MODIFIED == e.getErrCode()) {
                    throw e;
                }
                exception = e;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Request {} to server {} failed.", api, server, e);
                }
            }
            index = (index + 1) % servers.size();
        }
        
        LOGGER.error("Request: {} failed, servers: {}, code: {}, msg: {}", api, servers,
            exception.getErrCode(),
            exception.getErrMsg());
        throw new NacosException(exception.getErrCode(),
            "Failed to request API: " + api + " after all servers(" + servers + ") tried: "
                + exception.getMessage());
    }
    
    /** 向指定服务端发送 GET 请求并返回字符串响应。 */
    private String callServer(String api, Map<String, String> params, String server,
        RequestResource resource)
        throws NacosException {
        Map<String, String> securityHeaders = securityProxy.getIdentityContext(resource);
        Header header = Header.newInstance();
        header.addAll(securityHeaders);
        
        String url = buildUrl(server, api);
        
        try {
            HttpRestResult<String> restResult = nacosRestTemplate.get(url, header,
                Query.newInstance().initParams(params), String.class);
            
            if (restResult.ok()) {
                return restResult.getData();
            }
            if (HttpURLConnection.HTTP_NOT_MODIFIED == restResult.getCode()) {
                throw new NacosException(NacosException.NOT_MODIFIED, "not modified");
            }
            if (HttpURLConnection.HTTP_FORBIDDEN == restResult.getCode()) {
                securityProxy.reLogin();
            }
            throw new NacosException(restResult.getCode(), restResult.getMessage());
        } catch (NacosException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("[AI-HTTP] Failed to request {}", url, e);
            throw new NacosException(NacosException.SERVER_ERROR, e);
        }
    }
    
    /** 向指定服务端发送 GET 请求并返回字节响应。 */
    private byte[] callServerBytes(String api, Map<String, String> params, String server,
        RequestResource resource)
        throws NacosException {
        Map<String, String> securityHeaders = securityProxy.getIdentityContext(resource);
        Header header = Header.newInstance();
        header.addAll(securityHeaders);
        
        String url = buildUrl(server, api);
        
        try {
            HttpRestResult<byte[]> restResult = nacosRestTemplate.get(url, header,
                Query.newInstance().initParams(params), byte[].class);
            
            if (restResult.ok()) {
                return restResult.getData();
            }
            if (HttpURLConnection.HTTP_NOT_MODIFIED == restResult.getCode()) {
                throw new NacosException(NacosException.NOT_MODIFIED, "not modified");
            }
            if (HttpURLConnection.HTTP_FORBIDDEN == restResult.getCode()) {
                securityProxy.reLogin();
            }
            throw new NacosException(restResult.getCode(), restResult.getMessage());
        } catch (NacosException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("[AI-HTTP] Failed to request {}", url, e);
            throw new NacosException(NacosException.SERVER_ERROR, e);
        }
    }
    
    /**
     * {@link #callServerBytes} 的变体，返回原始 {@link HttpRestResult} 以便读取响应头（如 {@code X-Nacos-Skill-Md5}）。304 抛出 {@link NacosException#NOT_MODIFIED}，403 触发重新登录。
     */
    private HttpRestResult<byte[]> callServerBytesWithHeader(String api,
        Map<String, String> params, String server, RequestResource resource)
        throws NacosException {
        Map<String, String> securityHeaders = securityProxy.getIdentityContext(resource);
        Header header = Header.newInstance();
        header.addAll(securityHeaders);
        
        String url = buildUrl(server, api);
        
        try {
            HttpRestResult<byte[]> restResult = nacosRestTemplate.get(url, header,
                Query.newInstance().initParams(params), byte[].class);
            
            if (restResult.ok()) {
                return restResult;
            }
            if (HttpURLConnection.HTTP_NOT_MODIFIED == restResult.getCode()) {
                throw new NacosException(NacosException.NOT_MODIFIED, "not modified");
            }
            if (HttpURLConnection.HTTP_FORBIDDEN == restResult.getCode()) {
                securityProxy.reLogin();
            }
            throw new NacosException(restResult.getCode(), restResult.getMessage());
        } catch (NacosException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("[AI-HTTP] Failed to request {}", url, e);
            throw new NacosException(NacosException.SERVER_ERROR, e);
        }
    }
    
    /** 拼接完整 HTTP URL（含协议前缀与上下文路径）。 */
    private String buildUrl(String serverAddr, String relativePath) {
        if (!serverAddr.startsWith(HTTP_PREFIX) && !serverAddr.startsWith(HTTPS_PREFIX)) {
            serverAddr = (ENABLE_HTTPS ? HTTPS_PREFIX : HTTP_PREFIX) + serverAddr;
        }
        String contextPath = serverListManager.getContextPath();
        return serverAddr + ContextPathUtil.normalizeContextPath(contextPath) + relativePath;
    }
    
    /** 发送 GET 请求并返回含响应头的字符串结果（304 立即传播）。 */
    /** 向服务端列表轮询发送 GET 请求并返回含响应头的字符串结果。 */
    private HttpRestResult<String> reqApiStringWithHeader(String api,
        Map<String, String> params, RequestResource resource) throws NacosException {
        List<String> servers = serverListManager.getServerList();
        if (servers.isEmpty()) {
            throw new NacosException(NacosException.INVALID_PARAM, "no server available");
        }
        
        NacosException exception = new NacosException();
        int index = ThreadLocalRandom.current().nextInt(servers.size());
        
        for (int i = 0; i < Math.max(servers.size(), MAX_RETRY); i++) {
            String server = servers.get(index % servers.size());
            try {
                return callServerStringWithHeader(api, params, server, resource);
            } catch (NacosException e) {
                if (NacosException.NOT_MODIFIED == e.getErrCode()) {
                    throw e;
                }
                exception = e;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Request {} to server {} failed.", api, server, e);
                }
            }
            index = (index + 1) % servers.size();
        }
        
        LOGGER.error("Request: {} failed, servers: {}, code: {}, msg: {}", api, servers,
            exception.getErrCode(),
            exception.getErrMsg());
        throw new NacosException(exception.getErrCode(),
            "Failed to request API: " + api + " after all servers(" + servers + ") tried: "
                + exception.getMessage());
    }
    
    /** 向指定服务端发送 GET 请求并返回含响应头的字符串结果。 */
    private HttpRestResult<String> callServerStringWithHeader(String api,
        Map<String, String> params, String server, RequestResource resource)
        throws NacosException {
        Map<String, String> securityHeaders = securityProxy.getIdentityContext(resource);
        Header header = Header.newInstance();
        header.addAll(securityHeaders);
        
        String url = buildUrl(server, api);
        
        try {
            HttpRestResult<String> restResult = nacosRestTemplate.get(url, header,
                Query.newInstance().initParams(params), String.class);
            
            if (restResult.ok()) {
                return restResult;
            }
            if (HttpURLConnection.HTTP_NOT_MODIFIED == restResult.getCode()) {
                throw new NacosException(NacosException.NOT_MODIFIED, "not modified");
            }
            if (HttpURLConnection.HTTP_FORBIDDEN == restResult.getCode()) {
                securityProxy.reLogin();
            }
            throw new NacosException(restResult.getCode(), restResult.getMessage());
        } catch (NacosException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("[AI-HTTP] Failed to request {}", url, e);
            throw new NacosException(NacosException.SERVER_ERROR, e);
        }
    }
    
    @Override
    /** 关闭服务端列表管理器、安全代理及定时线程池。 */
    public void shutdown() throws NacosException {
        serverListManager.shutdown();
        if (securityProxy != null) {
            securityProxy.shutdown();
        }
        if (executorService != null) {
            ThreadUtils.shutdownThreadPool(executorService, LOGGER);
        }
    }
}
