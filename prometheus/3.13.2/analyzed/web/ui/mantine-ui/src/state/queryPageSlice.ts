// 查询页 Redux slice：多面板 PromQL 表达式、可视化选项、查询历史与 URL 同步。

import { randomId } from "@mantine/hooks";
import { PayloadAction, createSlice } from "@reduxjs/toolkit";
import { encodePanelOptionsToURLParams } from "../pages/query/urlStateEncoding";
import { initializeFromLocalStorage } from "./initializeFromLocalStorage";

export const localStorageKeyQueryHistory = "queryPage.queryHistory";

// GraphDisplayMode 定义图表展示模式：折线、堆叠或热力图。
export enum GraphDisplayMode {
  Lines = "lines",
  Stacked = "stacked",
  Heatmap = "heatmap",
}

export type GraphResolution =
  | {
      type: "auto";
      density: "low" | "medium" | "high";
    }
  | {
      type: "fixed";
// 步长单位为毫秒，用于 range 查询采样间隔。
      step: number; // Resolution step in milliseconds.
    }
  | {
      type: "custom";
      step: number; // Resolution step in milliseconds.
    };

// 根据 UI 分辨率设置与查询时间范围计算实际使用的毫秒步长。
// From the UI settings, compute the effective resolution
// in milliseconds to use for the graph query.
export const getEffectiveResolution = (
  resolution: GraphResolution,
  range: number
) => {
  switch (resolution.type) {
    case "auto": {
      const factor =
        resolution.density === "high"
          ? 750
          : resolution.density === "medium"
            ? 250
            : 100;
      return Math.max(Math.floor(range / factor / 1000) * 1000, 1000);
    }
    case "fixed":
      return resolution.step;
    case "custom":
      return resolution.step;
  }
};

// Visualizer 非判别联合类型，以便切换展示模式时部分保留共享字段。
// NOTE: This is not represented as a discriminated union type
// because we want to preserve and partially share settings while
// switching between display modes.
export interface Visualizer {
  activeTab: "table" | "graph" | "explain";
  endTime: number | null; // Timestamp in milliseconds.
  range: number; // Range in milliseconds.
  resolution: GraphResolution;
  displayMode: GraphDisplayMode;
  showExemplars: boolean;
  yAxisMin: number | null;
}

export type Panel = {
// Panel.id 作为 React 列表稳定 key，避免重排时组件状态错乱。
  // The id is helpful as a stable key for React.
  id: string;
  expr: string;
  showTree: boolean;
  showMetricsExplorer: boolean;
  visualizer: Visualizer;
};

interface QueryPageState {
  panels: Panel[];
  queryHistory: string[];
}

// newDefaultPanel 创建带默认可视化选项的新查询面板。
export const newDefaultPanel = (): Panel => ({
  id: randomId(),
  expr: "",
  showTree: false,
  showMetricsExplorer: false,
  visualizer: {
    activeTab: "table",
    endTime: null,
    range: 3600 * 1000,
    resolution: { type: "auto", density: "medium" },
    displayMode: GraphDisplayMode.Lines,
    showExemplars: false,
    yAxisMin: null,
  },
});

const initialState: QueryPageState = {
  panels: [newDefaultPanel()],
  queryHistory: initializeFromLocalStorage<string[]>(
    localStorageKeyQueryHistory,
    []
  ),
};

// updateURL 将面板状态编码进查询字符串并 pushState，支持分享与刷新恢复。
const updateURL = (panels: Panel[]) => {
  const query = "?" + encodePanelOptionsToURLParams(panels).toString();
  window.history.pushState({}, "", query);
};

export const queryPageSlice = createSlice({
  name: "queryPage",
  initialState,
  reducers: {
    setPanels: (state, { payload }: PayloadAction<Panel[]>) => {
      state.panels = payload;
    },
    addPanel: (state) => {
      state.panels.push(newDefaultPanel());
      updateURL(state.panels);
    },
    duplicatePanel: (
      state,
      { payload }: PayloadAction<{ idx: number; expr: string }>
    ) => {
      const newPanel = {
        ...state.panels[payload.idx],
        id: randomId(),
        expr: payload.expr,
      };
// 复制面板插入到原面板下方，保留可视化配置仅替换表达式。
      // Insert the duplicated panel just below the original panel.
      state.panels.splice(payload.idx + 1, 0, newPanel);
      updateURL(state.panels);
    },
    removePanel: (state, { payload }: PayloadAction<number>) => {
      state.panels.splice(payload, 1);
      updateURL(state.panels);
    },
    setExpr: (
      state,
      { payload }: PayloadAction<{ idx: number; expr: string }>
    ) => {
      state.panels[payload.idx].expr = payload.expr;
      updateURL(state.panels);
    },
    addQueryToHistory: (state, { payload: query }: PayloadAction<string>) => {
      state.queryHistory = [
        query,
        ...state.queryHistory.filter((q) => q !== query),
// 查询历史去重后置顶，最多保留 50 条最近查询。
      ].slice(0, 50);
    },
    setShowTree: (
      state,
      { payload }: PayloadAction<{ idx: number; showTree: boolean }>
    ) => {
      state.panels[payload.idx].showTree = payload.showTree;
      updateURL(state.panels);
    },
    setVisualizer: (
      state,
      { payload }: PayloadAction<{ idx: number; visualizer: Visualizer }>
    ) => {
      state.panels[payload.idx].visualizer = payload.visualizer;
      updateURL(state.panels);
    },
  },
});

export const {
  setPanels,
  addPanel,
  removePanel,
  duplicatePanel,
  setExpr,
  addQueryToHistory,
  setShowTree,
  setVisualizer,
} = queryPageSlice.actions;

export default queryPageSlice.reducer;
