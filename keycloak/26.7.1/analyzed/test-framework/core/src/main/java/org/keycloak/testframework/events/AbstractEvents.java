package org.keycloak.testframework.events;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.keycloak.common.util.Time;
import org.keycloak.testframework.realm.ManagedRealm;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;

/**
 * 从 Keycloak 服务器轮询事件的抽象基类。
 * <p>
 * 维护当前测试窗口内的事件队列，支持跳过、清空及按时间偏移增量拉取。
 *
 * @param <R> 事件表示类型
 */
public abstract class AbstractEvents<R> {

    /** 事件所属托管 realm。 */
    protected final ManagedRealm realm;
    /** 本地缓存的待消费事件队列。 */
    protected final LinkedList<R> events = new LinkedList<>();
    /** 已处理事件 ID，用于去重。 */
    protected final Set<String> processedEvents = new HashSet<>();

    /** 当前测试开始时间戳。 */
    protected long testStarted;
    /** 上次拉取时的服务器时间偏移（毫秒）。 */
    protected long timeOffset;
    /** 上次从服务器拉取事件的结束时间。 */
    protected long lastFetch;

    /** 待跳过的后续事件数量。 */
    protected int skip = 0;

    /**
     * @param realm 托管 realm，用于拉取与校验事件
     */
    public AbstractEvents(ManagedRealm realm) {
        this.realm = realm;
    }

    /**
     * 返回当前窗口内最早的一条事件。每个测试开始时窗口重置，先前测试产生的事件会被忽略。
     *
     * @return 当前窗口内最早的事件，无可用事件时返回 {@code null}
     */
    public R poll() {
        long currentTimeOffset = getCurrentTimeOffset();
        if (timeOffset != currentTimeOffset) {
            getLogger().debugv("Timeoffset changed to {0}, resetting events", timeOffset);

            events.clear();
            timeOffset = currentTimeOffset;
            lastFetch = -1;
        }

        if (events.isEmpty()) {
            long from = lastFetch != -1 ? lastFetch : testStarted + currentTimeOffset;
            long to = getCurrentTime() + currentTimeOffset;

            Logger logger = getLogger();
            if (logger.isDebugEnabled()) {
                getLogger().debugv("Fetching events from server between {0} and {1}" + (timeOffset != 0 ? "; current timeoffset is {2}" : ""), formatDate(from), formatDate(to), timeOffset);
            }

            getEvents(from, to)
                    .stream().filter(e -> !processedEvents.contains(getEventId(e)))
                    .forEach(e -> {
                        Assertions.assertEquals(realm.getId(), getRealmId(e));
                        Assertions.assertTrue(getTime(e) > 0);
                        processedEvents.add(getEventId(e));
                        this.events.add(e);
                    });

            lastFetch = to;
        }

        while(skip > 0) {
            if (events.poll() == null) {
                return null;
            }
            skip--;
        }

        return events.poll();
    }

    /** 跳过下一条事件。 */
    public void skip() {
        skip(1);
    }

    /**
     * 跳过指定数量的事件。
     *
     * @param events 要跳过的事件条数
     */
    public void skip(int events) {
        skip += events;
    }

    /** 跳过当前窗口内的全部事件（通过推进测试起始时间实现）。 */
    public void skipAll() {
        try {
            Thread.sleep(1); // 等待 1 毫秒以确保时间戳前进
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        testStarted = getCurrentTime();
        lastFetch = -1;
        events.clear();
    }

    /** 清空本地队列并清除服务器端事件记录。 */
    public void clear() {
        events.clear();
        clearServerEvents();
    }

    /** 在每个测试方法开始前重置时间窗口与拉取状态。 */
    void testStarted() {
        testStarted = getCurrentTime();
        timeOffset = getCurrentTimeOffset();
        lastFetch = -1;
    }

    /**
     * 从服务器拉取指定时间范围内的事件。
     *
     * @param from 起始时间（毫秒）
     * @param to 结束时间（毫秒）
     * @return 事件列表
     */
    protected abstract List<R> getEvents(long from, long to);

    /** 返回事件的唯一标识。 */
    protected abstract String getEventId(R representation);

    /** 返回事件关联的 realm ID。 */
    protected abstract String getRealmId(R representation);

    /** 返回事件发生时间戳。 */
    protected abstract long getTime(R representation);

    /** 清除服务器端存储的事件。 */
    protected abstract void clearServerEvents();

    /** 返回用于调试日志的记录器。 */
    protected abstract Logger getLogger();

    /** 返回当前系统时间（毫秒）。 */
    protected long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /** 返回 Keycloak 服务器时间偏移（毫秒）。 */
    protected long getCurrentTimeOffset() {
        return TimeUnit.MILLISECONDS.convert(Time.getOffset(), TimeUnit.SECONDS);
    }

    /** 将时间戳格式化为可读日期字符串。 */
    protected String formatDate(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(timestamp);
    }

}
