package org.keycloak.testframework.injection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 为注入注解创建动态代理，覆盖 {@code ref} 等属性默认值。
 * <p>
 * 注解类型不允许 {@code null} 默认值，故缺失 ref 时使用空字符串。
 */
public class DefaultAnnotationProxy implements InvocationHandler {

    /** 被代理的注解类型。 */
    private final Class<?> annotationClass;
    /** 要注入的 ref 值（空字符串表示默认实例）。 */
    private final String ref;

    /**
     * 创建指定 ref 的注解代理实例。
     *
     * @param annotationClass 注解接口 Class
     * @param ref 实例引用标识，{@code null} 时转为空字符串
     * @return 注解代理对象
     */
    public static <S> S proxy(Class<S> annotationClass, String ref) {
        // 注解属性不能有 null 默认值，故将 null ref 转为空字符串
        if (ref == null) {
            ref = "";
        }
        return (S) Proxy.newProxyInstance(DefaultAnnotationProxy.class.getClassLoader(), new Class<?>[]{annotationClass}, new DefaultAnnotationProxy(annotationClass, ref));
    }

    /** @param annotationClass 注解类型 @param ref 引用标识 */
    private <S> DefaultAnnotationProxy(Class<?> annotationClass, String ref) {
        this.annotationClass = annotationClass;
        this.ref = ref;
    }

    /** {@inheritDoc} 处理 {@code annotationType}、{@code ref} 及注解默认属性。 */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("annotationType")) {
            return annotationClass;
        } else if (method.getName().equals("ref")) {
            return ref != null ? ref : "";
        } else {
            return annotationClass.getMethod(method.getName()).getDefaultValue();
        }
    }

}
