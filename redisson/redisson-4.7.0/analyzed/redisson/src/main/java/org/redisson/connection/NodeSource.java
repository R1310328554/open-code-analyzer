/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.connection;

import org.redisson.client.RedisClient;
import org.redisson.misc.RedisURI;

import java.util.Objects;

/**
 * 命令路由节点来源描述，封装槽号、地址、客户端及集群重定向信息。
 * <p>
 * 集群模式下 {@link Redirect} 区分 MOVED/ASK/REDIRECT 重定向类型；
 * 也可直接绑定 {@link MasterSlaveEntry} 或 {@link RedisClient}。
 *
 * @author Nikita Koksharov
 *
 */
public class NodeSource {

    /** 集群重定向类型。 */
    public enum Redirect {MOVED, ASK, REDIRECT}

    /** 集群槽号（可为 null）。 */
    private Integer slot;
    /** 目标节点 URI（重定向场景）。 */
    private RedisURI addr;
    /** 目标 Redis 客户端实例。 */
    private RedisClient redisClient;
    /** 重定向类型。 */
    private Redirect redirect;
    /** 关联的主从条目。 */
    private MasterSlaveEntry entry;

    /** 复制现有 NodeSource 并替换 redisClient。 */
    public NodeSource(NodeSource nodeSource, RedisClient redisClient) {
        this.slot = nodeSource.slot;
        this.addr = nodeSource.addr;
        this.redisClient = redisClient;
        this.redirect = nodeSource.getRedirect();
        this.entry = nodeSource.getEntry();
    }

    /** 仅绑定主从条目。 */
    public NodeSource(MasterSlaveEntry entry) {
        this.entry = entry;
    }

    /** 仅指定槽号。 */
    public NodeSource(Integer slot) {
        this.slot = slot;
    }

    /** 绑定主从条目与客户端。 */
    public NodeSource(MasterSlaveEntry entry, RedisClient redisClient) {
        this.entry = entry;
        this.redisClient = redisClient;
    }
    
    /** 仅指定 Redis 客户端。 */
    public NodeSource(RedisClient redisClient) {
        this.redisClient = redisClient;
    }
    
    /** 指定槽号与客户端。 */
    public NodeSource(Integer slot, RedisClient redisClient) {
        this.slot = slot;
        this.redisClient = redisClient;
    }
    
    /** 集群重定向场景：槽号 + 地址 + 重定向类型。 */
    public NodeSource(Integer slot, RedisURI addr, Redirect redirect) {
        this.slot = slot;
        this.addr = addr;
        this.redirect = redirect;
    }

    /** 返回关联的主从条目。 */
    public MasterSlaveEntry getEntry() {
        return entry;
    }
    
    /** 返回重定向类型。 */
    public Redirect getRedirect() {
        return redirect;
    }

    /** 返回集群槽号。 */
    public Integer getSlot() {
        return slot;
    }

    /** 返回目标 Redis 客户端。 */
    public RedisClient getRedisClient() {
        return redisClient;
    }

    /** 返回目标 URI。 */
    public RedisURI getAddr() {
        return addr;
    }

    @Override
    public String toString() {
        return "NodeSource [slot=" + slot + ", addr=" + addr + ", redisClient=" + redisClient + ", redirect=" + redirect
                + ", entry=" + entry + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeSource that = (NodeSource) o;
        return Objects.equals(slot, that.slot) && Objects.equals(addr, that.addr) && Objects.equals(redisClient, that.redisClient) && redirect == that.redirect && Objects.equals(entry, that.entry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot, addr, redisClient, redirect, entry);
    }
}
