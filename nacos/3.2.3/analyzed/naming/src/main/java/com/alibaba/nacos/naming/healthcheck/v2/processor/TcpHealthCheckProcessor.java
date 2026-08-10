/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.healthcheck.v2.processor;

import com.alibaba.nacos.api.naming.pojo.healthcheck.HealthCheckType;
import com.alibaba.nacos.naming.core.v2.metadata.ClusterMetadata;
import com.alibaba.nacos.naming.core.v2.pojo.HealthCheckInstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.healthcheck.v2.HealthCheckTaskV2;
import com.alibaba.nacos.naming.misc.GlobalExecutor;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.naming.monitor.MetricsMonitor;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.naming.misc.Loggers.SRV_LOG;

/**
 * Nacos v2.x TCP 健康检查处理器。
 *
 * <p>基于 NIO {@link Selector} 异步探测实例 TCP 端口连通性，采用 SuperSense 多线程模型批量处理连接任务；当前逻辑与 v1.x 一致。</p>
 *
 * @author xiweng.yy
 */
@Component
public class TcpHealthCheckProcessor implements HealthCheckProcessorV2, Runnable {
    
    /** 健康检查类型标识：TCP。 */
    public static final String TYPE = HealthCheckType.TCP.name();
    
    /** TCP 连接超时（毫秒）。 */
    public static final int CONNECT_TIMEOUT_MS = 500;
    
    /**
     * NIO 工作线程数，经调优确定，请勿随意修改。
     */
    private static final int NIO_THREAD_COUNT = EnvUtil.getAvailableProcessors(0.5);
    
    /**
     * TCP 连接保活时长（毫秒）；部分主机不支持 keep-alive，暂设为 0 禁用复用。
     */
    private static final long TCP_KEEP_ALIVE_MILLIS = 0;
    
    private final HealthCheckCommonV2 healthCheckCommon;
    
    private final SwitchDomain switchDomain;
    
    private final Map<String, BeatKey> keyMap = new ConcurrentHashMap<>();
    
    private final BlockingQueue<Beat> taskQueue = new LinkedBlockingQueue<>();
    
    private final Selector selector;
    
    public TcpHealthCheckProcessor(HealthCheckCommonV2 healthCheckCommon,
        SwitchDomain switchDomain) {
        this.healthCheckCommon = healthCheckCommon;
        this.switchDomain = switchDomain;
        try {
            selector = Selector.open();
            GlobalExecutor.submitTcpCheck(this);
        } catch (Exception e) {
            throw new IllegalStateException("Error while initializing SuperSense(TM).");
        }
    }
    
    @Override
    public void process(HealthCheckTaskV2 task, Service service, ClusterMetadata metadata) {
        HealthCheckInstancePublishInfo instance = (HealthCheckInstancePublishInfo) task.getClient()
            .getInstancePublishInfo(service);
        if (null == instance) {
            return;
        }
        // TODO：与 v1.x 一致处理 marked（白名单）逻辑。
        if (!instance.tryStartCheck()) {
            SRV_LOG.warn(
                "[HEALTH-CHECK-V2] tcp check started before last one finished, service: {} : {} : {}:{}",
                service.getNameSpaceGroupedServiceName(), instance.getCluster(), instance.getIp(),
                instance.getPort());
            healthCheckCommon
                .reEvaluateCheckRt(task.getCheckRtNormalized() * 2, task,
                    switchDomain.getTcpHealthParams());
            return;
        }
        taskQueue.add(new Beat(task, service, metadata, instance));
        MetricsMonitor.getTcpHealthCheckMonitor().incrementAndGet();
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
    
    private void processTask() throws Exception {
        Collection<Callable<Void>> tasks = new LinkedList<>();
        do {
            Beat beat = taskQueue.poll(CONNECT_TIMEOUT_MS / 2, TimeUnit.MILLISECONDS);
            if (beat == null) {
                return;
            }
            
            tasks.add(new TaskProcessor(beat));
        } while (taskQueue.size() > 0 && tasks.size() < NIO_THREAD_COUNT * 64);
        
        for (Future<?> f : GlobalExecutor.invokeAllTcpSuperSenseTask(tasks)) {
            f.get();
        }
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                processTask();
                
                int readyCount = selector.selectNow();
                if (readyCount <= 0) {
                    continue;
                }
                
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    
                    GlobalExecutor.executeTcpSuperSense(new PostProcessor(key));
                }
            } catch (Throwable e) {
                SRV_LOG.error("[HEALTH-CHECK-V2] error while processing NIO task", e);
            }
        }
    }
    
    /** NIO 连接就绪后的后置处理器，判定 TCP 检查结果。 */
    public class PostProcessor implements Runnable {
        
        SelectionKey key;
        
        public PostProcessor(SelectionKey key) {
            this.key = key;
        }
        
        @Override
        public void run() {
            Beat beat = (Beat) key.attachment();
            SocketChannel channel = (SocketChannel) key.channel();
            try {
                if (!beat.isHealthy()) {
                    // beat 已过期，表示本节点不再负责该实例的健康检查
                    key.cancel();
                    key.channel().close();
                    
                    beat.finishCheck();
                    return;
                }
                
                if (key.isValid() && key.isConnectable()) {
                    // 连接成功，标记实例健康
                    channel.finishConnect();
                    beat.finishCheck(true, false,
                        System.currentTimeMillis() - beat.getTask().getStartTime(),
                        "tcp:ok+");
                    // Connections are not reused (keep-alive disabled), so close the
                    // channel as soon as the check succeeds to avoid leaking the FD.
                    // Closing failures must not flip the already-successful result, so
                    // they are swallowed here instead of falling into the catch below.
                    try {
                        key.cancel();
                        channel.close();
                    } catch (Exception ignore) {
                        // ignore close failure
                    }
                    return;
                }
                
                if (key.isValid() && key.isReadable()) {
                    // 对端关闭连接
                    ByteBuffer buffer = ByteBuffer.allocate(128);
                    if (channel.read(buffer) == -1) {
                        key.cancel();
                        key.channel().close();
                    } else {
                        // 非终止请求，忽略响应数据
                        SRV_LOG.warn(
                            "Tcp check ok, but the connected server responses some msg. Connection won't be closed.");
                    }
                }
            } catch (ConnectException e) {
                // 无法连接，可能端口未开放
                beat.finishCheck(false, true, switchDomain.getTcpHealthParams().getMax(),
                    "tcp:unable2connect:" + e.getMessage());
                // finishCheck() already removed this beat from keyMap, so the next round
                // can no longer close this channel; close it here to avoid leaking the FD
                // on the common connection-refused failure path.
                try {
                    key.cancel();
                    channel.close();
                } catch (Exception ignore) {
                    // ignore close failure
                }
            } catch (Exception e) {
                beat.finishCheck(false, false, switchDomain.getTcpHealthParams().getMax(),
                    "tcp:error:" + e.getMessage());
                
                try {
                    key.cancel();
                    key.channel().close();
                } catch (Exception ignore) {
                }
            }
        }
    }
    
    private class Beat {
        
        private final HealthCheckTaskV2 task;
        
        private final Service service;
        
        private final ClusterMetadata metadata;
        
        private final HealthCheckInstancePublishInfo instance;
        
        long startTime = System.currentTimeMillis();
        
        public Beat(HealthCheckTaskV2 task, Service service, ClusterMetadata metadata,
            HealthCheckInstancePublishInfo instance) {
            this.task = task;
            this.service = service;
            this.metadata = metadata;
            this.instance = instance;
        }
        
        public void setStartTime(long time) {
            startTime = time;
        }
        
        public long getStartTime() {
            return startTime;
        }
        
        public HealthCheckTaskV2 getTask() {
            return task;
        }
        
        public Service getService() {
            return service;
        }
        
        public ClusterMetadata getMetadata() {
            return metadata;
        }
        
        public HealthCheckInstancePublishInfo getInstance() {
            return instance;
        }
        
        public boolean isHealthy() {
            return System.currentTimeMillis() - startTime < TimeUnit.SECONDS.toMillis(30L);
        }
        
        /**
         * 仅结束检查流程，不修改实例 IP 健康状态。
         */
        public void finishCheck() {
            instance.finishCheck();
        }
        
        public void finishCheck(boolean success, boolean now, long rt, String msg) {
            if (success) {
                healthCheckCommon.checkOk(task, service, msg);
            } else {
                if (now) {
                    healthCheckCommon.checkFailNow(task, service, msg);
                } else {
                    healthCheckCommon.checkFail(task, service, msg);
                }
                
                keyMap.remove(toString());
            }
            
            healthCheckCommon.reEvaluateCheckRt(rt, task, switchDomain.getTcpHealthParams());
        }
        
        @Override
        public String toString() {
            return service.getNameSpaceGroupedServiceName() + ":" + instance.getCluster() + ":"
                + instance.getIp() + ":"
                + instance.getPort();
        }
        
        @Override
        public int hashCode() {
            return toString().hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Beat)) {
                return false;
            }
            
            return this.toString().equals(obj.toString());
        }
    }
    
    /** TCP 检查 beat 与 SelectionKey 的关联键。 */
    private static class BeatKey {
        
        public SelectionKey key;
        
        public long birthTime;
        
        public BeatKey(SelectionKey key) {
            this.key = key;
            this.birthTime = System.currentTimeMillis();
        }
    }
    
    /** TCP 连接超时任务，超时后关闭 channel 并标记失败。 */
    private static class TimeOutTask implements Runnable {
        
        SelectionKey key;
        
        public TimeOutTask(SelectionKey key) {
            this.key = key;
        }
        
        @Override
        public void run() {
            if (key != null && key.isValid()) {
                SocketChannel channel = (SocketChannel) key.channel();
                Beat beat = (Beat) key.attachment();
                
                if (channel.isConnected()) {
                    return;
                }
                
                try {
                    channel.finishConnect();
                } catch (Exception ignore) {
                }
                
                try {
                    beat.finishCheck(false, false, beat.getTask().getCheckRtNormalized() * 2,
                        "tcp:timeout");
                    key.cancel();
                    key.channel().close();
                } catch (Exception ignore) {
                }
            }
        }
    }
    
    /** TCP 检查任务处理器，发起非阻塞 connect 并注册到 Selector。 */
    private class TaskProcessor implements Callable<Void> {
        
        private static final int MAX_WAIT_TIME_MILLISECONDS = 500;
        
        Beat beat;
        
        public TaskProcessor(Beat beat) {
            this.beat = beat;
        }
        
        @Override
        public Void call() {
            long waited = System.currentTimeMillis() - beat.getStartTime();
            if (waited > MAX_WAIT_TIME_MILLISECONDS) {
                Loggers.SRV_LOG.warn("beat task waited too long: " + waited + "ms");
            }
            
            SocketChannel channel = null;
            try {
                HealthCheckInstancePublishInfo instance = beat.getInstance();
                
                BeatKey beatKey = keyMap.get(beat.toString());
                if (beatKey != null && beatKey.key.isValid()) {
                    if (System.currentTimeMillis() - beatKey.birthTime < TCP_KEEP_ALIVE_MILLIS) {
                        instance.finishCheck();
                        return null;
                    }
                    
                    beatKey.key.cancel();
                    beatKey.key.channel().close();
                }
                
                channel = SocketChannel.open();
                channel.configureBlocking(false);
                // 设置 SO_LINGER 使 socket 关闭事件异步触发
                channel.socket().setSoLinger(false, -1);
                channel.socket().setReuseAddress(true);
                channel.socket().setKeepAlive(true);
                channel.socket().setTcpNoDelay(true);
                
                ClusterMetadata cluster = beat.getMetadata();
                int port = cluster.isUseInstancePortForCheck() ? instance.getPort()
                    : cluster.getHealthyCheckPort();
                channel.connect(new InetSocketAddress(instance.getIp(), port));
                
                SelectionKey key =
                    channel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
                key.attach(beat);
                keyMap.put(beat.toString(), new BeatKey(key));
                
                beat.setStartTime(System.currentTimeMillis());
                
                GlobalExecutor
                    .scheduleTcpSuperSenseTask(new TimeOutTask(key), CONNECT_TIMEOUT_MS,
                        TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                beat.finishCheck(false, false, switchDomain.getTcpHealthParams().getMax(),
                    "tcp:error:" + e.getMessage());
                
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (Exception ignore) {
                    }
                }
            }
            
            return null;
        }
    }
}
