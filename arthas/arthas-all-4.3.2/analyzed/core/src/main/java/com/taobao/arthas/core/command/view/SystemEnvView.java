package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.SystemEnvModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code sysenv} 命令的终端渲染视图：以键值表展示 JVM 进程环境变量。
 * <p>
 * 委托 {@link ViewRenderUtil#renderKeyValueTable} 按终端宽度自适应列宽。
 *
 * @author gongdewei 2020/4/2
 */
public class SystemEnvView extends ResultView<SystemEnvModel> {

    @Override
    public void draw(CommandProcess process, SystemEnvModel result) {
        process.write(ViewRenderUtil.renderKeyValueTable(result.getEnv(), process.width()));
    }

}
