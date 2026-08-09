package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.StackModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.DateUtils;
import com.taobao.arthas.core.util.ThreadUtil;

/**
 * {@code stack} 命令的终端渲染视图：输出指定线程在某时刻的调用栈。
 * <p>
 * 首行由 {@link ThreadUtil#getThreadTitle} 生成线程标题；{@code @} 标记观测点
 * （栈顶帧）；其余帧缩进 {@code at} 格式，前缀带采样时间戳。
 *
 * @author gongdewei 2020/4/13
 */
public class StackView extends ResultView<StackModel> {

    @Override
    public void draw(CommandProcess process, StackModel result) {
        StringBuilder sb = new StringBuilder();
        sb.append(ThreadUtil.getThreadTitle(result)).append("\n");

        StackTraceElement[] stackTraceElements = result.getStackTrace();
        // 栈顶帧作为 @ 观测位置（与 watch/trace 输出风格一致）
        StackTraceElement locationStackTraceElement = stackTraceElements[0];
        String locationString = String.format("    @%s.%s()", locationStackTraceElement.getClassName(),
                locationStackTraceElement.getMethodName());
        sb.append(locationString).append("\n");

        // 跳过 index 0，从下一帧起输出完整 at 行（含源文件与行号）
        int skip = 1;
        for (int index = skip; index < stackTraceElements.length; index++) {
            StackTraceElement ste = stackTraceElements[index];
            sb.append("        at ")
                    .append(ste.getClassName())
                    .append(".")
                    .append(ste.getMethodName())
                    .append("(")
                    .append(ste.getFileName())
                    .append(":")
                    .append(ste.getLineNumber())
                    .append(")\n");
        }
        process.write("ts=" + DateUtils.formatDateTime(result.getTs()) + ";" + sb.toString() + "\n");
    }

}
