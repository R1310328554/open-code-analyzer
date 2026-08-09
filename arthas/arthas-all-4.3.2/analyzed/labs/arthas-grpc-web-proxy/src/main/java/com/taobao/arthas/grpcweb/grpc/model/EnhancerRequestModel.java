package com.taobao.arthas.grpcweb.grpc.model;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.advisor.AdviceListener;
import com.taobao.arthas.core.advisor.InvokeTraceable;
import com.taobao.arthas.core.command.model.EnhancerModel;
import com.taobao.arthas.core.command.monitor200.AbstractTraceAdviceListener;
import com.taobao.arthas.core.util.LogUtil;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.util.affect.EnhancerAffect;
import com.taobao.arthas.core.util.matcher.Matcher;
import com.taobao.arthas.core.view.Ansi;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;
import com.taobao.arthas.grpcweb.grpc.service.advisor.Enhancer;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.List;

/**
 * 增强类命令请求的抽象模型，封装类/方法匹配、监听器创建与字节码增强流程。
 * <p>
 * 子类（如 {@link WatchRequestModel}）负责解析 gRPC 请求参数并实现具体的
 * {@link AdviceListener}；本类统一调用 {@link Enhancer} 完成插桩并将结果推送到
 * {@link ArthasStreamObserver}。
 */
public abstract class EnhancerRequestModel {

    private static final Logger logger = LoggerFactory.getLogger(EnhancerRequestModel.class);
    /** 空列表常量，供子类复用 */
    protected static final List<String> EMPTY = Collections.emptyList();
    /** OGNL 表达式示例，供客户端参考 */
    public static final String[] EXPRESS_EXAMPLES = { "params", "returnObj", "throwExp", "target", "clazz", "method",
            "{params,returnObj}", "params[0]" };
    /** 排除类名匹配模式 */
    protected String excludeClassPattern;

    /** 类名匹配器（懒加载） */
    protected Matcher classNameMatcher;
    /** 排除类名匹配器（懒加载） */
    protected Matcher classNameExcludeMatcher;
    /** 方法名匹配器（懒加载） */
    protected Matcher methodNameMatcher;

    /** 关联的后台任务 ID */
    protected long jobId;
    /** 已存在的 Advice 监听器 ID，非 0 时复用已有监听器 */
    protected long listenerId;

    /** 是否输出详细增强日志 */
    protected boolean verbose;

    /** 最多匹配的类数量上限 */
    protected int maxNumOfMatchedClass;

    /**
     * 获取类名匹配器。
     *
     * @return 类名匹配器
     */
    protected abstract Matcher getClassNameMatcher();

    /**
     * 获取排除类名匹配器。
     *
     * @return 排除类名匹配器，可为 null
     */
    protected abstract Matcher getClassNameExcludeMatcher();

    /**
     * 获取方法名匹配器。
     *
     * @return 方法名匹配器
     */
    protected abstract Matcher getMethodNameMatcher();

    /**
     * 创建本次增强对应的 Advice 监听器。
     *
     * @param arthasStreamObserver 用于推送结果的 gRPC 流观察者
     * @return 监听器实例；为 null 时表示创建失败
     */
    protected abstract AdviceListener getAdviceListener(ArthasStreamObserver arthasStreamObserver);

    /**
     * 执行字节码增强：注册监听器、调用 {@link Enhancer} 插桩，并将成功或失败信息写入流。
     *
     * @param arthasStreamObserver gRPC 双向流观察者，承载增强结果与结束信号
     */
    public void enhance(ArthasStreamObserver arthasStreamObserver) {
        EnhancerAffect effect = null;
        try {
            Instrumentation inst = arthasStreamObserver.getInstrumentation();
            AdviceListener listener = getAdviceListener(arthasStreamObserver);
            if (listener == null) {
                logger.error("advice listener is null");
                String msg = "advice listener is null, check arthas log";
//                arthasStreamObserver.appendResult(new EnhancerModel(effect, false, msg));
                arthasStreamObserver.end(-1, msg);
                return;
            }
            // trace 类命令可配置是否跳过 JDK 类
            boolean skipJDKTrace = false;
            if(listener instanceof AbstractTraceAdviceListener) {
                skipJDKTrace = ((AbstractTraceAdviceListener) listener).getCommand().isSkipJDKTrace();
            }

            Enhancer enhancer = new Enhancer(listener, listener instanceof InvokeTraceable, skipJDKTrace, getClassNameMatcher(), getClassNameExcludeMatcher(), getMethodNameMatcher());
            // 注册通知监听器与 ClassFileTransformer
            arthasStreamObserver.register(listener, enhancer);
            effect = enhancer.enhance(inst, this.maxNumOfMatchedClass);
            if (effect.getThrowable() != null) {
                String msg = "error happens when enhancing class: "+effect.getThrowable().getMessage();
//                arthasStreamObserver.appendResult(new EnhancerModel(effect, false, msg));
                arthasStreamObserver.end(-1, msg + ", check arthas log: " + LogUtil.loggingFile());
                return;
            }

            if (effect.cCnt() == 0 || effect.mCnt() == 0) {
                // 未命中任何类或方法
                if (!StringUtils.isEmpty(effect.getOverLimitMsg())) {
                    String msg = "no class effected";
//                    arthasStreamObserver.appendResult(new EnhancerModel(effect, false));
                    arthasStreamObserver.end(-1, msg);
                    return;
                }
                // 可能是方法体过大等原因，给出排查指引
//                arthasStreamObserver.appendResult(new EnhancerModel(effect, false, "No class or method is affected"));

                String smCommand = Ansi.ansi().fg(Ansi.Color.GREEN).a("sm CLASS_NAME METHOD_NAME").reset().toString();
                String optionsCommand = Ansi.ansi().fg(Ansi.Color.GREEN).a("options unsafe true").reset().toString();
                String javaPackage = Ansi.ansi().fg(Ansi.Color.GREEN).a("java.*").reset().toString();
                String resetCommand = Ansi.ansi().fg(Ansi.Color.GREEN).a("reset CLASS_NAME").reset().toString();
                String logStr = Ansi.ansi().fg(Ansi.Color.GREEN).a(LogUtil.loggingFile()).reset().toString();
                String issueStr = Ansi.ansi().fg(Ansi.Color.GREEN).a("https://github.com/alibaba/arthas/issues/47").reset().toString();
                String msg = "No class or method is affected, try:\n"
                        + "1. Execute `" + smCommand + "` to make sure the method you are tracing actually exists (it might be in your parent class).\n"
                        + "2. Execute `" + optionsCommand + "`, if you want to enhance the classes under the `" + javaPackage + "` package.\n"
                        + "3. Execute `" + resetCommand + "` and try again, your method body might be too large.\n"
                        + "4. Match the constructor, use `<init>`, for example: `watch demo.MathGame <init>`\n"
                        + "5. Check arthas log: " + logStr + "\n"
                        + "6. Visit " + issueStr + " for more details.";
                arthasStreamObserver.end(-1, msg);
                return;
            }
            arthasStreamObserver.appendResult(new EnhancerModel(effect, true));

            // 异步 watch/trace 命令在 RpcAdviceListener 中结束流
        } catch (Throwable e) {
            String msg = "error happens when enhancing class: "+e.getMessage();
            logger.error(msg, e);
//            arthasStreamObserver.appendResult(new EnhancerModel(effect, false, msg));
            arthasStreamObserver.end(-1, msg);
        }
    }

}
