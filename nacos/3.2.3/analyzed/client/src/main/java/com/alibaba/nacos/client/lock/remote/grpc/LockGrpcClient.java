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

package com.alibaba.nacos.client.lock.remote.grpc;

import com.alibaba.nacos.api.ability.constant.AbilityKey;
import com.alibaba.nacos.api.ability.constant.AbilityStatus;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.api.lock.constant.PropertyConstants;
import com.alibaba.nacos.api.lock.model.LockInstance;
import com.alibaba.nacos.api.lock.remote.AbstractLockRequest;
import com.alibaba.nacos.api.lock.remote.LockOperationEnum;
import com.alibaba.nacos.api.lock.remote.request.LockOperationRequest;
import com.alibaba.nacos.api.lock.remote.response.LockOperationResponse;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.remote.response.ResponseCode;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.lock.remote.AbstractLockClient;
import com.alibaba.nacos.client.security.SecurityProxy;
import com.alibaba.nacos.client.utils.AppNameUtils;
import com.alibaba.nacos.common.remote.ConnectionType;
import com.alibaba.nacos.common.remote.client.RpcClient;
import com.alibaba.nacos.common.remote.client.RpcClientFactory;
import com.alibaba.nacos.common.remote.client.RpcClientTlsConfigFactory;
import com.alibaba.nacos.common.remote.client.ServerListFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 基于 gRPC 的 Nacos 分布式锁远程客户端。
 *
 * <p>通过 {@link RpcClient} 发送 {@link LockOperationRequest}，在服务端能力支持
 * {@link AbilityKey#SERVER_DISTRIBUTED_LOCK} 时完成加锁与解锁。</p>
 *
 * @author 985492783@qq.com
 * @description LockGrpcClient
 * @date 2023/6/28 17:35
 */
public class LockGrpcClient extends AbstractLockClient {
    
    /** 本客户端实例唯一标识，用作 RpcClient 名称。 */
    private final String uuid;
    
    /** RPC 请求超时（毫秒）；{@code -1} 表示使用默认超时。 */
    private final Long requestTimeout;
    
    /** 底层 gRPC RPC 客户端。 */
    private final RpcClient rpcClient;
    
    /**
     * 创建 gRPC 锁客户端并启动连接。
     *
     * @param properties         客户端属性
     * @param serverListFactory  服务端地址列表工厂
     * @param securityProxy      鉴权代理
     * @throws NacosException 启动 RPC 客户端失败时抛出
     */
    public LockGrpcClient(NacosClientProperties properties, ServerListFactory serverListFactory,
        SecurityProxy securityProxy) throws NacosException {
        super(securityProxy);
        this.uuid = UUID.randomUUID().toString();
        this.requestTimeout = Long
            .parseLong(properties.getProperty(PropertyConstants.LOCK_REQUEST_TIMEOUT, "-1"));
        Map<String, String> labels = new HashMap<>();
        labels.put(RemoteConstants.LABEL_SOURCE, RemoteConstants.LABEL_SOURCE_SDK);
        labels.put(RemoteConstants.LABEL_MODULE, RemoteConstants.LABEL_MODULE_LOCK);
        labels.put(Constants.APPNAME, AppNameUtils.getAppName());
        this.rpcClient = RpcClientFactory.createClient(uuid, ConnectionType.GRPC, labels,
            RpcClientTlsConfigFactory.getInstance().createSdkConfig(properties.asProperties()));
        start(serverListFactory);
    }
    
    /**
     * 绑定服务端列表并启动 RPC 连接。
     *
     * @param serverListFactory 服务端地址列表工厂
     * @throws NacosException 启动失败时抛出
     */
    private void start(ServerListFactory serverListFactory) throws NacosException {
        rpcClient.serverListFactory(serverListFactory);
        rpcClient.start();
    }
    
    /**
     * 远程加锁（ACQUIRE 操作）。
     *
     * @param instance 锁实例
     * @return 服务端返回的加锁结果
     * @throws NacosException 服务端不支持锁或 RPC 失败时抛出
     */
    @Override
    public Boolean lock(LockInstance instance) throws NacosException {
        if (!isAbilitySupportedByServer()) {
            throw new NacosRuntimeException(NacosException.SERVER_NOT_IMPLEMENTED,
                "Request Nacos server version is too low, not support lock feature.");
        }
        LockOperationRequest request = new LockOperationRequest();
        request.setLockInstance(instance);
        request.setLockOperationEnum(LockOperationEnum.ACQUIRE);
        LockOperationResponse acquireLockResponse =
            requestToServer(request, LockOperationResponse.class);
        return (Boolean) acquireLockResponse.getResult();
    }
    
    /**
     * 远程解锁（RELEASE 操作）。
     *
     * @param instance 锁实例
     * @return 服务端返回的解锁结果
     * @throws NacosException 服务端不支持锁或 RPC 失败时抛出
     */
    @Override
    public Boolean unLock(LockInstance instance) throws NacosException {
        if (!isAbilitySupportedByServer()) {
            throw new NacosRuntimeException(NacosException.SERVER_NOT_IMPLEMENTED,
                "Request Nacos server version is too low, not support lock feature.");
        }
        LockOperationRequest request = new LockOperationRequest();
        request.setLockInstance(instance);
        request.setLockOperationEnum(LockOperationEnum.RELEASE);
        LockOperationResponse acquireLockResponse =
            requestToServer(request, LockOperationResponse.class);
        return (Boolean) acquireLockResponse.getResult();
    }
    
    /**
     * 关闭底层 RPC 客户端。
     *
     * @throws NacosException 关闭失败时抛出
     */
    @Override
    public void shutdown() throws NacosException {
        rpcClient.shutdown();
    }
    
    /**
     * 发送锁操作请求并解析响应。
     *
     * @param request       锁操作请求
     * @param responseClass 期望的响应类型
     * @param <T>           响应泛型
     * @return 成功响应体
     * @throws NacosException 响应码非成功或类型不匹配时抛出
     */
    private <T extends Response> T requestToServer(AbstractLockRequest request,
        Class<T> responseClass)
        throws NacosException {
        try {
            request.putAllHeader(getSecurityHeaders());
            Response response =
                requestTimeout < 0 ? rpcClient.request(request)
                    : rpcClient.request(request, requestTimeout);
            if (ResponseCode.SUCCESS.getCode() != response.getResultCode()) {
                throw new NacosException(response.getErrorCode(), response.getMessage());
            }
            if (responseClass.isAssignableFrom(response.getClass())) {
                return (T) response;
            }
        } catch (NacosException e) {
            throw e;
        } catch (Exception e) {
            throw new NacosException(NacosException.SERVER_ERROR, "Request nacos server failed: ",
                e);
        }
        throw new NacosException(NacosException.SERVER_ERROR, "Server return invalid response");
    }
    
    /**
     * 检查当前连接的服务端是否支持分布式锁能力。
     *
     * @return 支持返回 true
     */
    private boolean isAbilitySupportedByServer() {
        return rpcClient.getConnectionAbility(
            AbilityKey.SERVER_DISTRIBUTED_LOCK) == AbilityStatus.SUPPORTED;
    }
}
