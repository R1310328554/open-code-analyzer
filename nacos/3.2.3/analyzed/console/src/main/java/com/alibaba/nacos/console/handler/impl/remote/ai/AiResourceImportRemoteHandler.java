/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.console.handler.impl.remote.ai;

import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportExecuteRequest;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportExecuteResponse;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSearchRequest;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSearchResponse;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSourceInfo;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportValidateRequest;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportValidateResponse;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.common.http.HttpUtils;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.console.handler.ai.AiResourceImportHandler;
import com.alibaba.nacos.console.handler.ai.EnabledAiHandler;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.console.handler.impl.remote.RemoteServerConnector;
import com.alibaba.nacos.core.cluster.Member;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 资源导入远程 Handler：通过 HTTP 调用远端 Nacos 节点的 AI 资源导入管理 API，支持源列举、搜索、校验与执行导入。
 * Remote implementation of Console AI resource import handler.
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
@Service
@EnabledRemoteHandler
@EnabledAiHandler
public class AiResourceImportRemoteHandler implements AiResourceImportHandler {
    
    /** 远端 AI 资源导入 API URL 模板：地址 + 上下文路径 + 管理路径 + 子路径 */
    private static final String REMOTE_IMPORT_URL = "http://%s%s%s%s";
    
    /** 远程服务器连接器，负责选取健康节点并附加鉴权信息 */
    private final RemoteServerConnector remoteServerConnector;
    
    /** 注入远程服务器连接器 */
    public AiResourceImportRemoteHandler(RemoteServerConnector remoteServerConnector) {
        this.remoteServerConnector = remoteServerConnector;
    }
    
    /** 列出远端已配置的 AI 资源导入源，可按资源类型过滤。 */
    @Override
    public List<AiResourceImportSourceInfo> listSources(String resourceType)
        throws NacosException {
        Query query = Query.newInstance();
        if (resourceType != null) {
            query.addParam("resourceType", resourceType);
        }
        Result<List<AiResourceImportSourceInfo>> result = executeGet("/sources", query,
            new TypeReference<>() {
            });
        return unwrap(result);
    }
    
    /** 在远端外部导入源中搜索候选 AI 资源。 */
    @Override
    public AiResourceImportSearchResponse search(AiResourceImportSearchRequest request)
        throws NacosException {
        Result<AiResourceImportSearchResponse> result = executePost("/search",
            buildSearchForm(request),
            new TypeReference<>() {
            });
        return unwrap(result);
    }
    
    /** 校验远端已选候选资源是否满足导入条件。 */
    @Override
    public AiResourceImportValidateResponse validate(AiResourceImportValidateRequest request)
        throws NacosException {
        Result<AiResourceImportValidateResponse> result = executePost("/validate",
            buildValidateForm(request),
            new TypeReference<>() {
            });
        return unwrap(result);
    }
    
    /** 对远端通过校验的候选资源执行导入。 */
    @Override
    public AiResourceImportExecuteResponse execute(AiResourceImportExecuteRequest request)
        throws NacosException {
        Result<AiResourceImportExecuteResponse> result = executePost("/execute",
            buildExecuteForm(request),
            new TypeReference<>() {
            });
        return unwrap(result);
    }
    
    /** 向随机选取的健康远端节点发送 GET 请求并解析 {@link Result}。 */
    private <T> Result<T> executeGet(String subPath, Query query,
        TypeReference<Result<T>> typeReference) throws NacosException {
        Member serverMember = remoteServerConnector.randomOneHealthyMember();
        String url = buildUrl(serverMember, subPath);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URI uri = HttpUtils.buildUri(url, query);
            HttpGet httpGet = new HttpGet(uri);
            remoteServerConnector.addAuthIdentity(httpGet);
            String response = httpClient.execute(httpGet, new BasicHttpClientResponseHandler());
            return JacksonUtils.toObj(response, typeReference);
        } catch (IOException | URISyntaxException e) {
            throw remoteFailed(serverMember, e);
        }
    }
    
    /** 向随机选取的健康远端节点发送 POST 表单请求并解析 {@link Result}。 */
    private <T> Result<T> executePost(String subPath, Map<String, String> form,
        TypeReference<Result<T>> typeReference) throws NacosException {
        Member serverMember = remoteServerConnector.randomOneHealthyMember();
        String url = buildUrl(serverMember, subPath);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            HttpUtils.initRequestFromEntity(httpPost, form, "UTF-8");
            remoteServerConnector.addAuthIdentity(httpPost);
            String response = httpClient.execute(httpPost, new BasicHttpClientResponseHandler());
            return JacksonUtils.toObj(response, typeReference);
        } catch (Exception e) {
            throw remoteFailed(serverMember, e);
        }
    }
    
    /** 构建搜索请求的表单参数。 */
    private Map<String, String> buildSearchForm(AiResourceImportSearchRequest request) {
        Map<String, String> result = buildSourceForm(request.getNamespaceId(),
            request.getResourceType(), request.getSourceId(), request.getOptions());
        addIfNotBlank(result, "query", request.getQuery());
        addIfNotBlank(result, "cursor", request.getCursor());
        addIfNotNull(result, "limit", request.getLimit());
        return result;
    }
    
    /** 构建校验请求的表单参数。 */
    private Map<String, String> buildValidateForm(AiResourceImportValidateRequest request) {
        Map<String, String> result = buildSourceForm(request.getNamespaceId(),
            request.getResourceType(), request.getSourceId(), request.getOptions());
        addIfNotNull(result, "selectedItems", request.getSelectedItems());
        if (request.isOverwriteExisting()) {
            result.put("overwriteExisting", Boolean.TRUE.toString());
        }
        return result;
    }
    
    /** 构建执行导入请求的表单参数。 */
    private Map<String, String> buildExecuteForm(AiResourceImportExecuteRequest request) {
        Map<String, String> result = buildSourceForm(request.getNamespaceId(),
            request.getResourceType(), request.getSourceId(), request.getOptions());
        addIfNotNull(result, "selectedItems", request.getSelectedItems());
        if (request.isOverwriteExisting()) {
            result.put("overwriteExisting", Boolean.TRUE.toString());
        }
        if (request.isSkipInvalid()) {
            result.put("skipInvalid", Boolean.TRUE.toString());
        }
        addIfNotBlank(result, "validationToken", request.getValidationToken());
        return result;
    }
    
    /** 构建导入源公共表单字段（命名空间、类型、源 ID 与选项）。 */
    private Map<String, String> buildSourceForm(String namespaceId, String resourceType,
        String sourceId, Map<String, String> options) {
        Map<String, String> result = new LinkedHashMap<>();
        addIfNotBlank(result, "namespaceId", namespaceId);
        addIfNotBlank(result, "resourceType", resourceType);
        addIfNotBlank(result, "sourceId", sourceId);
        addIfNotNull(result, "options", options);
        return result;
    }
    
    /** 非空字符串时写入表单字段。 */
    private void addIfNotBlank(Map<String, String> form, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            form.put(key, value);
        }
    }
    
    /** 非 null 时将对象 JSON 序列化后写入表单字段。 */
    private void addIfNotNull(Map<String, String> form, String key, Object value) {
        if (value != null) {
            form.put(key, JacksonUtils.toJson(value));
        }
    }
    
    /** 拼接远端 AI 资源导入 API 完整 URL。 */
    private String buildUrl(Member serverMember, String subPath) {
        return String.format(REMOTE_IMPORT_URL, serverMember.getAddress(),
            remoteServerConnector.getServerContextPath(), Constants.AI_RESOURCE_IMPORT_ADMIN_PATH,
            subPath);
    }
    
    /** 解包远端 {@link Result}，失败时抛出 {@link NacosApiException}。 */
    private <T> T unwrap(Result<T> result) throws NacosException {
        if (result == null) {
            throw new NacosApiException(NacosException.SERVER_ERROR, ErrorCode.DATA_ACCESS_ERROR,
                "Remote AI resource import response is empty.");
        }
        if (!ErrorCode.SUCCESS.getCode().equals(result.getCode())) {
            throw new NacosApiException(NacosException.SERVER_ERROR, ErrorCode.DATA_ACCESS_ERROR,
                result.getMessage());
        }
        return result.getData();
    }
    
    /** 封装远端 AI 资源导入请求失败异常。 */
    private NacosException remoteFailed(Member serverMember, Exception cause) {
        return new NacosApiException(NacosException.SERVER_ERROR, ErrorCode.DATA_ACCESS_ERROR,
            cause, "Remote AI resource import request failed: " + serverMember.getAddress());
    }
}
