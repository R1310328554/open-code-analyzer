/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
 */

package org.keycloak.cluster.infinispan;

import org.keycloak.marshalling.Marshalling;

import org.infinispan.protostream.annotations.Proto;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 分布式任务锁条目：记录持有锁的集群节点名称。
 * <p>
 * 通过 Infinispan work 缓存的 {@code putIfAbsent} 实现集群级互斥，
 * 供 {@link InfinispanClusterProvider} 与 {@link RemoteInfinispanClusterProvider} 使用。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.LOCK_ENTRY)
@Proto
public record LockEntry(String node) {
}
