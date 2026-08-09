package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.advisor.InvokeTraceable;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code trace} 命令的运行时监听器：实现 {@link InvokeTraceable}，在方法体内每个子调用前后维护调用树。
 * <p>
 * 字节码增强在每个 INVOKE 指令处插桩，{@link #invokeBeforeTracing} 入栈节点、
 * {@link #invokeAfterTracing} / {@link #invokeThrowTracing} 出栈；实际输出逻辑在
 * 父类 {@link AbstractTraceAdviceListener} 的根方法返回时触发。
 *
 * @author beiwei30 on 29/11/2016.
 */
public class TraceAdviceListener extends AbstractTraceAdviceListener implements InvokeTraceable {

    /** 绑定 trace 命令、输出通道及 verbose 模式 */
    public TraceAdviceListener(TraceCommand command, CommandProcess process, boolean verbose) {
        super(command, process);
        super.setVerbose(verbose);
    }

    /**
     * trace 会在被观测的方法体中，在每个方法调用前后插入字节码，所以方法调用开始，结束，抛异常的时候，都会回调下面的接口
     */
    @Override
    public void invokeBeforeTracing(ClassLoader classLoader, String tracingClassName, String tracingMethodName, String tracingMethodDesc, int tracingLineNumber)
            throws Throwable {
        // 类名规范化在 TraceTree 输出阶段完成；此处先记录子调用入栈
        threadLocalTraceEntity(classLoader).tree.begin(tracingClassName, tracingMethodName, tracingLineNumber, true);
    }

    /** 子调用正常返回：弹出调用树节点 */
    @Override
    public void invokeAfterTracing(ClassLoader classLoader, String tracingClassName, String tracingMethodName, String tracingMethodDesc, int tracingLineNumber)
            throws Throwable {
        threadLocalTraceEntity(classLoader).tree.end();
    }

    /** 子调用抛异常：标记节点异常并出栈 */
    @Override
    public void invokeThrowTracing(ClassLoader classLoader, String tracingClassName, String tracingMethodName, String tracingMethodDesc, int tracingLineNumber)
            throws Throwable {
        threadLocalTraceEntity(classLoader).tree.end(true);
    }

}
