package com.taobao.arthas.core.command.monitor200;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.advisor.Advice;
import com.taobao.arthas.core.advisor.AdviceListenerAdapter;
import com.taobao.arthas.core.advisor.ArthasMethod;
import com.taobao.arthas.core.command.model.StackModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.LogUtil;
import com.taobao.arthas.core.util.ThreadLocalWatch;
import com.taobao.arthas.core.util.ThreadUtil;

import java.time.LocalDateTime;

/**
 * {@code stack} 命令 Advice 监听器：在匹配方法调用完成时，若条件满足则 dump 当前线程栈。
 * 每次命中递增 {@link CommandProcess#times()}，达到 {@link StackCommand#getNumberOfLimit()} 后终止增强。
 *
 * @author beiwei30 on 29/11/2016.
 */
public class StackAdviceListener extends AdviceListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(StackAdviceListener.class);

    private final ThreadLocalWatch threadLocalWatch = new ThreadLocalWatch();
    private StackCommand command;
    private CommandProcess process;

    public StackAdviceListener(StackCommand command, CommandProcess process, boolean verbose) {
        this.command = command;
        this.process = process;
        super.setVerbose(verbose);
    }

    @Override
    public void before(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args)
            throws Throwable {
        // 开始计算本次方法调用耗时
        threadLocalWatch.start();
    }

    @Override
    public void afterThrowing(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args,
                              Throwable throwable) throws Throwable {
        Advice advice = Advice.newForAfterThrowing(loader, clazz, method, target, args, throwable);
        finishing(advice);
    }

    @Override
    public void afterReturning(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args,
                               Object returnObject) throws Throwable {
        Advice advice = Advice.newForAfterReturning(loader, clazz, method, target, args, returnObject);
        finishing(advice);
    }

    /** 求值 condition-express，通过则采集 {@link StackModel} 并检查次数上限 */
    private void finishing(Advice advice) {
        // 本次调用的耗时
        try {
            double cost = threadLocalWatch.costInMillis();
            boolean conditionResult = isConditionMet(command.getConditionExpress(), advice, cost);
            if (this.isVerbose()) {
                process.write("Condition express: " + command.getConditionExpress() + " , result: " + conditionResult + "\n");
            }
            if (conditionResult) {
                // 条件满足：输出调用点上下文下的线程栈（多线程并发 write 仍有竞态，见 TODO）
                StackModel stackModel = ThreadUtil.getThreadStackModel(advice.getLoader(), Thread.currentThread());
                stackModel.setTs(LocalDateTime.now());
                process.appendResult(stackModel);
                process.times().incrementAndGet();
                if (isLimitExceeded(command.getNumberOfLimit(), process.times().get())) {
                    abortProcess(process, command.getNumberOfLimit());
                }
            }
        } catch (Throwable e) {
            logger.warn("stack failed.", e);
            process.end(-1, "stack failed, condition is: " + command.getConditionExpress() + ", " + e.getMessage()
                          + ", visit " + LogUtil.loggingFile() + " for more details.");
        }
    }
}
