package com.taobao.arthas.core.util;

import com.taobao.arthas.core.command.model.ClassDetailVO;
import com.taobao.arthas.core.command.model.ClassVO;
import com.taobao.arthas.core.command.model.FieldVO;
import com.taobao.arthas.core.command.model.ObjectVO;
import com.taobao.arthas.core.view.ObjectView;
import com.taobao.text.ui.Element;
import com.taobao.text.ui.TableElement;
import com.taobao.text.ui.TreeElement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static com.taobao.text.ui.Element.label;

/**
 * 类/字段/方法信息的文本与 TUI 树形渲染工具。
 * <p>将反射元数据格式化为 sc -d、jad 等命令的表格与树形输出。</p>
 *
 * @author beiwei30 on 24/11/2016.
 */
public class TypeRenderUtils {

    /** 逗号连接类实现的全部接口名。 */
    public static String drawInterface(Class<?> clazz) {
        return StringUtils.concat(",", clazz.getInterfaces());
    }

    /** 方法参数类型列表，换行分隔。 */
    public static String drawParameters(Method method) {
        return StringUtils.concat("\n", method.getParameterTypes());
    }

    /** 构造器参数类型列表，换行分隔。 */
    public static String drawParameters(Constructor constructor) {
        return StringUtils.concat("\n", constructor.getParameterTypes());
    }

    /** 已格式化的参数类型名数组，换行连接。 */
    public static String drawParameters(String[] parameterTypes) {
        return StringUtils.concat("\n", parameterTypes);
    }

    /** 方法返回类型的可读类名。 */
    public static String drawReturn(Method method) {
        return StringUtils.classname(method.getReturnType());
    }

    /** 方法 throws 子句中的异常类型，换行分隔。 */
    public static String drawExceptions(Method method) {
        return StringUtils.concat("\n", method.getExceptionTypes());
    }

    /** 构造器声明异常，换行分隔。 */
    public static String drawExceptions(Constructor constructor) {
        return StringUtils.concat("\n", constructor.getExceptionTypes());
    }

    /** 异常类型名数组，换行连接。 */
    public static String drawExceptions(String[] exceptionTypes) {
        return StringUtils.concat("\n", exceptionTypes);
    }

    /** 父类继承链渲染为树形 {@link Element}。 */
    public static Element drawSuperClass(ClassDetailVO clazz) {
        return drawTree(clazz.getSuperClass());
    }

    /** ClassLoader 层级链格式化为树。 */
    public static Element drawClassLoader(ClassVO clazz) {
        String[] classloaders = clazz.getClassloader();
        if (classloaders == null) {
            return drawTree(new String[0]);
        }
        String[] formattedClassloaders = new String[classloaders.length];
        for (int i = 0; i < classloaders.length; i++) {
            formattedClassloaders[i] = ClassUtils.formatClassLoaderText(classloaders[i]);
        }
        return drawTree(formattedClassloaders);
    }

    /** 将字符串数组逐级嵌套为 {@link TreeElement}。 */
    public static Element drawTree(String[] nodes) {
        TreeElement root = new TreeElement();
        TreeElement parent = root;
        for (String node : nodes) {
            TreeElement child = new TreeElement(label(node));
            parent.addChild(child);
            parent = child;
        }
        return root;
    }

    /** 渲染类的字段表格（含注解与 static 字段值）。 */
    public static Element drawField(ClassDetailVO clazz) {
        TableElement fieldsTable = new TableElement(1).leftCellPadding(0).rightCellPadding(0);
        FieldVO[] fields = clazz.getFields();
        if (fields == null || fields.length == 0) {
            return fieldsTable;
        }

        for (FieldVO field : fields) {
            TableElement fieldTable = new TableElement().leftCellPadding(0).rightCellPadding(1);
            fieldTable.row("name", field.getName())
                    .row("type", field.getType())
                    .row("modifier", field.getModifier());

            String[] annotations = field.getAnnotations();
            if (annotations != null && annotations.length > 0) {
                fieldTable.row("annotation", drawAnnotation(annotations));
            }

            if (field.isStatic()) {
                ObjectVO objectVO = field.getValue();
                Object o = objectVO.needExpand() ? new ObjectView(objectVO).draw() : objectVO.getObject();
                fieldTable.row("value", StringUtils.objectToString(o));
            }

            fieldTable.row(label(""));
            fieldsTable.row(fieldTable);
        }

        return fieldsTable;
    }

    /** 逗号连接注解类型名。 */
    public static String drawAnnotation(String... annotations) {
        return StringUtils.concat(",", annotations);
    }

    /** 获取类声明注解的可读名数组。 */
    public static String[] getAnnotations(Class<?> clazz) {
        return getAnnotations(clazz.getDeclaredAnnotations());
    }

    /** 注解实例数组转类型名列表。 */
    public static String[] getAnnotations(Annotation[] annotations) {
        List<String> list = new ArrayList<String>();
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                list.add(StringUtils.classname(annotation.annotationType()));
            }
        }
        return list.toArray(new String[0]);
    }

    /** 类直接实现的接口全名列表。 */
    public static String[] getInterfaces(Class clazz) {
        Class[] interfaces = clazz.getInterfaces();
        return ClassUtils.getClassNameList(interfaces);
    }

    /** 从直接父类到 Object 的继承链。 */
    public static String[] getSuperClass(Class clazz) {
        List<String> list = new ArrayList<String>();
        Class<?> superClass = clazz.getSuperclass();
        if (null != superClass) {
            list.add(StringUtils.classname(superClass));
            while (true) {
                superClass = superClass.getSuperclass();
                if (null == superClass) {
                    break;
                }
                list.add(StringUtils.classname(superClass));
            }
        }
        return list.toArray(new String[0]);
    }

    /** 从定义该类的 loader 到 bootstrap 的层级描述。 */
    public static String[] getClassloader(Class clazz) {
        List<String> list = new ArrayList<String>();
        ClassLoader loader = clazz.getClassLoader();
        if (null != loader) {
            list.add(loader.toString());
            while (true) {
                loader = loader.getParent();
                if (null == loader) {
                    break;
                }
                list.add(loader.toString());
            }
        }
        return list.toArray(new String[0]);
    }

    /** 反射收集声明字段并封装为 {@link FieldVO} 数组。 */
    public static FieldVO[] getFields(Class clazz, Integer expand) {
        Field[] fields = clazz.getDeclaredFields();
        if (fields.length == 0) {
            return new FieldVO[0];
        }

        List<FieldVO> list = new ArrayList<FieldVO>(fields.length);
        for (Field field : fields) {
            FieldVO fieldVO = new FieldVO();
            fieldVO.setName(field.getName());
            fieldVO.setType(StringUtils.classname(field.getType()));
            fieldVO.setModifier(StringUtils.modifier(field.getModifiers(), ','));
            fieldVO.setAnnotations(getAnnotations(field.getAnnotations()));
            if (Modifier.isStatic(field.getModifiers())) {
                fieldVO.setStatic(true);
                fieldVO.setValue(new ObjectVO(getFieldValue(field), expand));
            } else {
                fieldVO.setStatic(false);
            }
            list.add(fieldVO);
        }
        return list.toArray(new FieldVO[0]);
    }

    /** 读取 static 字段值，临时放宽 accessible。 */
    private static Object getFieldValue(Field field) {
        final boolean isAccessible = field.isAccessible();
        try {
            field.setAccessible(true);
            Object value = field.get(null);
            return value;
        } catch (IllegalAccessException e) {
            // no op
        } finally {
            field.setAccessible(isAccessible);
        }
        return null;
    }

}
