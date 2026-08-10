// UI 通知列表类型，用于 Mantine 界面展示系统通知条目。

export interface Notification {
  text: string;
  date: string;
  active: boolean;
  modified: boolean;
}

export type NotificationsResult = Notification[];
