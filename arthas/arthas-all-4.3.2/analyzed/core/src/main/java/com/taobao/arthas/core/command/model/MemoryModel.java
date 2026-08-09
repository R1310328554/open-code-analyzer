package com.taobao.arthas.core.command.model;

import java.util.List;
import java.util.Map;

/**
 * memory 命令的结构化结果：按类别分组的内存区域统计。
 * <p>
 * {@link #memoryInfo} 的 key 通常为 heap / nonheap / buffer_pool 等，
 * value 为该类别下的 {@link MemoryEntryVO} 列表。
 *
 * @author hengyunabc 2022-03-01
 */
public class MemoryModel extends ResultModel {
    /** 内存类别 → 该区域下的各子项统计列表 */
    private Map<String, List<MemoryEntryVO>> memoryInfo;

    @Override
    public String getType() {
        return "memory";
    }

    public Map<String, List<MemoryEntryVO>> getMemoryInfo() {
        return memoryInfo;
    }

    public void setMemoryInfo(Map<String, List<MemoryEntryVO>> memoryInfo) {
        this.memoryInfo = memoryInfo;
    }
}
