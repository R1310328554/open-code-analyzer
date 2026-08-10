// ui/message.ts — 基于 sonner 的全局 Toast 封装：success/error/warning/info 统一配置。

import { ExternalToast, toast } from 'sonner';

/** 默认 Toast：顶部居中，持续 2.5 秒。 */
const configuration: ExternalToast = { duration: 2500, position: 'top-center' };

/** error 对象式调用参数：主文案、可选描述与自定义时长（秒）。 */
type MessageOptions = {
  message: string;
  description?: string;
  duration?: number;
};

/** 全局 message 单例：各方法委托 sonner toast 并合并默认 configuration。 */
const message = {
  /** 展示成功提示。 */
  success: (msg: string) => {
    toast.success(msg, configuration);
  },
  /** 展示错误提示，支持字符串或 { message, description, duration } 对象形式。 */
  error: (msg: string | MessageOptions, data?: ExternalToast) => {
    let messageText: string;
    let options: ExternalToast = { ...configuration };

    if (typeof msg === 'object') {
      // 对象式调用：合并 message 与 description，duration 转为毫秒
      messageText = msg.message;
      if (msg.description) {
        messageText += `\n${msg.description}`;
      }
      if (msg.duration !== undefined) {
        options.duration = msg.duration * 1000; // Convert to milliseconds
      }
    } else {
      // 字符串式调用：主文案与 data.description 拼接
      messageText = msg;
      if (data?.description) {
        messageText += `\n${data.description}`;
      }
      options = { ...options, ...data };
    }

    toast.error(messageText, options);
  },
  /** 展示警告提示。 */
  warning: (msg: string) => {
    toast.warning(msg, configuration);
  },
  /** 展示信息提示。 */
  info: (msg: string) => {
    toast.info(msg, configuration);
  },
};
export default message;
