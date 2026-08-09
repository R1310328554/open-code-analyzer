/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.store.timer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.apache.rocketmq.common.ConfigManager;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.protocol.DataVersion;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 定时消息指标：按 Topic 与延迟分布桶统计定时消息数量。
 */
public class TimerMetrics extends ConfigManager {
    /** 存储模块日志。 */
    private static final Logger log = LoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);
    private static final long LOCK_TIMEOUT_MILLIS = 3000;
    private transient final Lock lock = new ReentrantLock();

    /** Topic -> 定时消息计数。 */
    private final ConcurrentMap<String, Metric> timingCount = new ConcurrentHashMap<>(1024);

    /** 延迟桶（秒）-> 分布计数。 */
    private final ConcurrentMap<Integer, Metric> timingDistribution = new ConcurrentHashMap<>(1024);

    @SuppressWarnings("DoubleBraceInitialization")
    public List<Integer> timerDist = new ArrayList<Integer>() {{
            add(5);
            add(60);
            add(300); // 5s, 1min, 5min
            add(900);
            add(3600);
            add(14400); // 15min, 1h, 4h
            add(28800);
            add(86400); // 8h, 24h
        }};
    private final DataVersion dataVersion = new DataVersion();

    /** 指标 JSON 持久化路径。 */
    private final String configPath;

        /** @param configPath 指标持久化路径 */
    public TimerMetrics(String configPath) {
        this.configPath = configPath;
    }

    /** 按延迟分布桶累加计数。 */
    public long updateDistPair(int period, int value) {
        Metric distPair = getDistPair(period);
        return distPair.getCount().addAndGet(value);
    }

    /** 按真实 Topic 累加定时消息计数并刷新版本。 */
    public long addAndGet(MessageExt msg, int value) {
        String topic = msg.getProperty(MessageConst.PROPERTY_REAL_TOPIC);
        Metric pair = getTopicPair(topic);
        getDataVersion().nextVersion();
        pair.setTimeStamp(System.currentTimeMillis());
        return pair.getCount().addAndGet(value);
    }

    /** 获取或创建延迟分布桶指标。 */
    public Metric getDistPair(Integer period) {
        Metric pair = timingDistribution.get(period);
        if (null != pair) {
            return pair;
        }
        pair = new Metric();
        final Metric previous = timingDistribution.putIfAbsent(period, pair);
        if (null != previous) {
            return previous;
        }
        return pair;
    }

    /** 获取或创建 Topic 级指标。 */
    public Metric getTopicPair(String topic) {
        Metric pair = timingCount.get(topic);
        if (null != pair) {
            return pair;
        }
        pair = new Metric();
        final Metric previous = timingCount.putIfAbsent(topic, pair);
        if (null != previous) {
            return previous;
        }
        return pair;
    }

    /** 返回延迟分布桶边界列表（秒）。 */
    public List<Integer> getTimerDistList() {
        return this.timerDist;
    }

    /** 设置延迟分布桶边界。 */
    public void setTimerDistList(List<Integer> timerDist) {
        this.timerDist = timerDist;
    }

    /** 查询指定 Topic 定时消息累计数。 */
    public long getTimingCount(String topic) {
        Metric pair = timingCount.get(topic);
        if (null == pair) {
            return 0;
        } else {
            return pair.getCount().get();
        }
    }

    /** 返回全部 Topic 定时计数映射。 */
    public Map<String, Metric> getTimingCount() {
        return timingCount;
    }

    protected void write0(Writer writer) throws IOException {
        TimerMetricsSerializeWrapper wrapper = new TimerMetricsSerializeWrapper();
        wrapper.setTimingCount(timingCount);
        wrapper.setDataVersion(dataVersion);
        writer.write(JSON.toJSONString(wrapper, JSONWriter.Feature.BrowserCompatible));
    }

    @Override     /** {@inheritDoc} */
    public String encode() {
        return encode(false);
    }

    @Override     /** {@inheritDoc} */
    public String configFilePath() {
        return configPath;
    }

    @Override     /** {@inheritDoc} 从 JSON 恢复指标。 */
    public void decode(String jsonString) {
        if (jsonString != null) {
            TimerMetricsSerializeWrapper timerMetricsSerializeWrapper = TimerMetricsSerializeWrapper.fromJson(jsonString, TimerMetricsSerializeWrapper.class);
            if (timerMetricsSerializeWrapper != null) {
                this.timingCount.putAll(timerMetricsSerializeWrapper.getTimingCount());
                this.dataVersion.assignNewOne(timerMetricsSerializeWrapper.getDataVersion());
            }
        }
    }

    @Override     /** {@inheritDoc} */
    public String encode(boolean prettyFormat) {
        TimerMetricsSerializeWrapper metricsSerializeWrapper = new TimerMetricsSerializeWrapper();
        metricsSerializeWrapper.setDataVersion(this.dataVersion);
        metricsSerializeWrapper.setTimingCount(this.timingCount);
        return metricsSerializeWrapper.toJson(prettyFormat);
    }

    /** 返回指标数据版本。 */
    public DataVersion getDataVersion() {
        return dataVersion;
    }

    /** 清理不在 Topic 配置中的非系统/LMQ 指标。 */
    public void cleanMetrics(Set<String> topics) {
        if (topics == null || topics.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<String, Metric>> iterator = timingCount.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Metric> entry = iterator.next();
            final String topic = entry.getKey();
            if (topic.startsWith(TopicValidator.SYSTEM_TOPIC_PREFIX) || topic.startsWith(MixAll.LMQ_PREFIX)) {
                continue;
            }
            if (topics.contains(topic)) {
                continue;
            }

            iterator.remove();
            log.info("clean timer metrics, because not in topic config, {}", topic);
        }
    }

    /** 移除指定 Topic 的定时计数。 */
    public boolean removeTimingCount(String topic) {
        try {
            timingCount.remove(topic);
        } catch (Exception e) {
            log.error("removeTimingCount error", e);
            return false;
        }
        return true;
    }

    public static class TimerMetricsSerializeWrapper extends RemotingSerializable {
        private ConcurrentMap<String, Metric> timingCount = new ConcurrentHashMap<>(1024);
        private DataVersion dataVersion = new DataVersion();

        public ConcurrentMap<String, Metric> getTimingCount() {
            return timingCount;
        }

        public void setTimingCount(ConcurrentMap<String, Metric> timingCount) {
            this.timingCount = timingCount;
        }

        public DataVersion getDataVersion() {
            return dataVersion;
        }

        public void setDataVersion(DataVersion dataVersion) {
            this.dataVersion = dataVersion;
        }
    }

    @Override     /** 原子备份并持久化指标 JSON 文件。 */
    public synchronized void persist() {
        try {
            // bak metrics file
            String config = configFilePath();
            String backup = config + ".bak";
            File configFile = new File(config);
            File bakFile = new File(backup);

            if (configFile.exists()) {
                // atomic move
                Files.move(configFile.toPath(), bakFile.toPath(), StandardCopyOption.ATOMIC_MOVE);

                // sync the directory, ensure that the bak file is visible
                MixAll.fsyncDirectory(Paths.get(bakFile.getParent()));
            }

            File dir = new File(configFile.getParent());
            if (!dir.exists()) {
                Files.createDirectories(dir.toPath());
            }

            // persist metrics file
            StringWriter stringWriter = new StringWriter();
            write0(stringWriter);
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(config, "rw")) {
                randomAccessFile.write(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
                randomAccessFile.getChannel().force(true);
                // sync the directory, ensure that the config file is visible
                MixAll.fsyncDirectory(Paths.get(configFile.getParent()));
            }
        } catch (Throwable t) {
            log.error("Failed to persist", t);
        }
    }

    public static class Metric {
        private AtomicLong count;
        private long timeStamp;

        public Metric() {
            count = new AtomicLong(0);
            timeStamp = System.currentTimeMillis();
        }

        public AtomicLong getCount() {
            return count;
        }

        public void setCount(AtomicLong count) {
            this.count = count;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(long timeStamp) {
            this.timeStamp = timeStamp;
        }

        @Override public String toString() {
            return String.format("[%d,%d]", count.get(), timeStamp);
        }
    }

}
