// 服务发现页 Redux slice：抓取池折叠状态与结果数量限制提示。

import { PayloadAction, createSlice } from "@reduxjs/toolkit";
import { initializeFromLocalStorage } from "./initializeFromLocalStorage";

export const localStorageKeyCollapsedPools = "serviceDiscovery.collapsedPools";
export const localStorageKeyTargetHealthFilter =
  "serviceDiscovery.healthFilter";

interface ServiceDiscoveryPage {
  collapsedPools: string[];
  showLimitAlert: boolean;
}

const initialState: ServiceDiscoveryPage = {
  collapsedPools: initializeFromLocalStorage<string[]>(
    localStorageKeyCollapsedPools,
    []
  ),
  showLimitAlert: false,
};

// serviceDiscoveryPageSlice 管理服务发现列表 UI 局部状态。
export const serviceDiscoveryPageSlice = createSlice({
  name: "serviceDiscoveryPage",
  initialState,
  reducers: {
    setCollapsedPools: (state, { payload }: PayloadAction<string[]>) => {
      state.collapsedPools = payload;
    },
    setShowLimitAlert: (state, { payload }: PayloadAction<boolean>) => {
      state.showLimitAlert = payload;
    },
  },
});

export const { setCollapsedPools, setShowLimitAlert } =
  serviceDiscoveryPageSlice.actions;

export default serviceDiscoveryPageSlice.reducer;
