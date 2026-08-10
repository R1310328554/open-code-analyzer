package org.keycloak.scripting;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.keycloak.models.ScriptModel;

/**
 * 已编译脚本的可求值适配器。
 * <p>通过 {@link CompiledScript} 执行预编译脚本，性能优于每次 eval 源码。</p>
 *
 * @author <a href="mailto:jay@anslow.me.uk">Jay Anslow</a>
 */
class CompiledEvaluatableScriptAdapter extends AbstractEvaluatableScriptAdapter {
    /** 与 {@link ScriptModel} 对应的 {@link CompiledScript}。 */
    private final CompiledScript compiledScript;

    /** @param scriptModel 脚本模型 @param compiledScript 已编译脚本，不可为 null */
    CompiledEvaluatableScriptAdapter(final ScriptModel scriptModel, final CompiledScript compiledScript) {
        super(scriptModel);

        if (compiledScript == null) {
            throw new IllegalArgumentException("compiledScript must not be null");
        }

        this.compiledScript = compiledScript;
    }

    /** @return 编译脚本关联的引擎 */
    @Override
    protected ScriptEngine getEngine() {
        return compiledScript.getEngine();
    }

    /** 使用绑定执行已编译脚本 @param bindings 脚本绑定 @return 执行结果 */
    @Override
    protected Object eval(final Bindings bindings) throws ScriptException {
        return compiledScript.eval(bindings);
    }

    /** 使用 {@link ScriptContext} 执行已编译脚本 @param context 脚本上下文 @return 执行结果 */
    @Override
    public Object eval(ScriptContext context) throws ScriptExecutionException {
        try {
            return compiledScript.eval(context);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
}
