package org.keycloak.scripting;

import javax.script.ScriptContext;

import org.keycloak.models.ScriptModel;

/**
 * 可求值脚本适配器：包装 {@link ScriptModel}，支持以自定义绑定执行脚本。
 * <p>由 {@link ScriptingProvider#prepareEvaluatableScript} 创建。</p>
 *
 * @author <a href="mailto:jay@anslow.me.uk">Jay Anslow</a>
 */
public interface EvaluatableScriptAdapter {
    /** @return 被包装的 {@link ScriptModel} */
    ScriptModel getScriptModel();

    /**
     * 使用 {@link ScriptBindingsConfigurer} 配置绑定后执行脚本。
     * @param bindingsConfigurer 绑定配置器
     * @return 脚本执行结果
     * @throws ScriptExecutionException 执行失败时抛出
     */
    Object eval(ScriptBindingsConfigurer bindingsConfigurer) throws ScriptExecutionException;
    /**
     * 使用给定 {@link ScriptContext} 执行脚本。
     * @param context 脚本上下文
     * @return 脚本执行结果
     * @throws ScriptExecutionException 执行失败时抛出
     */
    Object eval(ScriptContext context) throws ScriptExecutionException;
}
