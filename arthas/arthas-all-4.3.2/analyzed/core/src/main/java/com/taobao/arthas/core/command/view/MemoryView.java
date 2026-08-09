package com.taobao.arthas.core.command.view;

import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.Map;

import com.taobao.arthas.core.command.model.MemoryEntryVO;
import com.taobao.arthas.core.command.model.MemoryModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.Style;
import com.taobao.text.ui.RowElement;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

/**
 * {@code memory} 命令的终端渲染视图。
 * <p>
 * 将堆、非堆、BufferPool 用量渲染为 used/total/max/usage 表格；
 * {@link #drawMemoryInfo} 亦被 {@link com.taobao.arthas.core.command.view.DashboardView} 复用。
 *
 * @author hengyunabc 2022-03-01
 */
public class MemoryView extends ResultView<MemoryModel> {

    @Override
    public void draw(CommandProcess process, MemoryModel result) {
        TableElement table = drawMemoryInfo(result.getMemoryInfo());
        process.write(RenderUtil.render(table, process.width()));
    }

    static TableElement drawMemoryInfo(Map<String, List<MemoryEntryVO>> memoryInfo) {
        TableElement table = new TableElement(3, 1, 1, 1, 1).rightCellPadding(1);
        table.add(new RowElement().style(Decoration.bold.fg(Color.black).bg(Color.white)).add("Memory",
                "used", "total", "max", "usage"));
        List<MemoryEntryVO> heapMemoryEntries = memoryInfo.get(MemoryEntryVO.TYPE_HEAP);
        // 堆内存：汇总行加粗
        for (MemoryEntryVO memoryEntryVO : heapMemoryEntries) {
            if (MemoryEntryVO.TYPE_HEAP.equals(memoryEntryVO.getName())) {
                new MemoryEntry(memoryEntryVO).addTableRow(table, Decoration.bold.bold());
            } else {
                new MemoryEntry(memoryEntryVO).addTableRow(table);
            }
        }

        // 非堆内存（Metaspace、Code Cache 等）
        List<MemoryEntryVO> nonheapMemoryEntries = memoryInfo.get(MemoryEntryVO.TYPE_NON_HEAP);
        for (MemoryEntryVO memoryEntryVO : nonheapMemoryEntries) {
            if (MemoryEntryVO.TYPE_NON_HEAP.equals(memoryEntryVO.getName())) {
                new MemoryEntry(memoryEntryVO).addTableRow(table, Decoration.bold.bold());
            } else {
                new MemoryEntry(memoryEntryVO).addTableRow(table);
            }
        }

        // Direct/Mapped 等 BufferPool（可能为 null）
        List<MemoryEntryVO> bufferPoolMemoryEntries = memoryInfo.get(MemoryEntryVO.TYPE_BUFFER_POOL);
        if (bufferPoolMemoryEntries != null) {
            for (MemoryEntryVO memoryEntryVO : bufferPoolMemoryEntries) {
                new MemoryEntry(memoryEntryVO).addTableRow(table);
            }
        }
        return table;
    }

    /** 单行内存条目：按用量自动选择 K/M 单位并计算 usage 百分比 */
    static class MemoryEntry {
        String name;
        long used;
        long total;
        long max;

        int unit;
        String unitStr;

        public MemoryEntry(String name, long used, long total, long max) {
            this.name = name;
            this.used = used;
            this.total = total;
            this.max = max;

            unitStr = "K";
            unit = 1024;
            if (used / 1024 / 1024 > 0) {
                unitStr = "M";
                unit = 1024 * 1024;
            }
        }

        public MemoryEntry(String name, MemoryUsage usage) {
            this(name, usage.getUsed(), usage.getCommitted(), usage.getMax());
        }

        public MemoryEntry(MemoryEntryVO memoryEntryVO) {
            this(memoryEntryVO.getName(), memoryEntryVO.getUsed(), memoryEntryVO.getTotal(), memoryEntryVO.getMax());
        }

        private String format(long value) {
            String valueStr = "-";
            // max 未定义时 JVM 返回 -1
            if (value == -1) {
                return "-1";
            }
            if (value != Long.MIN_VALUE) {
                valueStr = value / unit + unitStr;
            }
            return valueStr;
        }

        public void addTableRow(TableElement table) {
            double usage = used / (double) (max == -1 || max == Long.MIN_VALUE ? total : max) * 100;
            // max 为 -1 时用 committed 作分母，异常值归零
            if (Double.isNaN(usage) || Double.isInfinite(usage)) {
                usage = 0;
            }
            table.row(name, format(used), format(total), format(max), String.format("%.2f%%", usage));
        }

        public void addTableRow(TableElement table, Style.Composite style) {
            double usage = used / (double) (max == -1 || max == Long.MIN_VALUE ? total : max) * 100;
            if (Double.isNaN(usage) || Double.isInfinite(usage)) {
                usage = 0;
            }
            table.add(new RowElement().style(style).add(name, format(used), format(total), format(max),
                    String.format("%.2f%%", usage)));
        }
    }
}
