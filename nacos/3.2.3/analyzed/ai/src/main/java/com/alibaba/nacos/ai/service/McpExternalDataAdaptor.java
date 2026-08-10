/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.service;

import com.alibaba.nacos.ai.enums.ExternalDataTypeEnum;
import com.alibaba.nacos.ai.model.mcp.UrlPageResult;
import com.alibaba.nacos.api.ai.constant.AiConstants;
import com.alibaba.nacos.api.ai.model.mcp.FrontEndpointConfig;
import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportRequest;
import com.alibaba.nacos.api.ai.model.mcp.McpServerRemoteServiceConfig;
import com.alibaba.nacos.api.ai.model.mcp.registry.McpRegistryServerDetail;
import com.alibaba.nacos.api.ai.model.mcp.registry.McpRegistryServerList;
import com.alibaba.nacos.api.ai.model.mcp.registry.Remote;
import com.alibaba.nacos.api.ai.model.mcp.registry.ServerResponse;
import com.alibaba.nacos.api.ai.model.mcp.registry.ServerVersionDetail;
import com.alibaba.nacos.api.ai.model.mcp.registry.OfficialMeta;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * <p>Adapt the External data(mcp server json file, mcp registry api data) to Nacos MCP server format
 * {@link McpServerDetailInfo}. MCP official formats docs.</p>
 * <p>将外部 MCP 数据（官方 seed 文件、JSON 文本、Registry API）适配为 Nacos {@link McpServerDetailInfo} 格式，支持分页拉取与全量导入。</p>
 *
 * <p>1. MCP Server format is defined in
 * <a href="https://github.com/modelcontextprotocol/registry/blob/main/docs/reference/server-json/server.schema.json">
 * server.schema.json</a>.</p>
 *
 * <p>2. MCP Registry Api is defined in
 * <a href="https://github.com/modelcontextprotocol/registry/blob/main/docs/reference/api/openapi.yaml">
 * openapi.yaml</a>.</p>
 *
 * @author nacos
 */
@Service
public class McpExternalDataAdaptor {
    
    /** 可注入的 HTTP 客户端（测试用）。 */
    private HttpClient httpClient;
    
    private static final String CURSOR_QUERY_NAME = "cursor";
    
    private static final String LIMIT_QUERY_NAME = "limit";
    
    private static final String SEARCH_QUERY_NAME = "search";
    
    private static final String HEADER_ACCEPT = "Accept";
    
    private static final String HEADER_ACCEPT_JSON = "application/json";
    
    private static final String QUERY_MARK = "?";
    
    private static final String AMPERSAND = "&";
    
    private static final int HTTP_STATUS_SUCCESS_MIN = 200;
    
    private static final int HTTP_STATUS_SUCCESS_MAX = 299;
    
    private static final int CONNECT_TIMEOUT_SECONDS = 10;
    
    private static final int READ_TIMEOUT_SECONDS = 20;
    
    private static final int FETCH_ALL_LIMIT_MARK = -1;
    
    /**
     * Safety guard to avoid infinite loops when server keeps returning cursors.
     * <p>URL 分页拉取时的最大页数防护，避免 cursor 循环导致无限请求。</p>
     * Limits the maximum number of pages iterated when fetching from URL.
     */
    private static final int MAX_PAGES_GUARD = 200;
    
    /**
     * Adapt the external data to Nacos MCP server format.
     * <p>按 importType（FILE/JSON/URL）分发到对应适配路径。</p>
     *
     * @param request import request
     * @return Nacos MCP server format
     * @throws Exception if adapt failed
     */
    public List<McpServerDetailInfo> adaptExternalDataToNacosMcpServerFormat(
        McpServerImportRequest request) throws Exception {
        ExternalDataTypeEnum externalDataTypeEnum =
            ExternalDataTypeEnum.parseType(request.getImportType());
        if (ExternalDataTypeEnum.FILE.equals(externalDataTypeEnum)) {
            return adaptOfficialSeedFile(request.getData());
        } else if (ExternalDataTypeEnum.JSON.equals(externalDataTypeEnum)) {
            return adaptOfficialMcpServerJsonText(request.getData());
        } else if (ExternalDataTypeEnum.URL.equals(externalDataTypeEnum)) {
            return adaptOfficialRegistryUrl(request.getData(), request.getCursor(),
                request.getLimit(), request.getSearch());
        } else {
            throw new IllegalArgumentException("Unsupported import type: " + externalDataTypeEnum);
        }
    }
    
    /**
     * Fetch one official MCP registry page and adapt it to Nacos MCP server detail info.
     * <p>拉取 Registry 单页并转换为 {@link UrlPageResult}。</p>
     *
     * @param urlData registry endpoint
     * @param cursor page cursor
     * @param limit page size
     * @param search search keyword
     * @return adapted page result
     * @throws Exception if registry fetch or adaptation failed
     */
    public UrlPageResult fetchOfficialRegistryPage(String urlData, String cursor, Integer limit,
        String search)
        throws Exception {
        if (StringUtils.isBlank(urlData)) {
            throw new IllegalArgumentException("URL is blank");
        }
        return fetchUrlPage(urlData.trim(), cursor, limit, search);
    }
    
    /**
     * Fetch one official MCP registry server by name or generated id.
     * <p>按 externalId（名称或生成 ID）搜索 Registry 并返回首个匹配服务。</p>
     *
     * @param urlData registry endpoint
     * @param externalId selected server name or generated id
     * @param limit search page size
     * @return adapted MCP server detail
     * @throws Exception if registry fetch or adaptation failed
     */
    public McpServerDetailInfo fetchOfficialRegistryServer(String urlData, String externalId,
        int limit) throws Exception {
        if (StringUtils.isBlank(externalId)) {
            throw new IllegalArgumentException("MCP server external id is blank");
        }
        int actualLimit = limit > 0 ? limit : 30;
        UrlPageResult page = fetchOfficialRegistryPage(urlData, null, actualLimit, externalId);
        if (CollectionUtils.isNotEmpty(page.getServers())) {
            for (McpServerDetailInfo each : page.getServers()) {
                if (StringUtils.equals(externalId, each.getName())
                    || StringUtils.equals(externalId, each.getId())) {
                    return each;
                }
            }
        }
        throw new IllegalStateException("MCP server not found in registry: " + externalId);
    }
    
    /** HTTP GET 单页 Registry 并解析为 Nacos MCP 列表与 nextCursor。 */
    private UrlPageResult fetchUrlPage(String urlData, String cursor, Integer limit, String search)
        throws Exception {
        String base = urlData.trim();
        HttpClient client = getHttpClient();
        String pageUrl = buildPageUrl(base, cursor, limit, search);
        HttpRequest request = buildGetRequest(pageUrl);
        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        int code = resp.statusCode();
        if (!isSuccessStatus(code)) {
            throw new IllegalStateException("HTTP " + code + " when fetching " + pageUrl);
        }
        List<McpServerDetailInfo> servers = null;
        String next = null;
        try {
            McpRegistryServerList listPage =
                JacksonUtils.toObj(resp.body(), McpRegistryServerList.class);
            if (listPage != null && listPage.getServers() != null) {
                servers = listPage.getServers().stream()
                    .map(this::adaptOfficialMcpServerFromResponse)
                    .collect(Collectors.toList());
            }
            if (listPage != null && listPage.getMetadata() != null) {
                next = listPage.getMetadata().getNextCursor();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse response body", e);
        }
        return new UrlPageResult(servers, next);
    }
    
    /** 循环分页直至无 nextCursor 或达到 MAX_PAGES_GUARD。 */
    private List<McpServerDetailInfo> fetchUrlServersAll(String urlData, String search)
        throws Exception {
        List<McpServerDetailInfo> collected = new ArrayList<>();
        String cursor = null;
        int pages = 0;
        while (pages < MAX_PAGES_GUARD) {
            pages++;
            UrlPageResult page = fetchUrlPage(urlData, cursor, 30, search);
            if (CollectionUtils.isNotEmpty(page.getServers())) {
                collected.addAll(page.getServers());
            }
            String next = page.getNextCursor();
            if (next == null) {
                break;
            }
            cursor = next;
        }
        return collected;
    }
    
    /** 将 Registry 服务详情映射为 Nacos MCP 基本信息、版本、协议与远程配置。 */
    private McpServerDetailInfo adaptOfficialMcpServer(McpRegistryServerDetail registryServer) {
        if (registryServer == null) {
            return null;
        }
        McpServerDetailInfo server = new McpServerDetailInfo();
        applyBasicInfo(registryServer, server);
        applyVersionInfo(registryServer, server);
        applyProtocolInfo(registryServer, server);
        applyLocalAndRemoteConfig(registryServer, server);
        return server;
    }
    
    /**
     * Adapt official mcp server from server response.
     * <p>在 adaptOfficialMcpServer 基础上附加官方 meta（发布日期、latest 标记、status）。</p>
     * Just append version meta info to the result of adaptOfficialMcpServer.
     *
     * @param response the server response object
     * @return adapted mcp server detail info
     */
    private McpServerDetailInfo adaptOfficialMcpServerFromResponse(ServerResponse response) {
        McpServerDetailInfo adaptOfficialMcpServer = adaptOfficialMcpServer(response.getServer());
        ServerVersionDetail versionDetail = adaptOfficialMcpServer.getVersionDetail();
        OfficialMeta official =
            response.getMeta() == null ? null : response.getMeta().getOfficial();
        if (versionDetail != null && official != null) {
            versionDetail.setRelease_date(official.getPublishedAt());
            versionDetail.setIs_latest(true);
            String status = official.getStatus();
            if (StringUtils.isNotBlank(status)) {
                adaptOfficialMcpServer.setStatus(status);
            }
        }
        return adaptOfficialMcpServer;
    }
    
    /** 填充 id/name/description/repository。 */
    private void applyBasicInfo(McpRegistryServerDetail registryServer, McpServerDetailInfo out) {
        String id = generateMcpServerId(registryServer.getName());
        out.setId(id);
        out.setName(registryServer.getName());
        out.setDescription(registryServer.getDescription());
        out.setRepository(registryServer.getRepository());
    }
    
    /** 填充版本号到 VersionDetail。 */
    private void applyVersionInfo(McpRegistryServerDetail registryServer, McpServerDetailInfo out) {
        ServerVersionDetail v = null;
        if (StringUtils.isNotBlank(registryServer.getVersion())) {
            v = new ServerVersionDetail();
            v.setVersion(registryServer.getVersion());
        }
        out.setVersionDetail(v);
    }
    
    /** 根据 packages/remotes 推断 stdio/SSE/streamable 协议。 */
    private void applyProtocolInfo(McpRegistryServerDetail registryServer,
        McpServerDetailInfo out) {
        String protocol = resolveServerProtocol(registryServer);
        if (StringUtils.isNotBlank(protocol)) {
            out.setProtocol(protocol);
            out.setFrontProtocol(protocol);
        }
    }
    
    /** 设置 packages 与 remoteServerConfig（前端 endpoint 列表）。 */
    private void applyLocalAndRemoteConfig(McpRegistryServerDetail registryServer,
        McpServerDetailInfo server) {
        if (registryServer != null) {
            server.setPackages(registryServer.getPackages());
            server.setRemoteServerConfig(generateRemoteServiceConfig(registryServer.getRemotes()));
        }
    }
    
    /** packages 非空为 stdio；remotes 首项 type 映射 SSE/streamable。 */
    private String resolveServerProtocol(McpRegistryServerDetail detail) {
        if (CollectionUtils.isNotEmpty(detail.getPackages())) {
            return AiConstants.Mcp.MCP_PROTOCOL_STDIO;
        }
        
        if (CollectionUtils.isNotEmpty(detail.getRemotes())) {
            Remote first = detail.getRemotes().get(0);
            String tt = first != null ? first.getType() : null;
            if (tt != null) {
                String lower = tt.trim().toLowerCase();
                if (AiConstants.Mcp.OFFICIAL_TRANSPORT_SSE.equals(lower)) {
                    return AiConstants.Mcp.MCP_PROTOCOL_SSE;
                }
                if (AiConstants.Mcp.OFFICIAL_TRANSPORT_STREAMABLE.equals(lower)) {
                    return AiConstants.Mcp.MCP_PROTOCOL_STREAMABLE;
                }
            }
        }
        return null;
    }
    
    /** 将 Registry Remote URL 解析为 FrontEndpointConfig 列表。 */
    private McpServerRemoteServiceConfig generateRemoteServiceConfig(List<Remote> remotes) {
        if (CollectionUtils.isEmpty(remotes)) {
            return null;
        }
        
        McpServerRemoteServiceConfig remoteConfig = new McpServerRemoteServiceConfig();
        List<FrontEndpointConfig> endpoints = new ArrayList<>();
        
        for (Remote remote : remotes) {
            String url = remote.getUrl().trim();
            try {
                UrlComponents components = parseUrlComponents(url);
                boolean isHttps = "https".equalsIgnoreCase(components.getScheme());
                int effectivePort =
                    (components.getPort() > 0) ? components.getPort() : (isHttps ? 443 : 80);
                String endpointData = components.getHost() + ":" + effectivePort;
                FrontEndpointConfig cfg = new FrontEndpointConfig();
                cfg.setEndpointData(endpointData);
                cfg.setPath(
                    StringUtils.isNotBlank(components.getPath()) ? components.getPath() : "/");
                cfg.setType(remote.getType());
                cfg.setProtocol(components.getScheme());
                cfg.setEndpointType(AiConstants.Mcp.MCP_FRONT_ENDPOINT_TYPE_TO_BACK);
                cfg.setHeaders(remote.getHeaders());
                endpoints.add(cfg);
                
                // 首个 remote 的 path 作为 exportPath
                if (remoteConfig.getExportPath() == null) {
                    remoteConfig
                        .setExportPath(components.getPath() != null ? components.getPath() : "/");
                }
            } catch (Exception e) {
                throw new IllegalStateException("Invalid URL: " + url, e);
            }
        }
        
        remoteConfig.setFrontEndpointConfigList(endpoints);
        return remoteConfig;
    }
    
    /**
     * Parse URL into components (scheme, host, port, path).
     * <p>手动解析 URL 各组成部分（不使用 URI 类）。</p>
     * Manual parsing without using URI class.
     *
     * @param url the URL string to parse
     * @return UrlComponents containing scheme, host, port, and path
     */
    private UrlComponents parseUrlComponents(String url) {
        String scheme = null;
        String host = null;
        int port = -1;
        String path = null;
        
        // 解析 scheme
        int schemeEnd = url.indexOf("://");
        if (schemeEnd > 0) {
            scheme = url.substring(0, schemeEnd);
            url = url.substring(schemeEnd + 3);
        }
        
        // 解析 host、port 与 path
        int pathStart = url.indexOf('/');
        String hostPart;
        if (pathStart > 0) {
            hostPart = url.substring(0, pathStart);
            path = url.substring(pathStart);
        } else {
            hostPart = url;
            path = null;
        }
        
        // 从 hostPart 拆分 host 与 port
        int portStart = hostPart.lastIndexOf(':');
        if (portStart > 0) {
            host = hostPart.substring(0, portStart);
            try {
                port = Integer.parseInt(hostPart.substring(portStart + 1));
            } catch (NumberFormatException e) {
                // 端口非法则整段视为 host
                host = hostPart;
                port = -1;
            }
        } else {
            host = hostPart;
        }
        
        return new UrlComponents(scheme, host, port, path);
    }
    
    /**
     * Inner class to hold URL components parsed from a URI.
     * <p>URL 解析结果：scheme、host、port、path。</p>
     */
    private static class UrlComponents {
        
        private final String scheme;
        private final String host;
        private final int port;
        private final String path;
        
        public UrlComponents(String scheme, String host, int port, String path) {
            this.scheme = scheme;
            this.host = host;
            this.port = port;
            this.path = path;
        }
        
        public String getScheme() {
            return scheme;
        }
        
        public String getHost() {
            return host;
        }
        
        public int getPort() {
            return port;
        }
        
        public String getPath() {
            return path;
        }
    }
    
    /**
     * URL import wrapper: fetch contents from specified URL and adapt to Nacos mcp servers.
     * <p>URL 导入：limit=-1 时全量分页，否则单页。</p>
     * Fetch specified contents from specified URL and adapt to Nacos mcp servers.
     *
     * @param urlData URL data to parse. Only support official mcp registry api.
     * @param cursor Cursor for pagination
     * @param limit Limit for pagination. Fetch all pages when limit = -1
     * @param search fuzzy search keyword
     * @return list of adapted mcp servers
     * @throws Exception if adaptation failed
     */
    private List<McpServerDetailInfo> adaptOfficialRegistryUrl(String urlData, String cursor,
        Integer limit, String search)
        throws Exception {
        if (StringUtils.isBlank(urlData)) {
            throw new IllegalArgumentException("URL is blank");
        }
        
        // limit=-1 表示拉取全部页
        if (limit != null && limit == FETCH_ALL_LIMIT_MARK) {
            return fetchUrlServersAll(urlData.trim(), search);
        }
        
        // 否则仅拉取单页
        UrlPageResult page = fetchUrlPage(urlData.trim(), cursor, limit, search);
        return page.getServers();
    }
    
    /**
     * File import wrapper: parse into a list of RegistryDetails and convert to
     * Nacos servers.
     * <p>官方 seed 文件导入：反序列化为 Registry 列表并逐条适配。</p>
     */
    private List<McpServerDetailInfo> adaptOfficialSeedFile(String data) {
        return unmarshaledSeedToServerList(data).stream()
            .map(this::adaptOfficialMcpServer)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /** 单条官方 JSON 文本适配。 */
    private List<McpServerDetailInfo> adaptOfficialMcpServerJsonText(String data) {
        McpRegistryServerDetail detail = JacksonUtils.toObj(data, McpRegistryServerDetail.class);
        return Collections.singletonList(adaptOfficialMcpServer(detail));
    }
    
    /** 反序列化 seed 文件为 Registry 服务列表。 */
    private List<McpRegistryServerDetail> unmarshaledSeedToServerList(String data) {
        return JacksonUtils.toObj(data, new TypeReference<>() {
        });
    }
    
    /** 懒加载带超时与重定向的 HttpClient。 */
    private HttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
                .build();
        }
        return httpClient;
    }
    
    public void setHttpClient(HttpClient client) {
        this.httpClient = client;
    }
    
    /** 拼接 cursor/limit/search 查询参数。 */
    private String buildPageUrl(String base, String cursor, Integer limit, String search) {
        StringBuilder url = new StringBuilder(base);
        boolean hasQuery = base.contains(QUERY_MARK);
        if (StringUtils.isNotBlank(cursor)) {
            String enc = URLEncoder.encode(cursor, StandardCharsets.UTF_8);
            url.append(hasQuery ? AMPERSAND : QUERY_MARK).append(CURSOR_QUERY_NAME).append("=")
                .append(enc);
            hasQuery = true;
        }
        if (limit != null && limit > 0) {
            url.append(hasQuery ? AMPERSAND : QUERY_MARK).append(LIMIT_QUERY_NAME).append("=")
                .append(limit);
            hasQuery = true;
        }
        if (StringUtils.isNotBlank(search)) {
            String encSearch = URLEncoder.encode(search, StandardCharsets.UTF_8);
            url.append(hasQuery ? AMPERSAND : QUERY_MARK).append(SEARCH_QUERY_NAME).append("=")
                .append(encSearch);
        }
        return url.toString();
    }
    
    /** 构建带 Accept: application/json 的 GET 请求。 */
    private HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(READ_TIMEOUT_SECONDS))
            .GET()
            .header(HEADER_ACCEPT, HEADER_ACCEPT_JSON).build();
    }
    
    /** HTTP 2xx 视为成功。 */
    private boolean isSuccessStatus(int code) {
        return code >= HTTP_STATUS_SUCCESS_MIN && code <= HTTP_STATUS_SUCCESS_MAX;
    }
    
    /** 由服务名生成确定性 UUID 作为 Nacos MCP ID。 */
    private String generateMcpServerId(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
