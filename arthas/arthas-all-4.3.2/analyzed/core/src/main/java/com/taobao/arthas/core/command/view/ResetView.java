package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.ResetModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code reset} 命令的终端渲染视图：展示清除增强/监听器后的影响统计。
 * <p>
 * 通过 {@link ViewRenderUtil#renderEnhancerAffect} 格式化 Affect 信息
 *（受影响类数、方法数等）。
 *
 * @author gongdewei 2020/6/22
 */
public class ResetView extends ResultView<ResetModel> {

    @Override
    public void draw(CommandProcess process, ResetModel result) {
        process.write(ViewRenderUtil.renderEnhancerAffect(result.getAffect()));
    }

}
