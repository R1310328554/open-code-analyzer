package com.taobao.arthas.core.command.model;

/**
 * dashboard 命令中展示的 GC 收集器统计项。
 * <p>
 * 数据通常来自 {@code java.lang.management.GarbageCollectorMXBean}，
 * {@link #collectionCount} 与 {@link #collectionTime} 在 MXBean 不可用时可能为 0。
 *
 * GC info of dashboard
 * @author gongdewei 2020/4/23
 */
public class GcInfoVO {
    /** GC 收集器名称（如 G1 Young Generation） */
    private String name;
    /** 累计 GC 次数 */
    private long collectionCount;
    /** 累计 GC 耗时（毫秒） */
    private long collectionTime;

    public GcInfoVO(String name, long collectionCount, long collectionTime) {
        this.name = name;
        this.collectionCount = collectionCount;
        this.collectionTime = collectionTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCollectionCount() {
        return collectionCount;
    }

    public void setCollectionCount(long collectionCount) {
        this.collectionCount = collectionCount;
    }

    public long getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(long collectionTime) {
        this.collectionTime = collectionTime;
    }
}
