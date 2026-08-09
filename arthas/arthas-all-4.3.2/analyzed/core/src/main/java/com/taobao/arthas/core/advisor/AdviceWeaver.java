package com.taobao.arthas.core.advisor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;

/**
 * 按 adviceId 维护 {@link AdviceListener} 注册表，支持注册、暂停与恢复。
 * <br/>
 * <p/>
 * <h2>线程帧栈与执行帧栈</h2>
 * 编织者在执行通知的时候有两个重要的栈:线程帧栈(threadFrameStack),执行帧栈(frameStack)
 * <p/>
 * Created by vlinux on 15/5/17.
 */
public class AdviceWeaver {

    private static final Logger logger = LoggerFactory.getLogger(AdviceWeaver.class);

    // adviceId → 监听器，ConcurrentHashMap 支持并发注册
    private final static Map<Long/*ADVICE_ID*/, AdviceListener> advices
            = new ConcurrentHashMap<Long, AdviceListener>();

    /**
     * 注册监听器
     *
     * @param listener 通知监听器
     */
    public static void reg(AdviceListener listener) {

        // 触发监听器创建
        listener.create();

        // 注册监听器
        advices.put(listener.id(), listener);
    }

    /**
     * 注销监听器
     *
     * @param listener 通知监听器
     */
    public static void unReg(AdviceListener listener) {
        if (null != listener) {
            // 注销监听器
            advices.remove(listener.id());

            // 触发监听器销毁
            listener.destroy();
        }
    }

    /** 按 ID 查找监听器，不存在返回 null */
    public static AdviceListener listener(long id) {
        return advices.get(id);
    }

    /**
     * 恢复监听
     *
     * @param listener 通知监听器
     */
    public static void resume(AdviceListener listener) {
        // 注册监听器
        advices.put(listener.id(), listener);
    }

    /**
     * 暂停监听
     *
     * @param adviceId 通知ID
     */
    public static AdviceListener suspend(long adviceId) {
        // 注销监听器
        return advices.remove(adviceId);
    }

}
