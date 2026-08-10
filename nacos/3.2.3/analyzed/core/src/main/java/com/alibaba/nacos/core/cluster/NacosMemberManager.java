/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.core.cluster.lookup.AbstractMemberLookup;

import java.util.Collection;

/**
 * Nacos 集群成员管理 SPI：成员列表变更与查询的统一抽象，由 {@link ServerMemberManager} 实现并在寻址模块回调。
 * Nacos member managers.
 *
 * @author xiweng.yy
 */
public interface NacosMemberManager {
    
    /**
     * 成员列表发生变更时回调，通常在 {@link AbstractMemberLookup#afterLookup(Collection)} 中触发。
     *
     * @param members 最新成员集合
     * @return 若集群视图有变化返回 {@code true}，否则 {@code false}
     */
    boolean memberChange(Collection<Member> members);
    
    /**
     * 获取当前感知的全部集群成员。
     *
     * @return 成员 {@link Collection}
     */
    Collection<Member> allMembers();
}
