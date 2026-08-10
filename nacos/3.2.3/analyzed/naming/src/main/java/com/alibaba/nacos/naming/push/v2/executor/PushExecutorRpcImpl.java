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

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.remote.request.AbstractFuzzyWatchNotifyRequest;
import com.alibaba.nacos.api.naming.remote.request.NotifySubscriberRequest;
import com.alibaba.nacos.api.remote.PushCallBack;
import com.alibaba.nacos.core.remote.RpcPushService;
import com.alibaba.nacos.naming.misc.GlobalExecutor;
import com.alibaba.nacos.naming.pojo.Subscriber;
import com.alibaba.nacos.naming.push.v2.PushDataWrapper;
import com.alibaba.nacos.naming.push.v2.task.NamingPushCallback;
import com.alibaba.nacos.naming.utils.ServiceUtil;
import org.springframework.stereotype.Component;

/**
 * 基于 RPC 的默认推送执行器实现。
 *
 * <p>通过 {@link RpcPushService} 发送 {@link NotifySubscriberRequest} 或模糊 Watch 通知，推送前按健康保护与订阅者信息过滤实例。</p>
 *
 * @author xiweng.yy
 */
@Component
public class PushExecutorRpcImpl implements PushExecutor {
    
    private final RpcPushService pushService;
    
    public PushExecutorRpcImpl(RpcPushService pushService) {
        this.pushService = pushService;
    }
    
    /** 无 ACK 推送：构建 NotifySubscriberRequest 经 RPC 下发。 */
    @Override
    public void doPush(String clientId, Subscriber subscriber, PushDataWrapper data) {
        pushService.pushWithoutAck(clientId,
            NotifySubscriberRequest.buildNotifySubscriberRequest(getServiceInfo(data, subscriber)));
    }
    
    @Override
    public void doPushWithCallback(String clientId, Subscriber subscriber, PushDataWrapper data,
        NamingPushCallback callBack) {
        ServiceInfo actualServiceInfo = getServiceInfo(data, subscriber);
        callBack.setActualServiceInfo(actualServiceInfo);
        pushService.pushWithCallback(clientId,
            NotifySubscriberRequest.buildNotifySubscriberRequest(actualServiceInfo),
            callBack, GlobalExecutor.getCallbackExecutor());
    }
    
    /** 按服务元数据健康保护与订阅者信息筛选待推送实例列表。 */
    private ServiceInfo getServiceInfo(PushDataWrapper data, Subscriber subscriber) {
        return ServiceUtil
            .selectInstancesWithHealthyProtection(data.getOriginalData(), data.getServiceMetadata(),
                false, true,
                subscriber);
    }
    
    @Override
    public void doFuzzyWatchNotifyPushWithCallBack(String clientId,
        AbstractFuzzyWatchNotifyRequest watchNotifyRequest, PushCallBack callBack) {
        pushService.pushWithCallback(clientId, watchNotifyRequest, callBack,
            GlobalExecutor.getCallbackExecutor());
    }
    
}
