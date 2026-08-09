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
package org.apache.rocketmq.controller.impl;

import io.openmessaging.storage.dledger.entry.DLedgerEntry;
import io.openmessaging.storage.dledger.exception.DLedgerException;
import io.openmessaging.storage.dledger.snapshot.SnapshotReader;
import io.openmessaging.storage.dledger.snapshot.SnapshotWriter;
import io.openmessaging.storage.dledger.statemachine.CommittedEntryIterator;
import io.openmessaging.storage.dledger.statemachine.StateMachine;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.controller.impl.event.EventMessage;
import org.apache.rocketmq.controller.impl.event.EventSerializer;
import org.apache.rocketmq.controller.impl.manager.ReplicasInfoManager;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 基于 DLedger 的控制器状态机：将已提交日志反序列化为 {@link EventMessage} 并应用到副本状态。
 */
public class DLedgerControllerStateMachine implements StateMachine {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.CONTROLLER_LOGGER_NAME);
    /** 副本信息管理器，负责将事件写入内存元数据。 */
    private final ReplicasInfoManager replicasInfoManager;
    /** 控制器事件序列化与反序列化工具。 */
    private final EventSerializer eventSerializer;
    /** 本节点绑定的 DLedger 标识（groupId#selfId）。 */
    private final String dLedgerId;

    /** 构造 DLedger 控制器状态机并生成节点标识。 */
    public DLedgerControllerStateMachine(final ReplicasInfoManager replicasInfoManager,
        final EventSerializer eventSerializer, final String dLedgerGroupId, final String dLedgerSelfId) {
        this.replicasInfoManager = replicasInfoManager;
        this.eventSerializer = eventSerializer;
        this.dLedgerId = generateDLedgerId(dLedgerGroupId, dLedgerSelfId);
    }

    @Override
    public String generateDLedgerId(String dLedgerGroupId, String dLedgerSelfId) {
        return new StringBuilder(20).append(dLedgerGroupId).append("#").append(dLedgerSelfId).toString();
    }

    @Override
    /** 批量应用已提交的 DLedger 日志条目到副本状态。 */
    public void onApply(CommittedEntryIterator iterator) {
        int applyingSize = 0;
        long firstApplyIndex = -1;
        long lastApplyIndex = -1;
        while (iterator.hasNext()) {
            final DLedgerEntry entry = iterator.next();
            final byte[] body = entry.getBody();
            if (body != null && body.length > 0) {
                final EventMessage event = this.eventSerializer.deserialize(body);
                this.replicasInfoManager.applyEvent(event);
            }
            firstApplyIndex = firstApplyIndex == -1 ? entry.getIndex() : firstApplyIndex;
            lastApplyIndex = entry.getIndex();
            applyingSize++;
        }
        log.info("Apply {} events index from {} to {} on controller {}", applyingSize, firstApplyIndex, lastApplyIndex, this.dLedgerId);
    }

    @Override
    /** 保存快照（当前实现直接返回 true，未持久化状态）。 */
    public boolean onSnapshotSave(SnapshotWriter writer) {
        return true;
    }

    @Override
    /** 加载快照（当前实现返回 false，不从快照恢复）。 */
    public boolean onSnapshotLoad(SnapshotReader reader) {
        return false;
    }

    @Override
    /** 状态机关闭回调，记录日志。 */
    public void onShutdown() {
        log.info("StateMachine {} onShutdown", this.dLedgerId);
    }

    @Override
    /** DLedger 运行异常回调，记录错误并提示排查节点。 */
    public void onError(DLedgerException exception) {
        log.error("Encountered an error on StateMachine {}, dLedger may stop working since some error occurs, you should figure out the cause and repair or remove this node.", this.dLedgerId, exception);
    }

    @Override
    /** 返回绑定的 DLedger 节点 ID。 */
    public String getBindDLedgerId() {
        return this.dLedgerId;
    }
}
