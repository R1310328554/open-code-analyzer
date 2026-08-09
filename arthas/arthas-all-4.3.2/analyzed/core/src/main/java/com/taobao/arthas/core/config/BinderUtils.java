package com.taobao.arthas.core.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.taobao.arthas.core.env.Environment;

/**
 * 配置绑定工具：将 {@link Environment} 中的属性值注入到带 {@link Config} 注解的对象。
 * <p>
 * 通过反射扫描 setter 方法按前缀匹配配置键；支持 {@link NestedConfig} 嵌套对象递归注入。
 *
 * @author hengyunabc 2020-01-10
 */
public class BinderUtils {

    /** 无前缀注入：从 Environment 读取属性并调用 setter */
    public static void inject(Environment environment, Object instance) {
        inject(environment, null, null, instance);
    }

    /** 指定前缀注入 */
    public static void inject(Environment environment, String prefix, Object instance) {
        inject(environment, null, prefix, instance);
    }

    /**
     * 核心注入逻辑：解析 {@link Config#prefix()}、遍历 setter、递归处理 {@link NestedConfig} 字段。
     * @param environment 配置环境
     * @param parentPrefix 父级前缀
     * @param prefix 当前前缀
     * @param instance 待注入实例
     */
        if (prefix == null) {
            prefix = "";
        }
        Class<? extends Object> type = instance.getClass();
        try {
            Config annotation = type.getAnnotation(Config.class);

            if (annotation == null) {
                prefix = parentPrefix + '.' + prefix;
            } else {
                prefix = annotation.prefix();
                if (prefix != null) {
                    if (parentPrefix != null && parentPrefix.length() > 0) {
                        prefix = parentPrefix + '.' + prefix;
                    }
                }
            }

            Method[] declaredMethods = type.getDeclaredMethods();
            // 扫描 setter：setXxx -> xxx，拼接 prefix 后从 Environment 取值并 invoke
            for (Method method : declaredMethods) {
                String methodName = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();

                if (parameterTypes.length == 1 && methodName.startsWith("set") && methodName.length() > "set".length()) {

                    String field = getFieldNameFromSetterMethod(methodName);
                    String configKey = prefix + '.' + field;
                    if (environment.containsProperty(configKey)) {
                        Object reslovedValue = environment.getProperty(prefix + '.' + field, parameterTypes[0]);
                        if (reslovedValue != null) {
                            method.invoke(instance, new Object[] { reslovedValue });
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("inject error. prefix: " + prefix + ", instance: " + instance, e);
        }

        // 处理 @NestedConfig 标注的嵌套配置对象字段
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            NestedConfig nestedConfig = field.getAnnotation(NestedConfig.class);
            if (nestedConfig != null) {
                String prefixForField = field.getName();
                if (parentPrefix != null && prefix.length() > 0) {
                    prefixForField = prefix + '.' + prefixForField;
                }

                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(instance);
                    // 嵌套对象为空时先实例化再递归注入
                    if (fieldValue == null) {
                        fieldValue = field.getType().newInstance();
                    }
                    inject(environment, prefix, prefixForField, fieldValue);

                    field.set(instance, fieldValue);
                } catch (Exception e) {
                    throw new RuntimeException("process @NestedConfig error, field: " + field + ", prefix: "
                            + prefix + ", instance: " + instance, e);
                }
            }
        }
    }

    /**
     * 从setter方法获取到field的String。比如 setHost， 则获取到的是host。
     *
     * @param methodName
     * @return
     */
    private static String getFieldNameFromSetterMethod(String methodName) {
        String field = methodName.substring("set".length());
        String startPart = field.substring(0, 1).toLowerCase();
        String endPart = field.substring(1);

        field = startPart + endPart;
        return field;
    }

}
