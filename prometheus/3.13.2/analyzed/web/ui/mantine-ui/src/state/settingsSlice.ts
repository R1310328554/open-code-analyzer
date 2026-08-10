// 全局设置 Redux slice：服务端注入常量、路径前缀推断与用户偏好持久化。

import { PayloadAction, createSlice } from "@reduxjs/toolkit";
import { useAppSelector } from "./hooks";
import { initializeFromLocalStorage } from "./initializeFromLocalStorage";

interface Settings {
  consolesLink: string | null;
  lookbackDelta: string;
  agentMode: boolean;
  ready: boolean;
  pathPrefix: string;
  useLocalTime: boolean;
  enableQueryHistory: boolean;
  enableAutocomplete: boolean;
  enableSyntaxHighlighting: boolean;
  enableLinter: boolean;
  showAnnotations: boolean;
  showQueryWarnings: boolean;
  showQueryInfoNotices: boolean;
  ruleGroupsPerPage: number;
  alertGroupsPerPage: number;
}

// 以下全局变量在 index.html 声明，由 Prometheus 服务端渲染 bundle 时替换占位符。
// Declared/defined in public/index.html, value replaced by Prometheus when serving bundle.
declare const GLOBAL_CONSOLES_LINK: string;
declare const GLOBAL_AGENT_MODE: string;
declare const GLOBAL_READY: string;
declare const GLOBAL_LOOKBACKDELTA: string;

export const localStorageKeyUseLocalTime = "settings.useLocalTime";
export const localStorageKeyEnableQueryHistory = "settings.enableQueryHistory";
export const localStorageKeyEnableAutocomplete = "settings.enableAutocomplete";
export const localStorageKeyEnableSyntaxHighlighting =
  "settings.enableSyntaxHighlighting";
export const localStorageKeyEnableLinter = "settings.enableLinter";
export const localStorageKeyShowAnnotations = "settings.showAnnotations";
export const localStorageKeyShowQueryWarnings = "settings.showQueryWarnings";
export const localStorageKeyShowQueryInfoNotices =
  "settings.showQueryInfoNotices";
export const localStorageKeyRuleGroupsPerPage = "settings.ruleGroupsPerPage";
export const localStorageKeyAlertGroupsPerPage = "settings.alertGroupsPerPage";

// getPathPrefix 通过剥离已知页面路径后缀推断 UI 根路径前缀，适配反代部署。
// This dynamically/generically determines the pathPrefix by stripping the first known
// endpoint suffix from the window location path. It works out of the box for both direct
// hosting and reverse proxy deployments with no additional configurations required.
const getPathPrefix = (path: string) => {
  if (path.endsWith("/")) {
    path = path.slice(0, -1);
  }

  const pagePaths = [
    "/query",
    "/alerts",
    "/targets",
    "/rules",
    "/service-discovery",
    "/status",
    "/tsdb-status",
    "/flags",
    "/config",
    "/alertmanager-discovery",
    "/agent",
  ];

  const pagePath = pagePaths.find((p) => path.endsWith(p));
  return path.slice(0, path.length - (pagePath || "").length);
};

// initialState 合并服务端全局变量与 localStorage 中的用户偏好默认值。
export const initialState: Settings = {
  consolesLink:
    GLOBAL_CONSOLES_LINK === "CONSOLES_LINK_PLACEHOLDER" ||
    GLOBAL_CONSOLES_LINK === "" ||
    GLOBAL_CONSOLES_LINK === null
      ? null
      : GLOBAL_CONSOLES_LINK,
  agentMode: GLOBAL_AGENT_MODE === "true",
  ready: GLOBAL_READY === "true",
  lookbackDelta:
    GLOBAL_LOOKBACKDELTA === "LOOKBACKDELTA_PLACEHOLDER" ||
    GLOBAL_LOOKBACKDELTA === null
      ? ""
      : GLOBAL_LOOKBACKDELTA,
  pathPrefix: getPathPrefix(window.location.pathname),
  useLocalTime: initializeFromLocalStorage<boolean>(
    localStorageKeyUseLocalTime,
    false
  ),
  enableQueryHistory: initializeFromLocalStorage<boolean>(
    localStorageKeyEnableQueryHistory,
    false
  ),
  enableAutocomplete: initializeFromLocalStorage<boolean>(
    localStorageKeyEnableAutocomplete,
    true
  ),
  enableSyntaxHighlighting: initializeFromLocalStorage<boolean>(
    localStorageKeyEnableSyntaxHighlighting,
    true
  ),
  enableLinter: initializeFromLocalStorage<boolean>(
    localStorageKeyEnableLinter,
    true
  ),
  showAnnotations: initializeFromLocalStorage<boolean>(
    localStorageKeyShowAnnotations,
    false
  ),
  showQueryWarnings: initializeFromLocalStorage<boolean>(
    localStorageKeyShowQueryWarnings,
    true
  ),
  showQueryInfoNotices: initializeFromLocalStorage<boolean>(
    localStorageKeyShowQueryInfoNotices,
    true
  ),
  ruleGroupsPerPage: initializeFromLocalStorage<number>(
    localStorageKeyRuleGroupsPerPage,
    10
  ),
  alertGroupsPerPage: initializeFromLocalStorage<number>(
    localStorageKeyAlertGroupsPerPage,
    10
  ),
};

// settingsSlice 提供 updateSettings 批量更新部分设置字段。
export const settingsSlice = createSlice({
  name: "settings",
  initialState,
  reducers: {
    updateSettings: (state, { payload }: PayloadAction<Partial<Settings>>) => {
      Object.assign(state, payload);
    },
  },
});

export const { updateSettings } = settingsSlice.actions;

// useSettings 便捷 hook，订阅整个 settings 子树。
export const useSettings = () => {
  return useAppSelector((state) => state.settings);
};

export default settingsSlice.reducer;
