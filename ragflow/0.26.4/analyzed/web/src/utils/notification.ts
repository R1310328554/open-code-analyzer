/**
 * notification.ts — 全局 Toast 通知封装（基于 sonner），统一 success/error/warning/info。
 */

import { ExternalToast, toast } from 'sonner';

/** 默认 Toast：右上角、4 秒。 */
const defaultConfig: ExternalToast = { duration: 4000, position: 'top-right' };

/** 通知选项：标题 message、可选 description 与 duration（秒）。 */
type NotificationOptions = {
  message: string;
  description?: string;
  duration?: number;
};

/** 四类 Toast 方法，description 与 message 合并为单行文本。 */
const notification = {
  /** 成功提示。 */
  success: (options: NotificationOptions) => {
    const messageText = options.description
      ? `${options.message}\n${options.description}`
      : options.message;
    toast.success(messageText, {
      ...defaultConfig,
      duration: options.duration
        ? options.duration * 1000
        : defaultConfig.duration,
    });
  },
  /** 错误提示。 */
  error: (options: NotificationOptions) => {
    const messageText = options.description
      ? `${options.message}\n${options.description}`
      : options.message;
    toast.error(messageText, {
      ...defaultConfig,
      duration: options.duration
        ? options.duration * 1000
        : defaultConfig.duration,
    });
  },
  /** 警告提示。 */
  warning: (options: NotificationOptions) => {
    const messageText = options.description
      ? `${options.message}\n${options.description}`
      : options.message;
    toast.warning(messageText, {
      ...defaultConfig,
      duration: options.duration
        ? options.duration * 1000
        : defaultConfig.duration,
    });
  },
  /** 信息提示。 */
  info: (options: NotificationOptions) => {
    const messageText = options.description
      ? `${options.message}\n${options.description}`
      : options.message;
    toast.info(messageText, {
      ...defaultConfig,
      duration: options.duration
        ? options.duration * 1000
        : defaultConfig.duration,
    });
  },
};

/** 默认导出 notification 对象。 */
export default notification;
