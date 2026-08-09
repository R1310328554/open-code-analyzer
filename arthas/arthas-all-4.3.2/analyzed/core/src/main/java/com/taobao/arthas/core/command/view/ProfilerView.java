package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.ProfilerModel;
import com.taobao.arthas.core.command.monitor200.ProfilerCommand.ProfilerAction;
import com.taobao.arthas.core.shell.command.CommandProcess;


/**
 * {@code profiler} 命令的终端渲染视图：展示 async-profiler 启动/停止等操作的执行结果。
 * <p>
 * 若模型携带 {@code supportedActions} 则仅列出可用子命令；否则按 action 类型追加
 * 定时停止提示或输出文件路径（Markdown 格式下省略文件路径行，避免干扰复制）。
 *
 * @author gongdewei 2020/4/27
 */
public class ProfilerView extends ResultView<ProfilerModel> {
    @Override
    public void draw(CommandProcess process, ProfilerModel model) {
        // 帮助/探测模式：只输出支持的 action 列表
        if (model.getSupportedActions() != null) {
            process.write("Supported Actions: " + model.getSupportedActions()).write("\n");
            return;
        }

        drawExecuteResult(process, model);

        if (ProfilerAction.start.name().equals(model.getAction())) {
            // start 且设置了 duration：提示自动停止时间与 flame graph 输出路径
            if (model.getDuration() != null) {
                process.write(String.format("profiler will silent stop after %d seconds.\n", model.getDuration().longValue()));
                process.write("profiler output file will be: " + model.getOutputFile() + "\n");
            }
        } else if (ProfilerAction.stop.name().equals(model.getAction())) {
            // markdown 输出时，额外的提示行会影响复制粘贴给 LLM 的效果
            if (model.getOutputFile() != null && !isMarkdown(model.getFormat())) {
                process.write("profiler output file: " + model.getOutputFile() + "\n");
            }
        }

    }

    /** 写出 profiler 子进程/原生库返回的执行文本，末尾保证换行 */
    private void drawExecuteResult(CommandProcess process, ProfilerModel model) {
        if (model.getExecuteResult() != null) {
            process.write(model.getExecuteResult());
            if (!model.getExecuteResult().endsWith("\n")) {
                process.write("\n");
            }
        }
    }

    /** 判断输出格式是否为 Markdown（md 前缀，忽略大小写） */
    private boolean isMarkdown(String format) {
        return format != null && format.toLowerCase().startsWith("md");
    }
}
