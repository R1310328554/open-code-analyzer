// uPlot 堆叠图辅助：将多序列累加为 stacked 数据并配置 band 填充与动态重堆叠。

import { lighten } from "@mantine/core";
import uPlot, { AlignedData, TypedArray } from "uplot";

// stack 函数改编自 uPlot 官方堆叠示例，对非 omit 序列做前缀和并生成 band 区间。
// Stacking code adapted from https://leeoniya.github.io/uPlot/demos/stack.js
function stack(
  data: uPlot.AlignedData,
  omit: (i: number) => boolean
): { data: uPlot.AlignedData; bands: uPlot.Band[] } {
  const data2: uPlot.AlignedData = [];
  let bands: uPlot.Band[] = [];
  const d0Len = data[0].length;
  const accum = Array(d0Len);

  for (let i = 0; i < d0Len; i++) {
    accum[i] = 0;
  }

  for (let i = 1; i < data.length; i++) {
    data2.push(
      (omit(i)
        ? data[i]
        : data[i].map((v, i) => (accum[i] += +(v || 0)))) as TypedArray
    );
  }

  for (let i = 1; i < data.length; i++) {
    if (!omit(i)) {
      bands.push({
        series: [data.findIndex((_s, j) => j > i && !omit(j)), i],
      });
    }
  }

  bands = bands.filter((b) => b.series[1] > -1);

  return {
    data: [data[0]].concat(data2) as AlignedData,
    bands,
  };
}

// setStackedOpts 写入 bands/cursor/series 填充，并将 Y 轴下限固定为 0。
export function setStackedOpts(opts: uPlot.Options, data: uPlot.AlignedData) {
  const stacked = stack(data, (_i) => false);
  opts.bands = stacked.bands;

  opts.cursor = opts.cursor || {};
  opts.cursor.dataIdx = (_u, seriesIdx, closestIdx, _xValue) =>
    data[seriesIdx][closestIdx] == null ? null : closestIdx;

  opts.series.forEach((s) => {
    // s.value = (u, v, si, i) => data[si][i];

    s.points = s.points || {};

    if (s.stroke) {
      s.fill = lighten(s.stroke as string, 0.6);
    }

// points.filter 基于原始未堆叠数据决定哪些索引绘制散点。
    // scan raw unstacked data to return only real points
    s.points.filter = (
      _self: uPlot,
      seriesIdx: number,
      show: boolean,
      _gaps?: null | number[][]
    ): number[] | null => {
      if (show) {
        const pts: number[] = [];
        data[seriesIdx].forEach((v, i) => {
          if (v != null) {
            pts.push(i);
          }
        });
        return pts;
      }
      return null;
    };
  });

// 堆叠模式下 Y 轴 range 从 0 起算，使总和而非底层序列作为视觉基线。
  // force 0 to be the sum minimum this instead of the bottom series
  opts.scales = opts.scales || {};
  opts.scales.y = {
    range: (_u, _min, max) => {
      const minMax = uPlot.rangeNum(0, max, 0.1, true);
      return [0, minMax[1]];
    },
  };

// setSeries hook 在显隐切换时按当前 show 状态重新 stack 并刷新 bands/data。
  // restack on toggle (but not on focus/hover)
  opts.hooks = opts.hooks || {};
  opts.hooks.setSeries = opts.hooks.setSeries || [];
  opts.hooks.setSeries.push((u, _i, opts) => {
    if (opts.show != null) {
      const stacked = stack(data, (i) => !u.series[i].show);
      u.delBand(null);
      stacked.bands.forEach((b) => u.addBand(b));
      u.setData(stacked.data);
    }
  });

  return { opts, data: stacked.data };
}
