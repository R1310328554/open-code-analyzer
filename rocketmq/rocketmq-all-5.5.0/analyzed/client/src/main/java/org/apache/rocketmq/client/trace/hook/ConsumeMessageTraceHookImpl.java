/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.client.trace.hook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.rocketmq.client.consumer.listener.ConsumeReturnType;
import org.apache.rocketmq.client.hook.ConsumeMessageContext;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.apache.rocketmq.client.trace.TraceBean;
import org.apache.rocketmq.client.trace.TraceContext;
import org.apache.rocketmq.client.trace.TraceDispatcher;
import org.apache.rocketmq.client.trace.TraceType;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.NamespaceUtil;

/**
 * RocketMQ 原生消费轨迹 Hook：消费前上报 SubBefore，消费后上报 SubAfter，
 * 经 {@link TraceDispatcher} 异步写入轨迹 Topic。
 */
public class ConsumeMessageTraceHookImpl implements ConsumeMessageHook {

    /** 本地轨迹分发器（Consumer 侧）。 */
    private TraceDispatcher localDispatcher;

    /** 绑定 Consumer 侧 TraceDispatcher。 */
    public ConsumeMessageTraceHookImpl(TraceDispatcher localDispatcher) {
        this.localDispatcher = localDispatcher;
    }

    @Override
    public String hookName() {
        return "ConsumeMessageTraceHook";
    }

    @Override
    /** 消费前：构建 SubBefore 上下文并 append；trace 开关为 false 的消息跳过。 */
    public void consumeMessageBefore(ConsumeMessageContext context) {
        if (context == null || context.getMsgList() == null || context.getMsgList().isEmpty()) {
            return;
        }
        TraceContext traceContext = new TraceContext();
        context.setMqTraceContext(traceContext);
        traceContext.setTraceType(TraceType.SubBefore);
        traceContext.setGroupName(NamespaceUtil.withoutNamespace(context.getConsumerGroup()));
        List<TraceBean> beans = new ArrayList<>();
        for (MessageExt msg : context.getMsgList()) {
            if (msg == null) {
                continue;
            }
            String regionId = msg.getProperty(MessageConst.PROPERTY_MSG_REGION);
            String traceOn = msg.getProperty(MessageConst.PROPERTY_TRACE_SWITCH);

            if (traceOn != null && traceOn.equals("false")) {
                // 消息级 trace 开关关闭则跳过
                continue;
            }
            TraceBean traceBean = new TraceBean();
            traceBean.setTopic(NamespaceUtil.withoutNamespace(msg.getTopic()));
            traceBean.setMsgId(msg.getMsgId());
            traceBean.setTags(msg.getTags());
            traceBean.setKeys(msg.getKeys());
            traceBean.setStoreTime(msg.getStoreTimestamp());
            traceBean.setBodyLength(msg.getStoreSize());
            traceBean.setRetryTimes(msg.getReconsumeTimes());
            traceContext.setRegionId(regionId);
            beans.add(traceBean);
        }
        if (beans.size() > 0) {
            traceContext.setTraceBeans(beans);
            traceContext.setTimeStamp(System.currentTimeMillis());
            localDispatcher.append(traceContext);
        }
    }

    @Override
    /** 消费后：基于 SubBefore 计算耗时，构建 SubAfter 并 append。 */
    public void consumeMessageAfter(ConsumeMessageContext context) {
        if (context == null || context.getMsgList() == null || context.getMsgList().isEmpty()) {
            return;
        }
        TraceContext subBeforeContext = (TraceContext) context.getMqTraceContext();

        if (subBeforeContext.getTraceBeans() == null || subBeforeContext.getTraceBeans().size() < 1) {
            // 无有效 SubBefore 明细则跳过
            return;
        }
        TraceContext subAfterContext = new TraceContext();
        subAfterContext.setTraceType(TraceType.SubAfter);
        subAfterContext.setRegionId(subBeforeContext.getRegionId());
        subAfterContext.setGroupName(NamespaceUtil.withoutNamespace(subBeforeContext.getGroupName()));
        subAfterContext.setRequestId(subBeforeContext.getRequestId());
        subAfterContext.setAccessChannel(context.getAccessChannel());
        subAfterContext.setSuccess(context.isSuccess());

        // 按消息条数均摊计算单条处理耗时
        int costTime = (int) ((System.currentTimeMillis() - subBeforeContext.getTimeStamp()) / context.getMsgList().size());
        subAfterContext.setCostTime(costTime);
        subAfterContext.setTraceBeans(subBeforeContext.getTraceBeans());
        Map<String, String> props = context.getProps();
        if (props != null) {
            String contextType = props.get(MixAll.CONSUME_CONTEXT_TYPE);
            if (contextType != null) {
                subAfterContext.setContextCode(ConsumeReturnType.valueOf(contextType).ordinal());
            }
        }
        localDispatcher.append(subAfterContext);
    }
}
