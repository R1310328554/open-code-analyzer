package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.SystemPropertyModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code sysprop} 命令的终端渲染视图。
 * <p>
 * 将 {@link SystemPropertyModel#getProps()} 中的 JVM 系统属性渲染为 KEY/VALUE 两列表格，
 * 列宽受 {@link CommandProcess#width()} 约束。
 *
 * @author gongdewei 2020/4/2
 */
public class SystemPropertyView extends ResultView<SystemPropertyModel> {

    @Override
    @Override
    public void draw(CommandProcess process, SystemPropertyModel result) {
        // 复用通用 KV 表格渲染，无需额外格式化
        process.write(ViewRenderUtil.renderKeyValueTable(result.getProps(), process.width()));
    }

}
