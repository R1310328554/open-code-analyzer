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

package com.alibaba.nacos.naming.core.v2.client;

import com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.pojo.Subscriber;

import java.util.Collection;

/**
 * Nacos 命名模块服务端客户端抽象。
 *
 * <p>表示存储在命名服务器上的客户端概念，记录该客户端已发布与已订阅的服务集合，支持 Distro 同步所需的修订号与过期判定。</p>
 *
 * @author xiweng.yy
 */
public interface Client {
    
    /**
     * 获取当前客户端唯一标识。
     *
     * @return 客户端 ID
     */
    String getClientId();
    
    /**
     * 当前客户端是否为临时客户端。
     *
     * @return 临时客户端返回 true，否则 false
     */
    boolean isEphemeral();
    
    /** 将客户端最后更新时间设为当前时刻。 */
    void setLastUpdatedTime();
    
    /**
     * 获取客户端最后更新时间。
     *
     * @return 最后更新时间戳
     */
    long getLastUpdatedTime();
    
    /**
     * 为当前客户端添加服务实例发布信息。
     *
     * @param service             发布的目标服务
     * @param instancePublishInfo 实例发布信息
     * @return 添加成功返回 true，否则 false
     */
    boolean addServiceInstance(Service service, InstancePublishInfo instancePublishInfo);
    
    /**
     * 从客户端移除指定服务的实例发布信息。
     *
     * @param service 目标服务
     * @return 若存在则返回被移除的实例信息，否则 {@code null}
     */
    InstancePublishInfo removeServiceInstance(Service service);
    
    /**
     * 获取客户端对指定服务发布的实例信息。
     *
     * @param service 目标服务
     * @return 实例发布信息
     */
    InstancePublishInfo getInstancePublishInfo(Service service);
    
    /**
     * 获取当前客户端已发布的全部服务。
     *
     * @return 已发布服务集合
     */
    Collection<Service> getAllPublishedService();
    
    /**
     * 为当前客户端添加服务订阅。
     *
     * @param service    订阅的目标服务
     * @param subscriber 订阅者信息
     * @return 添加成功返回 true，否则 false
     */
    boolean addServiceSubscriber(Service service, Subscriber subscriber);
    
    /**
     * 移除客户端对指定服务的订阅。
     *
     * @param service 目标服务
     * @return 移除成功返回 true，否则 false
     */
    boolean removeServiceSubscriber(Service service);
    
    /**
     * 获取客户端对指定服务的订阅者信息。
     *
     * @param service 目标服务
     * @return 订阅者对象
     */
    Subscriber getSubscriber(Service service);
    
    /**
     * 获取当前客户端已订阅的全部服务。
     *
     * @return 已订阅服务集合
     */
    Collection<Service> getAllSubscribeService();
    
    /**
     * 生成用于 Distro 同步的客户端数据快照。
     *
     * @return 客户端同步数据
     */
    ClientSyncData generateSyncData();
    
    /**
     * 判断客户端是否已过期。
     *
     * @param currentTime 统一的当前时间戳
     * @return 已过期返回 true，否则 false
     */
    boolean isExpire(long currentTime);
    
    /** 释放客户端并回退相关监控指标。 */
    void release();
    
    /**
     * 重新计算客户端修订号并返回新值。
     *
     * @return 重算后的修订号
     */
    long recalculateRevision();
    
    /**
     * 获取当前修订号（不重算）。
     *
     * @return 当前修订号
     */
    long getRevision();
    
    /**
     * 设置客户端修订号。
     *
     * @param revision 待更新的修订号
     */
    void setRevision(long revision);
    
}
