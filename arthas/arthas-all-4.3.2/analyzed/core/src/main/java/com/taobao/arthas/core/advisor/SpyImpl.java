package com.taobao.arthas.core.advisor;

import java.arthas.SpyAPI.AbstractSpy;
import java.util.List;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.shell.system.ExecStatus;
import com.taobao.arthas.core.shell.system.Process;
import com.taobao.arthas.core.shell.system.ProcessAware;
import com.taobao.arthas.core.util.StringUtils;

/**
 * {@link java.arthas.SpyAPI.AbstractSpy} 实现：运行时桥接字节码插桩与 {@link AdviceListenerManager}。
 * <p>
 * 各 atXxx 方法解析 methodInfo/invokeInfo，查询监听器并分发回调。
 * <pre>
 * 怎么从 className|methodDesc 到 id 对应起来？？
 * 当id少时，可以id自己来判断是否符合？
 * 
 * 如果是每个 className|methodDesc 为 key ，是否
 * </pre>
 * 
 * @author hengyunabc 2020-04-24
 *
 */
public class SpyImpl extends AbstractSpy {
    private static final Logger logger = LoggerFactory.getLogger(SpyImpl.class);

    /** 方法进入：查询并回调 before 监听器 */
    @Override
    public void atEnter(Class<?> clazz, String methodInfo, Object target, Object[] args) {
        ClassLoader classLoader = clazz.getClassLoader();

        String[] info = StringUtils.splitMethodInfo(methodInfo);
        String methodName = info[0];
        String methodDesc = info[1];
        // TODO 可将 listener 列表缓存到 ThreadLocal，避免每次 Spy 调用都查表
        List<AdviceListener> listeners = AdviceListenerManager.queryAdviceListeners(classLoader, clazz.getName(),
                methodName, methodDesc);
        if (listeners != null) {
            for (AdviceListener adviceListener : listeners) {
                try {
                    if (skipAdviceListener(adviceListener)) {
                        continue;
                    }
                    adviceListener.before(clazz, methodName, methodDesc, target, args);
                } catch (Throwable e) {
                    logger.error("class: {}, methodInfo: {}", clazz.getName(), methodInfo, e);
                }
            }
        }

    }

    /** 正常返回：回调 afterReturning 监听器 */
    @Override
    public void atExit(Class<?> clazz, String methodInfo, Object target, Object[] args, Object returnObject) {
        ClassLoader classLoader = clazz.getClassLoader();

        String[] info = StringUtils.splitMethodInfo(methodInfo);
        String methodName = info[0];
        String methodDesc = info[1];

        List<AdviceListener> listeners = AdviceListenerManager.queryAdviceListeners(classLoader, clazz.getName(),
                methodName, methodDesc);
        if (listeners != null) {
            for (AdviceListener adviceListener : listeners) {
                try {
                    if (skipAdviceListener(adviceListener)) {
                        continue;
                    }
                    adviceListener.afterReturning(clazz, methodName, methodDesc, target, args, returnObject);
                } catch (Throwable e) {
                    logger.error("class: {}, methodInfo: {}", clazz.getName(), methodInfo, e);
                }
            }
        }
    }

    @Override
    public void atExceptionExit(Class<?> clazz, String methodInfo, Object target, Object[] args, Throwable throwable) {
        ClassLoader classLoader = clazz.getClassLoader();

        String[] info = StringUtils.splitMethodInfo(methodInfo);
        String methodName = info[0];
        String methodDesc = info[1];

        List<AdviceListener> listeners = AdviceListenerManager.queryAdviceListeners(classLoader, clazz.getName(),
                methodName, methodDesc);
        if (listeners != null) {
            for (AdviceListener adviceListener : listeners) {
                try {
                    if (skipAdviceListener(adviceListener)) {
                        continue;
                    }
                    adviceListener.afterThrowing(clazz, methodName, methodDesc, target, args, throwable);
                } catch (Throwable e) {
                    logger.error("class: {}, methodInfo: {}", clazz.getName(), methodInfo, e);
                }
            }
        }
    }

    /** 子调用开始前：分发给 {@link InvokeTraceable#invokeBeforeTracing} */
    @Override
    public void atBeforeInvoke(Class<?> clazz, String invokeInfo, Object target) {
        ClassLoader classLoader = clazz.getClassLoader();
        String[] info = StringUtils.splitInvokeInfo(invokeInfo);
        String owner = info[0];
        String methodName = info[1];
        String methodDesc = info[2];

        List<AdviceListener> listeners = AdviceListenerManager.queryTraceAdviceListeners(classLoader, clazz.getName(),
                owner, methodName, methodDesc);

        if (listeners != null) {
            for (AdviceListener adviceListener : listeners) {
                try {
                    if (skipAdviceListener(adviceListener)) {
                        continue;
                    }
                    final InvokeTraceable listener = (InvokeTraceable) adviceListener;
                    listener.invokeBeforeTracing(classLoader, owner, methodName, methodDesc, Integer.parseInt(info[3]));
                } catch (Throwable e) {
                    logger.error("class: {}, invokeInfo: {}", clazz.getName(), invokeInfo, e);
                }
            }
        }
    }

    @Override
    public void atAfterInvoke(Class<?> clazz, String invokeInfo, Object target) {
        ClassLoader classLoader = clazz.getClassLoader();
        String[] info = StringUtils.splitInvokeInfo(invokeInfo);
        String owner = info[0];
        String methodName = info[1];
        String methodDesc = info[2];
        List<AdviceListener> listeners = AdviceListenerManager.queryTraceAdviceListeners(classLoader, clazz.getName(),
                owner, methodName, methodDesc);

        if (listeners != null) {
            for (AdviceListener adviceListener : listeners) {
                try {
                    if (skipAdviceListener(adviceListener)) {
                        continue;
                    }
                    final InvokeTraceable listener = (InvokeTraceable) adviceListener;
                    listener.invokeAfterTracing(classLoader, owner, methodName, methodDesc, Integer.parseInt(info[3]));
                } catch (Throwable e) {
                    logger.error("class: {}, invokeInfo: {}", clazz.getName(), invokeInfo, e);
                }
            }
        }

    }

    @Override
    public void atInvokeException(Class<?> clazz, String invokeInfo, Object target, Throwable throwable) {
        ClassLoader classLoader = clazz.getClassLoader();
        String[] info = StringUtils.splitInvokeInfo(invokeInfo);
        String owner = info[0];
        String methodName = info[1];
        String methodDesc = info[2];

        List<AdviceListener> listeners = AdviceListenerManager.queryTraceAdviceListeners(classLoader, clazz.getName(),
                owner, methodName, methodDesc);

        if (listeners != null) {
            for (AdviceListener adviceListener : listeners) {
                try {
                    if (skipAdviceListener(adviceListener)) {
                        continue;
                    }
                    final InvokeTraceable listener = (InvokeTraceable) adviceListener;
                    listener.invokeThrowTracing(classLoader, owner, methodName, methodDesc, Integer.parseInt(info[3]));
                } catch (Throwable e) {
                    logger.error("class: {}, invokeInfo: {}", clazz.getName(), invokeInfo, e);
                }
            }
        }
    }

    /** 命中源码行：回调 atLine 并传递局部变量 */
    @Override
    public void atLine(Class<?> clazz, String methodInfo, int lineNumber, Object target, Object[] args,
            String[] argNames, Object[] localVars, String[] localVarNames) {
        ClassLoader classLoader = clazz.getClassLoader();

        String[] info = StringUtils.splitMethodInfo(methodInfo);
        String methodName = info[0];
        String methodDesc = info[1];

        List<AdviceListener> listeners = AdviceListenerManager.queryLineAdviceListeners(classLoader, clazz.getName(),
                methodName, methodDesc, lineNumber);
        if (listeners != null) {
            for (AdviceListener adviceListener : listeners) {
                try {
                    if (skipAdviceListener(adviceListener)) {
                        continue;
                    }
                    adviceListener.atLine(clazz, methodName, methodDesc, target, args, lineNumber, argNames,
                            localVars, localVarNames);
                } catch (Throwable e) {
                    logger.error("class: {}, methodInfo: {}, lineNumber: {}", clazz.getName(), methodInfo, lineNumber, e);
                }
            }
        }
    }

    /** 关联 shell 进程已终止或停止时跳过该监听器 */
    private static boolean skipAdviceListener(AdviceListener adviceListener) {
        if (adviceListener instanceof ProcessAware) {
            ProcessAware processAware = (ProcessAware) adviceListener;
            Process process = processAware.getProcess();
            if (process == null) {
                return true;
            }
            ExecStatus status = process.status();
            if (status.equals(ExecStatus.TERMINATED) || status.equals(ExecStatus.STOPPED)) {
                return true;
            }
        }
        return false;
    }

}
