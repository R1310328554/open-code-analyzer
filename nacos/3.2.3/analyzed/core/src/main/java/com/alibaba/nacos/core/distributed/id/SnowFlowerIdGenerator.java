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

package com.alibaba.nacos.core.distributed.id;

import com.alibaba.nacos.consistency.IdGenerator;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.utils.InetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 雪花算法分布式 ID 生成器（参考 shariding-JDBC-snowflake 实现）。
 *
 * <p><strong>WorkerId</strong> 生成策略：优先读取配置 {@code nacos.core.snowflake.worker-id}，未配置时根据本机 IP 末两字节计算；10 位 workerId 与 12 位序列号组合，配合单调时钟避免同一毫秒内序列溢出。</p>
 *
 * <p>WorkerId 重复周期与 Raft 选主间隔相关：若每次选举间隔 5 秒，最大 workerId 重复约需 150 秒（假设每次选主后需重新分配）。</p>
 *
 * copy from http://www.cluozy.com/home/hexo/2018/08/11/shariding-JDBC-snowflake/.
 *
 * <strong>WorkerId</strong> generation policy: Calculate the InetAddress hashcode
 *
 * <p>The repeat rate of the dataCenterId, the value of the maximum dataCenterId times the time of each Raft election.
 * The
 * time for raft to select the master is generally measured in seconds. If the interval of an election is 5 seconds, it
 * will take 150 seconds for the DataCenterId to be repeated. This is still based on the situation that the new master
 * needs to be selected after each election of the Leader
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 */
@SuppressWarnings("all")
public class SnowFlowerIdGenerator implements IdGenerator {
    
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    
    /**
     * 雪花算法纪元起点（2018-08-05 08:34），用于压缩时间戳位数。
     * Start time intercept (2018-08-05 08:34)
     */
    public static final long EPOCH = 1533429240000L;
    
    private static final Logger logger = LoggerFactory.getLogger(SnowFlowerIdGenerator.class);
    
    // 序列号占用位数
    private static final long SEQUENCE_BITS = 12L;
    
    // workerId 占用位数
    private static final long WORKER_ID_BITS = 10L;
    
    // 序列号掩码（111111111111B = 4095）
    private static final long SEQUENCE_MASK = 4095L;
    
    // workerId 左移位数，等于序列号位数 12
    private static final long WORKER_ID_LEFT_SHIFT_BITS = 12L;
    
    // 时间戳左移位数 22（序列 12 + workerId 10）
    private static final long TIMESTAMP_LEFT_SHIFT_BITS = 22L;
    
    // workerId 最大值为 1024
    private static final long WORKER_ID_MAX_VALUE = 1024L;
    
    // 墙钟起始时间（CLOCK_REALTIME）
    private final long startWallTime = System.currentTimeMillis();
    
    // 单调时钟起始纳秒（CLOCK_MONOTONIC）
    private final long monotonicStartTime = System.nanoTime();
    
    /** 工作节点 ID（0~1024）。 */
    private long workerId;
    
    /** 当前毫秒内的序列号。 */
    private long sequence;
    
    /** 上次生成 ID 的毫秒时间戳。 */
    private long lastTime;
    
    /** 最近一次生成的 ID 值。 */
    private long currentId;
    
    {
        long workerId = EnvUtil.getProperty("nacos.core.snowflake.worker-id", Integer.class, -1);
        
        if (workerId != -1) {
            this.workerId = workerId;
        } else {
            InetAddress address;
            try {
                address = InetAddress.getByName(InetUtils.getSelfIP());
            } catch (final UnknownHostException e) {
                throw new IllegalStateException(
                    "Cannot get LocalHost InetAddress, please check your network!", e);
            }
            byte[] ipAddressByteArray = address.getAddress();
            this.workerId =
                (((ipAddressByteArray[ipAddressByteArray.length - 2] & 0B11) << Byte.SIZE)
                    + (ipAddressByteArray[ipAddressByteArray.length - 1] & 0xFF));
        }
    }
    
    /** {@inheritDoc} 使用实例块解析的 workerId 初始化。 */
    @Override
    public void init() {
        initialize(workerId);
    }
    
    /** {@inheritDoc} 返回最近一次生成的 ID。 */
    @Override
    public long currentId() {
        return currentId;
    }
    
    /** {@inheritDoc} 返回当前 workerId。 */
    @Override
    public long workerId() {
        return workerId;
    }
    
    /**
     * {@inheritDoc} 生成下一个雪花 ID：同毫秒内序列自增，溢出则等待下一毫秒。
     *
     * @return 64 位分布式 ID
     */
    @Override
    public synchronized long nextId() {
        long currentMillis = currentTimeMillis();
        if (this.lastTime == currentMillis) {
            if (0L == (this.sequence = ++this.sequence & SEQUENCE_MASK)) {
                currentMillis = this.waitUntilNextTime(currentMillis);
            }
        } else {
            this.sequence = 0L;
        }
        
        this.lastTime = currentMillis;
        
        if (logger.isDebugEnabled()) {
            logger.debug("{}-{}-{}",
                (new SimpleDateFormat(DATETIME_PATTERN)).format(new Date(this.lastTime)),
                workerId, this.sequence);
        }
        
        currentId = currentMillis - EPOCH << 22 | workerId << 12 | this.sequence;
        return currentId;
    }
    
    /** {@inheritDoc} 返回 currentId 与 workerId 等运行时信息。 */
    @Override
    public Map<Object, Object> info() {
        Map<Object, Object> info = new HashMap<>(4);
        info.put("currentId", currentId);
        info.put("workerId", workerId);
        return info;
    }
    
    // ============================== 构造与初始化 =============================
    
    /**
     * 显式设置 workerId 并校验范围。
     *
     * @param workerId worker id (0~1024)
     */
    public void initialize(long workerId) {
        if (workerId > WORKER_ID_MAX_VALUE || workerId < 0) {
            throw new IllegalArgumentException(
                String.format(
                    "worker Id can't be greater than %d or less than 0, current workId %d",
                    WORKER_ID_MAX_VALUE, workerId));
        }
        this.workerId = workerId;
    }
    
    /**
     * 阻塞直至获得大于 lastTimestamp 的新毫秒时间戳（序列溢出时使用）。
     *
     * @param lastTimestamp The time intercept of the last ID generated
     * @return Current timestamp
     */
    private long waitUntilNextTime(long lastTimestamp) {
        long time;
        time = currentTimeMillis();
        while (time <= lastTimestamp) {
            time = currentTimeMillis();
        }
        
        return time;
    }
    
    /** 基于单调时钟换算当前毫秒时间戳，避免系统时间回拨。 */
    private long currentTimeMillis() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - monotonicStartTime)
            + startWallTime;
    }
    
}
