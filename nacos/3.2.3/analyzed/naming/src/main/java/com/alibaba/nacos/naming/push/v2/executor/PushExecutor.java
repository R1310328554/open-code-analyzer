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

/**
 * v2 推送执行器接口。
 *
 * <p>定义向客户端推送服务实例、带回调推送及模糊 Watch 通知三类能力。</p>
 *
 * @author xiweng.yy
 */
public interface PushExecutor {
    
    /**
     * 向指定客户端推送服务实例数据（无 ACK）。
     *
     * @param clientId   客户端 ID
     * @param subscriber 订阅者信息
     * @param data       推送数据包装
     */
    void doPush(String clientId, Subscriber subscriber, PushDataWrapper data);
    
    /**
     * 带回调的推送，用于延迟任务引擎统计成功/失败与重试。
     *
     * @param clientId   客户端 ID
     * @param subscriber 订阅者信息
     * @param data       推送数据包装
     * @param callBack   推送完成回调
     */
    void doPushWithCallback(String clientId, Subscriber subscriber, PushDataWrapper data,
        NamingPushCallback callBack);
    
    /**
     * 向模糊 Watch 客户端推送变更/同步通知（带回调）。
     *
     * @param clientId 客户端 ID
     * @param fuzzyWatchNotifyRequest 模糊 Watch 通知请求
     * @param callBack 推送完成回调
     */
    void doFuzzyWatchNotifyPushWithCallBack(String clientId,
        AbstractFuzzyWatchNotifyRequest fuzzyWatchNotifyRequest, PushCallBack callBack);
    
}
