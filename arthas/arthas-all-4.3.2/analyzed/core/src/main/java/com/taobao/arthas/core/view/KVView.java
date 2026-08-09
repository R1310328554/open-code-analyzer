package com.taobao.arthas.core.view;

import com.taobao.arthas.core.util.StringUtils;

import java.util.Scanner;

/**
 * 键值对排版控件：三列表格（键 | 分隔符 | 值），输出前去除行尾多余空格。
 * Created by vlinux on 15/5/9.
 */
public class KVView implements View {

    /** 底层三列表格：键列、冒号列、值列 */
    private final TableView tableView;

    /** 使用默认列对齐（键右对齐、值左对齐）构造 */
    public KVView() {
        this.tableView = new TableView(new TableView.ColumnDefine[]{
                new TableView.ColumnDefine(TableView.Align.RIGHT),
                new TableView.ColumnDefine(TableView.Align.RIGHT),
                new TableView.ColumnDefine(TableView.Align.LEFT)
        })
                .hasBorder(false)
                .padding(0);
    }

    /**
     * @param keyColumnDefine 键列定义
     * @param valueColumnDefine 值列定义
     */
    public KVView(TableView.ColumnDefine keyColumnDefine, TableView.ColumnDefine valueColumnDefine) {
        this.tableView = new TableView(new TableView.ColumnDefine[]{
                keyColumnDefine,
                new TableView.ColumnDefine(TableView.Align.RIGHT),
                valueColumnDefine
        })
                .hasBorder(false)
                .padding(0);
    }

    /**
     * 追加一行键值对。
     *
     * @param key 键
     * @param value 值
     * @return this，支持链式调用
     */
    public KVView add(final Object key, final Object value) {
        tableView.addRow(key, " : ", value);
        return this;
    }

    @Override
    public String draw() {
        String content = tableView.draw();
        StringBuilder sb = new StringBuilder();
        // 清理多余的空格
        Scanner scanner = new Scanner(content);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line != null) {
                // 清理一行后面多余的空格
                line = StringUtils.stripEnd(line, " ");
            }
            sb.append(line).append('\n');
        }
        scanner.close();
        return sb.toString();
    }
}
