package org.keycloak.db.compatibility.verifier;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Keycloak 数据迁移类扫描器：在指定包下发现所有 {@code Migration} 实现类的 FQCN。
 * <p>
 * 支持 IDE 文件系统与 JAR 类路径两种布局，供 db-compatibility-verifier 与代码库中的
 * supported/unsupported JSON 快照比对。
 */
record KeycloakMigrationParser(ClassLoader classLoader, String packageName) {

    /**
     * 扫描包内全部顶层迁移类（排除匿名类、lambda 与内部类）。
     *
     * @return 迁移类 FQCN 集合
     * @throws IOException 读取类路径资源失败时
     */
    Set<Migration> discoverAllMigrations() throws IOException {
        return findAllClassNamesInPackage(classLoader, packageName)
              .filter(s -> {
                  var parts = s.split("\\.");
                  var clazz = parts[parts.length - 1];
                  // 忽略匿名类、lambda 与内部类（类名含 $）
                  return !clazz.contains("$");
              })
              .map(Migration::new)
              .collect(Collectors.toSet());
    }

    /** 在类路径上递归枚举指定包下的 {@code .class} 文件名并转为 FQCN。 */
    private Stream<String> findAllClassNamesInPackage(ClassLoader classLoader, String packageName) throws IOException {
         if (packageName == null) {
             return Stream.of();
         }

        List<String> classNames = new ArrayList<>();
        String path = packageName.replace('.', '/');

        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            if (resource.getProtocol().equals("file")) {
                URI uri;
                try {
                    uri = resource.toURI();
                } catch (URISyntaxException e) {
                    // 正常不应发生
                    throw new IllegalStateException(e);
                }
                classNames.addAll(findNamesInDirectory(new File(uri), packageName));
            } else if (resource.getProtocol().equals("jar")) {
                classNames.addAll(findNamesInJar(resource, path));
            }
        }
        return classNames.stream();
    }

    /** 文件系统（IDE）下递归扫描目录中的 {@code .class} 文件。 */
    private static List<String> findNamesInDirectory(File directory, String packageName) {
        List<String> classNames = new ArrayList<>();
        if (!directory.exists()) {
            return classNames;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归进入子包
                    classNames.addAll(findNamesInDirectory(file, packageName + "." + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    // 去掉 .class 后缀并拼成完整类名
                    String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                    classNames.add(className);
                }
            }
        }
        return classNames;
    }

    /** JAR（Maven）内按包路径前缀匹配 {@code .class} 条目。 */
    private static List<String> findNamesInJar(URL resource, String packagePath) throws IOException {
        List<String> classNames = new ArrayList<>();

        JarURLConnection jarConn = (JarURLConnection) resource.openConnection();
        try (JarFile jarFile = jarConn.getJarFile()) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 匹配包路径且为 class 文件；路径末尾加 "/" 避免 com/test 误匹配 com/tester
                if (entryName.startsWith(packagePath + "/") && entryName.endsWith(".class")) {

                    // 路径 "com/example/MyClass.class" -> "com.example.MyClass"
                    String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                    classNames.add(className);
                }
            }
        }
        return classNames;
    }
}
