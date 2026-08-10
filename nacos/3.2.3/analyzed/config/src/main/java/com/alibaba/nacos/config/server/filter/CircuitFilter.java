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

package com.alibaba.nacos.config.server.filter;

import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.SmartSubscriber;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import com.alibaba.nacos.persistence.model.event.RaftDbErrorEvent;
import com.alibaba.nacos.config.server.model.event.RaftDbErrorRecoverEvent;
import com.alibaba.nacos.consistency.ProtocolMetaData;
import com.alibaba.nacos.consistency.cp.CPProtocol;
import com.alibaba.nacos.consistency.cp.MetadataKey;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.cluster.MemberMetaDataConstants;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.persistence.constants.PersistenceConstant;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.alibaba.nacos.config.server.utils.LogUtil.DEFAULT_LOG;

/**
 * 嵌入式 Raft 存储熔断过滤器：节点未入组或触发降级时拒绝写请求，
 * 防止 Follower 或异常节点处理一致性写操作。
 * If the embedded distributed storage is enabled, all requests are routed to the Leader node for processing, and the
 * maximum number of forwards for a single request cannot exceed three.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class CircuitFilter implements Filter {
    
    /** Raft 数据库不可恢复错误触发的降级标志 */
    private volatile boolean isDowngrading = false;
    
    /** 本节点已加入 Raft 组且可对外提供服务 */
    private volatile boolean isOpenService = false;
    
    @Autowired
    private ServerMemberManager memberManager;
    
    @Autowired
    private CPProtocol protocol;
    
    /** 初始化：订阅集群成员与 Raft 错误事件 */
    @PostConstruct
    protected void init() {
        listenerSelfInCluster();
        registerSubscribe();
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        if (!isOpenService) {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "In the node initialization, unable to process any requests at this time");
            return;
        }
        
        try {
            // 本节点发生不可恢复异常时拒绝写请求（重要告警场景）
            if (isDowngrading) {
                resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Unable to process the request at this time: System triggered degradation");
                return;
            }
            
            chain.doFilter(req, response);
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                "access denied: " + ExceptionUtil.getAllExceptionMsg(e));
        } catch (Throwable e) {
            DEFAULT_LOG.warn("[CURCUIT-FILTER] Server failed: ", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server failed, " + e);
        }
    }
    
    @Override
    public void destroy() {
        
    }
    
    /** 订阅 CONFIG Raft 组成员变更，判断本节点是否在 peers 中 */
    private void listenerSelfInCluster() {
        protocol.protocolMetaData().subscribe(PersistenceConstant.CONFIG_MODEL_RAFT_GROUP,
            MetadataKey.RAFT_GROUP_MEMBER, o -> {
                if (!(o instanceof ProtocolMetaData.ValueItem)) {
                    return;
                }
                final List<String> peers =
                    (List<String>) ((ProtocolMetaData.ValueItem) o).getData();
                if (CollectionUtils.isEmpty(peers)) {
                    isOpenService = false;
                    return;
                }
                final Member self = memberManager.getSelf();
                final String raftAddress =
                    self.getIp() + ":" + self.getExtendVal(MemberMetaDataConstants.RAFT_PORT);
                // 仅当本节点地址在 Raft 成员列表中时才开放对外服务
                isOpenService = peers.contains(raftAddress);
            });
    }
    
    /** 注册 RaftDbError/Recover 事件，控制 isDowngrading 开关 */
    private void registerSubscribe() {
        NotifyCenter.registerSubscriber(new SmartSubscriber() {
            
            @Override
            public void onEvent(Event event) {
                // @JustForTest — 单元测试中模拟 Raft 恢复事件
                if (event instanceof RaftDbErrorRecoverEvent) {
                    isDowngrading = false;
                    return;
                }
                if (event instanceof RaftDbErrorEvent) {
                    isDowngrading = true;
                }
            }
            
            @Override
            public List<Class<? extends Event>> subscribeTypes() {
                return Arrays.asList(RaftDbErrorRecoverEvent.class, RaftDbErrorEvent.class);
            }
        });
    }
    
}
