// Prometheus 热力图辅助：识别 histogram heatmap 查询结果并将 cumulative 桶转为增量值。

import { DataTableProps } from './DataTable';
import { GraphProps, GraphSeries } from './Graph';

export function isHeatmapData(data: DataTableProps['data']) {
  if (data?.resultType === 'scalar' || data?.resultType === 'string' || !data?.result?.length || data?.result?.length < 2) {
    return false;
  }
// 将 result 断言为 GraphProps 类型以便访问 metric 字段。
  // Type assertion to prevent TS2349 error.
  const result = data.result as GraphProps['data']['result'];
  const firstLabels = Object.keys(result[0].metric).filter((label) => label !== 'le');
  return result.every(({ metric }) => {
    const labels = Object.keys(metric).filter((label) => label !== 'le');
    const allLabelsMatch = labels.every((label) => metric[label] === result[0].metric[label]);
    return metric.le && labels.length === firstLabels.length && allLabelsMatch;
  });
}

// prepareHeatmapData 按 le 排序后做相邻 cumulative 差分，得到各桶独立计数。
export function prepareHeatmapData(buckets: GraphSeries[]) {
  if (!buckets.every((a) => a.labels.le)) {
    return buckets;
  }

  const sortedBuckets = buckets.sort((a, b) => promValueToNumber(a.labels.le) - promValueToNumber(b.labels.le));
  const result: GraphSeries[] = [];

  for (let i = 0; i < sortedBuckets.length; i++) {
    const values = [];
    const { data, labels, color } = sortedBuckets[i];

    for (const [timestamp, value] of data) {
      const prevVal = sortedBuckets[i - 1]?.data.find((v) => v[0] === timestamp)?.[1] || 0;
      const newVal = Number(value) - prevVal;
      values.push([Number(timestamp), newVal]);
    }

    result.push({
      data: values,
      labels,
      color,
      index: i,
    });
  }
  return result;
}

// promValueToNumber 解析 Prometheus 特殊浮点字符串（NaN/±Inf）为 JS 数值。
export function promValueToNumber(s: string) {
  switch (s) {
    case 'NaN':
      return NaN;
    case 'Inf':
    case '+Inf':
      return Infinity;
    case '-Inf':
      return -Infinity;
    default:
      return parseFloat(s);
  }
}
