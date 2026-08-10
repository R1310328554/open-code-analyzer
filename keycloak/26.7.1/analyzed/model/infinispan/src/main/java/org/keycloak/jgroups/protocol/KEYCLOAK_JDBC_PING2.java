/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.jgroups.protocol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.keycloak.common.util.Time;
import org.keycloak.connections.jpa.JpaConnectionProviderFactory;

import org.jgroups.Address;
import org.jgroups.PhysicalAddress;
import org.jgroups.View;
import org.jgroups.annotations.Property;
import org.jgroups.conf.AttributeType;
import org.jgroups.protocols.JDBC_PING2;
import org.jgroups.protocols.PingData;
import org.jgroups.protocols.relay.SiteUUID;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.NameCache;
import org.jgroups.util.UUID;
import org.jgroups.util.Util;

import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;

/**
 * Keycloak 定制的 JGroups JDBC_PING2 发现协议。
 * <p>
 * 通过 {@link JpaConnectionProviderFactory} 复用 Keycloak 数据库连接池进行集群成员发现，
 * 支持过期条目清理、脑裂检测及健康状态评估。
 */
public class KEYCLOAK_JDBC_PING2 extends JDBC_PING2 {

    /** Keycloak JPA 连接工厂，提供数据库连接。 */
    private JpaConnectionProviderFactory factory;

    @Property(description="Staleness timeout in milliseconds. The coordinator will update the entries once 50%-75% of the time has passed.", type= AttributeType.TIME)
    /** 条目过期超时（毫秒）；协调者会在 50%–75% 超时时间内刷新条目。 */
    protected long staleness_timeout = 60000L;

    @Override
    /** 跳过 JDBC 驱动加载，改用 Keycloak JPA 连接池。 */
    protected void loadDriver() {
        //no-op, using JpaConnectionProviderFactory
    }

    @Override
    /** 从 Keycloak JPA 工厂获取数据库连接。 */
    protected Connection getConnection() throws SQLException {
        try {
            return factory.getConnection();
        } catch (Exception e) {
            var cause = e.getCause();
            if (cause instanceof SQLException sql) {
                // 正常情况下应走此分支，直接抛出 SQL 异常
                throw sql;
            }
            //... but to be future proof ...
            throw new SQLException(e);
        }
    }

    @Override
    public void init() throws Exception {
        if (!write_data_on_find) {
            throw new RuntimeException("Running this without write_data_on_find is not safe");
        }
        if (!remove_all_data_on_view_change) {
            throw new RuntimeException("Running this without remove_all_data_on_view_change is not safe");
        }
        super.init();
    }

    protected void insert(Connection connection, PingData data, String clustername) throws SQLException {
        lock.lock();
        try(PreparedStatement ps=connection.prepareStatement(insert_single_sql)) {
            Address address=data.getAddress();
            String addr= Util.addressToString(address);
            String name=address instanceof SiteUUID ? ((SiteUUID)address).getName() : NameCache.get(address);
            PhysicalAddress ip_addr=data.getPhysicalAddr();
            String ip=ip_addr.toString();
            ps.setString(1, addr);
            ps.setString(2, name);
            ps.setString(3, clustername);
            ps.setString(4, ip);
            ps.setBoolean(5, data.isCoord());
            ps.setLong(6, Time.currentTime());
            ps.setString(7, view != null && view.getCoord() != null ? Util.addressToString(view.getCoord()) : null);
            if (log.isTraceEnabled())
                log.trace("%s: SQL for insertion: %s", local_addr, ps);
            ps.executeUpdate();
            log.debug("%s: inserted %s for cluster %s", local_addr, address, clustername);
        } finally {
            lock.unlock();
        }
    }

    @Override
    /** 视图变更且协调者切换时，清理数据库中的过期发现条目。 */
    protected void handleView(View new_view, View old_view, boolean coord_changed) {
        super.handleView(new_view, old_view, coord_changed);
        if (coord_changed) {
            try {
                removeStaleEntries();
            } catch (Exception e) {
                log.error(String.format("%s: failed handling view change", local_addr), e);
            }
        }
    }

    /** 删除已不在当前视图中、且由本节点协调的成员条目。 */
    protected void removeAllNotInCurrentView() {
        View local_view = view;
        if (local_view == null) {
            return;
        }
        String cluster_name = getClusterName();
        try {
            List<PingData> list = readFromDB(getClusterName());
            PingData my_data = list.stream().filter(p -> Objects.equals(p.getAddress(), addr())).findFirst().orElse(null);
            if (my_data == null || my_data.mbrs() == null) {
                return;
            }
            for (PingData data : list) {
                Address addr = data.getAddress();
                // Only delete an entry if it is currently allocated to us, and not someone else
                if (!local_view.containsMember(addr) && my_data.mbrs().contains(addr)) {
                    try (var conn = getConnection()) {
                        addDiscoveryResponseToCaches(addr, data.getLogicalName(), data.getPhysicalAddr());
                        delete(conn, cluster_name, addr);
                    }
                }
            }
        } catch (Exception e) {
            log.error(String.format("%s: failed reading from the DB", local_addr), e);
        }
    }

    /**
     * 协调者周期性刷新发现信息；间隔为 staleness_timeout 的一半加随机抖动。
     * <p>
     * 只要当前节点仍是协调者就会持续运行，不仅限于视图变更后。
     */
    protected synchronized void startInfoWriter() {
        if(info_writer == null || info_writer.isDone())
            info_writer=timer.scheduleWithDynamicInterval(new InfoWriter(info_writer_max_writes_after_view, info_writer_sleep_time) {
                @Override
                public long nextInterval() {
                    return is_coord ? (staleness_timeout / 2 + Util.random(sleep_interval / 4)) : 0;
                }
            });
    }

    /** 从数据库读取 Ping 数据，过滤过期条目并重建成员关系。 */
    protected List<PingData> readFromDB(String cluster) throws Exception {
        try(Connection conn=getConnection();
            PreparedStatement ps=prepare(conn, select_all_pingdata_sql, TYPE_FORWARD_ONLY, CONCUR_UPDATABLE)) {
            ps.setString(1, cluster);
            if(log.isTraceEnabled())
                log.trace("%s: SQL for reading: %s", local_addr, ps);
            try(ResultSet resultSet=ps.executeQuery()) {
                reads++;
                List<PingData> retval=new LinkedList<>();
                Map<Address, Set<Address>> members = new HashMap<>();
                while(resultSet.next()) {
                    String uuid=resultSet.getString(1);
                    String name=resultSet.getString(2);
                    String ip=resultSet.getString(3);
                    boolean coord=resultSet.getBoolean(4);
                    String coordinated_by=resultSet.getString(5);
                    long last_update=resultSet.getLong(6);
                    if (last_update < getStalenessCutoff()) {
                        continue;
                    }
                    Address addr=Util.addressFromString(uuid);
                    IpAddress ip_addr=new IpAddress(ip);
                    PingData data=new PingData(addr, true, name, ip_addr).coord(coord);
                    retval.add(data);
                    if (coordinated_by != null) {
                        Address coordinate_by_address = Util.addressFromString(coordinated_by);
                        members.computeIfAbsent(coordinate_by_address, address -> new HashSet<>())
                                .add(addr);
                    }
                }
                retval.forEach(a -> a.mbrs(members.get(a.getAddress())));
                return retval;
            }
        }
    }

    /** 协调者清理数据库中超过 staleness_timeout 的过期条目。 */
    protected void removeStaleEntries() throws Exception {
        try(Connection conn=getConnection();
            PreparedStatement ps=prepare(conn, select_all_pingdata_sql, TYPE_FORWARD_ONLY, CONCUR_UPDATABLE)) {
            ps.setString(1, getClusterName());
            if(log.isTraceEnabled())
                log.trace("%s: SQL for reading: %s", local_addr, ps);
            try(ResultSet resultSet=ps.executeQuery()) {
                reads++;
                while(resultSet.next()) {
                    String uuid=resultSet.getString(1);
                    long last_update=resultSet.getLong(6);
                    if (last_update < getStalenessCutoff()) {
                        Address addr=Util.addressFromString(uuid);
                        delete(conn, getClusterName(), addr);
                    }
                }
            }
        }
    }

    /** 计算过期截止时间戳（秒）。 */
    private long getStalenessCutoff() {
        return TimeUnit.MILLISECONDS.toSeconds(Time.currentTimeMillis() - staleness_timeout);
    }

    /** 注入 Keycloak JPA 连接工厂。 */
    public void setJpaConnectionProviderFactory(JpaConnectionProviderFactory factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    // 脑裂裁决：优先选最大分区，同分区大小则按地址排序保证稳定
    private final static Comparator<PingData> SPLIT_BRAIN_DECIDER = Comparator
            .<PingData, Integer>comparing(p -> p.mbrs() != null ? p.mbrs().size() : 0).reversed()
            .thenComparing(PingData::getAddress);

    /**
     * 检测网络分区并判断本节点是否属于「获胜」分区。
     * <p>
     * The algorithm performs the following steps
     *
     * <ul>
     *     <li>从数据库读取 Ping 数据</li>
     *     <li>读取失败则返回 {@link HealthStatus#ERROR}</li>
     *     <li>过滤非协调者成员</li>
     *     <li>未找到协调者则返回 {@link HealthStatus#NO_COORDINATOR}</li>
     *     <li>多个协调者时使用 {@link SPLIT_BRAIN_DECIDER} 选出唯一协调者</li>
     *     <li>与当前视图协调者比对，一致则 {@link HealthStatus#HEALTHY}，否则 {@link HealthStatus#UNHEALTHY}</li>
     * </ul>
     *
     * @return 集群健康状态
     * @see HealthStatus
     */
    public HealthStatus healthStatus() {
        try {
            return readFromDB(cluster_name)
                    .stream()
                    .filter(PingData::isCoord)
                    .sorted(SPLIT_BRAIN_DECIDER)
                    .map(PingData::getAddress)
                    .findFirst()
                    .map(view.getCoord()::equals)
                    .map(isCoordinatorInView -> isCoordinatorInView ? HealthStatus.HEALTHY : HealthStatus.UNHEALTHY)
                    .orElse(HealthStatus.NO_COORDINATOR);
        } catch (Exception e) {
            // 数据库不可达
            log.warn("Failed to fetch the cluster members from the database.", e);
            return HealthStatus.ERROR;
        }
    }

    /** JDBC_PING2 集群健康状态枚举。 */
    public enum HealthStatus {
        /**
         * 未检测到分区，或本实例位于正确分区。
         */
        HEALTHY,
        /**
         * 检测到分区且本实例不在正确分区，应停止处理请求。
         */
        UNHEALTHY,
        /**
         * 数据库表中无协调者记录。
         */
        NO_COORDINATOR,
        /**
         * 读取数据库时发生错误。
         */
        ERROR
    }
}
