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

import io.netty.util.NetUtil;
import io.netty.util.Timeout;
import org.redisson.client.RedisClient;
import org.redisson.misc.RedisURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * DNS 变更监视器，周期性解析主从主机名并触发拓扑更新。
 * <p>
 * 当检测到 IP 变化时，调用 {@link MasterSlaveEntry#changeMaster} 或
 * {@link MasterSlaveEntry#addSlave}/{@link MasterSlaveEntry#slaveDown} 同步连接池。
 *
 * @author Nikita Koksharov
 *
 */
public class DNSMonitor {
    
    private static final Logger log = LoggerFactory.getLogger(DNSMonitor.class);
    
    /** 所属连接管理器。 */
    private final ConnectionManager connectionManager;
    /** 主节点 URI → 当前解析地址。 */
    private final Map<RedisURI, InetSocketAddress> masters = new HashMap<>();
    /** 从节点 URI → 当前解析地址。 */
    private final Map<RedisURI, InetSocketAddress> slaves = new HashMap<>();

    /** 下一次 DNS 扫描的 Netty 定时任务。 */
    private volatile Timeout dnsMonitorFuture;
    /** DNS 扫描间隔（毫秒）。 */
    private final long dnsMonitoringInterval;
    /** 每次扫描的 DNS 解析重试次数。 */
    private final int dnsMonitoringTimes;

    /** 是否已打印多 IP 警告（避免重复日志）。 */

    /** 构造监视器并解析初始主从地址。 */
    public DNSMonitor(ConnectionManager connectionManager, RedisClient masterHost, Collection<RedisClient> slaveHosts, long dnsMonitoringInterval, int dnsMonitoringTimes) {
        masterHost.resolveAddr().join();
        masters.put(masterHost.getConfig().getAddress(), masterHost.getAddr());
        
        for (RedisClient slaveHost : slaveHosts) {
            slaveHost.resolveAddr().join();
            slaves.put(slaveHost.getConfig().getAddress(), slaveHost.getAddr());
        }
        this.connectionManager = connectionManager;
        this.dnsMonitoringInterval = dnsMonitoringInterval;
        this.dnsMonitoringTimes = dnsMonitoringTimes;
    }
    
    /** 启动周期性 DNS 变更检测。 */
    public void start() {
        monitorDnsChange();
        log.debug("DNS monitoring enabled; Current masters: {}, slaves: {}", masters, slaves);
    }
    
    /** 停止 DNS 监视定时任务。 */
    public void stop() {
        if (dnsMonitorFuture != null) {
            dnsMonitorFuture.cancel();
        }
    }
    
    /** 调度下一次 DNS 扫描（主从并行检测）。 */
    private void monitorDnsChange() {
        dnsMonitorFuture = connectionManager.getServiceManager().newTimeout(t -> {
            if (connectionManager.getServiceManager().isShuttingDown()) {
                return;
            }

            CompletableFuture<Void> mf = monitorMasters();
            CompletableFuture<Void> sf = monitorSlaves();
            CompletableFuture.allOf(mf, sf)
                    .whenComplete((r, e) -> monitorDnsChange());
        }, dnsMonitoringInterval, TimeUnit.MILLISECONDS);
    }

    /** 检测所有主节点 DNS 是否变更，变更时触发 changeMaster。 */
    private CompletableFuture<Void> monitorMasters() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Entry<RedisURI, InetSocketAddress> entry : masters.entrySet()) {
            CompletableFuture<Void> promise = new CompletableFuture<>();
            futures.add(promise);
            Set<RedisURI> addressSet = new HashSet<>();
            CompletableFuture<Boolean> ipChanged = resolveTimes(entry, addressSet, 0);
            ipChanged.whenComplete((flag, ex) -> {
                if (ex != null) {
                    log.error("Unable to resolve {}", entry.getKey().getHost(), ex);
                    promise.complete(null);
                    return;
                }

                if (addressSet.isEmpty()) {
                    log.debug("Unable to resolve {}", entry.getKey().getHost());
                    promise.complete(null);
                    return;
                }

                if (addressSet.size() > 1) {
                    if (!printed) {
                        log.warn("Use Redisson PRO version (https://redisson.pro/feature-comparison.html) with Proxy mode feature to utilize all ip addresses: {} resolved by: {}",
                                addressSet, entry.getKey());
                        printed = true;
                    }
                }

                if (flag) {
                    log.debug("{} resolved to {}", entry.getKey().getHost(), addressSet);
                    promise.complete(null);
                    return;
                }

                int index = 0;
                List<RedisURI> addresses = new ArrayList<>(addressSet);
                if (addresses.size() > 1) {
                    addresses.sort(Comparator.comparing(RedisURI::getHost));
                }

                RedisURI address = addresses.get(index);

                log.debug("{} resolved to {} and {} selected", entry.getKey().getHost(), addresses, address);



                try {
                    InetSocketAddress currentMasterAddr = entry.getValue();
                    byte[] addr = NetUtil.createByteArrayFromIpAddressString(address.getHost());
                    InetSocketAddress newMasterAddr = new InetSocketAddress(InetAddress.getByAddress(entry.getKey().getHost(), addr), address.getPort());
                    if (!address.equals(currentMasterAddr)) {
                        log.info("Detected DNS change. Master {} has changed ip from {} to {}",
                                entry.getKey(), currentMasterAddr.getAddress().getHostAddress(),
                                newMasterAddr.getAddress().getHostAddress());

                        MasterSlaveEntry masterSlaveEntry = connectionManager.getEntry(currentMasterAddr);
                        if (masterSlaveEntry == null) {
                            log.error("Unable to find entry for current master {}", currentMasterAddr);
                            promise.complete(null);
                            return;
                        }

                        CompletableFuture<RedisClient> changeFuture = masterSlaveEntry.changeMaster(newMasterAddr, entry.getKey());
                        changeFuture.whenComplete((r, e) -> {
                            promise.complete(null);

                            if (e == null) {
                                masters.put(entry.getKey(), newMasterAddr);
                            }
                        });
                    } else {
                        promise.complete(null);
                    }
                } catch (UnknownHostException e) {
                    log.error(e.getMessage(), e);
                    promise.complete(null);
                }
            });
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /** 检测从节点 DNS 变更，更新 slaveUp/slaveDown/addSlave。 */
    private CompletableFuture<Void> monitorSlaves() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Entry<RedisURI, InetSocketAddress> entry : slaves.entrySet()) {
            CompletableFuture<Void> promise = new CompletableFuture<>();
            futures.add(promise);
            log.debug("Request sent to resolve ip address for slave host: {}", entry.getKey().getHost());

            Set<RedisURI> addressSet = new HashSet<>();
            CompletableFuture<Boolean> ipChanged = resolveTimes(entry, addressSet, 0);
            ipChanged.whenComplete((flag, ex) -> {
                if (ex != null) {
                    log.error("Unable to resolve {}", entry.getKey().getHost(), ex);
                    promise.complete(null);
                    return;
                }

                if (addressSet.isEmpty()) {
                    log.debug("Unable to resolve {}", entry.getKey().getHost());
                    promise.complete(null);
                    return;
                }

                if (flag) {
                    log.debug("{} resolved to {}", entry.getKey().getHost(), addressSet);
                    promise.complete(null);
                    return;
                }

                int index = 0;
                List<RedisURI> addresses = new ArrayList<>(addressSet);
                if (addresses.size() > 1) {
                    addresses.sort(Comparator.comparing(RedisURI::getHost));
                }
                RedisURI address = addresses.get(index);

                log.debug("{} resolved to {} and {} selected", entry.getKey().getHost(), addresses, address);

                try {
                    InetSocketAddress currentSlaveAddr = entry.getValue();
                    byte[] addr = NetUtil.createByteArrayFromIpAddressString(address.getHost());
                    InetSocketAddress newSlaveAddr = new InetSocketAddress(InetAddress.getByAddress(entry.getKey().getHost(), addr), address.getPort());
                    log.debug("Resolved ip: {} for slave host: {}", newSlaveAddr.getAddress(), entry.getKey().getHost());
                    if (!newSlaveAddr.getAddress().equals(currentSlaveAddr.getAddress())) {
                        log.info("Detected DNS change. Slave {} has changed ip from {} to {}",
                                entry.getKey().getHost(), currentSlaveAddr.getAddress().getHostAddress(), newSlaveAddr.getAddress().getHostAddress());
                        boolean slaveFound = false;
                        for (MasterSlaveEntry masterSlaveEntry : connectionManager.getEntrySet()) {
                            if (!masterSlaveEntry.hasSlave(currentSlaveAddr)) {
                                continue;
                            }

                            slaveFound = true;
                            if (masterSlaveEntry.hasSlave(newSlaveAddr)) {
                                CompletableFuture<Boolean> slaveUpFuture = masterSlaveEntry.slaveUpAsync(newSlaveAddr);
                                slaveUpFuture.whenComplete((r, e) -> {
                                    if (e != null) {
                                        promise.complete(null);
                                        return;
                                    }
                                    if (r) {
                                        slaves.put(entry.getKey(), newSlaveAddr);
                                        masterSlaveEntry.slaveDown(currentSlaveAddr);
                                    }
                                    promise.complete(null);
                                });
                            } else {
                                CompletableFuture<Void> addFuture = masterSlaveEntry.addSlave(newSlaveAddr, entry.getKey());
                                addFuture.whenComplete((res, e) -> {
                                    if (e != null) {
                                        log.error("Can't add slave: {}", newSlaveAddr, e);
                                        promise.complete(null);
                                        return;
                                    }

                                    slaves.put(entry.getKey(), newSlaveAddr);
                                    masterSlaveEntry.slaveDown(currentSlaveAddr);
                                    promise.complete(null);
                                });
                            }
                            break;
                        }
                        if (!slaveFound) {
                            promise.complete(null);
                        }
                    } else {
                        promise.complete(null);
                    }
                } catch (UnknownHostException e) {
                    log.error(e.getMessage(), e);
                    promise.complete(null);
                }
            });
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
    
    /** 多次解析 DNS，收集所有 IP 并判断是否与当前地址一致。 */
    private CompletableFuture<Boolean> resolveTimes(Entry<RedisURI, InetSocketAddress> entry, Set<RedisURI> addressSet, int times) {
        CompletableFuture<List<RedisURI>> ipsFuture = connectionManager.getServiceManager().resolveAll(entry.getKey());
        return ipsFuture.thenCompose(addresses -> {
            for (RedisURI address : addresses) {
                addressSet.add(address);
                if (address.equals(entry.getValue())) {
                    return CompletableFuture.completedFuture(true);
                }
            }
            
            if (times + 1 < dnsMonitoringTimes) {
                return resolveTimes(entry, addressSet, times + 1);
            }
            return CompletableFuture.completedFuture(false);
        });
    }

}
