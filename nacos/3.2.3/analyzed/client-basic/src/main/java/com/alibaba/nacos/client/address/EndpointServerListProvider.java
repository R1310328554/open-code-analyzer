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

package com.alibaba.nacos.client.address;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.SystemPropertyKeyConst;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.constant.Constants.Address;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.utils.ClientBasicParamUtil;
import com.alibaba.nacos.client.utils.ContextPathUtil;
import com.alibaba.nacos.client.utils.TemplateUtils;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.HttpUtils;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.InternetAddressUtil;
import com.alibaba.nacos.common.utils.IoUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Endpoint server list provider.
 * <p>基于 Address Server / Endpoint 的动态服务端列表提供者：周期性 HTTP 拉取集群节点，变更时发布 {@link ServerListChangeEvent}，适用于云上 endpoint 寻址场景。</p>
 *
 * @author totalo
 */
public class EndpointServerListProvider extends AbstractServerListProvider {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointServerListProvider.class);
    
    /** 未配置时默认启用 endpoint 解析规则（如占位符替换） */
    private static final boolean USE_ENDPOINT_PARSING_RULE_DEFAULT_VALUE = true;
    
    private NacosRestTemplate nacosRestTemplate;
    
    /** {@link #getServerName()} 前缀，标识 endpoint 自定义寻址 */
    private static final String CUSTOM_NAME = "custom";
    
    /** 两次刷新之间的最小间隔（毫秒），防止过于频繁请求 */
    private final long refreshServerListInternal = TimeUnit.SECONDS.toMillis(30);
    
    /** 启动阶段拉取地址列表的最大重试轮数 */
    private final int initServerListRetryTimes = 5;
    
    private long lastServerListRefreshTime = 0L;
    
    private ScheduledExecutorService refreshServerListExecutor;
    
    private String endpoint;
    
    private int endpointPort = 8080;
    
    private String endpointContextPath;
    
    private String serverListName = ClientBasicParamUtil.getDefaultNodesPath();
    
    /** 自 endpoint 解析得到的当前 server 地址列表（volatile 保证可见性） */
    private volatile List<String> serversFromEndpoint = new ArrayList<>();
    
    private String addressServerUrl;
    
    private String moduleName = "default";
    
    @Override
    public void init(final NacosClientProperties properties,
        final NacosRestTemplate nacosRestTemplate)
        throws NacosException {
        super.init(properties, nacosRestTemplate);
        this.nacosRestTemplate = nacosRestTemplate;
        initEndpoint(properties);
        initEndpointPort(properties);
        initEndpointContextPath(properties);
        initServerListName(properties);
        initAddressServerUrl(properties);
        initModuleName(properties);
        startRefreshServerListTask(properties);
    }
    
    @Override
    public List<String> getServerList() {
        return serversFromEndpoint;
    }
    
    @Override
    public String getServerName() {
        String contextPathTmp =
            StringUtils.isNotBlank(this.endpointContextPath) ? this.endpointContextPath
                : this.contextPath;
        return CUSTOM_NAME + "-"
            + String.join("_", endpoint, String.valueOf(endpointPort), contextPathTmp,
                serverListName)
            + (StringUtils.isNotBlank(namespace) ? ("_" + StringUtils.trim(namespace)) : "");
    }
    
    @Override
    public int getOrder() {
        return Address.ENDPOINT_SERVER_LIST_PROVIDER_ORDER;
    }
    
    @Override
    public boolean match(final NacosClientProperties properties) {
        String endpointTmp = getEndPointTmp(properties);
        return StringUtils.isNotBlank(endpointTmp);
    }
    
    @Override
    public String getAddressSource() {
        return this.addressServerUrl;
    }
    
    private String getEndPointTmp(NacosClientProperties properties) {
        String endpointTmp = properties.getProperty(PropertyKeyConst.ENDPOINT);
        String isUseEndpointRuleParsing =
            properties.getProperty(PropertyKeyConst.IS_USE_ENDPOINT_PARSING_RULE,
                properties.getProperty(SystemPropertyKeyConst.IS_USE_ENDPOINT_PARSING_RULE,
                    String.valueOf(USE_ENDPOINT_PARSING_RULE_DEFAULT_VALUE)));
        if (Boolean.parseBoolean(isUseEndpointRuleParsing)) {
            endpointTmp = ClientBasicParamUtil.parsingEndpointRule(endpointTmp);
        }
        return endpointTmp;
    }
    
    /**
     * Start refresh server list task.
     * <p>启动前同步重试直至列表非空，再调度定时任务按 {@link PropertyKeyConst#ENDPOINT_REFRESH_INTERVAL_SECONDS} 刷新。</p>
     *
     * @throws NacosException nacos exception
     */
    public void startRefreshServerListTask(NacosClientProperties properties) throws NacosException {
        for (int i = 0; i < initServerListRetryTimes && getServerList().isEmpty(); ++i) {
            refreshServerListIfNeed();
            if (!serversFromEndpoint.isEmpty()) {
                break;
            }
            try {
                this.wait((i + 1) * 100L);
            } catch (Exception e) {
                LOGGER.warn("get serverlist fail,url: {}", addressServerUrl);
            }
        }
        
        if (serversFromEndpoint.isEmpty()) {
            LOGGER.error("[init-serverlist] fail to get NACOS-server serverlist! url: {}",
                addressServerUrl);
            throw new NacosException(NacosException.SERVER_ERROR,
                "fail to get NACOS-server serverlist! not connnect url:" + addressServerUrl);
        }
        
        refreshServerListExecutor = new ScheduledThreadPoolExecutor(1,
            new NameThreadFactory(
                "com.alibaba.nacos.client.address.EndpointServerListProvider.refreshServerList"));
        // 定时任务按配置间隔刷新 endpoint 地址列表
        long refreshInterval = Long.parseLong(
            properties.getProperty(PropertyKeyConst.ENDPOINT_REFRESH_INTERVAL_SECONDS, "30"));
        refreshServerListExecutor.scheduleWithFixedDelay(this::refreshServerListIfNeed, 0L,
            refreshInterval,
            TimeUnit.SECONDS);
    }
    
    /** 节流后拉取 endpoint 列表，有变更则更新缓存并发布 ServerListChangeEvent */
    private void refreshServerListIfNeed() {
        try {
            if (System.currentTimeMillis()
                - lastServerListRefreshTime < refreshServerListInternal) {
                return;
            }
            List<String> list = getServerListFromEndpoint();
            if (CollectionUtils.isEmpty(list)) {
                throw new Exception("Can not acquire Nacos list");
            }
            list.sort(String::compareTo);
            if (!CollectionUtils.isEqualCollection(list, serversFromEndpoint)) {
                LOGGER.info("[SERVER-LIST] server list is updated: {}", list);
                serversFromEndpoint = list;
                lastServerListRefreshTime = System.currentTimeMillis();
                NotifyCenter.publishEvent(new ServerListChangeEvent());
            }
        } catch (Throwable e) {
            LOGGER.warn("failed to update server list", e);
        }
    }
    
    /** HTTP GET addressServerUrl，解析每行 ip[:port] 并补默认端口 */
    private List<String> getServerListFromEndpoint() {
        try {
            HttpRestResult<String> httpResult = nacosRestTemplate.get(addressServerUrl,
                HttpUtils.builderHeader(moduleName), Query.EMPTY, String.class);
            
            if (!httpResult.ok()) {
                LOGGER.error("[check-serverlist] error. addressServerUrl: {}, code: {}",
                    addressServerUrl,
                    httpResult.getCode());
                return null;
            }
            List<String> lines = IoUtils.readLines(new StringReader(httpResult.getData()));
            List<String> result = new ArrayList<>(lines.size());
            for (String serverAddr : lines) {
                String[] ipPort = InternetAddressUtil.splitIpPortStr(serverAddr);
                String ip = ipPort[0].trim();
                if (ipPort.length == 1) {
                    result.add(ip + InternetAddressUtil.IP_PORT_SPLITER
                        + ClientBasicParamUtil.getDefaultServerPort());
                } else {
                    result.add(serverAddr);
                }
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("[check-serverlist] exception. url: {}", addressServerUrl, e);
            return null;
        }
    }
    
    /** 解析并保存 endpoint 主机名（可经 parsing 规则处理） */
    private void initEndpoint(NacosClientProperties properties) {
        // match 已通过，endpoint 必非空
        this.endpoint = getEndPointTmp(properties);
    }
    
    /** 优先环境变量 ALIBABA_ALIWARE_ENDPOINT_PORT，否则读 ENDPOINT_PORT */
    private void initEndpointPort(NacosClientProperties properties) {
        String endpointPortTmp = TemplateUtils.stringEmptyAndThenExecute(
            properties.getProperty(PropertyKeyConst.SystemEnv.ALIBABA_ALIWARE_ENDPOINT_PORT),
            () -> properties.getProperty(PropertyKeyConst.ENDPOINT_PORT));
        if (StringUtils.isNotBlank(endpointPortTmp)) {
            this.endpointPort = Integer.parseInt(endpointPortTmp);
        }
    }
    
    /** 初始化 endpoint 专用 context path（可与 Nacos Server path 不同） */
    private void initEndpointContextPath(NacosClientProperties properties) {
        String endpointContextPathTmp = TemplateUtils.stringEmptyAndThenExecute(
            properties.getProperty(
                PropertyKeyConst.SystemEnv.ALIBABA_ALIWARE_ENDPOINT_CONTEXT_PATH),
            () -> properties.getProperty(PropertyKeyConst.ENDPOINT_CONTEXT_PATH));
        if (StringUtils.isNotBlank(endpointContextPathTmp)) {
            this.endpointContextPath = endpointContextPathTmp;
        }
    }
    
    /** 设置拉取路径中的 serverList 名称（集群名/ENDPOINT_CLUSTER_NAME） */
    private void initServerListName(NacosClientProperties properties) {
        String serverListNameTmp = properties.getProperty(PropertyKeyConst.ENDPOINT_CLUSTER_NAME);
        boolean isUseClusterName = Boolean.parseBoolean(
            properties.getProperty(PropertyKeyConst.IS_ADAPT_CLUSTER_NAME_USAGE));
        if (StringUtils.isBlank(serverListNameTmp) && isUseClusterName) {
            serverListNameTmp = properties.getProperty(PropertyKeyConst.CLUSTER_NAME);
        }
        if (!StringUtils.isBlank(serverListNameTmp)) {
            this.serverListName = serverListNameTmp;
        }
    }
    
    /** 拼装完整 address server HTTP URL（含 namespace 与 query 参数） */
    private void initAddressServerUrl(NacosClientProperties properties) {
        String contextPathTmp = StringUtils.isNotBlank(this.endpointContextPath)
            ? ContextPathUtil.normalizeContextPath(
                this.endpointContextPath)
            : ContextPathUtil.normalizeContextPath(this.contextPath);
        StringBuilder addressServerUrlTem = new StringBuilder(
            String.format("http://%s:%d%s/%s", this.endpoint, this.endpointPort, contextPathTmp,
                this.serverListName));
        boolean hasQueryString = false;
        if (StringUtils.isNotBlank(namespace)) {
            addressServerUrlTem.append("?namespace=").append(namespace);
            hasQueryString = true;
        }
        if (properties.containsKey(PropertyKeyConst.ENDPOINT_QUERY_PARAMS)) {
            addressServerUrlTem.append(hasQueryString ? "&" : "?");
            addressServerUrlTem
                .append(properties.getProperty(PropertyKeyConst.ENDPOINT_QUERY_PARAMS));
        }
        this.addressServerUrl = addressServerUrlTem.toString();
        LOGGER.info("address server url = {}", this.addressServerUrl);
    }
    
    /** 设置 HTTP 请求头中的客户端模块类型 */
    private void initModuleName(NacosClientProperties properties) {
        String moduleNameTmp = properties.getProperty(Constants.CLIENT_MODULE_TYPE);
        if (StringUtils.isNotBlank(moduleNameTmp)) {
            this.moduleName = moduleNameTmp;
        }
    }
    
    @Override
    public void shutdown() throws NacosException {
        if (null != refreshServerListExecutor) {
            refreshServerListExecutor.shutdown();
        }
    }
}
