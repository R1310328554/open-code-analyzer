// Redux store 配置：组合各页面 slice 并前置 localStorage 持久化 middleware。

import { configureStore } from "@reduxjs/toolkit";
import queryPageSlice from "./queryPageSlice";
import settingsSlice from "./settingsSlice";
import targetsPageSlice from "./targetsPageSlice";
import { localStorageMiddleware } from "./localStorageMiddleware";
import serviceDiscoveryPageSlice from "./serviceDiscoveryPageSlice";

// configureStore 注册 settings、queryPage、targets 与服务发现 reducer。
const store = configureStore({
  reducer: {
    settings: settingsSlice,
    queryPage: queryPageSlice,
    targetsPage: targetsPageSlice,
    serviceDiscoveryPage: serviceDiscoveryPageSlice,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().prepend(localStorageMiddleware.middleware),
});

// 从 store 实例推导 RootState 与 AppDispatch，供 hooks 与 middleware 使用。
// Infer the `RootState` and `AppDispatch` types from the store itself
export type RootState = ReturnType<typeof store.getState>;
// AppDispatch 即 store.dispatch 的类型，包含 thunk 与 listener 扩展。
// Inferred type: {posts: PostsState, comments: CommentsState, users: UsersState}
export type AppDispatch = typeof store.dispatch;

export default store;
