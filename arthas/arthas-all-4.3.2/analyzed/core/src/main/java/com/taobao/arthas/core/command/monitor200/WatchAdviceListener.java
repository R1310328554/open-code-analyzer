package com.taobao.arthas.core.command.monitor200;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.advisor.AccessPoint;
import com.taobao.arthas.core.advisor.Advice;
import com.taobao.arthas.core.advisor.AdviceListenerAdapter;
import com.taobao.arthas.core.advisor.ArthasMethod;
import com.taobao.arthas.core.command.model.ObjectVO;
import com.taobao.arthas.core.command.model.WatchModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.LogUtil;
import com.taobao.arthas.core.util.ThreadLocalWatch;

import java.time.LocalDateTime;

/**
 * {@code watch} 命令的运行时 Advice 监听器：在 before/return/throw 切点观测方法并输出 {@link WatchModel}。
 * <p>
 * 通过 {@link ThreadLocalWatch} 统计耗时；OGNL 条件与表达式求值失败时结束命令并提示查看日志。
 *
 * @author beiwei30 on 29/11/2016.
 */
class WatchAdviceListener extends AdviceListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(WatchAdviceListener.class);
    /** 线程局部计时器，统计单次方法调用耗时 */
    private final ThreadLocalWatch threadLocalWatch = new ThreadLocalWatch();
    /** 关联的 watch 命令配置（切点、表达式、次数上限等） */
    private WatchCommand command;
    /** 命令输出通道 */
    private CommandProcess process;

    public WatchAdviceListener(WatchCommand command, CommandProcess process, boolean verbose) {
        this.command = command;
        this.process = process;
        super.setVerbose(verbose);
    }

    /** 判断是否需要在方法退出时（finish 切点）触发观测 */
    private boolean isFinish() {
        return command.isFinish() || !command.isBefore() && !command.isException() && !command.isSuccess();
    }

    @Override
    public void before(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args)
            throws Throwable {
        // 开始计算本次方法调用耗时
        threadLocalWatch.start();
        if (command.isBefore()) {
            watching(Advice.newForBefore(loader, clazz, method, target, args));
        }
    }

    @Override
    public void afterReturning(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args,
                               Object returnObject) throws Throwable {
        Advice advice = Advice.newForAfterReturning(loader, clazz, method, target, args, returnObject);
        if (command.isSuccess()) {
            watching(advice);
        }

        finishing(advice);
    }

    @Override
    public void afterThrowing(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args,
                              Throwable throwable) {
        Advice advice = Advice.newForAfterThrowing(loader, clazz, method, target, args, throwable);
        if (command.isException()) {
            watching(advice);
        }

        finishing(advice);
    }

    private void finishing(Advice advice) {
        if (isFinish()) {
            watching(advice);
        }
    }


    /** 评估条件表达式，满足则求值 express 并 append WatchModel；达上限则 abortProcess */
    private void watching(Advice advice) {
        try {
            // 本次调用的耗时
            double cost = threadLocalWatch.costInMillis();
            boolean conditionResult = isConditionMet(command.getConditionExpress(), advice, cost);
            if (this.isVerbose()) {
                process.write("Condition express: " + command.getConditionExpress() + " , result: " + conditionResult + "\n");
            }
            if (conditionResult) {
                // TODO: concurrency issues for process.write

                Object value = getExpressionResult(command.getExpress(), advice, cost);

                WatchModel model = new WatchModel();
                model.setTs(LocalDateTime.now());
                model.setCost(cost);
                model.setValue(new ObjectVO(value, command.getExpand()));
                model.setSizeLimit(command.getSizeLimit());
                model.setClassName(advice.getClazz().getName());
                model.setMethodName(advice.getMethod().getName());
                if (advice.isBefore()) {
                    model.setAccessPoint(AccessPoint.ACCESS_BEFORE.getKey());
                } else if (advice.isAfterReturning()) {
                    model.setAccessPoint(AccessPoint.ACCESS_AFTER_RETUNING.getKey());
                } else if (advice.isAfterThrowing()) {
                    model.setAccessPoint(AccessPoint.ACCESS_AFTER_THROWING.getKey());
                }

                process.appendResult(model);
                process.times().incrementAndGet();
                if (isLimitExceeded(command.getNumberOfLimit(), process.times().get())) {
                    abortProcess(process, command.getNumberOfLimit());
                }
            }
        } catch (Throwable e) {
            logger.warn("watch failed.", e);
            process.end(-1, "watch failed, condition is: " + command.getConditionExpress() + ", express is: "
                    + command.getExpress() + ", " + e.getMessage() + ", visit " + LogUtil.loggingFile()
                    + " for more details.");
        }
    }
}
