package org.keycloak.scripting;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.keycloak.models.ScriptModel;

/**
 * 未编译脚本的可求值适配器。
 * <p>当 {@link ScriptEngine} 不支持 {@link Compilable} 时，每次通过 {@link ScriptEngine#eval(String, Bindings)} 解释执行源码。</p>
 *
 * @author <a href="mailto:jay@anslow.me.uk">Jay Anslow</a>
 */
class UncompiledEvaluatableScriptAdapter extends AbstractEvaluatableScriptAdapter {
    /** 用于解释执行的 {@link ScriptEngine} 实例。 */
    private final ScriptEngine scriptEngine;

    /** @param scriptModel 脚本模型 @param scriptEngine 脚本引擎，不可为 null */
    UncompiledEvaluatableScriptAdapter(final ScriptModel scriptModel, final ScriptEngine scriptEngine) {
        super(scriptModel);
        if (scriptEngine == null) {
            throw new IllegalArgumentException("scriptEngine must not be null");
        }

        this.scriptEngine = scriptEngine;
    }

    /** @return 底层脚本引擎 */
    @Override
    protected ScriptEngine getEngine() {
        return scriptEngine;
    }

    /** 解释执行脚本源码 @param bindings 脚本绑定 @return 执行结果 */
    @Override
    protected Object eval(final Bindings bindings) throws ScriptException {
        return getEngine().eval(getCode(), bindings);
    }

    /** 使用 {@link ScriptContext} 解释执行脚本 @param context 脚本上下文 @return 执行结果 */
    @Override
    public Object eval(ScriptContext context) throws ScriptExecutionException {
        try {
            return getEngine().eval(getCode(), context);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
}
