package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.ClassLoaderMetaspaceModel;
import com.taobao.arthas.core.command.model.ClassLoaderMetaspaceModel.Row;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.text.Decoration;
import com.taobao.text.ui.Overflow;
import com.taobao.text.ui.RowElement;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.util.List;

/**
 * {@code classloader-metaspace} 命令的终端渲染视图：以表格展示各 ClassLoader 的 Metaspace 占用。
 * <p>
 * {@code -v} verbose 模式额外输出 ClassLoaderData 指针、hiddenBlockSize 与类型；
 * 无数据时提示未找到统计信息。
 *
 * @author Codex 2026-05-08
 */
public class ClassLoaderMetaspaceView extends ResultView<ClassLoaderMetaspaceModel> {

    /** 按 verbose 标志选择默认或详细列布局渲染表格 */
    @Override
    public void draw(CommandProcess process, ClassLoaderMetaspaceModel result) {
        List<Row> rows = result.getRows();
        if (rows == null || rows.isEmpty()) {
            writeln(process, "No classloader metaspace statistics found.");
            return;
        }

        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1).overflow(Overflow.HIDDEN);
        if (result.isVerbose()) {
            drawVerboseTable(process, table, rows);
        } else {
            drawDefaultTable(process, table, rows);
        }
    }

    /** 默认列：hash、classes、chunkSize、blockSize、name */
    private void drawDefaultTable(CommandProcess process, TableElement table, List<Row> rows) {
        table.add(new RowElement().style(Decoration.bold.bold())
                .add("hash", "classes", "chunkSize", "blockSize", "name"));
        for (Row row : rows) {
            table.row(
                    value(row.getHash()),
                    String.valueOf(row.getClassCount()),
                    String.valueOf(row.getChunkSize()),
                    String.valueOf(row.getBlockSize()),
                    value(row.getName()));
        }
        process.write(RenderUtil.render(table, process.width()));
    }

    /** verbose 列：额外展示 classLoaderData 地址、hiddenBlockSize 与 type */
    private void drawVerboseTable(CommandProcess process, TableElement table, List<Row> rows) {
        table.add(new RowElement().style(Decoration.bold.bold())
                .add("hash", "classLoaderData", "classes", "chunkSize", "blockSize", "hiddenBlockSize", "type",
                        "name"));
        for (Row row : rows) {
            table.row(
                    value(row.getHash()),
                    String.format("0x%016x", row.getClassLoaderData()),
                    String.valueOf(row.getClassCount()),
                    String.valueOf(row.getChunkSize()),
                    String.valueOf(row.getBlockSize()),
                    String.valueOf(row.getHiddenBlockSize()),
                    value(row.getType()),
                    value(row.getName()));
        }
        process.write(RenderUtil.render(table, process.width()));
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }
}
