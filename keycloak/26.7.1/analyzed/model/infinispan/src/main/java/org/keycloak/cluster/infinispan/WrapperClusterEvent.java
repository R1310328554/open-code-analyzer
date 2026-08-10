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

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.keycloak.cluster.ClusterEvent;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.marshalling.Marshalling;

import org.infinispan.protostream.WrappedMessage;
import org.infinispan.protostream.annotations.Proto;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 集群事件的包装载体：携带事件键、发送方信息、站点过滤策略及委托事件集合。
 * <p>
 * 写入 Infinispan work 缓存时作为值对象，由 {@link InfinispanClusterProvider.CacheEntryListener}
 * 或 {@link RemoteInfinispanNotificationManager} 接收并分发给已注册的 {@link ClusterListener}。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.WRAPPED_CLUSTER_EVENT)
public class WrapperClusterEvent implements ClusterEvent {

    /** 事件键，对应 {@link ClusterProvider#registerListener} 注册的 taskKey。 */
    @ProtoField(1)
    final String eventKey;
    /** 发送方节点地址；null 表示在所有节点触发。 */
    @ProtoField(2)
    final String senderAddress; // null means invoke everywhere
    /** 发送方站点名称，跨数据中心场景使用。 */
    @ProtoField(3)
    final String senderSite; // can be null
    /** 站点过滤策略，控制事件在本地/远端 DC 的分发范围。 */
    @ProtoField(4)
    final SiteFilter siteFilter;
    /** 实际要分发的集群事件集合。 */
    private final Collection<? extends ClusterEvent> events;

    private WrapperClusterEvent(String eventKey, String senderAddress, String senderSite, SiteFilter siteFilter, Collection<? extends ClusterEvent> events) {
        this.eventKey = Objects.requireNonNull(eventKey);
        this.senderAddress = senderAddress;
        this.senderSite = senderSite;
        this.siteFilter = Objects.requireNonNull(siteFilter);
        this.events = Objects.requireNonNull(events);
    }

    /** ProtoStream 反序列化工厂方法。 */
    @ProtoFactory
    static WrapperClusterEvent protoFactory(String eventKey, String senderAddress, String senderSite, SiteFilter siteFilter, List<WrappedMessage> eventPS) {
        var events = eventPS.stream().map(WrappedMessage::getValue).map(ClusterEvent.class::cast).toList();
        return new WrapperClusterEvent(eventKey, senderAddress, senderSite, siteFilter, events);
    }

    /**
     * 根据 DC 通知策略与 ignoreSender 标志构造包装事件。
     *
     * @param eventKey      事件键
     * @param events        委托事件集合
     * @param senderAddress 发送方节点地址
     * @param senderSite    发送方站点
     * @param dcNotify      数据中心通知范围
     * @param ignoreSender  是否忽略发送方节点（不在发送方本地触发）
     */
    public static WrapperClusterEvent wrap(String eventKey, Collection<? extends ClusterEvent> events, String senderAddress, String senderSite, ClusterProvider.DCNotify dcNotify, boolean ignoreSender) {
        senderAddress = ignoreSender ? Objects.requireNonNull(senderAddress) : null;
        senderSite = dcNotify == ClusterProvider.DCNotify.ALL_DCS ? null : senderSite;
        var siteNotification = switch (dcNotify) {
            case ALL_DCS -> SiteFilter.ALL;
            case LOCAL_DC_ONLY -> SiteFilter.LOCAL;
            case ALL_BUT_LOCAL_DC -> SiteFilter.REMOTE;
        };
        return new WrapperClusterEvent(eventKey, senderAddress, senderSite, siteNotification, events);
    }

    @ProtoField(5)
    List<WrappedMessage> getEventPS() {
        return events.stream().map(WrappedMessage::new).toList();
    }

    public String getEventKey() {
        return eventKey;
    }

    public Collection<? extends ClusterEvent> getDelegateEvents() {
        return events;
    }

    /**
     * 判断本节点是否应拒绝处理此事件（发送方过滤或站点过滤）。
     *
     * @param mySiteAddress 本节点地址
     * @param mySiteName    本站点名称
     * @return true 表示应忽略此事件
     */
    public boolean rejectEvent(String mySiteAddress, String mySiteName) {
        return (senderAddress != null && senderAddress.equals(mySiteAddress)) ||
                (senderSite != null  && siteFilter.reject(senderSite, mySiteName));

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WrapperClusterEvent that = (WrapperClusterEvent) o;
        return eventKey.equals(that.eventKey) &&
                Objects.equals(senderAddress, that.senderAddress) &&
                Objects.equals(senderSite, that.senderSite) &&
                siteFilter == that.siteFilter &&
                events.equals(that.events);
    }

    @Override
    public int hashCode() {
        int result = eventKey.hashCode();
        result = 31 * result + Objects.hashCode(senderAddress);
        result = 31 * result + Objects.hashCode(senderSite);
        result = 31 * result + siteFilter.hashCode();
        result = 31 * result + events.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("WrapperClusterEvent [ eventKey=%s, sender=%s, senderSite=%s, delegateEvents=%s ]", eventKey, senderAddress, senderSite, events);
    }

    /**
     * 跨数据中心站点过滤枚举：控制事件在全部/本地/远端 DC 的分发。
     */
    @Proto
    @ProtoTypeId(Marshalling.WRAPPED_CLUSTER_EVENT_SITE_FILTER)
    public enum SiteFilter {
        /** 所有站点均接收。 */
        ALL {
            @Override
            boolean reject(String senderSite, String mySite) {
                return false;
            }
        }, /** 仅本地 DC 接收。 */
        LOCAL {
            @Override
            boolean reject(String senderSite, String mySite) {
                return !Objects.equals(senderSite, mySite);
            }
        }, /** 仅远端 DC 接收（排除本地）。 */
        REMOTE {
            @Override
            boolean reject(String senderSite, String mySite) {
                return Objects.equals(senderSite, mySite);
            }
        };

        /** 根据发送方站点与本站点判断是否拒绝。 */
        abstract boolean reject(String senderSite, String mySite);
    }
}
