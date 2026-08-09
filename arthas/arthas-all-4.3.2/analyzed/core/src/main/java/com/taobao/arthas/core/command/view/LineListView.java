package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.LineListModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code line -l} 列表模式的终端渲染视图。
 * <p>
 * 输出已注册行号探针的类、源文件、方法签名与行号集合，便于确认当前 line 增强范围。
 */
public class LineListView extends ResultView<LineListModel> {

    @Override
    @Override
    public void draw(CommandProcess process, LineListModel model) {
        StringBuilder sb = new StringBuilder();
        sb.append("class=").append(model.getClassName());
        if (model.getSourceFile() != null) {
            sb.append(" source=").append(model.getSourceFile());
        }
        sb.append("\n");
        sb.append("method=").append(model.getMethodName()).append(model.getMethodDesc())
                .append(" lines=").append(model.getLines()).append("\n");
        process.write(sb.toString());
    }
}
