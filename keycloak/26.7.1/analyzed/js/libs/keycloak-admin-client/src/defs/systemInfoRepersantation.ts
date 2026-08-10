/**
 * Keycloak 服务器系统信息表示：Admin API 返回的运行时环境与健康状态摘要。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_systeminforepresentation
 */

export default interface SystemInfoRepresentation {
  /** Keycloak 版本号 */
  version?: string;
  /** 服务器当前时间（ISO 8601 字符串） */
  serverTime?: string;
  /** 进程已运行时长（人类可读字符串） */
  uptime?: string;
  /** 进程已运行时长（毫秒） */
  uptimeMillis?: number;
  /** Java 运行时版本 */
  javaVersion?: string;
  /** Java 供应商 */
  javaVendor?: string;
  /** Java 虚拟机名称 */
  javaVm?: string;
  /** Java 虚拟机版本 */
  javaVmVersion?: string;
  /** Java 运行时名称 */
  javaRuntime?: string;
  /** JAVA_HOME 路径 */
  javaHome?: string;
  /** 操作系统名称 */
  osName?: string;
  /** 操作系统 CPU 架构 */
  osArchitecture?: string;
  /** 操作系统版本 */
  osVersion?: string;
  /** 默认文件编码 */
  fileEncoding?: string;
  /** 运行 Keycloak 进程的操作系统用户名 */
  userName?: string;
  /** 进程工作目录 */
  userDir?: string;
  /** JVM 默认时区 ID */
  userTimezone?: string;
  /** JVM 默认区域设置 */
  userLocale?: string;
}
