package com.taobao.arthas.core.command.express;

import java.util.Map;

import com.taobao.arthas.core.GlobalOptions;

import ognl.ObjectPropertyAccessor;
import ognl.OgnlException;

/**
 * Arthas 定制的 OGNL 对象属性访问器：在 {@link GlobalOptions#strict} 模式下禁止写属性。
 *
 * @author hengyunabc 2022-03-24
 */
public class ArthasObjectPropertyAccessor extends ObjectPropertyAccessor {

    /**
     * 尝试设置目标对象属性；严格模式下直接抛出 {@link IllegalAccessError}。
     */
    @Override
    public Object setPossibleProperty(Map context, Object target, String name, Object value) throws OgnlException {
        if (GlobalOptions.strict) {
            throw new IllegalAccessError(GlobalOptions.STRICT_MESSAGE);
        }
        return super.setPossibleProperty(context, target, name, value);
    }

}
