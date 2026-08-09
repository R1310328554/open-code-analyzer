package com.taobao.arthas.core.shell.term.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.taobao.arthas.common.ArthasConstants;
import com.taobao.arthas.core.shell.session.Session;

import io.termd.core.readline.Function;
import io.termd.core.readline.Readline;
import io.termd.core.readline.Readline.Interaction;

/**
 * readline {@link Function} 的动态代理：未认证用户拦截 apply 调用。
 * <p>
 * 对 {@code apply} 方法，若 Session 中无 {@link ArthasConstants#SUBJECT_KEY}，
 * 则直接 resume interaction 而不执行目标函数，防止未登录用户使用 Tab 历史等能力。
 *
 * @author hengyunabc 2023-08-24
 */
public class FunctionInvocationHandler implements InvocationHandler {

    /** 关联终端，用于读取 Session 认证状态 */
    private TermImpl termImpl;

    /** 被代理的 termd Function 实例 */
    private Function target;

    /**
     * @param termImpl 当前 TermImpl
     * @param target 原始 Function
     */
    public FunctionInvocationHandler(TermImpl termImpl, Function target) {
        this.termImpl = termImpl;
        this.target = target;
    }

    @Override
    /** 代理 invoke：未认证时跳过 apply，其余方法透传 */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String name = method.getName();

        if (name.equals("apply")) {
            Session session = termImpl.getSession();
            if (session != null) {
                boolean authenticated = session.get(ArthasConstants.SUBJECT_KEY) != null;
                if (authenticated) {
                    return method.invoke(target, args);
                } else {
                    Readline.Interaction interaction = (Interaction) args[0];
                    // 未认证时必须 resume，否则 readline 会卡住
                    interaction.resume();
                    return null;
                }
            }
        }

        return method.invoke(target, args);
    }

}
