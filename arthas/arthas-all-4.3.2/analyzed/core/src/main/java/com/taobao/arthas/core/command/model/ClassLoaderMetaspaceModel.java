package com.taobao.arthas.core.command.model;

import java.util.List;

/**
 * classloader-metaspace 命令的结构化结果。
 *
 * @author Codex 2026-05-08
 */
public class ClassLoaderMetaspaceModel extends ResultModel {

    /** 各 ClassLoader 的 Metaspace 占用行数据 */
    private List<Row> rows;
    /** 采样总时长（毫秒） */
    private long durationMillis;
    /** 采样间隔（毫秒） */
    private long periodMillis;
    /** 是否输出 verbose 明细 */
    private boolean verbose;

    @Override
    public String getType() {
        return "classloader-metaspace";
    }

    public List<Row> getRows() {
        return rows;
    }

    public ClassLoaderMetaspaceModel setRows(List<Row> rows) {
        this.rows = rows;
        return this;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public ClassLoaderMetaspaceModel setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
        return this;
    }

    public long getPeriodMillis() {
        return periodMillis;
    }

    public ClassLoaderMetaspaceModel setPeriodMillis(long periodMillis) {
        this.periodMillis = periodMillis;
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public ClassLoaderMetaspaceModel setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    /** 单个 ClassLoader 在 Metaspace 中的一行统计 */
    public static class Row {
        /** ClassLoader 描述名 */
        private String name;
        /** ClassLoader 哈希 */
        private String hash;
        /** 加载器类型（如 sun.misc.Launcher$AppClassLoader） */
        private String type;
        /** ClassLoaderData 结构占用（字节） */
        private long classLoaderData;
        /** 已加载类数量 */
        private long classCount;
        /** Metaspace Chunk 大小 */
        private long chunkSize;
        /** Metaspace Block 大小 */
        private long blockSize;
        /** 隐藏 Block 占用（JVM 内部结构） */
        private long hiddenBlockSize;

        public String getName() {
            return name;
        }

        public Row setName(String name) {
            this.name = name;
            return this;
        }

        public String getHash() {
            return hash;
        }

        public Row setHash(String hash) {
            this.hash = hash;
            return this;
        }

        public String getType() {
            return type;
        }

        public Row setType(String type) {
            this.type = type;
            return this;
        }

        public long getClassLoaderData() {
            return classLoaderData;
        }

        public Row setClassLoaderData(long classLoaderData) {
            this.classLoaderData = classLoaderData;
            return this;
        }

        public long getClassCount() {
            return classCount;
        }

        public Row setClassCount(long classCount) {
            this.classCount = classCount;
            return this;
        }

        public long getChunkSize() {
            return chunkSize;
        }

        public Row setChunkSize(long chunkSize) {
            this.chunkSize = chunkSize;
            return this;
        }

        public long getBlockSize() {
            return blockSize;
        }

        public Row setBlockSize(long blockSize) {
            this.blockSize = blockSize;
            return this;
        }

        public long getHiddenBlockSize() {
            return hiddenBlockSize;
        }

        public Row setHiddenBlockSize(long hiddenBlockSize) {
            this.hiddenBlockSize = hiddenBlockSize;
            return this;
        }
    }
}
