// Targets 页 Redux slice：抓取池折叠状态与 targets 列表数量限制提示。

import { PayloadAction, createSlice } from "@reduxjs/toolkit";
import { initializeFromLocalStorage } from "./initializeFromLocalStorage";

export const localStorageKeyCollapsedPools = "targetsPage.collapsedPools";
export const localStorageKeyTargetHealthFilter = "targetsPage.healthFilter";

interface TargetsPage {
  collapsedPools: string[];
  showLimitAlert: boolean;
}

const initialState: TargetsPage = {
  collapsedPools: initializeFromLocalStorage<string[]>(
    localStorageKeyCollapsedPools,
    []
  ),
  showLimitAlert: false,
};

// targetsPageSlice 管理 targets 页面局部 UI 状态。
export const targetsPageSlice = createSlice({
  name: "targetsPage",
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
  targetsPageSlice.actions;

export default targetsPageSlice.reducer;
