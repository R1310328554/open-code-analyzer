package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.advisor.Advice;
import com.taobao.arthas.core.advisor.AdviceListenerAdapter;
import com.taobao.arthas.core.advisor.ArthasMethod;
import com.taobao.arthas.core.command.ScriptSupportCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * 已废弃的 Groovy 脚本 Advice 监听器：将方法切点事件转发给 {@link ScriptSupportCommand.ScriptListener}。
 * <p>
 * Arthas 3.0 起因严重内存泄漏已移除 Groovy 支持；本类保留仅为兼容旧字节码引用。
 *
 * @author beiwei30 on 01/12/2016.
 */
@Deprecated
public class GroovyAdviceListener extends AdviceListenerAdapter {
    /** 用户 Groovy 脚本实现的回调接口 */
    private ScriptSupportCommand.ScriptListener scriptListener;
    /** 脚本输出适配器，桥接到 CommandProcess */
    private ScriptSupportCommand.Output output;

    /** 包装 CommandProcess 为 ScriptSupportCommand.Output */
    public GroovyAdviceListener(ScriptSupportCommand.ScriptListener scriptListener, CommandProcess process) {
        this.scriptListener = scriptListener;
        this.output = new CommandProcessAdaptor(process);
    }

    @Override
    public void create() {
        scriptListener.create(output);
    }

    @Override
    public void destroy() {
        scriptListener.destroy(output);
    }

    @Override
    public void before(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args)
            throws Throwable {
        scriptListener.before(output, Advice.newForBefore(loader, clazz, method, target, args));
    }

    @Override
    public void afterReturning(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args,
                               Object returnObject) throws Throwable {
        scriptListener.afterReturning(output, Advice.newForAfterReturning(loader, clazz, method, target, args, returnObject));
    }

    @Override
    public void afterThrowing(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args,
                              Throwable throwable) throws Throwable {
        scriptListener.afterThrowing(output, Advice.newForAfterThrowing(loader, clazz, method, target, args, throwable));
    }

    /** 将 CommandProcess.write/end 适配为脚本 Output 接口 */
    private static class CommandProcessAdaptor implements ScriptSupportCommand.Output {
        private CommandProcess process;

        public CommandProcessAdaptor(CommandProcess process) {
            this.process = process;
        }

        @Override
        public ScriptSupportCommand.Output print(String string) {
            process.write(string);
            return this;
        }

        @Override
        public ScriptSupportCommand.Output println(String string) {
            process.write(string).write("\n");
            return this;
        }

        @Override
        public ScriptSupportCommand.Output finish() {
            process.end();
            return this;
        }
    }
}
