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

package org.apache.rocketmq.tools.monitor;

import java.util.TreeMap;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;

/**
 * 监控事件回调接口。
 * <p>{@link MonitorService} 每轮巡检完成后通过本接口向业务侧上报各类指标与异常。
 */
public interface MonitorListener {
    /** 一轮监控开始前的钩子。 */
    void beginRound();

    /** 上报指定消费组/Topic 的未消费消息积压情况。
     * @param undoneMsgs 积压统计快照 */
    void reportUndoneMsgs(UndoneMsgs undoneMsgs);

    /** 上报近期消费失败消息统计。
     * @param failedMsgs 失败消息统计 */
    void reportFailedMsgs(FailedMsgs failedMsgs);

    /** 上报偏移量迁移导致的消息删除事件。
     * @param deleteMsgsEvent 删除事件详情 */
    void reportDeleteMsgsEvent(DeleteMsgsEvent deleteMsgsEvent);

    /** 上报消费组各客户端运行态（按 clientId 索引）。
     * @param criTable 客户端 ID 到 {@link ConsumerRunningInfo} 的有序映射 */
    void reportConsumerRunningInfo(TreeMap<String/* clientId */, ConsumerRunningInfo> criTable);

    /** 一轮监控结束后的钩子。 */
    void endRound();
}
