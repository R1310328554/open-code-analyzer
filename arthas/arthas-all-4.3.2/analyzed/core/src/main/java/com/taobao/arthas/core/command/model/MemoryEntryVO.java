package com.taobao.arthas.core.command.model;

/**
 * 内存区域条目视图：dashboard / memory 命令共用的堆、非堆或缓冲池统计项。
 * <p>
 * {@link #type} 取 {@link #TYPE_HEAP}、{@link #TYPE_NON_HEAP} 或 {@link #TYPE_BUFFER_POOL}；
 * {@link #max} 为 -1 表示无上限（如部分 Metaspace 配置）。
 *
 * @author gongdewei 2020/4/22
 */
public class MemoryEntryVO {

    /** 堆内存区域 */
    public static final String TYPE_HEAP = "heap";
    /** 非堆内存区域（Metaspace、Code Cache 等） */
    public static final String TYPE_NON_HEAP = "nonheap";
    /** NIO 缓冲池（Direct/Mapped 等） */
    public static final String TYPE_BUFFER_POOL = "buffer_pool";

    /** 区域类型，见 TYPE_* 常量 */
    private String type;
    /** 区域或池名称（如 PS Eden Space、Metaspace） */
    private String name;
    /** 当前已使用字节数 */
    private long used;
    /** 当前已提交/总容量字节数 */
    private long total;
    /** 最大可扩展字节数，-1 表示未定义上限 */
    private long max;

    public MemoryEntryVO() {
    }

    public MemoryEntryVO(String type, String name, long used, long total, long max) {
        this.type = type;
        this.name = name;
        this.used = used;
        this.total = total;
        this.max = max;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }
}
