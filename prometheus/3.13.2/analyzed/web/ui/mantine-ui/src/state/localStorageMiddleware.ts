// Redux Listener Middleware：将 targets/服务发现/查询历史/设置变更同步写入 localStorage。

import { createListenerMiddleware } from "@reduxjs/toolkit";
import { AppDispatch, RootState } from "./store";
import {
  localStorageKeyCollapsedPools as localStorageKeyTargetsPageCollapsedPools,
  setCollapsedPools as targetsPageSetCollapsedPools,
} from "./targetsPageSlice";
import {
  localStorageKeyCollapsedPools as localStorageKeyServiceDiscoveryPageCollapsedPools,
  setCollapsedPools as serviceDiscoveryPageSetCollapsedPools,
} from "./serviceDiscoveryPageSlice";
import { updateSettings } from "./settingsSlice";
import {
  addQueryToHistory,
  localStorageKeyQueryHistory,
} from "./queryPageSlice";

// persistToLocalStorage 将任意可 JSON 序列化的值写入指定键。
const persistToLocalStorage = <T>(key: string, value: T) => {
  localStorage.setItem(key, JSON.stringify(value));
};

export const localStorageMiddleware = createListenerMiddleware();

const startAppListening = localStorageMiddleware.startListening.withTypes<
  RootState,
  AppDispatch
>();

startAppListening({
  actionCreator: targetsPageSetCollapsedPools,
  effect: ({ payload }) => {
    persistToLocalStorage(localStorageKeyTargetsPageCollapsedPools, payload);
  },
});

startAppListening({
  actionCreator: serviceDiscoveryPageSetCollapsedPools,
  effect: ({ payload }) => {
    persistToLocalStorage(
      localStorageKeyServiceDiscoveryPageCollapsedPools,
      payload
    );
  },
});

startAppListening({
  actionCreator: addQueryToHistory,
  effect: (_, { getState }) => {
    persistToLocalStorage(
      localStorageKeyQueryHistory,
      getState().queryPage.queryHistory
    );
  },
});

startAppListening({
  actionCreator: updateSettings,
  effect: ({ payload }) => {
    Object.entries(payload).forEach(([key, value]) => {
      switch (key) {
        case "useLocalTime":
        case "enableQueryHistory":
        case "enableAutocomplete":
        case "enableSyntaxHighlighting":
        case "enableLinter":
        case "showAnnotations":
        case "showQueryWarnings":
        case "showQueryInfoNotices":
        case "alertGroupsPerPage":
        case "ruleGroupsPerPage":
          return persistToLocalStorage(`settings.${key}`, value);
      }
    });
  },
});
