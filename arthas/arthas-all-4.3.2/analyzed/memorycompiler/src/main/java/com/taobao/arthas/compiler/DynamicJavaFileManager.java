package com.taobao.arthas.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

/**
 * 动态编译用的 {@link JavaFileManager} 装饰器：将编译输出写入内存字节码，
 * 并从自定义 {@link DynamicClassLoader} 中解析 classpath 上的类资源。
 */
public class DynamicJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    /** 需委托给标准文件管理器处理的 location 名称（含 JPMS 模块路径） */
    private static final String[] superLocationNames = { StandardLocation.PLATFORM_CLASS_PATH.name(),
            /** JPMS StandardLocation.SYSTEM_MODULES **/
            "SYSTEM_MODULES" };
    /** 在 ClassLoader 内按包名查找 .class 资源的辅助类 */
    private final PackageInternalsFinder finder;

    /** 承载编译产物并负责加载的自定义类加载器 */
    private final DynamicClassLoader classLoader;
    /** 本次编译会话中生成的内存字节码对象列表 */
    private final List<MemoryByteCode> byteCodes = new ArrayList<MemoryByteCode>();

    public DynamicJavaFileManager(JavaFileManager fileManager, DynamicClassLoader classLoader) {
        super(fileManager);
        this.classLoader = classLoader;
        this.finder = new PackageInternalsFinder(classLoader);
    }

    /**
     * 为编译输出创建或复用 {@link MemoryByteCode}，并注册到 {@link DynamicClassLoader}。
     */
    @Override
    public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className,
                    JavaFileObject.Kind kind, FileObject sibling) throws IOException {

        for (MemoryByteCode byteCode : byteCodes) {
            if (byteCode.getClassName().equals(className)) {
                return byteCode;
            }
        }

        MemoryByteCode innerClass = new MemoryByteCode(className);
        byteCodes.add(innerClass);
        classLoader.registerCompiledSource(innerClass);
        return innerClass;

    }

    @Override
    public ClassLoader getClassLoader(JavaFileManager.Location location) {
        return classLoader;
    }

    /**
     * 推断 JavaFileObject 对应的二进制类名；自定义对象直接返回其类名，其余委托标准管理器。
     */
    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof CustomJavaFileObject) {
            return ((CustomJavaFileObject) file).getClassName();
        } else {
            // 非 CustomJavaFileObject 来自标准文件管理器，交由父类处理
            return super.inferBinaryName(location, file);
        }
    }

    /**
     * 列出指定包下的 JavaFileObject；CLASS_PATH 位置会合并标准 classpath 与 ClassLoader 内资源。
     */
    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds,
                                         boolean recurse) throws IOException {
        if (location instanceof StandardLocation) {
            String locationName = ((StandardLocation) location).name();
            for (String name : superLocationNames) {
                if (name.equals(locationName)) {
                    return super.list(location, packageName, kinds, recurse);
                }
            }
        }

        // 合并标准 classpath 与指定 ClassLoader 中的类文件
        if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
            return new IterableJoin<JavaFileObject>(super.list(location, packageName, kinds, recurse),
                    finder.find(packageName));
        }

        return super.list(location, packageName, kinds, recurse);
    }

    /** 顺序遍历两个 Iterable 的合并视图 */
    static class IterableJoin<T> implements Iterable<T> {
        private final Iterable<T> first, next;

        public IterableJoin(Iterable<T> first, Iterable<T> next) {
            this.first = first;
            this.next = next;
        }

        @Override
        public Iterator<T> iterator() {
            return new IteratorJoin<T>(first.iterator(), next.iterator());
        }
    }

    /** 先耗尽 first 迭代器，再遍历 next */
    static class IteratorJoin<T> implements Iterator<T> {
        private final Iterator<T> first, next;

        public IteratorJoin(Iterator<T> first, Iterator<T> next) {
            this.first = first;
            this.next = next;
        }

        @Override
        public boolean hasNext() {
            return first.hasNext() || next.hasNext();
        }

        @Override
        public T next() {
            if (first.hasNext()) {
                return first.next();
            }
            return next.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
