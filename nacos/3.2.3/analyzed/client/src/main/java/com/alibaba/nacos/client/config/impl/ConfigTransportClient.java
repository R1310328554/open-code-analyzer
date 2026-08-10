/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.config.impl;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.config.filter.impl.ConfigResponse;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.security.SecurityProxy;
import com.alibaba.nacos.client.utils.AppNameUtils;
import com.alibaba.nacos.client.utils.ClientBasicParamUtil;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.utils.ConvertUtils;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.api.RequestResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 配置传输客户端抽象基类。
 *
 * <p>封装编码、命名空间、安全代理与公共请求头，定义监听、查询、发布、删除等配置模块基础远程操作的模板方法。</p>
 *
 * @author liuzunfei
 * @version $Id: ConfigTransportClient.java, v 0.1 2020年08月24日 2:01 PM liuzunfei Exp $
 */
public abstract class ConfigTransportClient {
    
    private static final String CONFIG_INFO_HEADER = "exConfigInfo";
    
    private static final String DEFAULT_CONFIG_INFO = "true";
    
    /** 配置内容字符编码。 */
    String encode;
    
    /** 默认命名空间（tenant）。 */
    String tenant;
    
    private ThreadPoolExecutor executor;
    
    final ConfigServerListManager serverListManager;
    
    final Properties properties;
    
    private int maxRetry = 3;
    
    private final long securityInfoRefreshIntervalMills = TimeUnit.SECONDS.toMillis(5);
    
    private ScheduledExecutorService loginScheduledExecutor;
    
    /** 安全代理，负责登录与请求签名。 */
    protected SecurityProxy securityProxy;
    
    /** 关闭客户端，释放安全代理与登录调度线程等资源。 */
    /** Shut down to ensure resource release. */
    /** 关闭以释放资源。 */
    public void shutdown() throws NacosException {
        securityProxy.shutdown();
        if (loginScheduledExecutor != null && !loginScheduledExecutor.isShutdown()) {
            loginScheduledExecutor.shutdown();
        }
    }
    
    public ConfigTransportClient(NacosClientProperties properties,
        ConfigServerListManager serverListManager) {
        
        String encodeTmp = properties.getProperty(PropertyKeyConst.ENCODE);
        if (StringUtils.isBlank(encodeTmp)) {
            this.encode = Constants.ENCODE;
        } else {
            this.encode = encodeTmp.trim();
        }
        
        this.tenant = properties.getProperty(PropertyKeyConst.NAMESPACE);
        this.serverListManager = serverListManager;
        this.properties = properties.asProperties();
        this.securityProxy = new SecurityProxy(serverListManager,
            ConfigHttpClientManager.getInstance().getNacosRestTemplate());
    }
    
    /**
     * 构建当前请求的鉴权资源描述。
     *
     * @param tenant 命名空间
     * @param group  配置分组
     * @param dataId 配置 Data ID
     * @return 鉴权资源对象
     */
    protected RequestResource buildResource(String tenant, String group, String dataId) {
        return RequestResource.configBuilder().setNamespace(tenant).setGroup(group)
            .setResource(dataId).build();
    }
    
    protected Map<String, String> getSecurityHeaders(RequestResource resource) throws Exception {
        return securityProxy.getIdentityContext(resource);
    }
    
    /**
     * 组装配置请求的公共 HTTP 头（应用名、时间戳、Token、编码等）。
     *
     * @return 请求头映射
     */
    protected Map<String, String> getCommonHeader() {
        Map<String, String> headers = new HashMap<>(16);
        
        String ts = String.valueOf(System.currentTimeMillis());
        String token = MD5Utils.md5Hex(ts + ClientBasicParamUtil.getAppKey(), Constants.ENCODE);
        
        headers.put(Constants.CLIENT_APPNAME_HEADER, AppNameUtils.getAppName());
        headers.put(Constants.CLIENT_REQUEST_TS_HEADER, ts);
        headers.put(Constants.CLIENT_REQUEST_TOKEN_HEADER, token);
        headers.put(CONFIG_INFO_HEADER, DEFAULT_CONFIG_INFO);
        headers.put(Constants.CHARSET_KEY, encode);
        return headers;
    }
    
    private void initMaxRetry(Properties properties) {
        maxRetry = ConvertUtils.toInt(String.valueOf(properties.get(PropertyKeyConst.MAX_RETRY)),
            Constants.MAX_RETRY);
    }
    
    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }
    
    public ThreadPoolExecutor getExecutor() {
        return this.executor;
    }
    
    /** 启动客户端：登录并周期性刷新安全凭证，再调用 {@link #startInternal()}。 */
    /** base start client. */
    /** 启动客户端基类逻辑。 */
    public void start() throws NacosException {
        securityProxy.login(this.properties);
        this.loginScheduledExecutor =
            Executors.newSingleThreadScheduledExecutor(
                new NameThreadFactory("com.alibaba.nacos.client.login-executor"));
        this.loginScheduledExecutor.scheduleWithFixedDelay(() -> securityProxy.login(properties), 0,
            this.securityInfoRefreshIntervalMills, TimeUnit.MILLISECONDS);
        startInternal();
    }
    
    public void reLogin() {
        securityProxy.reLogin();
    }
    
    /**
     * start client inner.
     *
     * @throws NacosException exception may throw.
      * <p>配置传输抽象基类；定义远程 CRUD 与监听接口。</p>
     */
    public abstract void startInternal() throws NacosException;
    
    /**
     * get client name.
     *
     * @return name.
      * <p>配置传输抽象基类；定义远程 CRUD 与监听接口。</p>
     */
    public abstract String getName();
    
    /**
     * get encode.
     *
     * @return encode.
      * <p>配置传输抽象基类；定义远程 CRUD 与监听接口。</p>
     */
    public String getEncode() {
        return this.encode;
    }
    
    /**
     * get tenant.
     *
     * @return tenant.
      * <p>配置传输抽象基类；定义远程 CRUD 与监听接口。</p>
     */
    public String getTenant() {
        return this.tenant;
    }
    
    /**
     * notify listen config.
     **/
    public abstract void notifyListenConfig();
    
    /**
     * listen change .
     *
     * @throws NacosException nacos exception throws, should retry.
      * <p>配置传输抽象基类；定义远程 CRUD 与监听接口。</p>
     */
    public abstract void executeConfigListen() throws NacosException;
    
    /**
     * remove cache implements.
     *
     * @param dataId dataId.
     * @param group  group
      * <p>配置传输抽象基类；定义远程 CRUD 与监听接口。</p>
     */
    public abstract void removeCache(String dataId, String group);
    
    /**
     * query config.
     *
     * @param dataId      dataId.
     * @param group       group.
     * @param tenat       tenat.
     * @param readTimeous readTimeous.
     * @param notify      query for notify sync.
     * @return content.
     * @throws NacosException throw where query fail .
      * <p>配置传输抽象基类；定义远程 CRUD 与监听接口。</p>
     */
    public abstract ConfigResponse queryConfig(String dataId, String group, String tenat,
        long readTimeous,
        boolean notify) throws NacosException;
    
    /**
     * publish config.
     *
     * @param dataId           dataId.
     * @param group            group.
     * @param tenant           tenant.
     * @param appName          appName.
     * @param tag              tag.
     * @param betaIps          betaIps.
     * @param content          content.
     * @param encryptedDataKey encryptedDataKey
     * @param casMd5           casMd5.
     * @param type             type.
     * @return success or not.
     * @throws NacosException throw where publish fail.
      * <p>配置传输抽象基类；定义远程 CRUD 与监听接口。</p>
     */
    public abstract boolean publishConfig(String dataId, String group, String tenant,
        String appName, String tag,
        String betaIps, String content, String encryptedDataKey, String casMd5, String type)
        throws NacosException;
    
    /**
     * remove config.
     *
     * @param dataid dataid.
     * @param group  group.
     * @param tenat  tenat.
     * @param tag    tag.
     * @return success or not.
     * @throws NacosException throw where publish fail.
      * <p>配置传输抽象基类；定义远程 CRUD 与监听接口。</p>
     */
    public abstract boolean removeConfig(String dataid, String group, String tenat, String tag)
        throws NacosException;
    
}
