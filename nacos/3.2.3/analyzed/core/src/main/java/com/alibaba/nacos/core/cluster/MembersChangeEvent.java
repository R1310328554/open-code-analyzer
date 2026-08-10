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

package com.alibaba.nacos.core.cluster;

import com.alibaba.nacos.common.notify.Event;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * 集群成员列表变更事件：当 Nacos 集群节点集合发生变化时发布，订阅方可据此刷新路由、一致性协议或分片映射。
 * Publish this event when the node list changes，All interested in the node list change event can listen to this event.
 *
 * <ul>
 *     <li>{@link com.alibaba.nacos.core.distributed.ProtocolManager}</li>
 *     <li>{@link com.alibaba.nacos.naming.core.DistroMapper}</li>
 *     <li>{@link com.alibaba.nacos.naming.consistency.persistent.raft.RaftPeerSet}</li>
 * </ul>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class MembersChangeEvent extends Event {
    
    private static final long serialVersionUID = 7308126651076668976L;
    
    /** 变更后的完整成员快照。 */
    private final Collection<Member> members;
    
    /** 触发本次变更的具体成员（可为空集合）。 */
    private final Collection<Member> triggers;
    
    private MembersChangeEvent(Collection<Member> members, Collection<Member> triggers) {
        this.members = members;
        this.triggers = new HashSet<>();
        if (triggers != null) {
            this.triggers.addAll(triggers);
        }
    }
    
    /** 获取事件构建器。 */
    public static MemberChangeEventBuilder builder() {
        return new MemberChangeEventBuilder();
    }
    
    /** 返回当前集群全部成员。 */
    public Collection<Member> getMembers() {
        return members;
    }
    
    /** 是否存在显式触发成员。 */
    public boolean hasTriggers() {
        return !triggers.isEmpty();
    }
    
    /** 返回触发本次变更的成员集合。 */
    public Collection<Member> getTriggers() {
        return triggers;
    }
    
    @Override
    public String toString() {
        return "MembersChangeEvent{" + "members=" + members + ", triggers=" + triggers + ", no="
            + sequence() + '}';
    }
    
    public static final class MemberChangeEventBuilder {
        
        /** 待写入事件的全部成员列表。 */
        private Collection<Member> allMembers;
        
        /** 触发变更的成员集合。 */
        private Collection<Member> triggers;
        
        private MemberChangeEventBuilder() {
        }
        
        /** 设置完整成员列表。 */
        public MemberChangeEventBuilder members(Collection<Member> allMembers) {
            this.allMembers = allMembers;
            return this;
        }
        
        /** 设置多个触发成员。 */
        public MemberChangeEventBuilder triggers(Collection<Member> triggers) {
            this.triggers = triggers;
            return this;
        }
        
        /** 设置单个触发成员。 */
        public MemberChangeEventBuilder trigger(Member trigger) {
            this.triggers = Collections.singleton(trigger);
            return this;
        }
        
        /**
         * 构建 {@link MembersChangeEvent} 实例。
         *
         * @return {@link MembersChangeEvent}
         */
        public MembersChangeEvent build() {
            return new MembersChangeEvent(allMembers, triggers);
        }
    }
}
