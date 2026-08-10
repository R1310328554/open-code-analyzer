package org.keycloak.scripting;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.keycloak.models.ScriptModel;

/**
 * 可求值脚本适配器抽象基类。
 * <p>包装 {@link ScriptModel}，通过 JSR-223 {@link ScriptEngine} 执行脚本；子类区分编译与未编译执行路径。</p>
 *
 * @author <a href="mailto:jay@anslow.me.uk">Jay Anslow</a>
 */
abstract class AbstractEvaluatableScriptAdapter implements EvaluatableScriptAdapter {
    /** 被包装的 {@link ScriptModel}。 */
    private final ScriptModel scriptModel;

    /** @param scriptModel 脚本模型，不可为 null */
    AbstractEvaluatableScriptAdapter(final ScriptModel scriptModel) {
        if (scriptModel == null) {
            throw new IllegalArgumentException("scriptModel must not be null");
        }
        this.scriptModel = scriptModel;
    }

    /** 配置绑定后执行脚本 @param bindingsConfigurer 绑定配置器 @return 执行结果 @throws ScriptExecutionException 执行失败 */
    @Override
    public Object eval(final ScriptBindingsConfigurer bindingsConfigurer) throws ScriptExecutionException {
        return evalUnchecked(createBindings(bindingsConfigurer));
    }

    /** @return 被包装的脚本模型 */
    @Override
    public ScriptModel getScriptModel() {
        return scriptModel;
    }

    /**
     * 准备可调用脚本适配器（会修改底层 {@link ScriptEngine} 绑定，不可并发使用）。
     * <p>Nashorn 的 {@link ScriptEngine} 与 {@link javax.script.CompiledScript} 线程安全，但 {@link Bindings} 不是。</p>
     * @param bindingsConfigurer 绑定配置器
     * @return {@link InvocableScriptAdapter} 实例
     */
    InvocableScriptAdapter prepareInvokableScript(final ScriptBindingsConfigurer bindingsConfigurer) {
        final Bindings bindings = createBindings(bindingsConfigurer);
        evalUnchecked(bindings);
        final ScriptEngine engine = getEngine();
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        return new InvocableScriptAdapter(scriptModel, engine);
    }

    /** @return 脚本源代码 */
    protected String getCode() {
        return scriptModel.getCode();
    }

    /** @return 底层脚本引擎 */
    protected abstract ScriptEngine getEngine();

    /** 使用给定绑定执行脚本 @param bindings 脚本绑定 @return 执行结果 @throws ScriptException 脚本异常 */
    protected abstract Object eval(Bindings bindings) throws ScriptException;

    private Object evalUnchecked(final Bindings bindings) {
        try {
            return eval(bindings);
        }
        catch (ScriptException e) {
            throw new ScriptExecutionException(scriptModel, e);
        }
    }

    private Bindings createBindings(final ScriptBindingsConfigurer bindingsConfigurer) {
        if (bindingsConfigurer == null) {
            throw new IllegalArgumentException("bindingsConfigurer must not be null");
        }
        final Bindings bindings = getEngine().createBindings();
        bindingsConfigurer.configureBindings(bindings);
        return bindings;
    }
}
