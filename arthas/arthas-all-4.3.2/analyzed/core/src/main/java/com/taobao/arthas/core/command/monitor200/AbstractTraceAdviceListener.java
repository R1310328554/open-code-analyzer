package com.taobao.arthas.core.command.monitor200;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.advisor.Advice;
import com.taobao.arthas.core.advisor.AdviceListenerAdapter;
import com.taobao.arthas.core.advisor.ArthasMethod;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.LogUtil;
import com.taobao.arthas.core.util.ThreadLocalWatch;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@code trace} 系列命令的 Advice 监听器基类：维护调用树与耗时，满足条件时输出 {@link TraceModel}。
 * <p>
 * 每个线程持有独立的 {@link TraceEntity} 与 {@link ThreadLocalWatch}，在根调用返回时
 * 评估 OGNL 条件表达式并递增输出计数；{@link #abortProcess} 用 CAS 保证多线程仅终止一次。
 *
 * @author ralf0131 2017-01-06 16:02.
 */
public class AbstractTraceAdviceListener extends AdviceListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTraceAdviceListener.class);
    /** 线程局部计时器，统计单次根 trace 总耗时 */
    protected final ThreadLocalWatch threadLocalWatch = new ThreadLocalWatch();
    /** 关联的 trace 命令配置（条件、次数上限等） */
    protected TraceCommand command;
    /** 命令输出通道 */
    protected CommandProcess process;
    /** 防止并发多次调用 abortProcess 终止会话 */
    private final AtomicBoolean processAborted = new AtomicBoolean(false);

    /** 当前线程的 trace 调用树与嵌套深度 */
    protected final ThreadLocal<TraceEntity> threadBoundEntity = new ThreadLocal<TraceEntity>();

    /** 绑定 trace 命令与输出进程 */
    public AbstractTraceAdviceListener(TraceCommand command, CommandProcess process) {
        this.command = command;
        this.process = process;
    }

    /** 懒初始化当前线程的 TraceEntity，首次调用时创建并绑定 ClassLoader */
    protected TraceEntity threadLocalTraceEntity(ClassLoader loader) {
        TraceEntity traceEntity = threadBoundEntity.get();
        if (traceEntity == null) {
            traceEntity = new TraceEntity(loader);
            threadBoundEntity.set(traceEntity);
        }
        return traceEntity;
    }

    /** 增强卸载时清理 ThreadLocal，避免 ClassLoader 泄漏 */
    @Override
    public void destroy() {
        threadBoundEntity.remove();
    }

    @Override
    public void before(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args)
            throws Throwable {
        TraceEntity traceEntity = threadLocalTraceEntity(loader);
        traceEntity.tree.begin(clazz.getName(), method.getName(), -1, false);
        traceEntity.deep++;
        // 开始计算本次方法调用耗时
        threadLocalWatch.start();
    }

    @Override
    public void afterReturning(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args,
                               Object returnObject) throws Throwable {
        threadLocalTraceEntity(loader).tree.end();
        final Advice advice = Advice.newForAfterReturning(loader, clazz, method, target, args, returnObject);
        finishing(loader, advice);
    }

    @Override
    public void afterThrowing(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args,
                              Throwable throwable) throws Throwable {
        int lineNumber = -1;
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace.length != 0) {
            lineNumber = stackTrace[0].getLineNumber();
        }

        threadLocalTraceEntity(loader).tree.end(throwable, lineNumber);
        final Advice advice = Advice.newForAfterThrowing(loader, clazz, method, target, args, throwable);
        finishing(loader, advice);
    }

    public TraceCommand getCommand() {
        return command;
    }

    /** 方法退出回调：递减嵌套深度，根调用完成时评估条件并可能输出 trace 树 */
    private void finishing(ClassLoader loader, Advice advice) {
        // 本次调用的耗时
        TraceEntity traceEntity = threadLocalTraceEntity(loader);
        if (traceEntity.deep >= 1) { // #1817 防止deep为负数
            traceEntity.deep--;
        }
        if (traceEntity.deep == 0) {
            double cost = threadLocalWatch.costInMillis();
            try {
                boolean conditionResult = isConditionMet(command.getConditionExpress(), advice, cost);
                if (this.isVerbose()) {
                    process.write("Condition express: " + command.getConditionExpress() + " , result: " + conditionResult + "\n");
                }
                if (conditionResult) {
                    // 满足输出条件
                    process.times().incrementAndGet();
                    // TODO: concurrency issues for process.write
                    process.appendResult(traceEntity.getModel());

                    // 是否到达数量限制
                    if (isLimitExceeded(command.getNumberOfLimit(), process.times().get())) {
                        abortProcess(process, command.getNumberOfLimit());
                    }
                }
            } catch (Throwable e) {
                logger.warn("trace failed.", e);
                process.end(1, "trace failed, condition is: " + command.getConditionExpress() + ", " + e.getMessage()
                              + ", visit " + LogUtil.loggingFile() + " for more details.");
            } finally {
                threadBoundEntity.remove();
            }
        }
    }

    /** 达到输出次数上限时终止命令；CAS 保证仅首个线程执行 super.abortProcess */
    @Override
    protected void abortProcess(CommandProcess process, int limit) {
        // Only proceed if this thread is the first one to set the flag to true
        if (processAborted.compareAndSet(false, true)) {
            super.abortProcess(process, limit);
        }
    }
}
