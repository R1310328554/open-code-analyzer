/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.lock;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.lock.LockService;
import com.alibaba.nacos.api.lock.model.LockInstance;
import com.alibaba.nacos.client.address.AbstractServerListManager;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.lock.remote.grpc.LockGrpcClient;
import com.alibaba.nacos.client.naming.core.NamingServerListManager;
import com.alibaba.nacos.client.naming.remote.http.NamingHttpClientManager;
import com.alibaba.nacos.client.security.SecurityProxy;

import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.client.constant.Constants.Security.SECURITY_INFO_REFRESH_INTERVAL_MILLS;

/**
 * Nacos 分布式锁客户端服务实现。
 *
 * <p>实现 {@link LockService}，通过 {@link LockGrpcClient} 与 Nacos 服务端通信完成加锁/解锁；
 * 同时维护 {@link SecurityProxy} 定期刷新鉴权信息。</p>
 *
 * @author 985492783@qq.com
 * @date 2023/8/24 19:51
 */
public class NacosLockService implements LockService {
    
    /** gRPC 锁远程客户端。 */
    private final LockGrpcClient lockGrpcClient;
    
    /** 鉴权代理，负责登录与请求头注入。 */
    private final SecurityProxy securityProxy;
    
    /** 鉴权信息定时刷新线程池。 */
    private ScheduledExecutorService executorService;
    
    /**
     * 根据客户端属性初始化锁服务。
     *
     * <p>启动命名服务地址列表管理器、安全代理与 gRPC 锁客户端。</p>
     *
     * @param properties 客户端配置
     * @throws NacosException 初始化失败时抛出
     */
    public NacosLockService(Properties properties) throws NacosException {
        NacosClientProperties nacosClientProperties =
            NacosClientProperties.PROTOTYPE.derive(properties);
        AbstractServerListManager serverListManager = new NamingServerListManager(properties);
        serverListManager.start();
        this.securityProxy = new SecurityProxy(serverListManager,
            NamingHttpClientManager.getInstance().getNacosRestTemplate());
        initSecurityProxy(nacosClientProperties);
        this.lockGrpcClient =
            new LockGrpcClient(nacosClientProperties, serverListManager, securityProxy);
    }
    
    /**
     * 初始化安全代理并启动鉴权信息定时刷新任务。
     *
     * @param properties 客户端属性视图
     */
    private void initSecurityProxy(NacosClientProperties properties) {
        this.executorService = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r);
            t.setName("com.alibaba.nacos.client.lock.security");
            t.setDaemon(true);
            return t;
        });
        final Properties nacosClientPropertiesView = properties.asProperties();
        this.securityProxy.login(nacosClientPropertiesView);
        // 周期性刷新鉴权令牌，避免过期
        this.executorService.scheduleWithFixedDelay(
            () -> securityProxy.login(nacosClientPropertiesView), 0,
            SECURITY_INFO_REFRESH_INTERVAL_MILLS, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 本地加锁：委托 {@link LockInstance#lock(LockService)}。
     *
     * @param instance 锁实例
     * @return 加锁是否成功
     * @throws NacosException 加锁失败时抛出
     */
    @Override
    public Boolean lock(LockInstance instance) throws NacosException {
        return instance.lock(this);
    }
    
    /**
     * 本地解锁：委托 {@link LockInstance#unLock(LockService)}。
     *
     * @param instance 锁实例
     * @return 解锁是否成功
     * @throws NacosException 解锁失败时抛出
     */
    @Override
    public Boolean unLock(LockInstance instance) throws NacosException {
        return instance.unLock(this);
    }
    
    /**
     * 远程尝试加锁。
     *
     * @param instance 锁实例
     * @return 服务端返回的加锁结果
     * @throws NacosException RPC 或业务失败时抛出
     */
    @Override
    public Boolean remoteTryLock(LockInstance instance) throws NacosException {
        return lockGrpcClient.lock(instance);
    }
    
    /**
     * 远程释放锁。
     *
     * @param instance 锁实例
     * @return 服务端返回的解锁结果
     * @throws NacosException RPC 或业务失败时抛出
     */
    @Override
    public Boolean remoteReleaseLock(LockInstance instance) throws NacosException {
        return lockGrpcClient.unLock(instance);
    }
    
    /**
     * 关闭 gRPC 客户端与鉴权刷新线程池。
     *
     * @throws NacosException 关闭失败时抛出
     */
    @Override
    public void shutdown() throws NacosException {
        lockGrpcClient.shutdown();
        if (null != executorService) {
            executorService.shutdown();
        }
    }
}
