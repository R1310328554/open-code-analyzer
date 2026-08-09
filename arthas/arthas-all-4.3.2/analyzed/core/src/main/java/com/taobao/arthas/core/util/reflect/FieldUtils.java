package com.taobao.arthas.core.util.reflect;

import com.taobao.arthas.core.util.StringUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 反射字段读写工具类（Apache Commons Lang 风格 API）。
 *
 * @author ralf0131 2016-12-28 14:39.
 */
public class FieldUtils {

    private static final int ACCESS_TEST = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;

    /**
     * 读取指定对象上声明的 {@code public} {@link Field}（仅当前类，不含父类）。
     *
     * @param target 目标对象，不能为 {@code null}
     * @param fieldName 字段名
     * @return 字段值
     * @throws IllegalArgumentException 若 target 为 null、字段名为空或字段不存在
     * @throws IllegalAccessException 若字段非 {@code public}
     */
    public static Object readDeclaredField(final Object target, final String fieldName) throws IllegalAccessException {
        return readDeclaredField(target, fieldName, false);
    }

    /**
     * 按名称读取字段值（仅当前类声明的字段）。
     *
     * @param target 目标对象，不能为 {@code null}
     * @param fieldName 字段名
     * @param forceAccess 为 true 时通过 {@link AccessibleObject#setAccessible(boolean)} 突破访问限制；
     *                    为 false 时仅匹配 public 字段
     * @return 字段值
     * @throws IllegalArgumentException 若 target 为 null、字段名为空或字段不存在
     * @throws IllegalAccessException 若无法访问该字段
     */
    public static Object readDeclaredField(final Object target, final String fieldName, final boolean forceAccess) throws IllegalAccessException {
        isTrue(target != null, "target object must not be null");
        final Class<?> cls = target.getClass();
        final Field field = getDeclaredField(cls, fieldName, forceAccess);
        isTrue(field != null, "Cannot locate declared field %s.%s", cls, fieldName);
        // 上面已按需 setAccessible，此处不再重复
        return readField(field, target, false);
    }

    /**
     * 获取可访问的声明字段（仅当前类）；{@code forceAccess} 为 true 时可突破非 public 限制。
     *
     * @param cls 待反射的 {@link Class}，不能为 {@code null}
     * @param fieldName 字段名
     * @param forceAccess 是否强制 setAccessible
     * @return 找到的 Field；不存在或不可访问且未 force 时返回 null
     * @throws IllegalArgumentException 若 cls 为 null 或字段名为空
     */
    public static Field getDeclaredField(final Class<?> cls, final String fieldName, final boolean forceAccess) {
        isTrue(cls != null, "The class must not be null");
        isTrue(!StringUtils.isBlank(fieldName), "The field name must not be blank/empty");
        try {
            // 仅查当前类 getDeclaredField
            final Field field = cls.getDeclaredField(fieldName);
            if (!isAccessible(field)) {
                if (forceAccess) {
                    field.setAccessible(true);
                } else {
                    return null;
                }
            }
            return field;
        } catch (final NoSuchFieldException e) { // NOPMD
            // ignore
        }
        return null;
    }

    /**
     * 读取 {@link Field} 的值。
     *
     * @param field 字段对象
     * @param target 实例对象；静态字段可传 {@code null}
     * @param forceAccess 是否强制 setAccessible
     * @return 字段值
     * @throws IllegalArgumentException 若 field 为 null
     * @throws IllegalAccessException 若无法访问
     */
    public static Object readField(final Field field, final Object target, final boolean forceAccess) throws IllegalAccessException {
        isTrue(field != null, "The field must not be null");
        if (forceAccess && !field.isAccessible()) {
            field.setAccessible(true);
        } else {
            setAccessibleWorkaround(field);
        }
        return field.get(target);
    }

    /**
     * 读取可访问的 {@code static} {@link Field}。
     *
     * @param field 静态字段
     * @return 字段值
     * @throws IllegalArgumentException 若 field 为 null 或非 static
     * @throws IllegalAccessException 若无法访问
     */
    public static Object readStaticField(final Field field) throws IllegalAccessException {
        return readStaticField(field, false);
    }

    /**
     * 读取静态 {@link Field}。
     *
     * @param field 静态字段
     * @param forceAccess 是否强制 setAccessible
     * @return 字段值
     * @throws IllegalArgumentException 若 field 为 null 或非 static
     * @throws IllegalAccessException 若无法访问
     */
    public static Object readStaticField(final Field field, final boolean forceAccess) throws IllegalAccessException {
        isTrue(field != null, "The field must not be null");
        isTrue(Modifier.isStatic(field.getModifiers()), "The field '%s' is not static", field.getName());
        return readField(field, (Object) null, forceAccess);
    }

    /**
     * 写入 {@code public static} {@link Field}。
     *
     * @param field 静态字段
     * @param value 新值
     * @throws IllegalArgumentException 若 field 为 null、非 static 或 value 类型不兼容
     * @throws IllegalAccessException 若字段非 public 或为 final
     */
    public static void writeStaticField(final Field field, final Object value) throws IllegalAccessException {
        writeStaticField(field, value, false);
    }

    /**
     * 写入静态 {@link Field}。
     *
     * @param field 静态字段
     * @param value 新值
     * @param forceAccess 是否强制 setAccessible
     * @throws IllegalArgumentException 若 field 为 null、非 static 或 value 类型不兼容
     * @throws IllegalAccessException 若无法访问或为 final
     */
    public static void writeStaticField(final Field field, final Object value, final boolean forceAccess) throws IllegalAccessException {
        isTrue(field != null, "The field must not be null");
        isTrue(Modifier.isStatic(field.getModifiers()), "The field %s.%s is not static", field.getDeclaringClass().getName(),
                field.getName());
        writeField(field, (Object) null, value, forceAccess);
    }

    /**
     * 写入 {@link Field}。
     *
     * @param field 字段
     * @param target 实例对象；静态字段可传 {@code null}
     * @param value 新值
     * @param forceAccess 是否强制 setAccessible
     * @throws IllegalArgumentException 若 field 为 null 或 value 类型不兼容
     * @throws IllegalAccessException 若无法访问或为 final
     */
    public static void writeField(final Field field, final Object target, final Object value, final boolean forceAccess)
            throws IllegalAccessException {
        isTrue(field != null, "The field must not be null");
        if (forceAccess && !field.isAccessible()) {
            field.setAccessible(true);
        } else {
            setAccessibleWorkaround(field);
        }
        field.set(target, value);
    }

    /**
     * 获取类及其所有父类上的字段数组。
     *
     * @param cls 待查询的 {@link Class}
     * @return 字段数组（可能为空）
     * @throws IllegalArgumentException 若 cls 为 null
     * @since 3.2
     */
    public static Field[] getAllFields(final Class<?> cls) {
        final List<Field> allFieldsList = getAllFieldsList(cls);
        return allFieldsList.toArray(new Field[0]);
    }

    /**
     * 获取类及其所有父类上的字段列表。
     *
     * @param cls 待查询的 {@link Class}
     * @return 字段列表（可能为空）
     * @throws IllegalArgumentException 若 cls 为 null
     * @since 3.2
     */
    public static List<Field> getAllFieldsList(final Class<?> cls) {
        isTrue(cls != null, "The class must not be null");
        final List<Field> allFields = new ArrayList<Field>();
        Class<?> currentClass = cls;
        while (currentClass != null) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            allFields.addAll(Arrays.asList(declaredFields));
            currentClass = currentClass.getSuperclass();
        }
        return allFields;
    }

    /**
     * 按名称获取可访问字段（沿继承链向上查找，含接口 public 字段）。
     *
     * @param cls 待反射的 {@link Class}，不能为 {@code null}
     * @param fieldName 字段名
     * @return Field 对象
     * @throws IllegalArgumentException 若 cls 为 null 或字段名为空
     */
    public static Field getField(final Class<?> cls, final String fieldName) {
        final Field field = getField(cls, fieldName, false);
        setAccessibleWorkaround(field);
        return field;
    }

    /**
     * 按名称获取字段，必要时突破访问限制；沿超类与接口查找。
     *
     * @param cls 待反射的 {@link Class}，不能为 {@code null}
     * @param fieldName 字段名
     * @param forceAccess 是否强制 setAccessible
     * @return Field 对象；继承层次中多处同名 public 字段时可能抛异常
     * @throws IllegalArgumentException 若 cls 为 null、字段名为空或在继承层次中匹配歧义
     */
    public static Field getField(final Class<?> cls, final String fieldName, final boolean forceAccess) {
        isTrue(cls != null, "The class must not be null");
        isTrue(!StringUtils.isBlank(fieldName), "The field name must not be blank/empty");
        // FIXME 此 workaround 是否仍需要？lang 已要求 Java 6+
        // Sun Java 1.3 的 getField 有 bug，故自行实现查找逻辑

        // getField() 返回的 Field 其 declaringClass 指向真正声明该字段的类；
        // 在子类上请求字段时会得到父类字段。
        //
        // 查找优先级：
        // 当前类 private/protected/package/public
        // 父类 protected/package/public
        // private/不同包会阻断继续向上
        // 已实现接口 public

        // 沿超类向上查找
        for (Class<?> acls = cls; acls != null; acls = acls.getSuperclass()) {
            try {
                final Field field = acls.getDeclaredField(fieldName);
                // getDeclaredField 会检查非 public 作用域
                if (!Modifier.isPublic(field.getModifiers())) {
                    if (forceAccess) {
                        field.setAccessible(true);
                    } else {
                        continue;
                    }
                }
                return field;
            } catch (final NoSuchFieldException ex) { // NOPMD
                // ignore
            }
        }
        // 再查 public 接口字段（可能被 private/package 超类字段遮挡）
        Field match = null;
        for (final Class<?> class1 : getAllInterfaces(cls)) {
            try {
                final Field test = class1.getField(fieldName);
                isTrue(match == null, "Reference to field %s is ambiguous relative to %s"
                        + "; a matching field exists on two or more implemented interfaces.", fieldName, cls);
                match = test;
            } catch (final NoSuchFieldException ex) { // NOPMD
                // ignore
            }
        }
        return match;
    }

    /**
     * <p>获取给定类及其超类实现的所有接口列表。</p>
     *
     * <p>顺序按源文件中接口声明顺序及其继承关系深度优先，再按超类同样规则；
     * 重复接口仅保留首次出现。</p>
     *
     * @param cls 待查找的类，可为 {@code null}
     * @return 接口 {@code List}，输入为 null 时返回 null
     */
    public static List<Class<?>> getAllInterfaces(final Class<?> cls) {
        if (cls == null) {
            return null;
        }

        final LinkedHashSet<Class<?>> interfacesFound = new LinkedHashSet<Class<?>>();
        getAllInterfaces(cls, interfacesFound);

        return new ArrayList<Class<?>>(interfacesFound);
    }

    /**
     * 递归收集指定类实现的全部接口。
     *
     * @param cls 当前类，可为 {@code null}
     * @param interfacesFound 已发现的接口集合
     */
    private static void getAllInterfaces(Class<?> cls, final HashSet<Class<?>> interfacesFound) {
        while (cls != null) {
            final Class<?>[] interfaces = cls.getInterfaces();

            for (final Class<?> i : interfaces) {
                if (interfacesFound.add(i)) {
                    getAllInterfaces(i, interfacesFound);
                }
            }

            cls = cls.getSuperclass();
        }
    }


    /**
     * 默认访问权限超类的反射 workaround。
     *
     * 当 {@code public} 子类的默认访问超类含有 {@code public} 成员时，编译期调用正常，
     * 但部分 JVM 上反射调用会错误地拒绝访问；{@code setAccessible(true)} 可缓解（需足够权限）。
     *
     * @param o 待设为可访问的 AccessibleObject
     * @return 是否成功将可访问性设为 true
     */
    static boolean setAccessibleWorkaround(final AccessibleObject o) {
        if (o == null || o.isAccessible()) {
            return false;
        }
        final Member m = (Member) o;
        if (!o.isAccessible() && Modifier.isPublic(m.getModifiers()) && isPackageAccess(m.getDeclaringClass().getModifiers())) {
            try {
                o.setAccessible(true);
                return true;
            } catch (final SecurityException e) { // NOPMD
                // 忽略，后续由 IllegalAccessException 体现
            }
        }
        return false;
    }

    /**
     * 判断修饰符是否表示包级访问（无 public/protected/private）。
     *
     * @param modifiers 待检测修饰符位
     * @return 若无 package/protected/private 则返回 {@code true}
     */
    static boolean isPackageAccess(final int modifiers) {
        return (modifiers & ACCESS_TEST) == 0;
    }

    /**
     * 判断 {@link Member} 是否可直接反射访问。
     *
     * @param m 成员
     * @return 若 m 非 null、public 且非 synthetic 则返回 {@code true}
     */
    static boolean isAccessible(final Member m) {
        return m != null && Modifier.isPublic(m.getModifiers()) && !m.isSynthetic();
    }

    /** 条件断言，失败时抛出带格式化消息的 {@link IllegalArgumentException} */
    static void isTrue(final boolean expression, final String message, final Object... values) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(message, values));
        }
    }
}
