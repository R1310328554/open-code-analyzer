package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.EnhancerModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * 字节码增强类命令（watch/trace/monitor 等）的终端渲染视图：展示 {@link EnhancerModel#getEffect()} 影响摘要。
 * <p>
 * 增强成功与否由后续命令输出判断，此处仅渲染 Affect 统计（类数、方法数等）。
 *
 * @author gongdewei 2020/7/21
 */
public class EnhancerView extends ResultView<EnhancerModel> {
    @Override
    public void draw(CommandProcess process, EnhancerModel result) {
        // 增强结果状态在此忽略，实际成败由后续 watch/trace 输出判断
        if (result.getEffect() != null) {
            process.write(ViewRenderUtil.renderEnhancerAffect(result.getEffect()));
        }
    }
}
