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

package com.alibaba.nacos.naming.push.v2.executor;

import com.alibaba.nacos.api.naming.remote.request.AbstractFuzzyWatchNotifyRequest;
import com.alibaba.nacos.api.remote.PushCallBack;
import com.alibaba.nacos.naming.pojo.Subscriber;
import com.alibaba.nacos.naming.push.v2.PushDataWrapper;
import com.alibaba.nacos.naming.push.v2.task.NamingPushCallback;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 推送执行器委托，按 SPI 或默认 RPC 路由推送。
 *
 * <p>优先查找 {@link SpiPushExecutor} 扩展实现，未命中则使用 {@link PushExecutorRpcImpl}。</p>
 *
 * @author xiweng.yy
 */
@Component
public class PushExecutorDelegate implements PushExecutor {
    
    private final PushExecutorRpcImpl rpcPushExecuteService;
    
    public PushExecutorDelegate(PushExecutorRpcImpl rpcPushExecuteService) {
        this.rpcPushExecuteService = rpcPushExecuteService;
    }
    
    @Override
    public void doPush(String clientId, Subscriber subscriber, PushDataWrapper data) {
        getPushExecuteService(clientId, subscriber).doPush(clientId, subscriber, data);
    }
    
    @Override
    public void doPushWithCallback(String clientId, Subscriber subscriber, PushDataWrapper data,
        NamingPushCallback callBack) {
        getPushExecuteService(clientId, subscriber).doPushWithCallback(clientId, subscriber, data,
            callBack);
    }
    
    @Override
    public void doFuzzyWatchNotifyPushWithCallBack(String clientId,
        AbstractFuzzyWatchNotifyRequest watchNotifyRequest,
        PushCallBack callBack) {
        // 模糊 Watch 通知目前仅通过 RPC 推送
        rpcPushExecuteService.doFuzzyWatchNotifyPushWithCallBack(clientId, watchNotifyRequest,
            callBack);
    }
    
    /** 根据 clientId 与 subscriber 选择 SPI 或默认 RPC 执行器。 */
    private PushExecutor getPushExecuteService(String clientId, Subscriber subscriber) {
        Optional<SpiPushExecutor> result = SpiImplPushExecutorHolder.getInstance()
            .findPushExecutorSpiImpl(clientId, subscriber);
        if (result.isPresent()) {
            return result.get();
        }
        // 无 SPI 匹配时使用默认 RPC 推送执行器
        return rpcPushExecuteService;
    }
}
