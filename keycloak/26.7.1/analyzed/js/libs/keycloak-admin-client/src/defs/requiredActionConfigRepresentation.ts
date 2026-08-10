/**
 * Required Action 实例配置：键值对形式的运行时参数。
 */
export default interface RequiredActionConfigRepresentation {
  /** 配置项（键为属性名，值为字符串） */
  config?: { [index: string]: string };
}
