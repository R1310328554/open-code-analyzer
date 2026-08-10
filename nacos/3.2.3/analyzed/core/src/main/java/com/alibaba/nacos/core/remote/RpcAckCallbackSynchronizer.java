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

package com.alibaba.nacos.core.remote;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.DefaultRequestFuture;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.core.utils.Loggers;
import com.alipay.hessian.clhm.ConcurrentLinkedHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 服务端 Push 请求的 ACK 回调同步器，按 connectionId 维护待确认 Future。
 * server push ack synchronier.
 *
 * @author liuzunfei
 * @version $Id: RpcAckCallbackSynchronizer.java, v 0.1 2020年07月29日 7:56 PM liuzunfei Exp $
 */
public class RpcAckCallbackSynchronizer {
    
    /** 全局 ACK 回调上下文：connectionId → (requestId → Future)。 */
    @SuppressWarnings("checkstyle:linelength")
    public static final Map<String, Map<String, DefaultRequestFuture>> CALLBACK_CONTEXT =
        new ConcurrentLinkedHashMap.Builder<String, Map<String, DefaultRequestFuture>>()
            .maximumWeightedCapacity(1000000)
            .listener((s, pushCallBack) -> pushCallBack.entrySet().forEach(
                stringDefaultPushFutureEntry -> stringDefaultPushFutureEntry.getValue()
                    .setFailResult(new TimeoutException())))
            .build();
    
    /**
     * 收到客户端 ACK 后完成对应 Future 并移除回调。
     * notify  ack.
     *
     * @param connectionId connectionId
     * @param response     response
     */
    public static void ackNotify(String connectionId, Response response) {
        
        Map<String, DefaultRequestFuture> stringDefaultPushFutureMap =
            CALLBACK_CONTEXT.get(connectionId);
        if (stringDefaultPushFutureMap == null) {
            
            Loggers.REMOTE_DIGEST
                .warn("Ack receive on a outdated connection ,connection id={},requestId={} ",
                    connectionId,
                    response.getRequestId());
            return;
        }
        
        DefaultRequestFuture currentCallback =
            stringDefaultPushFutureMap.remove(response.getRequestId());
        if (currentCallback == null) {
            
            Loggers.REMOTE_DIGEST
                .warn("Ack receive on a outdated request ,connection id={},requestId={} ",
                    connectionId,
                    response.getRequestId());
            return;
        }
        
        if (response.isSuccess()) {
            currentCallback.setResponse(response);
        } else {
            currentCallback
                .setFailResult(new NacosException(response.getErrorCode(), response.getMessage()));
        }
    }
    
    /**
     * 注册待 ACK 的 Push Future，requestId 冲突时抛出异常。
     * sync callback.
     *
     * @param connectionId      connectionId
     * @param requestId         requestId
     * @param defaultPushFuture defaultPushFuture
     * @throws NacosException NacosException
     */
    public static void syncCallback(String connectionId, String requestId,
        DefaultRequestFuture defaultPushFuture)
        throws NacosException {
        
        Map<String, DefaultRequestFuture> stringDefaultPushFutureMap =
            initContextIfNecessary(connectionId);
        
        if (!stringDefaultPushFutureMap.containsKey(requestId)) {
            DefaultRequestFuture pushCallBackPrev = stringDefaultPushFutureMap
                .putIfAbsent(requestId, defaultPushFuture);
            if (pushCallBackPrev == null) {
                return;
            }
        }
        throw new NacosException(NacosException.INVALID_PARAM, "request id conflict");
        
    }
    
    /**
     * 连接断开时清除该 connectionId 的全部 ACK 上下文。
     * clear context of connectionId.
     *
     * @param connectionId connectionId
     */
    public static void clearContext(String connectionId) {
        CALLBACK_CONTEXT.remove(connectionId);
    }
    
    /**
     * 按需为 connectionId 初始化 ACK 回调子 Map。
     * init context of connectionId if necessary.
     *
     * @param connectionId connectionId
     */
    public static Map<String, DefaultRequestFuture> initContextIfNecessary(String connectionId) {
        if (!CALLBACK_CONTEXT.containsKey(connectionId)) {
            Map<String, DefaultRequestFuture> context = new HashMap<>(128);
            Map<String, DefaultRequestFuture> stringDefaultRequestFutureMap = CALLBACK_CONTEXT
                .putIfAbsent(connectionId, context);
            return stringDefaultRequestFutureMap == null ? context : stringDefaultRequestFutureMap;
        } else {
            return CALLBACK_CONTEXT.get(connectionId);
        }
    }
    
    /**
     * 移除指定 requestId 的待 ACK Future。
     * clear context of requestId.
     *
     * @param connectionId connectionId
     * @param requestId    requestId
     */
    public static void clearFuture(String connectionId, String requestId) {
        Map<String, DefaultRequestFuture> stringDefaultPushFutureMap =
            CALLBACK_CONTEXT.get(connectionId);
        
        if (stringDefaultPushFutureMap == null
            || !stringDefaultPushFutureMap.containsKey(requestId)) {
            return;
        }
        stringDefaultPushFutureMap.remove(requestId);
    }
    
}
