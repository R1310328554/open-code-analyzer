"""Chinese JavaDoc replacements for Spring Boot wave14a system classes."""

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ApplicationPid.java": [
        (
            "/**\n * An application process ID.\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * 应用进程 ID。\n * 通过 {@link ProcessHandle} 获取当前 JVM 进程标识，并支持写入 PID 文件。\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Return if the application PID is available.\n\t * @return {@code true} if the PID is available\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 返回应用 PID 是否可用。\n\t *\n\t * @return {@code true} if the PID is available 若 PID 可用则为 {@code true}\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Return the application PID as a {@link Long}.\n\t * @return the application PID or {@code null}\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 以 {@link Long} 形式返回应用 PID。\n\t *\n\t * @return the application PID or {@code null} 应用 PID 或 {@code null}\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Write the PID to the specified file.\n\t * @param file the PID file\n\t * @throws IllegalStateException if no PID is available.\n\t * @throws IOException if the file cannot be written\n\t */",
            "\t/**\n\t * 将 PID 写入指定文件。\n\t * 若文件已存在则校验写权限；父目录不存在时会自动创建。\n\t *\n\t * @param file the PID file PID 文件\n\t * @throws IllegalStateException if no PID is available. 若无可用 PID\n\t * @throws IOException if the file cannot be written 若无法写入文件\n\t */",
        ),
        (
            "\t\t\t// Assume that we can\n",
            "\t\t\t// 假定可写\n",
        ),
    ],
    "ApplicationTemp.java": [
        (
            "/**\n * Provides access to an application specific temporary directory. Generally speaking\n * different Spring Boot applications will get different locations, however, simply\n * restarting an application will give the same location.\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * 提供应用专属临时目录的访问。\n * 不同 Spring Boot 应用通常获得不同位置；仅重启同一应用时位置保持不变。\n * 目录名由应用来源、工作目录、类路径等信息的 SHA-1 哈希决定。\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link ApplicationTemp} instance.\n\t */",
            "\t/**\n\t * 创建新的 {@link ApplicationTemp} 实例。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link ApplicationTemp} instance for the specified source class.\n\t * @param sourceClass the source class or {@code null}\n\t */",
            "\t/**\n\t * 为指定源类创建新的 {@link ApplicationTemp} 实例。\n\t *\n\t * @param sourceClass the source class or {@code null} 源类或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Return the directory to be used for application specific temp files.\n\t * @return the application temp directory\n\t */",
            "\t/**\n\t * 返回用于应用专属临时文件的目录。\n\t *\n\t * @return the application temp directory 应用临时目录\n\t */",
        ),
        (
            "\t/**\n\t * Return a subdirectory of the application temp.\n\t * @param subDir the subdirectory name\n\t * @return a subdirectory\n\t */",
            "\t/**\n\t * 返回应用临时目录下的子目录。\n\t *\n\t * @param subDir the subdirectory name 子目录名称\n\t * @return a subdirectory 子目录\n\t */",
        ),
        (
            "\t\t\t\t\t// Ownership check not supported. Continue.\n",
            "\t\t\t\t\t// 不支持所有权检查，继续。\n",
        ),
    ],
    "JavaVersion.java": [
        (
            "/**\n * Known Java versions.\n *\n * @author Oliver Gierke\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 2.0.0\n */",
            "/**\n * 已知的 Java 版本枚举。\n * 通过检测各版本特有 API 是否存在来推断当前运行时版本。\n *\n * @author Oliver Gierke\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Java 17.\n\t * @since 2.5.3\n\t */",
            "\t/**\n\t * Java 17。\n\t * @since 2.5.3\n\t */",
        ),
        (
            "\t/**\n\t * Java 18.\n\t * @since 2.5.11\n\t */",
            "\t/**\n\t * Java 18。\n\t * @since 2.5.11\n\t */",
        ),
        (
            "\t/**\n\t * Java 19.\n\t * @since 2.6.12\n\t */",
            "\t/**\n\t * Java 19。\n\t * @since 2.6.12\n\t */",
        ),
        (
            "\t/**\n\t * Java 20.\n\t * @since 2.7.13\n\t */",
            "\t/**\n\t * Java 20。\n\t * @since 2.7.13\n\t */",
        ),
        (
            "\t/**\n\t * Java 21.\n\t * @since 2.7.16\n\t */",
            "\t/**\n\t * Java 21。\n\t * @since 2.7.16\n\t */",
        ),
        (
            "\t/**\n\t * Java 22.\n\t * @since 3.2.4\n\t */",
            "\t/**\n\t * Java 22。\n\t * @since 3.2.4\n\t */",
        ),
        (
            "\t/**\n\t * Java 23.\n\t * @since 3.2.9\n\t */",
            "\t/**\n\t * Java 23。\n\t * @since 3.2.9\n\t */",
        ),
        (
            "\t/**\n\t * Java 24.\n\t * @since 3.4.3\n\t */",
            "\t/**\n\t * Java 24。\n\t * @since 3.4.3\n\t */",
        ),
        (
            "\t/**\n\t * Java 25.\n\t * @since 3.5.7\n\t */",
            "\t/**\n\t * Java 25。\n\t * @since 3.5.7\n\t */",
        ),
        (
            "\t/**\n\t * Java 26.\n\t * @since 4.0.3\n\t */",
            "\t/**\n\t * Java 26。\n\t * @since 4.0.3\n\t */",
        ),
        (
            "\t/**\n\t * Returns the {@link JavaVersion} of the current runtime.\n\t * @return the {@link JavaVersion}\n\t */",
            "\t/**\n\t * 返回当前运行时的 {@link JavaVersion}。\n\t * 从高版本向低版本依次检测，未匹配时默认 {@link #SEVENTEEN}。\n\t *\n\t * @return the {@link JavaVersion} Java 版本\n\t */",
        ),
        (
            "\t/**\n\t * Return if this version is equal to or newer than a given version.\n\t * @param version the version to compare\n\t * @return {@code true} if this version is equal to or newer than {@code version}\n\t */",
            "\t/**\n\t * 判断本版本是否等于或高于给定版本。\n\t *\n\t * @param version the version to compare 待比较的版本\n\t * @return {@code true} if this version is equal to or newer than {@code version} 若本版本不低于给定版本则为 {@code true}\n\t */",
        ),
        (
            "\t/**\n\t * Return if this version is older than a given version.\n\t * @param version the version to compare\n\t * @return {@code true} if this version is older than {@code version}\n\t */",
            "\t/**\n\t * 判断本版本是否低于给定版本。\n\t *\n\t * @param version the version to compare 待比较的版本\n\t * @return {@code true} if this version is older than {@code version} 若本版本低于给定版本则为 {@code true}\n\t */",
        ),
    ],
    "SystemProperties.java": [
        (
            "/**\n * Access to system properties.\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * 系统属性访问工具。\n * 按顺序尝试 {@link System#getProperty(String)} 与 {@link System#getenv(String)}，\n * 解析失败时输出到 {@link System#err} 并继续尝试下一候选名。\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
    ],
}
