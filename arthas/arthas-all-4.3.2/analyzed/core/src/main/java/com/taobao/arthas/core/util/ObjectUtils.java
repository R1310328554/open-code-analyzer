//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.taobao.arthas.core.util;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * 对象、数组与异常相关的通用判断与字符串化工具（Spring Framework 风格 API）。
 * <p>
 * 提供 null 安全的 equals/hashCode/toString、checked 异常与 throws 子句兼容性检查、
 * 以及各基本类型数组的格式化输出，供命令层与 Spring 集成代码复用。
 */
public abstract class ObjectUtils {
    private static final int INITIAL_HASH = 7;
    private static final int MULTIPLIER = 31;
    private static final String EMPTY_STRING = "";
    private static final String NULL_STRING = "null";
    private static final String ARRAY_START = "{";
    private static final String ARRAY_END = "}";
    private static final String EMPTY_ARRAY = "{}";
    private static final String ARRAY_ELEMENT_SEPARATOR = ", ";

    public ObjectUtils() {
    }

    /** 判断是否为 checked 异常（非 {@link RuntimeException} 且非 {@link Error}） */
    public static boolean isCheckedException(Throwable ex) {
        return !(ex instanceof RuntimeException) && !(ex instanceof Error);
    }

    /** 判断异常是否与方法 throws 子句声明兼容（unchecked 异常恒为 true） */
    public static boolean isCompatibleWithThrowsClause(Throwable ex, Class... declaredExceptions) {
        if(!isCheckedException(ex)) {
            return true;
        } else {
            if(declaredExceptions != null) {
                Class[] var2 = declaredExceptions;
                int var3 = declaredExceptions.length;

                for(int var4 = 0; var4 < var3; ++var4) {
                    Class declaredException = var2[var4];
                    if(declaredException.isInstance(ex)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    /** 判断对象是否为数组实例 */
    public static boolean isArray(Object obj) {
        return obj != null && obj.getClass().isArray();
    }

    /** 判断对象数组为 null 或长度为 0 */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /** 在数组中 null 安全地查找元素（使用 {@link #nullSafeEquals}） */
    public static boolean containsElement(Object[] array, Object element) {
        if(array == null) {
            return false;
        } else {
            Object[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Object arrayEle = var2[var4];
                if(nullSafeEquals(arrayEle, element)) {
                    return true;
                }
            }

            return false;
        }
    }

    /** 判断枚举常量集合是否包含指定名称（大小写敏感） */
    public static boolean containsConstant(Enum<?>[] enumValues, String constant) {
        return containsConstant(enumValues, constant, false);
    }

    /** 判断枚举常量集合是否包含指定名称，可配置大小写 sensitivity */
    public static boolean containsConstant(Enum<?>[] enumValues, String constant, boolean caseSensitive) {
        Enum[] var3 = enumValues;
        int var4 = enumValues.length;
        int var5 = 0;

        while(true) {
            if(var5 >= var4) {
                return false;
            }

            Enum candidate = var3[var5];
            if(caseSensitive) {
                if(candidate.toString().equals(constant)) {
                    break;
                }
            } else if(candidate.toString().equalsIgnoreCase(constant)) {
                break;
            }

            ++var5;
        }

        return true;
    }

    /** 将任意数组或 Object[] 统一转为 Object[]；非数组抛 IllegalArgumentException */
    public static Object[] toObjectArray(Object source) {
        if(source instanceof Object[]) {
            return (Object[])((Object[])source);
        } else if(source == null) {
            return new Object[0];
        } else if(!source.getClass().isArray()) {
            throw new IllegalArgumentException("Source is not an array: " + source);
        } else {
            int length = Array.getLength(source);
            if(length == 0) {
                return new Object[0];
            } else {
                Class wrapperType = Array.get(source, 0).getClass();
                Object[] newArray = (Object[])((Object[])Array.newInstance(wrapperType, length));

                for(int i = 0; i < length; ++i) {
                    newArray[i] = Array.get(source, i);
                }

                return newArray;
            }
        }
    }

    /** null 安全的 equals，数组类型按元素逐段比较 */
    public static boolean nullSafeEquals(Object o1, Object o2) {
        if(o1 == o2) {
            return true;
        } else if(o1 != null && o2 != null) {
            if(o1.equals(o2)) {
                return true;
            } else {
                if(o1.getClass().isArray() && o2.getClass().isArray()) {
                    if(o1 instanceof Object[] && o2 instanceof Object[]) {
                        return Arrays.equals((Object[])((Object[])o1), (Object[])((Object[])o2));
                    }

                    if(o1 instanceof boolean[] && o2 instanceof boolean[]) {
                        return Arrays.equals((boolean[])((boolean[])o1), (boolean[])((boolean[])o2));
                    }

                    if(o1 instanceof byte[] && o2 instanceof byte[]) {
                        return Arrays.equals((byte[])((byte[])o1), (byte[])((byte[])o2));
                    }

                    if(o1 instanceof char[] && o2 instanceof char[]) {
                        return Arrays.equals((char[])((char[])o1), (char[])((char[])o2));
                    }

                    if(o1 instanceof double[] && o2 instanceof double[]) {
                        return Arrays.equals((double[])((double[])o1), (double[])((double[])o2));
                    }

                    if(o1 instanceof float[] && o2 instanceof float[]) {
                        return Arrays.equals((float[])((float[])o1), (float[])((float[])o2));
                    }

                    if(o1 instanceof int[] && o2 instanceof int[]) {
                        return Arrays.equals((int[])((int[])o1), (int[])((int[])o2));
                    }

                    if(o1 instanceof long[] && o2 instanceof long[]) {
                        return Arrays.equals((long[])((long[])o1), (long[])((long[])o2));
                    }

                    if(o1 instanceof short[] && o2 instanceof short[]) {
                        return Arrays.equals((short[])((short[])o1), (short[])((short[])o2));
                    }
                }

                return false;
            }
        } else {
            return false;
        }
    }

    /** null 安全的 hashCode；数组按元素递归哈希 */
    public static int nullSafeHashCode(Object obj) {
        if(obj == null) {
            return 0;
        } else {
            if(obj.getClass().isArray()) {
                if(obj instanceof Object[]) {
                    return nullSafeHashCode((Object[])((Object[])obj));
                }

                if(obj instanceof boolean[]) {
                    return nullSafeHashCode((boolean[])((boolean[])obj));
                }

                if(obj instanceof byte[]) {
                    return nullSafeHashCode((byte[])((byte[])obj));
                }

                if(obj instanceof char[]) {
                    return nullSafeHashCode((char[])((char[])obj));
                }

                if(obj instanceof double[]) {
                    return nullSafeHashCode((double[])((double[])obj));
                }

                if(obj instanceof float[]) {
                    return nullSafeHashCode((float[])((float[])obj));
                }

                if(obj instanceof int[]) {
                    return nullSafeHashCode((int[])((int[])obj));
                }

                if(obj instanceof long[]) {
                    return nullSafeHashCode((long[])((long[])obj));
                }

                if(obj instanceof short[]) {
                    return nullSafeHashCode((short[])((short[])obj));
                }
            }

            return obj.hashCode();
        }
    }

    /** Object[] 的 null 安全哈希（31 倍增算法） */
    public static int nullSafeHashCode(Object[] array) {
        if(array == null) {
            return 0;
        } else {
            int hash = 7;
            Object[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Object element = var2[var4];
                hash = 31 * hash + nullSafeHashCode(element);
            }

            return hash;
        }
    }

    /** boolean[] 的 null 安全哈希 */
    public static int nullSafeHashCode(boolean[] array) {
        if(array == null) {
            return 0;
        } else {
            int hash = 7;
            boolean[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                boolean element = var2[var4];
                hash = 31 * hash + hashCode(element);
            }

            return hash;
        }
    }

    /** byte[] 的 null 安全哈希 */
    public static int nullSafeHashCode(byte[] array) {
        if(array == null) {
            return 0;
        } else {
            int hash = 7;
            byte[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                byte element = var2[var4];
                hash = 31 * hash + element;
            }

            return hash;
        }
    }

    /** char[] 的 null 安全哈希 */
    public static int nullSafeHashCode(char[] array) {
        if(array == null) {
            return 0;
        } else {
            int hash = 7;
            char[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                char element = var2[var4];
                hash = 31 * hash + element;
            }

            return hash;
        }
    }

    /** double[] 的 null 安全哈希 */
    public static int nullSafeHashCode(double[] array) {
        if(array == null) {
            return 0;
        } else {
            int hash = 7;
            double[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                double element = var2[var4];
                hash = 31 * hash + hashCode(element);
            }

            return hash;
        }
    }

    /** float[] 的 null 安全哈希 */
    public static int nullSafeHashCode(float[] array) {
        if(array == null) {
            return 0;
        } else {
            int hash = 7;
            float[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                float element = var2[var4];
                hash = 31 * hash + hashCode(element);
            }

            return hash;
        }
    }

    /** int[] 的 null 安全哈希 */
    public static int nullSafeHashCode(int[] array) {
        if(array == null) {
            return 0;
        } else {
            int hash = 7;
            int[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                int element = var2[var4];
                hash = 31 * hash + element;
            }

            return hash;
        }
    }

    /** long[] 的 null 安全哈希 */
    public static int nullSafeHashCode(long[] array) {
        if(array == null) {
            return 0;
        } else {
            int hash = 7;
            long[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                long element = var2[var4];
                hash = 31 * hash + hashCode(element);
            }

            return hash;
        }
    }

    /** short[] 的 null 安全哈希 */
    public static int nullSafeHashCode(short[] array) {
        if(array == null) {
            return 0;
        } else {
            int hash = 7;
            short[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                short element = var2[var4];
                hash = 31 * hash + element;
            }

            return hash;
        }
    }

    /** boolean 值的哈希（与 Boolean.hashCode 一致） */
    public static int hashCode(boolean bool) {
        return bool?1231:1237;
    }

    /** double 值的哈希 */
    public static int hashCode(double dbl) {
        return hashCode(Double.doubleToLongBits(dbl));
    }

    /** float 值的哈希 */
    public static int hashCode(float flt) {
        return Float.floatToIntBits(flt);
    }

    /** long 值的哈希（高低位异或） */
    public static int hashCode(long lng) {
        return (int)(lng ^ lng >>> 32);
    }

    /** 返回 {@code ClassName@hexIdentity} 形式的调试字符串 */
    public static String identityToString(Object obj) {
        return obj == null?"":obj.getClass().getName() + "@" + getIdentityHexString(obj);
    }

    /** 返回对象 identityHashCode 的十六进制字符串 */
    public static String getIdentityHexString(Object obj) {
        return Integer.toHexString(System.identityHashCode(obj));
    }

    /** 用于 UI 展示：null 转为空串，否则 nullSafeToString */
    public static String getDisplayString(Object obj) {
        return obj == null?"":nullSafeToString(obj);
    }

    /** null 时返回 {@code "null"}，否则返回 {@link Class#getName()} */
    public static String nullSafeClassName(Object obj) {
        return obj != null?obj.getClass().getName():"null";
    }

    /** null 安全 toString，数组格式化为 {@code {a, b}} 形式 */
    public static String nullSafeToString(Object obj) {
        if(obj == null) {
            return "null";
        } else if(obj instanceof String) {
            return (String)obj;
        } else if(obj instanceof Object[]) {
            return nullSafeToString((Object[])((Object[])obj));
        } else if(obj instanceof boolean[]) {
            return nullSafeToString((boolean[])((boolean[])obj));
        } else if(obj instanceof byte[]) {
            return nullSafeToString((byte[])((byte[])obj));
        } else if(obj instanceof char[]) {
            return nullSafeToString((char[])((char[])obj));
        } else if(obj instanceof double[]) {
            return nullSafeToString((double[])((double[])obj));
        } else if(obj instanceof float[]) {
            return nullSafeToString((float[])((float[])obj));
        } else if(obj instanceof int[]) {
            return nullSafeToString((int[])((int[])obj));
        } else if(obj instanceof long[]) {
            return nullSafeToString((long[])((long[])obj));
        } else if(obj instanceof short[]) {
            return nullSafeToString((short[])((short[])obj));
        } else {
            String str = obj.toString();
            return str != null?str:"";
        }
    }

    /** Object[] 格式化为 {@code {elem1, elem2}} */
    public static String nullSafeToString(Object[] array) {
        if(array == null) {
            return "null";
        } else {
            int length = array.length;
            if(length == 0) {
                return "{}";
            } else {
                StringBuilder sb = new StringBuilder("{");

                for(int i = 0; i < length; ++i) {
                    if(i > 0) {
                        sb.append(", ");
                    }

                    sb.append(array[i]);
                }

                sb.append("}");
                return sb.toString();
            }
        }
    }

    /** boolean[] 格式化输出 */
    public static String nullSafeToString(boolean[] array) {
        if(array == null) {
            return "null";
        } else {
            int length = array.length;
            if(length == 0) {
                return "{}";
            } else {
                StringBuilder sb = new StringBuilder("{");

                for(int i = 0; i < length; ++i) {
                    if(i > 0) {
                        sb.append(", ");
                    }

                    sb.append(array[i]);
                }

                sb.append("}");
                return sb.toString();
            }
        }
    }

    /** byte[] 格式化输出 */
    public static String nullSafeToString(byte[] array) {
        if(array == null) {
            return "null";
        } else {
            int length = array.length;
            if(length == 0) {
                return "{}";
            } else {
                StringBuilder sb = new StringBuilder("{");

                for(int i = 0; i < length; ++i) {
                    if(i > 0) {
                        sb.append(", ");
                    }

                    sb.append(array[i]);
                }

                sb.append("}");
                return sb.toString();
            }
        }
    }

    /** char[] 格式化输出（字符加单引号） */
    public static String nullSafeToString(char[] array) {
        if(array == null) {
            return "null";
        } else {
            int length = array.length;
            if(length == 0) {
                return "{}";
            } else {
                StringBuilder sb = new StringBuilder("{");

                for(int i = 0; i < length; ++i) {
                    if(i > 0) {
                        sb.append(", ");
                    }

                    sb.append("\'").append(array[i]).append("\'");
                }

                sb.append("}");
                return sb.toString();
            }
        }
    }

    /** double[] 格式化输出 */
    public static String nullSafeToString(double[] array) {
        if(array == null) {
            return "null";
        } else {
            int length = array.length;
            if(length == 0) {
                return "{}";
            } else {
                StringBuilder sb = new StringBuilder("{");

                for(int i = 0; i < length; ++i) {
                    if(i > 0) {
                        sb.append(", ");
                    }

                    sb.append(array[i]);
                }

                sb.append("}");
                return sb.toString();
            }
        }
    }

    /** float[] 格式化输出 */
    public static String nullSafeToString(float[] array) {
        if(array == null) {
            return "null";
        } else {
            int length = array.length;
            if(length == 0) {
                return "{}";
            } else {
                StringBuilder sb = new StringBuilder("{");

                for(int i = 0; i < length; ++i) {
                    if(i > 0) {
                        sb.append(", ");
                    }

                    sb.append(array[i]);
                }

                sb.append("}");
                return sb.toString();
            }
        }
    }

    /** int[] 格式化输出 */
    public static String nullSafeToString(int[] array) {
        if(array == null) {
            return "null";
        } else {
            int length = array.length;
            if(length == 0) {
                return "{}";
            } else {
                StringBuilder sb = new StringBuilder("{");

                for(int i = 0; i < length; ++i) {
                    if(i > 0) {
                        sb.append(", ");
                    }

                    sb.append(array[i]);
                }

                sb.append("}");
                return sb.toString();
            }
        }
    }

    /** long[] 格式化输出 */
    public static String nullSafeToString(long[] array) {
        if(array == null) {
            return "null";
        } else {
            int length = array.length;
            if(length == 0) {
                return "{}";
            } else {
                StringBuilder sb = new StringBuilder("{");

                for(int i = 0; i < length; ++i) {
                    if(i > 0) {
                        sb.append(", ");
                    }

                    sb.append(array[i]);
                }

                sb.append("}");
                return sb.toString();
            }
        }
    }

    /** short[] 格式化输出 */
    public static String nullSafeToString(short[] array) {
        if(array == null) {
            return "null";
        } else {
            int length = array.length;
            if(length == 0) {
                return "{}";
            } else {
                StringBuilder sb = new StringBuilder("{");

                for(int i = 0; i < length; ++i) {
                    if(i > 0) {
                        sb.append(", ");
                    }

                    sb.append(array[i]);
                }

                sb.append("}");
                return sb.toString();
            }
        }
    }
}
