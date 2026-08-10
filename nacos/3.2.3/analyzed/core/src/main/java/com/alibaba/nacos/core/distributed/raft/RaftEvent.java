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

package com.alibaba.nacos.core.distributed.raft;

import com.alibaba.nacos.common.notify.SlowEvent;

import java.util.Collections;
import java.util.List;

/**
 * Raft 运行期元数据变更慢事件：Leader 切换、任期变化、成员配置更新或状态机错误时发布，供监控与上层订阅。
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings("all")
public class RaftEvent extends SlowEvent {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = -4304258594602886451L;
    
    /** Raft Group 标识。 */
    private String groupId;
    
    /** 当前 Leader 端点地址。 */
    private String leader = null;
    
    /** 当前 Raft 任期。 */
    private Long term = null;
    
    /** 错误信息（状态机 onError 时填充）。 */
    private String errMsg = "";
    
    /** 当前集群全部 Peer 地址列表。 */
    private List<String> raftClusterInfo = Collections.emptyList();
    
    /** 创建事件构建器。 */
    public static RaftEventBuilder builder() {
        return new RaftEventBuilder();
    }
    
    /** 返回 Group 标识。 */
    public String getGroupId() {
        return groupId;
    }
    
    /** 设置 Group 标识。 */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    /** 返回 Leader 地址。 */
    public String getLeader() {
        return leader;
    }
    
    /** 设置 Leader 地址。 */
    public void setLeader(String leader) {
        this.leader = leader;
    }
    
    /** 返回任期。 */
    public Long getTerm() {
        return term;
    }
    
    /** 设置任期。 */
    public void setTerm(Long term) {
        this.term = term;
    }
    
    /** 返回集群 Peer 列表。 */
    public List<String> getRaftClusterInfo() {
        return raftClusterInfo;
    }
    
    /** 设置集群 Peer 列表。 */
    public void setRaftClusterInfo(List<String> raftClusterInfo) {
        this.raftClusterInfo = raftClusterInfo;
    }
    
    /** 返回错误信息。 */
    public String getErrMsg() {
        return errMsg;
    }
    
    /** 设置错误信息。 */
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
    
    @Override
    public String toString() {
        return "RaftEvent{" + "groupId='" + groupId + '\'' + ", leader='" + leader + '\''
            + ", term=" + term
            + ", raftClusterInfo=" + raftClusterInfo + '}';
    }
    
    /** {@link RaftEvent} 流式构建器。 */
    public static final class RaftEventBuilder {
        
        /** 待设置的 Group 标识。 */
        private String groupId;
        
        /** 待设置的 Leader 地址。 */
        private String leader;
        
        /** 待设置的任期。 */
        private Long term = null;
        
        /** 待设置的集群 Peer 列表。 */
        private List<String> raftClusterInfo = Collections.emptyList();
        
        /** 待设置的错误信息。 */
        private String errMsg = "";
        
        /** 私有构造，通过 {@link #builder()} 创建。 */
        private RaftEventBuilder() {
        }
        
        /** 设置 Group 标识。 */
        public RaftEventBuilder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }
        
        /** 设置 Leader 地址。 */
        public RaftEventBuilder leader(String leader) {
            this.leader = leader;
            return this;
        }
        
        /** 设置任期。 */
        public RaftEventBuilder term(long term) {
            this.term = term;
            return this;
        }
        
        /** 设置集群 Peer 列表。 */
        public RaftEventBuilder raftClusterInfo(List<String> raftClusterInfo) {
            this.raftClusterInfo = raftClusterInfo;
            return this;
        }
        
        /** 设置错误信息。 */
        public RaftEventBuilder errMsg(String errMsg) {
            this.errMsg = errMsg;
            return this;
        }
        
        /** 构建不可变 {@link RaftEvent} 实例。 */
        public RaftEvent build() {
            RaftEvent raftEvent = new RaftEvent();
            raftEvent.setGroupId(groupId);
            raftEvent.setLeader(leader);
            raftEvent.setTerm(term);
            raftEvent.setRaftClusterInfo(raftClusterInfo);
            raftEvent.setErrMsg(errMsg);
            return raftEvent;
        }
    }
}
