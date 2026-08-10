/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.keycloak.cluster.infinispan;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.keycloak.marshalling.Marshalling;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 判断 work 缓存条目是否由已离群节点持有的分布式锁。
 * <p>
 * 在 JGroups 视图变更时，由协调者节点用于清理失效节点遗留的 {@link LockEntry}。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.LOCK_ENTRY_PREDICATE)
public class LockEntryPredicate implements Predicate<Map.Entry<String, Object>> {

    /** 已从集群视图中移除的节点地址集合。 */
    private final Set<String> removedNodesAddresses;

    @ProtoFactory
    public LockEntryPredicate(Set<String> removedNodesAddresses) {
        this.removedNodesAddresses = removedNodesAddresses;
    }

    @ProtoField(value = 1, collectionImplementation = HashSet.class)
    Set<String> getRemovedNodesAddresses() {
        return removedNodesAddresses;
    }

    /** 若条目值为 {@link LockEntry} 且持有节点已离群，则返回 true（应被移除）。 */
    @Override
    public boolean test(Map.Entry<String, Object> entry) {
        return entry.getValue() instanceof LockEntry lock &&
                removedNodesAddresses.contains(lock.node());

    }
}
