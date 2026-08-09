package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.ObjectVO;
import com.taobao.arthas.core.command.model.WatchModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.DateUtils;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.view.ObjectView;

/**
 * {@code watch} 命令的终端渲染视图。
 * <p>
 * 每次方法触发时输出 method/location、时间戳、耗时与表达式求值结果；
 * 对象长度受 {@link ObjectView#normalizeMaxObjectLength(int)} 限制。
 *
 * @author gongdewei 2020/3/27
 */
public class WatchView extends ResultView<WatchModel> {

    @Override
    public void draw(CommandProcess process, WatchModel model) {
        // 根据 sizeLimit 决定对象是直接 toString 还是树形展开
        ObjectVO objectVO = model.getValue();
        int sizeLimit = ObjectView.normalizeMaxObjectLength(model.getSizeLimit());
        String result = StringUtils.objectToString(
                objectVO.needExpand() ? new ObjectView(sizeLimit, objectVO).draw() : objectVO.getObject());

        // 首行：类名.方法名 + 观测点（before/after/exception）
        StringBuilder sb = new StringBuilder();
        sb.append("method=").append(model.getClassName()).append(".").append(model.getMethodName())
                .append(" location=").append(model.getAccessPoint()).append("\n");
        sb.append("ts=").append(DateUtils.formatDateTime(model.getTs()))
                .append("; [cost=").append(model.getCost()).append("ms] result=").append(result).append("\n");

        process.write(sb.toString());
    }
}
