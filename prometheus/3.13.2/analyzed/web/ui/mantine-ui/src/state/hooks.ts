// Redux 类型化 hooks：为 useDispatch/useSelector 绑定 RootState 与 AppDispatch。

import { useDispatch, useSelector } from "react-redux";
import type { RootState, AppDispatch } from "./store";

// 应用内应使用这些带类型的 hooks，避免裸用 useDispatch/useSelector。
// Use these typed hooks throughout the app instead of plain `useDispatch` and `useSelector`
export const useAppDispatch = useDispatch.withTypes<AppDispatch>();
export const useAppSelector = useSelector.withTypes<RootState>();
