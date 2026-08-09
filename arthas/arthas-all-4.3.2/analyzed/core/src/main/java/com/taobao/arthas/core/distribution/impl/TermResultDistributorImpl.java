package com.taobao.arthas.core.distribution.impl;

import com.taobao.arthas.core.command.model.ResultModel;
import com.taobao.arthas.core.command.view.ResultView;
import com.taobao.arthas.core.command.view.ResultViewResolver;
import com.taobao.arthas.core.distribution.ResultDistributor;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * 终端（Term/Tty）结果分发器：将命令结果通过 {@link ResultView} 渲染后
 * 写入 {@link CommandProcess} 的输出流，供本地 Telnet/Console 用户查看。
 * <p>
 * 渲染过程加锁，保证多线程 append 时终端输出顺序正确。
 *
 * @author gongdewei 2020-03-26
 */
public class TermResultDistributorImpl implements ResultDistributor {

    private final CommandProcess commandProcess;
    private final ResultViewResolver resultViewResolver;

    /** 终端输出互斥锁，避免并发 draw 导致乱序 */
    private final Object outputLock = new Object();

    public TermResultDistributorImpl(CommandProcess commandProcess, ResultViewResolver resultViewResolver) {
        this.commandProcess = commandProcess;
        this.resultViewResolver = resultViewResolver;
    }

    @Override
    public void appendResult(ResultModel model) {
        // 按模型类型解析对应 View 并渲染到终端
        ResultView resultView = resultViewResolver.getResultView(model);
        if (resultView != null) {
            synchronized (outputLock) {
                resultView.draw(commandProcess, model);
            }
        }
    }

    @Override
    public void close() {
        // 终端输出无需释放额外资源
    }

}
