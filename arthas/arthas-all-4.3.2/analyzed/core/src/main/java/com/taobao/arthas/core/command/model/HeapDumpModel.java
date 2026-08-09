package com.taobao.arthas.core.command.model;

/**
 * {@code heapdump} 命令的结果模型，报告堆转储文件路径与是否仅含存活对象。
 * <p>
 * {@link #live} 为 true 时对应 {@code heapdump --live}，触发 Full GC 后 dump；
 * {@link #dumpFile} 为服务端写入的 hprof 绝对路径。
 *
 * Model of `heapdump` command
 * @author gongdewei 2020/4/24
 */
public class HeapDumpModel extends ResultModel {

    /** 生成的 heap dump 文件路径 */
    private String dumpFile;

    /** 是否仅 dump 存活对象（会先触发 GC） */
    private boolean live;

    public HeapDumpModel() {
    }

    public HeapDumpModel(String dumpFile, boolean live) {
        this.dumpFile = dumpFile;
        this.live = live;
    }

    public String getDumpFile() {
        return dumpFile;
    }

    public void setDumpFile(String dumpFile) {
        this.dumpFile = dumpFile;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    @Override
    public String getType() {
        return "heapdump";
    }

}
