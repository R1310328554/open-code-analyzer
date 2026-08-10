// Prometheus 浮点字符串互转：解析与格式化 +Inf/-Inf 及普通 parseFloat 数值。

export const parsePrometheusFloat = (str: string): number => {
  switch (str) {
    case "+Inf":
      return Infinity;
    case "-Inf":
      return -Infinity;
    default:
      return parseFloat(str);
  }
};

// formatPrometheusFloat 将 JS number 写回 +Inf/-Inf 或 toString，用于表格展示。
export const formatPrometheusFloat = (num: number): string => {
  switch (num) {
    case Infinity:
      return "+Inf";
    case -Infinity:
      return "-Inf";
    default:
      return num.toString();
  }
};
