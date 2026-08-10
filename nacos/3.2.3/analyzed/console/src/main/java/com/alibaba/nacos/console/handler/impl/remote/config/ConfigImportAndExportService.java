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

package com.alibaba.nacos.console.handler.impl.remote.config;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.model.SameConfigPolicy;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.common.http.HttpUtils;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.common.utils.IoUtils;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.config.server.utils.RequestUtil;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.console.handler.impl.remote.RemoteServerConnector;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.utils.WebUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * 配置导入导出远程服务：通过 HTTP 向随机选取的健康 Nacos 节点发送 multipart 导入请求或 GET 导出请求。
 * Nacos config import and export service.
 *
 * @author xiweng.yy
 */
@Service
@EnabledRemoteHandler
public class ConfigImportAndExportService {
    
    /** 配置导入导出服务日志记录器 */
    private static final Logger LOGGER =
        LoggerFactory.getLogger(ConfigImportAndExportService.class);
    
    /** 远端配置导入 API URL 模板 */
    private static final String REMOTE_CONFIG_IMPORT_URL = "http://%s%s/v3/admin/cs/config/import";
    
    /** 远端配置导出 API URL 模板 */
    private static final String REMOTE_CONFIG_EXPORT_URL = "http://%s%s/v3/admin/cs/config/export";
    
    /** 远程服务器连接器，负责选取健康节点并附加鉴权信息 */
    private final RemoteServerConnector remoteServerConnector;
    
    /** 注入远程服务器连接器 */
    public ConfigImportAndExportService(RemoteServerConnector remoteServerConnector) {
        this.remoteServerConnector = remoteServerConnector;
    }
    
    /**
     * 向远端 Nacos 节点导入配置 ZIP 文件。
     * Do import config to remote server.
     *
     * @param sourceUser    控制台请求来源用户
     * @param namespaceId   目标命名空间 ID
     * @param policy        同名配置冲突策略
     * @param importFile    待导入的配置文件
     * @param sourceIp      控制台请求来源 IP
     * @param sourceApp     控制台请求来源应用
     * @return 导入成功与失败计数的映射
     */
    public Result<Map<String, Object>> importConfig(String sourceUser, String namespaceId,
        SameConfigPolicy policy,
        MultipartFile importFile, String sourceIp, String sourceApp) throws NacosException {
        String serverContextPath = remoteServerConnector.getServerContextPath();
        Member serverMember = remoteServerConnector.randomOneHealthyMember();
        String url =
            String.format(REMOTE_CONFIG_IMPORT_URL, serverMember.getAddress(), serverContextPath);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            Query query = Query.newInstance().addParam("namespaceId", namespaceId)
                .addParam("srcUser", sourceUser);
            URI uri = HttpUtils.buildUri(url, query);
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeader(WebUtils.X_FORWARDED_FOR, sourceIp);
            httpPost.setHeader(RequestUtil.CLIENT_APPNAME_HEADER, sourceApp);
            remoteServerConnector.addAuthIdentity(httpPost);
            String contentTypeString =
                null == importFile.getContentType() ? MediaType.MULTIPART_FORM_DATA_VALUE
                    : importFile.getContentType();
            ContentType contentType = ContentType.create(contentTypeString, Constants.ENCODE);
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.addBinaryBody("file", importFile.getInputStream(), contentType,
                importFile.getOriginalFilename());
            multipartEntityBuilder.addTextBody("policy", policy.name(), contentType);
            HttpEntity entity = multipartEntityBuilder.build();
            httpPost.setEntity(entity);
            String executeResult =
                httpClient.execute(httpPost, new BasicHttpClientResponseHandler());
            return JacksonUtils.toObj(executeResult, new TypeReference<>() {
            });
        } catch (HttpResponseException responseException) {
            LOGGER.error("Import config to server {} failed with code {}: ",
                serverMember.getAddress(),
                responseException.getStatusCode());
            throw new NacosRuntimeException(responseException.getStatusCode(),
                responseException.getMessage());
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Import config to server {} failed: ", serverMember.getAddress(), e);
            throw new NacosRuntimeException(NacosException.SERVER_ERROR,
                "Import config to server failed.");
        }
    }
    
    /**
     * 从远端 Nacos 节点导出配置为二进制 ZIP 响应。
     * Do export config to from server.
     *
     * @param dataId        导出配置的 dataId
     * @param group         导出配置的 group
     * @param namespaceId   导出配置的命名空间
     * @param appName       导出配置的应用名
     * @param ids           导出配置的存储 ID 列表
     * @return 导出文件字节实体
     * @throws Exception    导出过程中的任意异常
     */
    public ResponseEntity<byte[]> exportConfig(String dataId, String group, String namespaceId,
        String appName,
        List<Long> ids) throws Exception {
        String serverContextPath = remoteServerConnector.getServerContextPath();
        Member serverMember = remoteServerConnector.randomOneHealthyMember();
        String url =
            String.format(REMOTE_CONFIG_EXPORT_URL, serverMember.getAddress(), serverContextPath);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            Query query =
                Query.newInstance().addParam("namespaceId", namespaceId).addParam("dataId", dataId)
                    .addParam("groupName", group).addParam("ids", StringUtils.join(ids, ","));
            URI uri = HttpUtils.buildUri(url, query);
            HttpGet httpGet = new HttpGet(uri);
            remoteServerConnector.addAuthIdentity(httpGet);
            return httpClient.execute(httpGet, new ExportHttpClientResponseHandler());
        } catch (HttpResponseException responseException) {
            LOGGER.error("Export config from server {} failed with code {}: ",
                serverMember.getAddress(),
                responseException.getStatusCode());
            throw new NacosRuntimeException(responseException.getStatusCode(),
                responseException.getMessage());
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Export config from server {} failed: ", serverMember.getAddress(), e);
            throw new NacosRuntimeException(NacosException.SERVER_ERROR,
                "Export config to server failed.");
        }
    }
    
    /** 导出 HTTP 响应处理器，解析 Content-Disposition 并返回字节实体。 */
    static class ExportHttpClientResponseHandler
        extends AbstractHttpClientResponseHandler<ResponseEntity<byte[]>> {
        
        /** 响应头中的 Content-Disposition，用于保留导出文件名 */
        private String contentDisposition;
        
        /** 从响应头提取 Content-Disposition 后委托父类处理。 */
        @Override
        public ResponseEntity<byte[]> handleResponse(ClassicHttpResponse response)
            throws IOException {
            try {
                contentDisposition = response.getHeader("Content-Disposition").getValue();
            } catch (ProtocolException e) {
                throw new NacosRuntimeException(NacosException.SERVER_ERROR,
                    "Export config from server, parse response file name failed; ", e);
            }
            return super.handleResponse(response);
        }
        
        /** 读取响应实体字节流并封装为 Spring {@link ResponseEntity}。 */
        @Override
        public ResponseEntity<byte[]> handleEntity(HttpEntity entity) throws IOException {
            InputStream inputStream = entity.getContent();
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                IoUtils.copy(inputStream, outputStream);
                byte[] responseBody = outputStream.toByteArray();
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", contentDisposition).body(responseBody);
            }
        }
    }
}
