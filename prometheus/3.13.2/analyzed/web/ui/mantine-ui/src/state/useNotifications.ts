// Prometheus 实时通知 React Context 与 useNotifications hook。

import { createContext, useContext } from 'react';
import { Notification } from "../api/responseTypes/notifications";

export type NotificationsContextType = {
  notifications: Notification[];
  isConnectionError: boolean;
};

const defaultContextValue: NotificationsContextType = {
  notifications: [],
  isConnectionError: false,
};

export const NotificationsContext = createContext<NotificationsContextType>(defaultContextValue);

// useNotifications 供组件读取 NotificationsContext 当前值。
// Custom hook to access notifications context
export const useNotifications = () => useContext(NotificationsContext);
