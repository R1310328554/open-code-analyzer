package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.LineModel;
import com.taobao.arthas.core.command.model.ObjectVO;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.DateUtils;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.view.ObjectView;

/**
 * {@code line} 行号探针命中时的终端渲染视图。
 * <p>
 * 每次命中输出时间戳、线程、耗时、类方法行号、局部变量/表达式结果；
 * 复杂对象经 {@link ObjectView} 按 {@link LineModel#getSizeLimit()} 深度展开。
 */
public class LineView extends ResultView<LineModel> {

    @Override
    public void draw(CommandProcess process, LineModel model) {
        // 大对象按 sizeLimit 递归展开，否则直接 toString
        ObjectVO objectVO = model.getValue();
        int sizeLimit = ObjectView.normalizeMaxObjectLength(model.getSizeLimit());
        String result = StringUtils.objectToString(
                objectVO.needExpand() ? new ObjectView(sizeLimit, objectVO).draw() : objectVO.getObject());

        StringBuilder sb = new StringBuilder();
        sb.append("ts=").append(DateUtils.formatDateTime(model.getTs()))
                .append("; [thread=").append(model.getThreadName())
                .append("(").append(model.getThreadId()).append(")")
                .append(" cost=").append(model.getCost()).append("ms] ")
                .append(model.getClassName()).append(".").append(model.getMethodName())
                .append(model.getMethodDesc()).append(":").append(model.getLineNumber()).append("\n");
        sb.append("result=").append(result).append("\n");
        StackTraceElement[] stackTrace = model.getStackTrace();
        // --stack 模式下附加调用栈
        if (stackTrace != null && stackTrace.length > 0) {
            sb.append("stack=\n");
            for (StackTraceElement stackTraceElement : stackTrace) {
                sb.append("    at ").append(stackTraceElement.getClassName()).append(".")
                        .append(stackTraceElement.getMethodName()).append("(")
                        .append(stackTraceElement.getFileName()).append(":")
                        .append(stackTraceElement.getLineNumber()).append(")\n");
            }
        }

        process.write(sb.toString());
    }
}
