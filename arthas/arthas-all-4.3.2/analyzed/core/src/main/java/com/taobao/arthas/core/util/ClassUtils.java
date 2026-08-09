package com.taobao.arthas.core.util;

import static com.taobao.text.ui.Element.label;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.alibaba.deps.org.objectweb.asm.Type;
import com.taobao.arthas.core.command.model.ClassDetailVO;
import com.taobao.arthas.core.command.model.ClassLoaderVO;
import com.taobao.arthas.core.command.model.ClassVO;
import com.taobao.arthas.core.command.model.MethodVO;
import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.ui.Element;
import com.taobao.text.ui.LabelElement;
import com.taobao.text.ui.TableElement;

import static com.taobao.text.Decoration.bold;

/**
 * 类、方法、ClassLoader 信息的 VO 构建与终端表格渲染工具。
 * <p>
 * 供 sc/sm/classloader 等命令将反射元数据转为 {@link ClassDetailVO}、
 * {@link MethodVO} 等模型，并通过 {@link TableElement} 在 TTY 上格式化输出。
 *
 * @author hengyunabc 2018-10-18
 */
public class ClassUtils {

    /**
     * 从 {@link CodeSource} 提取 class 文件所在路径（jar 或目录）。
     *
     * @param cs ProtectionDomain 的 CodeSource
     * @return 文件路径；缺失时返回 {@link Constants#EMPTY_STRING}
     */
    public static String getCodeSource(final CodeSource cs) {
        if (null == cs || null == cs.getLocation() || null == cs.getLocation().getFile()) {
            return com.taobao.arthas.core.util.Constants.EMPTY_STRING;
        }

        return cs.getLocation().getFile();
    }

    /** 判断是否为 Lambda 合成类（synthetic 且类名含 {@code $$Lambda}） */
    public static boolean isLambdaClass(Class<?> clazz) {
        return clazz.isSynthetic() && clazz.getName().contains("$$Lambda");
    }

    /** 渲染类详情表格（不含字段列表） */
    public static Element renderClassInfo(ClassDetailVO clazz) {
        return renderClassInfo(clazz, false);
    }

    /**
     * 将 {@link ClassDetailVO} 渲染为终端表格。
     *
     * @param clazz 类详情 VO
     * @param isPrintField 是否在表格中输出 fields 行
     * @return 可写入 TTY 的 {@link Element}
     */
    public static Element renderClassInfo(ClassDetailVO clazz, boolean isPrintField) {
        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);

        table.row(label("class-info").style(Decoration.bold.bold()), label(clazz.getClassInfo()))
                .row(label("code-source").style(Decoration.bold.bold()), label(clazz.getCodeSource()))
                .row(label("name").style(Decoration.bold.bold()), label(clazz.getName()))
                .row(label("isInterface").style(Decoration.bold.bold()), label("" + clazz.isInterface()))
                .row(label("isAnnotation").style(Decoration.bold.bold()), label("" + clazz.isAnnotation()))
                .row(label("isEnum").style(Decoration.bold.bold()), label("" + clazz.isEnum()))
                .row(label("isAnonymousClass").style(Decoration.bold.bold()), label("" + clazz.isAnonymousClass()))
                .row(label("isArray").style(Decoration.bold.bold()), label("" + clazz.isArray()))
                .row(label("isLocalClass").style(Decoration.bold.bold()), label("" + clazz.isLocalClass()))
                .row(label("isMemberClass").style(Decoration.bold.bold()), label("" + clazz.isMemberClass()))
                .row(label("isPrimitive").style(Decoration.bold.bold()), label("" + clazz.isPrimitive()))
                .row(label("isSynthetic").style(Decoration.bold.bold()), label("" + clazz.isSynthetic()))
                .row(label("simple-name").style(Decoration.bold.bold()), label(clazz.getSimpleName()))
                .row(label("modifier").style(Decoration.bold.bold()), label(clazz.getModifier()))
                .row(label("annotation").style(Decoration.bold.bold()), label(StringUtils.join(clazz.getAnnotations(), ",")))
                .row(label("interfaces").style(Decoration.bold.bold()), label(StringUtils.join(clazz.getInterfaces(), ",")))
                .row(label("super-class").style(Decoration.bold.bold()), TypeRenderUtils.drawSuperClass(clazz))
                .row(label("class-loader").style(Decoration.bold.bold()), TypeRenderUtils.drawClassLoader(clazz))
                .row(label("classLoaderHash").style(Decoration.bold.bold()), label(clazz.getClassLoaderHash()));

        if (isPrintField) {
            table.row(label("fields").style(Decoration.bold.bold()), TypeRenderUtils.drawField(clazz));
        }
        return table;
    }

    /**
     * 从 {@link Class} 反射对象构建完整 {@link ClassDetailVO}。
     *
     * @param clazz 目标类
     * @param withFields 是否填充字段信息
     * @param expand 字段展开层级（传给 TypeRenderUtils）
     * @return 类详情 VO
     */
    public static ClassDetailVO createClassInfo(Class clazz, boolean withFields, Integer expand) {
        CodeSource cs = clazz.getProtectionDomain().getCodeSource();
        ClassDetailVO classInfo = new ClassDetailVO();
        classInfo.setName(StringUtils.classname(clazz));
        classInfo.setClassInfo(StringUtils.classname(clazz));
        classInfo.setCodeSource(ClassUtils.getCodeSource(cs));
        classInfo.setInterface(clazz.isInterface());
        classInfo.setAnnotation(clazz.isAnnotation());
        classInfo.setEnum(clazz.isEnum());
        classInfo.setAnonymousClass(clazz.isAnonymousClass());
        classInfo.setArray(clazz.isArray());
        classInfo.setLocalClass(clazz.isLocalClass());
        classInfo.setMemberClass(clazz.isMemberClass());
        classInfo.setPrimitive(clazz.isPrimitive());
        classInfo.setSynthetic(clazz.isSynthetic());
        classInfo.setSimpleName(clazz.getSimpleName());
        classInfo.setModifier(StringUtils.modifier(clazz.getModifiers(), ','));
        classInfo.setAnnotations(TypeRenderUtils.getAnnotations(clazz));
        classInfo.setInterfaces(TypeRenderUtils.getInterfaces(clazz));
        classInfo.setSuperClass(TypeRenderUtils.getSuperClass(clazz));
        classInfo.setClassloader(TypeRenderUtils.getClassloader(clazz));
        classInfo.setClassLoaderHash(StringUtils.classLoaderHash(clazz));
        if (withFields) {
            classInfo.setFields(TypeRenderUtils.getFields(clazz, expand));
        }
        return classInfo;
    }

    /** 构建仅含名称与 ClassLoader 信息的精简 {@link ClassVO} */
    public static ClassVO createSimpleClassInfo(Class clazz) {
        ClassVO classInfo = new ClassVO();
        fillSimpleClassVO(clazz, classInfo);
        return classInfo;
    }

    /** 向已有 {@link ClassVO} 填充类名、ClassLoader 描述与 hash */
    public static void fillSimpleClassVO(Class clazz, ClassVO classInfo) {
        classInfo.setName(StringUtils.classname(clazz));
        classInfo.setClassloader(TypeRenderUtils.getClassloader(clazz));
        classInfo.setClassLoaderHash(StringUtils.classLoaderHash(clazz));
    }

    /**
     * 从 {@link Method} 构建 {@link MethodVO}。
     *
     * @param method 反射 Method
     * @param clazz 声明类
     * @param detail true 时填充修饰符、参数、返回值等详情
     */
    public static MethodVO createMethodInfo(Method method, Class clazz, boolean detail) {
        MethodVO methodVO = new MethodVO();
        methodVO.setDeclaringClass(clazz.getName());
        methodVO.setMethodName(method.getName());
        methodVO.setDescriptor(Type.getMethodDescriptor(method));
        methodVO.setConstructor(false);
        if (detail) {
            methodVO.setModifier(StringUtils.modifier(method.getModifiers(), ','));
            methodVO.setAnnotations(TypeRenderUtils.getAnnotations(method.getDeclaredAnnotations()));
            methodVO.setParameters(getClassNameList(method.getParameterTypes()));
            methodVO.setReturnType(StringUtils.classname(method.getReturnType()));
            methodVO.setExceptions(getClassNameList(method.getExceptionTypes()));
            methodVO.setClassLoaderHash(StringUtils.classLoaderHash(clazz));
        }
        return methodVO;
    }

    /**
     * 从 {@link Constructor} 构建 {@link MethodVO}（methodName 固定为 {@code <init>}）。
     *
     * @param constructor 反射构造器
     * @param clazz 声明类
     * @param detail 是否填充详细元数据
     */
    public static MethodVO createMethodInfo(Constructor constructor, Class clazz, boolean detail) {
        MethodVO methodVO = new MethodVO();
        methodVO.setDeclaringClass(clazz.getName());
        methodVO.setDescriptor(Type.getConstructorDescriptor(constructor));
        methodVO.setMethodName("<init>");
        methodVO.setConstructor(true);
        if (detail) {
            methodVO.setModifier(StringUtils.modifier(constructor.getModifiers(), ','));
            methodVO.setAnnotations(TypeRenderUtils.getAnnotations(constructor.getDeclaredAnnotations()));
            methodVO.setParameters(getClassNameList(constructor.getParameterTypes()));
            methodVO.setExceptions(getClassNameList(constructor.getExceptionTypes()));
            methodVO.setClassLoaderHash(StringUtils.classLoaderHash(clazz));
        }
        return methodVO;
    }

    /** 将方法 VO 渲染为终端详情表格 */
    public static Element renderMethod(MethodVO method) {
        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);
        table.row(label("declaring-class").style(bold.bold()), label(method.getDeclaringClass()))
                .row(label("method-name").style(bold.bold()), label(method.getMethodName()).style(bold.bold()))
                .row(label("modifier").style(bold.bold()), label(method.getModifier()))
                .row(label("annotation").style(bold.bold()), label(TypeRenderUtils.drawAnnotation(method.getAnnotations())))
                .row(label("parameters").style(bold.bold()), label(TypeRenderUtils.drawParameters(method.getParameters())))
                .row(label("return").style(bold.bold()), label(method.getReturnType()))
                .row(label("exceptions").style(bold.bold()), label(TypeRenderUtils.drawExceptions(method.getExceptions())))
                .row(label("classLoaderHash").style(bold.bold()), label(method.getClassLoaderHash()));
        return table;
    }

    /** 将构造器 VO 渲染为终端详情表格 */
    public static Element renderConstructor(MethodVO constructor) {
        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);
        table.row(label("declaring-class").style(bold.bold()), label(constructor.getDeclaringClass()))
                .row(label("constructor-name").style(bold.bold()), label("<init>").style(bold.bold()))
                .row(label("modifier").style(bold.bold()), label(constructor.getModifier()))
                .row(label("annotation").style(bold.bold()), label(TypeRenderUtils.drawAnnotation(constructor.getAnnotations())))
                .row(label("parameters").style(bold.bold()), label(TypeRenderUtils.drawParameters(constructor.getParameters())))
                .row(label("exceptions").style(bold.bold()), label(TypeRenderUtils.drawExceptions(constructor.getExceptions())))
                .row(label("classLoaderHash").style(bold.bold()), label(constructor.getClassLoaderHash()));
        return table;
    }

    /** 将 Class 数组转为可读类名数组 */
    public static String[] getClassNameList(Class[] classes) {
        List<String> list = new ArrayList<String>();
        for (Class anInterface : classes) {
            list.add(StringUtils.classname(anInterface));
        }
        return list.toArray(new String[0]);
    }

    /** 批量将 Class 集合转为 {@link ClassVO} 列表 */
    public static List<ClassVO> createClassVOList(Collection<Class<?>> matchedClasses) {
        List<ClassVO> classVOs = new ArrayList<ClassVO>(matchedClasses.size());
        for (Class<?> aClass : matchedClasses) {
            ClassVO classVO = createSimpleClassInfo(aClass);
            classVOs.add(classVO);
        }
        return classVOs;
    }

    /** 从 ClassLoader 构建 {@link ClassLoaderVO}（含 hash、名称、父加载器） */
    public static ClassLoaderVO createClassLoaderVO(ClassLoader classLoader) {
        ClassLoaderVO classLoaderVO = new ClassLoaderVO();
        classLoaderVO.setHash(classLoaderHash(classLoader));
        classLoaderVO.setName(classLoader==null?"BootstrapClassLoader":classLoader.toString());
        ClassLoader parent = classLoader == null ? null : classLoader.getParent();
        classLoaderVO.setParent(parent==null?null:parent.toString());
        return classLoaderVO;
    }

    /**
     * 将 ClassLoader 描述文本中的换行、制表符转义为可单行展示的形式。
     *
     * @param value 原始 toString 等文本
     * @return 转义后的字符串；null 入参返回 null
     */
    public static String formatClassLoaderText(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\r\n", "\\n")
                .replace("\r", "\\n")
                .replace("\n", "\\n")
                .replace("\t", "    ");
    }

    /** 批量构建 ClassLoaderVO 列表 */
    public static List<ClassLoaderVO> createClassLoaderVOList(Collection<ClassLoader> classLoaders) {
        List<ClassLoaderVO> classLoaderVOList = new ArrayList<ClassLoaderVO>();
        for (ClassLoader classLoader : classLoaders) {
            classLoaderVOList.add(createClassLoaderVO(classLoader));
        }
        return classLoaderVOList;
    }

    /** 取类所属 ClassLoader 的十六进制 hash；无 ClassLoader 时返回 {@code "null"} */
    public static String classLoaderHash(Class<?> clazz) {
        if (clazz == null || clazz.getClassLoader() == null) {
            return "null";
        }
        return Integer.toHexString(clazz.getClassLoader().hashCode());
    }

    /** 取 ClassLoader 的十六进制 hash；null 时返回 {@code "null"} */
    public static String classLoaderHash(ClassLoader classLoader) {
        if (classLoader == null ) {
            return "null";
        }
        return Integer.toHexString(classLoader.hashCode());
    }

    /** 将匹配的类列表渲染为 NAME / HASHCODE / CLASSLOADER 三列表格 */
    public static Element renderMatchedClasses(Collection<ClassVO> matchedClasses) {
        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);
        table.row(new LabelElement("NAME").style(Decoration.bold.bold()),
                new LabelElement("HASHCODE").style(Decoration.bold.bold()),
                new LabelElement("CLASSLOADER").style(Decoration.bold.bold()));

        for (ClassVO c : matchedClasses) {
            table.row(label(c.getName()),
                    label(c.getClassLoaderHash()).style(Decoration.bold.fg(Color.red)),
                    TypeRenderUtils.drawClassLoader(c));
        }
        return table;
    }

}
