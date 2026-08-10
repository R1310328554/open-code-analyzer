package org.keycloak.common.util;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将对象序列化为分号分隔字符串及反序列化工具；类型安全由调用方保证。
 *
 * @author hmlnarik
 */
public class StringSerialization {

    // 仍需兼容 JDK 7，无法使用函数式接口
    /** 已知类型的序列化/反序列化策略。 */
    private static enum DeSerializerFunction {
        OBJECT {
            @Override public String serialize(Object o)   { return o.toString(); }
            @Override public Object deserialize(String s) { return s; }
        },
        URI {
            @Override public String serialize(Object o)   { return o.toString(); }
            @Override public Object deserialize(String s) { return java.net.URI.create(s); }
        },
        ;

        /** 序列化非 null 值 */
        public abstract String serialize(Object o);
        public abstract Object deserialize(String s);
    }

    private static final Map<Class<?>, DeSerializerFunction> WELL_KNOWN_DESERIALIZERS = new LinkedHashMap<>();
    /** 字段分隔符。 */
    private static final String SEPARATOR = ";";
    private static final Pattern ESCAPE_PATTERN = Pattern.compile(SEPARATOR);
    private static final Pattern UNESCAPE_PATTERN = Pattern.compile(SEPARATOR + SEPARATOR);
    private static final Pattern VALUE_PATTERN = Pattern.compile("([NV])" +
      "(" +
        "(?:[^" + SEPARATOR + "]|" + SEPARATOR + SEPARATOR + ")*?" +
      ")($|" + SEPARATOR + "(?!" + SEPARATOR + "))",
      Pattern.DOTALL
    );

    static {
        WELL_KNOWN_DESERIALIZERS.put(URI.class, DeSerializerFunction.URI);
        WELL_KNOWN_DESERIALIZERS.put(String.class, DeSerializerFunction.OBJECT);
    }

    /**
     * 按 {@link #WELL_KNOWN_DESERIALIZERS} 将对象序列化为 {@link #SEPARATOR} 分隔的字符串。
     *
     * @param toSerialize 待序列化对象（可变参数）
     * @return 序列化结果
     */
    public static String serialize(Object... toSerialize) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < toSerialize.length; i ++) {
            Object o = toSerialize[i];
            String stringO = getStringFrom(o);
            String escapedStringO = ESCAPE_PATTERN.matcher(stringO).replaceAll(SEPARATOR + SEPARATOR);
            sb.append(escapedStringO);

            if (i < toSerialize.length - 1) {
                sb.append(SEPARATOR);
            }
        }

        return sb.toString();
    }

    /** 创建用于顺序读取序列化字符串的 {@link Deserializer}。 */
    public static Deserializer deserialize(String what) {
        return new Deserializer(what);
    }

    /** 将对象编码为 {@code N}（null）或 {@code V} + 值字符串。 */
    private static String getStringFrom(Object o) {
        if (o == null) {
            return "N";
        }

        Class<?> c = o.getClass();
        DeSerializerFunction f = WELL_KNOWN_DESERIALIZERS.get(c);
        return "V" + (f == null ? o : f.serialize(o));
    }

    private static <T> T getObjectFrom(String escapedString, Class<T> clazz) {
        DeSerializerFunction f = WELL_KNOWN_DESERIALIZERS.get(clazz);
        Object res = f == null ? escapedString : f.deserialize(escapedString);
        return clazz.cast(res);
    }

    /** 顺序解析 {@link #serialize(Object...)} 产物的迭代器。 */
    public static class Deserializer {

        private final Matcher valueMatcher;

        public Deserializer(String what) {
            this.valueMatcher = VALUE_PATTERN.matcher(what);
        }

        /**
         * 读取下一个值并转为 {@code clazz}；无更多值时返回 null。
         *
         * @param clazz 目标类型
         */
        public <T> T next(Class<T> clazz) {
            if (! this.valueMatcher.find()) {
                return null;
            }
            String valueOrNull = this.valueMatcher.group(1);
            if (valueOrNull == null || Objects.equals(valueOrNull, "N")) {
                return null;
            }
            String escapedStringO = this.valueMatcher.group(2);
            String unescapedStringO = UNESCAPE_PATTERN.matcher(escapedStringO).replaceAll(SEPARATOR);
            return getObjectFrom(unescapedStringO, clazz);
        }
    }
}
