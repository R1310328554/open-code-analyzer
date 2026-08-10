/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.service.dump;

import com.alibaba.nacos.common.utils.Observable;
import com.alibaba.nacos.common.utils.Observer;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.ThreadUtils;
import com.alibaba.nacos.config.server.service.ConfigMigrateService;
import com.alibaba.nacos.config.server.service.repository.ConfigInfoGrayPersistService;
import com.alibaba.nacos.config.server.service.repository.ConfigInfoPersistService;
import com.alibaba.nacos.config.server.service.repository.HistoryConfigInfoPersistService;
import com.alibaba.nacos.consistency.ProtocolMetaData;
import com.alibaba.nacos.consistency.cp.CPProtocol;
import com.alibaba.nacos.consistency.cp.MetadataKey;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.core.distributed.ProtocolManager;
import com.alibaba.nacos.core.namespace.repository.NamespacePersistService;
import com.alibaba.nacos.core.utils.GlobalExecutor;
import com.alibaba.nacos.persistence.configuration.condition.ConditionOnEmbeddedStorage;
import com.alibaba.nacos.persistence.constants.PersistenceConstant;
import com.alibaba.nacos.persistence.repository.embedded.EmbeddedStorageContextHolder;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 嵌入式存储（Derby + Raft）场景下的 Dump 服务实现。
 * <p>启动时订阅配置 Raft 组 Leader 元数据，选主成功后执行 {@link #dumpOperate()}；仅 Leader 节点执行 dump 与历史清理。</p>
 * Embedded dump service.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@Conditional(ConditionOnEmbeddedStorage.class)
@Component
public class EmbeddedDumpService extends DumpService {
    
    /**
     * 可重试的读失败错误信息（一致性协议暂时不可读等）。
     * If it's just a normal reading failure, it can be resolved by retrying.
     */
    final String[] retryMessages =
        new String[] {"The conformance protocol is temporarily unavailable for reading"};
    
    /**
     * 不可重试的 Raft 状态机内部错误（FSM 过载、STATE_ERROR 等）。
     * If the read failed due to an internal problem in the Raft state machine, it cannot be remedied by retrying.
     */
    final String[] errorMessages = new String[] {"FSMCaller is overload.", "STATE_ERROR"};
    
    private final ProtocolManager protocolManager;
    
    /**
     * Here you inject the dependent objects constructively, ensuring that some of the dependent functionality is
     * initialized ahead of time.
     *
     * @param memberManager   {@link ServerMemberManager}
     * @param protocolManager {@link ProtocolManager}
      * <p>嵌入式存储 Dump 实现；详见类级说明。</p>
     */
    public EmbeddedDumpService(ConfigInfoPersistService configInfoPersistService,
        NamespacePersistService namespacePersistService,
        HistoryConfigInfoPersistService historyConfigInfoPersistService,
        ConfigInfoGrayPersistService configInfoGrayPersistService,
        ServerMemberManager memberManager,
        ProtocolManager protocolManager, ConfigMigrateService configMigrateService) {
        super(configInfoPersistService, namespacePersistService, historyConfigInfoPersistService,
            configInfoGrayPersistService, memberManager, configMigrateService);
        this.protocolManager = protocolManager;
    }
    
    @PostConstruct
    @Override
    protected void init() throws Throwable {
        if (EnvUtil.getStandaloneMode()) {
            dumpOperate();
            return;
        }
        
        CPProtocol protocol = protocolManager.getCpProtocol();
        AtomicReference<Throwable> errorReference = new AtomicReference<>(null);
        CountDownLatch waitDumpFinish = new CountDownLatch(1);
        
        // 订阅 /nacos_config/leader/ 元数据，Leader 就绪后触发 dump
        Observer observer = new Observer() {
            
            @Override
            public void update(Observable o) {
                if (!(o instanceof ProtocolMetaData.ValueItem)) {
                    return;
                }
                final Object arg = ((ProtocolMetaData.ValueItem) o).getData();
                GlobalExecutor.executeByCommon(() -> {
                    // Leader 元数据非空才继续 dump，避免空值误触发
                    if (Objects.isNull(arg)) {
                        return;
                    }
                    // 标记需持续读直到有数据（嵌入式存储扩展上下文）
                    EmbeddedStorageContextHolder.putExtendInfo(
                        PersistenceConstant.EXTEND_NEED_READ_UNTIL_HAVE_DATA,
                        "true");
                    // dump 成功后取消订阅，避免 Observer 任务堆积
                    boolean canEnd = false;
                    for (;;) {
                        try {
                            dumpOperate();
                            protocol.protocolMetaData().unSubscribe(
                                PersistenceConstant.CONFIG_MODEL_RAFT_GROUP,
                                MetadataKey.LEADER_META_DATA, this);
                            canEnd = true;
                        } catch (Throwable ex) {
                            if (!shouldRetry(ex)) {
                                errorReference.set(ex);
                                canEnd = true;
                            }
                        }
                        if (canEnd) {
                            ThreadUtils.countDown(waitDumpFinish);
                            break;
                        }
                        ThreadUtils.sleep(500L);
                    }
                    EmbeddedStorageContextHolder.cleanAllContext();
                });
            }
        };
        
        protocol.protocolMetaData()
            .subscribe(PersistenceConstant.CONFIG_MODEL_RAFT_GROUP, MetadataKey.LEADER_META_DATA,
                observer);
        
        // 必须等待 dump 回调完成再继续节点初始化，否则启动顺序错乱
        // continuing with the initialization
        ThreadUtils.latchAwait(waitDumpFinish);
        
        // dump 失败则向上抛出，触发节点启动失败流程
        // needs to be thrown, triggering the node to start the failed process
        final Throwable ex = errorReference.get();
        if (Objects.nonNull(ex)) {
            throw ex;
        }
    }
    
    private boolean shouldRetry(Throwable ex) {
        final String errMsg = ex.getMessage();
        
        for (String failedMsg : errorMessages) {
            if (StringUtils.containsIgnoreCase(errMsg, failedMsg)) {
                return false;
            }
        }
        for (final String retryMsg : retryMessages) {
            if (StringUtils.containsIgnoreCase(errMsg, retryMsg)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected boolean canExecute() {
        if (EnvUtil.getStandaloneMode()) {
            return true;
        }
        // Derby + Raft 模式下仅 Leader 可执行 dump 与清理
        CPProtocol protocol = protocolManager.getCpProtocol();
        return protocol.isLeader(PersistenceConstant.CONFIG_MODEL_RAFT_GROUP);
    }
}
