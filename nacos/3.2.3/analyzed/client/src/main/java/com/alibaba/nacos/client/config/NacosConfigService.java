/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.config;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.ConfigQueryResult;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.filter.IConfigFilter;
import com.alibaba.nacos.api.config.listener.FuzzyWatchEventWatcher;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.config.filter.impl.ConfigFilterChainManager;
import com.alibaba.nacos.client.config.filter.impl.ConfigRequest;
import com.alibaba.nacos.client.config.filter.impl.ConfigResponse;
import com.alibaba.nacos.client.config.impl.ClientWorker;
import com.alibaba.nacos.client.config.impl.ConfigFuzzyWatchContext;
import com.alibaba.nacos.client.config.impl.ConfigServerListManager;
import com.alibaba.nacos.client.config.impl.LocalConfigInfoProcessor;
import com.alibaba.nacos.client.config.impl.LocalEncryptedDataKeyProcessor;
import com.alibaba.nacos.client.config.utils.ParamUtils;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.utils.ClientBasicParamUtil;
import com.alibaba.nacos.client.utils.LogUtils;
import com.alibaba.nacos.client.utils.PreInitUtils;
import com.alibaba.nacos.client.utils.ValidatorUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;

import static com.alibaba.nacos.api.common.Constants.ALL_PATTERN;

/**
 * {@link ConfigService} 客户端实现，提供配置拉取、发布、监听与模糊监听能力。
 *
 * <p>内部通过 {@link ClientWorker} 长轮询服务端，经 {@link ConfigFilterChainManager} 处理加解密等过滤逻辑。</p>
 *
 * @author Nacos
 */
public class NacosConfigService implements ConfigService {
    
    /** 日志记录器。 */
    private static final Logger LOGGER = LogUtils.logger(NacosConfigService.class);
    
    /** 服务端健康状态：可用。 */
    private static final String UP = "UP";
    
    /** 服务端健康状态：不可用。 */
    private static final String DOWN = "DOWN";
    
    /** 长轮询工作线程，负责与配置中心通信。 */
    private final ClientWorker worker;
    
    /** 当前命名空间。 */
    private String namespace;
    
    /** 配置过滤器链管理器。 */
    private final ConfigFilterChainManager configFilterChainManager;
    
    /**
     * 根据客户端属性初始化配置服务。
     *
     * @param properties 含 serverAddr、namespace 等
     * @throws NacosException 参数校验或启动失败
     */
    public NacosConfigService(Properties properties) throws NacosException {
        PreInitUtils.asyncPreLoadCostComponent();
        final NacosClientProperties clientProperties =
            NacosClientProperties.PROTOTYPE.derive(properties);
        LOGGER.info(ClientBasicParamUtil.getInputParameters(clientProperties.asProperties()));
        ValidatorUtils.checkInitParam(clientProperties);
        
        initNamespace(clientProperties);
        this.configFilterChainManager =
            new ConfigFilterChainManager(clientProperties.asProperties());
        ConfigServerListManager serverListManager = new ConfigServerListManager(clientProperties);
        serverListManager.start();
        
        this.worker = new ClientWorker(this.configFilterChainManager, serverListManager,
            clientProperties);
        
    }
    
    /** 解析并设置命名空间到客户端属性。 */
    private void initNamespace(NacosClientProperties properties) {
        namespace = ClientBasicParamUtil.parseNamespace(properties);
        properties.setProperty(PropertyKeyConst.NAMESPACE, namespace);
    }
    
    @Override
    /** 拉取配置内容（不含 MD5 等元数据）。 */
    public String getConfig(String dataId, String group, long timeoutMs) throws NacosException {
        return getConfigInner(namespace, dataId, group, timeoutMs);
    }
    
    @Override
    /** 拉取配置并注册监听器，返回解密后的内容。 */
    public String getConfigAndSignListener(String dataId, String group, long timeoutMs,
        Listener listener)
        throws NacosException {
        group = StringUtils.isBlank(group) ? Constants.DEFAULT_GROUP : group.trim();
        ConfigResponse configResponse = worker.getAgent()
            .queryConfig(dataId, group, worker.getAgent().getTenant(), timeoutMs, false);
        String content = configResponse.getContent();
        String encryptedDataKey = configResponse.getEncryptedDataKey();
        worker.addTenantListenersWithContent(dataId, group, content, encryptedDataKey,
            Collections.singletonList(listener));
        
        // 经过滤器链解密后再返回，修复 issue #7039
        ConfigResponse cr = new ConfigResponse();
        cr.setDataId(dataId);
        cr.setGroup(group);
        cr.setContent(content);
        cr.setEncryptedDataKey(encryptedDataKey);
        configFilterChainManager.doFilter(null, cr);
        return cr.getContent();
    }
    
    @Override
    /** 为指定 dataId/group 添加配置变更监听器。 */
    public void addListener(String dataId, String group, Listener listener) throws NacosException {
        worker.addTenantListeners(dataId, group, Collections.singletonList(listener));
    }
    
    @Override
    /** 按 group 模式模糊监听配置变更。 */
    public void fuzzyWatch(String groupNamePattern, FuzzyWatchEventWatcher watcher)
        throws NacosException {
        doAddFuzzyWatch(ALL_PATTERN, groupNamePattern, watcher);
    }
    
    @Override
    public void fuzzyWatch(String dataIdPattern, String groupNamePattern,
        FuzzyWatchEventWatcher watcher)
        throws NacosException {
        doAddFuzzyWatch(dataIdPattern, groupNamePattern, watcher);
    }
    
    @Override
    public Future<Set<String>> fuzzyWatchWithGroupKeys(String groupNamePattern,
        FuzzyWatchEventWatcher watcher)
        throws NacosException {
        return doAddFuzzyWatch(ALL_PATTERN, groupNamePattern, watcher);
    }
    
    @Override
    public Future<Set<String>> fuzzyWatchWithGroupKeys(String dataIdPattern,
        String groupNamePattern,
        FuzzyWatchEventWatcher watcher) throws NacosException {
        return doAddFuzzyWatch(dataIdPattern, groupNamePattern, watcher);
    }
    
    private Future<Set<String>> doAddFuzzyWatch(String dataIdPattern, String groupNamePattern,
        FuzzyWatchEventWatcher watcher) throws NacosException {
        ConfigFuzzyWatchContext configFuzzyWatchContext =
            worker.addTenantFuzzyWatcher(dataIdPattern, groupNamePattern,
                watcher);
        return configFuzzyWatchContext.createNewFuture();
    }
    
    @Override
    public void cancelFuzzyWatch(String groupNamePattern, FuzzyWatchEventWatcher watcher)
        throws NacosException {
        cancelFuzzyWatch(ALL_PATTERN, groupNamePattern, watcher);
    }
    
    @Override
    public void cancelFuzzyWatch(String dataIdPattern, String groupNamePattern,
        FuzzyWatchEventWatcher watcher)
        throws NacosException {
        doCancelFuzzyWatch(dataIdPattern, groupNamePattern, watcher);
    }
    
    private void doCancelFuzzyWatch(String dataIdPattern, String groupNamePattern,
        FuzzyWatchEventWatcher watcher)
        throws NacosException {
        if (null == watcher) {
            return;
        }
        worker.removeFuzzyListenListener(dataIdPattern, groupNamePattern, watcher);
    }
    
    @Override
    /** 发布配置（默认类型）。 */
    public boolean publishConfig(String dataId, String group, String content)
        throws NacosException {
        return publishConfig(dataId, group, content, ConfigType.getDefaultType().getType());
    }
    
    @Override
    public boolean publishConfig(String dataId, String group, String content, String type)
        throws NacosException {
        return publishConfigInner(namespace, dataId, group, null, null, null, content, type, null);
    }
    
    @Override
    /** CAS 条件发布配置，md5 不匹配则失败。 */
    public boolean publishConfigCas(String dataId, String group, String content, String casMd5)
        throws NacosException {
        return publishConfigInner(namespace, dataId, group, null, null, null, content,
            ConfigType.getDefaultType().getType(), casMd5);
    }
    
    @Override
    public boolean publishConfigCas(String dataId, String group, String content, String casMd5,
        String type)
        throws NacosException {
        return publishConfigInner(namespace, dataId, group, null, null, null, content, type,
            casMd5);
    }
    
    @Override
    /** 删除指定配置。 */
    public boolean removeConfig(String dataId, String group) throws NacosException {
        return removeConfigInner(namespace, dataId, group, null);
    }
    
    @Override
    /** 移除配置变更监听器。 */
    public void removeListener(String dataId, String group, Listener listener) {
        worker.removeTenantListener(dataId, group, listener);
    }
    
    private String getConfigInner(String tenant, String dataId, String group, long timeoutMs)
        throws NacosException {
        group = blank2defaultGroup(group);
        ParamUtils.checkKeyParam(dataId, group);
        ConfigResponse cr = new ConfigResponse();
        
        cr.setDataId(dataId);
        cr.setTenant(tenant);
        cr.setGroup(group);
        
        // 优先尝试本地 failover 缓存（由用户维护，非客户端自动生成）
        // 适用于服务端宕机时客户端紧急重启仍需使用配置的场景
        String content =
            LocalConfigInfoProcessor.getFailover(worker.getAgentName(), dataId, group, tenant);
        if (content != null) {
            LOGGER.warn("[{}] [get-config] get failover ok, dataId={}, group={}, tenant={}",
                worker.getAgentName(),
                dataId, group, tenant);
            cr.setContent(content);
            String encryptedDataKey =
                LocalEncryptedDataKeyProcessor.getEncryptDataKeyFailover(worker.getAgentName(),
                    dataId, group, tenant);
            cr.setEncryptedDataKey(encryptedDataKey);
            configFilterChainManager.doFilter(null, cr);
            content = cr.getContent();
            return content;
        }
        
        try {
            ConfigResponse response =
                worker.getServerConfig(dataId, group, tenant, timeoutMs, false);
            cr.setContent(response.getContent());
            cr.setEncryptedDataKey(response.getEncryptedDataKey());
            configFilterChainManager.doFilter(null, cr);
            content = cr.getContent();
            
            return content;
        } catch (NacosException ioe) {
            if (NacosException.NO_RIGHT == ioe.getErrCode()) {
                throw ioe;
            }
            LOGGER.warn(
                "[{}] [get-config] get from server error, dataId={}, group={}, tenant={}, msg={}",
                worker.getAgentName(), dataId, group, tenant, ioe.toString());
        }
        
        content =
            LocalConfigInfoProcessor.getSnapshot(worker.getAgentName(), dataId, group, tenant);
        if (content != null) {
            LOGGER.warn("[{}] [get-config] get snapshot ok, dataId={}, group={}, tenant={}",
                worker.getAgentName(),
                dataId, group, tenant);
        }
        cr.setContent(content);
        String encryptedDataKey =
            LocalEncryptedDataKeyProcessor.getEncryptDataKeySnapshot(worker.getAgentName(),
                dataId, group, tenant);
        cr.setEncryptedDataKey(encryptedDataKey);
        configFilterChainManager.doFilter(null, cr);
        content = cr.getContent();
        return content;
    }
    
    /** 空 group 时使用默认分组。 */
    private String blank2defaultGroup(String group) {
        return (StringUtils.isBlank(group)) ? Constants.DEFAULT_GROUP : group.trim();
    }
    
    private ConfigResponse getConfigInnerWithResponse(String tenant, String dataId, String group,
        long timeoutMs)
        throws NacosException {
        group = blank2defaultGroup(group);
        ParamUtils.checkKeyParam(dataId, group);
        ConfigResponse cr = new ConfigResponse();
        
        cr.setDataId(dataId);
        cr.setTenant(tenant);
        cr.setGroup(group);
        
        // 优先尝试本地 failover
        String content =
            LocalConfigInfoProcessor.getFailover(worker.getAgentName(), dataId, group, tenant);
        if (content != null) {
            LOGGER.warn("[{}] [get-config] get failover ok, dataId={}, group={}, tenant={}",
                worker.getAgentName(),
                dataId, group, tenant);
            cr.setContent(content);
            String encryptedDataKey =
                LocalEncryptedDataKeyProcessor.getEncryptDataKeyFailover(worker.getAgentName(),
                    dataId, group, tenant);
            cr.setEncryptedDataKey(encryptedDataKey);
            // Failover 路径无服务端 MD5
            configFilterChainManager.doFilter(null, cr);
            return cr;
        }
        
        try {
            ConfigResponse response =
                worker.getServerConfig(dataId, group, tenant, timeoutMs, false);
            cr.setContent(response.getContent());
            cr.setMd5(response.getMd5());
            cr.setEncryptedDataKey(response.getEncryptedDataKey());
            cr.setConfigType(response.getConfigType());
            configFilterChainManager.doFilter(null, cr);
            return cr;
        } catch (NacosException ioe) {
            if (NacosException.NO_RIGHT == ioe.getErrCode()) {
                throw ioe;
            }
            LOGGER.warn(
                "[{}] [get-config] get from server error, dataId={}, group={}, tenant={}, msg={}",
                worker.getAgentName(), dataId, group, tenant, ioe.toString());
        }
        
        // 回退到本地 snapshot
        content =
            LocalConfigInfoProcessor.getSnapshot(worker.getAgentName(), dataId, group, tenant);
        if (content != null) {
            LOGGER.warn("[{}] [get-config] get snapshot ok, dataId={}, group={}, tenant={}",
                worker.getAgentName(),
                dataId, group, tenant);
        }
        cr.setContent(content);
        String encryptedDataKey =
            LocalEncryptedDataKeyProcessor.getEncryptDataKeySnapshot(worker.getAgentName(),
                dataId, group, tenant);
        cr.setEncryptedDataKey(encryptedDataKey);
        // Snapshot 路径无服务端 MD5
        configFilterChainManager.doFilter(null, cr);
        return cr;
    }
    
    @Override
    /** 拉取配置并返回含 MD5、类型等元数据的结果。 */
    public ConfigQueryResult getConfigWithResult(String dataId, String group, long timeoutMs)
        throws NacosException {
        ConfigResponse response = getConfigInnerWithResponse(namespace, dataId, group, timeoutMs);
        ConfigQueryResult result = new ConfigQueryResult();
        result.setContent(response.getContent());
        result.setMd5(response.getMd5());
        result.setConfigType(response.getConfigType());
        result.setEncryptedDataKey(response.getEncryptedDataKey());
        return result;
    }
    
    private boolean removeConfigInner(String tenant, String dataId, String group, String tag)
        throws NacosException {
        group = blank2defaultGroup(group);
        ParamUtils.checkKeyParam(dataId, group);
        return worker.removeConfig(dataId, group, tenant, tag);
    }
    
    private boolean publishConfigInner(String tenant, String dataId, String group, String tag,
        String appName,
        String betaIps, String content, String type, String casMd5) throws NacosException {
        group = blank2defaultGroup(group);
        ParamUtils.checkParam(dataId, group, content);
        
        ConfigRequest cr = new ConfigRequest();
        cr.setDataId(dataId);
        cr.setTenant(tenant);
        cr.setGroup(group);
        cr.setContent(content);
        cr.setType(type);
        configFilterChainManager.doFilter(cr, null);
        content = cr.getContent();
        String encryptedDataKey = cr.getEncryptedDataKey();
        
        return worker.publishConfig(dataId, group, tenant, appName, tag, betaIps, content,
            encryptedDataKey, casMd5,
            type);
    }
    
    @Override
    /** 返回配置服务端健康状态 UP/DOWN。 */
    public String getServerStatus() {
        if (worker.isHealthServer()) {
            return UP;
        } else {
            return DOWN;
        }
    }
    
    @Override
    /** 动态追加自定义配置过滤器。 */
    public void addConfigFilter(IConfigFilter configFilter) {
        configFilterChainManager.addFilter(configFilter);
    }
    
    @Override
    /** 关闭客户端工作线程与资源。 */
    public void shutDown() throws NacosException {
        worker.shutdown();
    }
}
