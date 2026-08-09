package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.JvmModel;
import com.taobao.arthas.core.command.model.JvmItemVO;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.text.Decoration;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.taobao.text.ui.Element.label;

/**
 * {@code jvm} 命令的终端渲染视图。
 * <p>
 * 将 {@link JvmModel#getJvmInfo()} 按分组（RUNTIME、CLASS-LOADING、GC 等）渲染为两列表格；
 * 内存项（名称以 MEMORY-USAGE 结尾）单独格式化为 init/used/committed/max 四行。
 *
 * @author gongdewei 2020/4/24
 */
public class JvmView extends ResultView<JvmModel> {

    @Override
    public void draw(CommandProcess process, JvmModel result) {
        TableElement table = new TableElement(2, 5).leftCellPadding(1).rightCellPadding(1);

        for (Map.Entry<String, List<JvmItemVO>> entry : result.getJvmInfo().entrySet()) {
            String group = entry.getKey();
            List<JvmItemVO> items = entry.getValue();

            // 分组标题行（加粗）
            table.row(true, label(group).style(Decoration.bold.bold()));
            for (JvmItemVO item : items) {
                String valueStr;
                if (item.getValue() instanceof Map && item.getName().endsWith("MEMORY-USAGE")) {
                    valueStr = renderMemoryUsage((Map<String, Object>) item.getValue());
                } else {
                    valueStr = renderItemValue(item.getValue());
                }
                // 有 desc 时换行附在名称下方
                if (item.getDesc() != null) {
                    table.row(item.getName() + "\n[" + item.getDesc() + "]", valueStr);
                } else {
                    table.row(item.getName(), valueStr);
                }
            }
            // 组间空行分隔
            table.row("", "");
        }

        process.write(RenderUtil.render(table, process.width()));
    }

    /** count/time 数组格式化为 "count/time" */
    private String renderCountTime(long[] value) {
        return value[0] + "/" + value[1];
    }

    /** 按运行时类型分发到集合/数组/Map 或 toString */
    private String renderItemValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Collection) {
            return renderCollectionValue((Collection) value);
        } else if (value instanceof String[]) {
            return renderArrayValue((String[]) value);
        } else if (value instanceof Map) {
            return renderMapValue((Map) value);
        }
        return String.valueOf(value);
    }

    private String renderCollectionValue(Collection<String> strings) {
        final StringBuilder colSB = new StringBuilder();
        if (strings.isEmpty()) {
            colSB.append("[]");
        } else {
            for (String str : strings) {
                colSB.append(str).append("\n");
            }
        }
        return colSB.toString();
    }

    private String renderArrayValue(String... stringArray) {
        final StringBuilder colSB = new StringBuilder();
        if (null == stringArray
                || stringArray.length == 0) {
            colSB.append("[]");
        } else {
            for (String str : stringArray) {
                colSB.append(str).append("\n");
            }
        }
        return colSB.toString();
    }

    private String renderMapValue(Map<String, Object> valueMap) {
        final StringBuilder colSB = new StringBuilder();
        if (valueMap != null) {
            for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                colSB.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
            }
        }
        return colSB.toString();
    }

    /** 内存池四项指标，字节数附带 human-readable 后缀 */
    private String renderMemoryUsage(Map<String, Object> valueMap) {
        final StringBuilder colSB = new StringBuilder();
        String[] keys = new String[]{"init", "used", "committed", "max"};
        for (String key : keys) {
            Object value = valueMap.get(key);
            String valueStr = value != null ? formatMemoryByte((Long) value) : "";
            colSB.append(key).append(" : ").append(valueStr).append("\n");
        }
        return colSB.toString();
    }

    private String formatMemoryByte(long bytes) {
        return String.format("%s(%s)", bytes, StringUtils.humanReadableByteCount(bytes));
    }
}
