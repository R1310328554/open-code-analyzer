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
package org.apache.rocketmq.client.trace;

import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 轨迹数据编解码器：在分隔符分隔的字符串与 {@link TraceContext} 列表之间互转。
 * 支持发布、消费前/后、事务结束、撤回等多种轨迹类型的序列化与兼容旧版格式。
 */
public class TraceDataEncoder {

    /**
     * 从轨迹数据字符串解析 {@link TraceContext} 列表。
     *
     * @param traceData 以 {@link TraceConstants#FIELD_SPLITOR} 分隔的轨迹载荷
     * @return 解析出的轨迹上下文列表，空输入返回空列表
     */
    public static List<TraceContext> decoderFromTraceDataString(String traceData) {
        List<TraceContext> resList = new ArrayList<>();
        if (traceData == null || traceData.length() <= 0) {
            return resList;
        }
        String[] contextList = traceData.split(String.valueOf(TraceConstants.FIELD_SPLITOR));
        for (String context : contextList) {
            String[] line = context.split(String.valueOf(TraceConstants.CONTENT_SPLITOR));
            if (line[0].equals(TraceType.Pub.name())) {
                TraceContext pubContext = new TraceContext();
                pubContext.setTraceType(TraceType.Pub);
                pubContext.setTimeStamp(Long.parseLong(line[1]));
                pubContext.setRegionId(line[2]);
                pubContext.setGroupName(line[3]);
                TraceBean bean = new TraceBean();
                bean.setTopic(line[4]);
                bean.setMsgId(line[5]);
                bean.setTags(line[6]);
                bean.setKeys(line[7]);
                bean.setStoreHost(line[8]);
                bean.setBodyLength(Integer.parseInt(line[9]));
                pubContext.setCostTime(Integer.parseInt(line[10]));
                bean.setMsgType(MessageType.values()[Integer.parseInt(line[11])]);

                if (line.length == 13) {
                    pubContext.setSuccess(Boolean.parseBoolean(line[12]));
                } else if (line.length == 14) {
                    bean.setOffsetMsgId(line[12]);
                    pubContext.setSuccess(Boolean.parseBoolean(line[13]));
                }

                // 兼容旧版轨迹格式（含 clientHost 等扩展字段）
                if (line.length >= 15) {
                    bean.setOffsetMsgId(line[12]);
                    pubContext.setSuccess(Boolean.parseBoolean(line[13]));
                    bean.setClientHost(line[14]);
                }

                pubContext.setTraceBeans(new ArrayList<>(1));
                pubContext.getTraceBeans().add(bean);
                resList.add(pubContext);
            } else if (line[0].equals(TraceType.SubBefore.name())) {
                TraceContext subBeforeContext = new TraceContext();
                subBeforeContext.setTraceType(TraceType.SubBefore);
                subBeforeContext.setTimeStamp(Long.parseLong(line[1]));
                subBeforeContext.setRegionId(line[2]);
                subBeforeContext.setGroupName(line[3]);
                subBeforeContext.setRequestId(line[4]);
                TraceBean bean = new TraceBean();
                bean.setMsgId(line[5]);
                bean.setRetryTimes(Integer.parseInt(line[6]));
                bean.setKeys(line[7]);
                subBeforeContext.setTraceBeans(new ArrayList<>(1));
                subBeforeContext.getTraceBeans().add(bean);
                resList.add(subBeforeContext);
            } else if (line[0].equals(TraceType.SubAfter.name())) {
                TraceContext subAfterContext = new TraceContext();
                subAfterContext.setTraceType(TraceType.SubAfter);
                subAfterContext.setRequestId(line[1]);
                TraceBean bean = new TraceBean();
                bean.setMsgId(line[2]);
                bean.setKeys(line[5]);
                subAfterContext.setTraceBeans(new ArrayList<>(1));
                subAfterContext.getTraceBeans().add(bean);
                subAfterContext.setCostTime(Integer.parseInt(line[3]));
                subAfterContext.setSuccess(Boolean.parseBoolean(line[4]));
                if (line.length >= 7) {
                    // 追加消费返回类型编码
                    subAfterContext.setContextCode(Integer.parseInt(line[6]));
                }
                // 兼容旧版 SubAfter 格式（含 timeStamp 与 groupName）
                if (line.length >= 9) {
                    subAfterContext.setTimeStamp(Long.parseLong(line[7]));
                    subAfterContext.setGroupName(line[8]);
                }
                resList.add(subAfterContext);
            } else if (line[0].equals(TraceType.EndTransaction.name())) {
                TraceContext endTransactionContext = new TraceContext();
                endTransactionContext.setTraceType(TraceType.EndTransaction);
                endTransactionContext.setTimeStamp(Long.parseLong(line[1]));
                endTransactionContext.setRegionId(line[2]);
                endTransactionContext.setGroupName(line[3]);
                TraceBean bean = new TraceBean();
                bean.setTopic(line[4]);
                bean.setMsgId(line[5]);
                bean.setTags(line[6]);
                bean.setKeys(line[7]);
                bean.setStoreHost(line[8]);
                bean.setMsgType(MessageType.values()[Integer.parseInt(line[9])]);
                bean.setTransactionId(line[10]);
                bean.setTransactionState(LocalTransactionState.valueOf(line[11]));
                bean.setFromTransactionCheck(Boolean.parseBoolean(line[12]));

                endTransactionContext.setTraceBeans(new ArrayList<>(1));
                endTransactionContext.getTraceBeans().add(bean);
                resList.add(endTransactionContext);
            } else if (line[0].equals(TraceType.Recall.name())) {
                TraceContext recallContext = new TraceContext();
                recallContext.setTraceType(TraceType.Recall);
                recallContext.setTimeStamp(Long.parseLong(line[1]));
                recallContext.setRegionId(line[2]);
                recallContext.setGroupName(line[3]);
                TraceBean bean = new TraceBean();
                bean.setTopic(line[4]);
                bean.setMsgId(line[5]);
                recallContext.setSuccess(Boolean.parseBoolean(line[6]));
                recallContext.setTraceBeans(new ArrayList<>(1));
                recallContext.getTraceBeans().add(bean);
                resList.add(recallContext);
            }
        }
        return resList;
    }

    /**
     * 将 {@link TraceContext} 编码为传输实体：分隔符拼接的 transData 与 msgId/keys 索引集合。
     *
     * @param ctx 待编码的轨迹上下文
     * @return 轨迹传输 Bean，ctx 为 null 时返回 null
     */
    public static TraceTransferBean encoderFromContextBean(TraceContext ctx) {
        if (ctx == null) {
            return null;
        }
        // 按轨迹类型拼接 transData 字段
        TraceTransferBean transferBean = new TraceTransferBean();
        StringBuilder sb = new StringBuilder(256);
        switch (ctx.getTraceType()) {
            case Pub: {
                TraceBean bean = ctx.getTraceBeans().get(0);
                // 发布轨迹：写入 topic、msgId、耗时、成功标志等
                sb.append(ctx.getTraceType()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.getTimeStamp()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.getRegionId()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.getGroupName()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getTopic()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getMsgId()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getTags()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getKeys()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getStoreHost()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getBodyLength()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.getCostTime()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getMsgType().ordinal()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getOffsetMsgId()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.isSuccess()).append(TraceConstants.FIELD_SPLITOR);//
            }
            break;
            case SubBefore: {
                for (TraceBean bean : ctx.getTraceBeans()) {
                    sb.append(ctx.getTraceType()).append(TraceConstants.CONTENT_SPLITOR)//
                        .append(ctx.getTimeStamp()).append(TraceConstants.CONTENT_SPLITOR)//
                        .append(ctx.getRegionId()).append(TraceConstants.CONTENT_SPLITOR)//
                        .append(ctx.getGroupName()).append(TraceConstants.CONTENT_SPLITOR)//
                        .append(ctx.getRequestId()).append(TraceConstants.CONTENT_SPLITOR)//
                        .append(bean.getMsgId()).append(TraceConstants.CONTENT_SPLITOR)//
                        .append(bean.getRetryTimes()).append(TraceConstants.CONTENT_SPLITOR)//
                        .append(bean.getKeys()).append(TraceConstants.FIELD_SPLITOR);//
                }
            }
            break;
            case SubAfter: {
                for (TraceBean bean : ctx.getTraceBeans()) {
                    sb.append(ctx.getTraceType()).append(TraceConstants.CONTENT_SPLITOR)//
                        .append(ctx.getRequestId()).append(TraceConstants.CONTENT_SPLITOR)//
                        .append(bean.getMsgId()).append(TraceConstants.CONTENT_SPLITOR)//
                        .append(ctx.getCostTime()).append(TraceConstants.CONTENT_SPLITOR)//
                        .append(ctx.isSuccess()).append(TraceConstants.CONTENT_SPLITOR)//
                        .append(bean.getKeys()).append(TraceConstants.CONTENT_SPLITOR)//
                        .append(ctx.getContextCode()).append(TraceConstants.CONTENT_SPLITOR);
                    if (!ctx.getAccessChannel().equals(AccessChannel.CLOUD)) {
                        sb.append(ctx.getTimeStamp()).append(TraceConstants.CONTENT_SPLITOR);
                        sb.append(ctx.getGroupName());
                    }
                    sb.append(TraceConstants.FIELD_SPLITOR);
                }
            }
            break;
            case EndTransaction: {
                TraceBean bean = ctx.getTraceBeans().get(0);
                sb.append(ctx.getTraceType()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.getTimeStamp()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.getRegionId()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(ctx.getGroupName()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getTopic()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getMsgId()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getTags()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getKeys()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getStoreHost()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getMsgType().ordinal()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getTransactionId()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.getTransactionState().name()).append(TraceConstants.CONTENT_SPLITOR)//
                    .append(bean.isFromTransactionCheck()).append(TraceConstants.FIELD_SPLITOR);
            }
            break;
            case Recall: {
                TraceBean bean = ctx.getTraceBeans().get(0);
                sb.append(ctx.getTraceType()).append(TraceConstants.CONTENT_SPLITOR)
                    .append(ctx.getTimeStamp()).append(TraceConstants.CONTENT_SPLITOR)
                    .append(ctx.getRegionId()).append(TraceConstants.CONTENT_SPLITOR)
                    .append(ctx.getGroupName()).append(TraceConstants.CONTENT_SPLITOR)
                    .append(bean.getTopic()).append(TraceConstants.CONTENT_SPLITOR)
                    .append(bean.getMsgId()).append(TraceConstants.CONTENT_SPLITOR)
                    .append(ctx.isSuccess()).append(TraceConstants.FIELD_SPLITOR);//
            }
            break;
            default:
        }
        transferBean.setTransData(sb.toString());
        for (TraceBean bean : ctx.getTraceBeans()) {

            transferBean.getTransKey().add(bean.getMsgId());
            if (bean.getKeys() != null && bean.getKeys().length() > 0) {
                String[] keys = bean.getKeys().split(MessageConst.KEY_SEPARATOR);
                transferBean.getTransKey().addAll(Arrays.asList(keys));
            }
        }
        return transferBean;
    }
}
