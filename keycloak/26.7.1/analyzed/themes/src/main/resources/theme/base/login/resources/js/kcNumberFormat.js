// @ts-check — TypeScript 类型检查指令
// @ts-check
import { formatNumber } from "./common.js";
import { registerElementAnnotatedBy } from "./userProfile.js";

// 用户配置注解名称，对应 data-kcNumberFormat 属性
const KC_NUMBER_FORMAT = "kcNumberFormat";

// 注册带 kcNumberFormat 注解的输入框，keyup 时按 data 属性格式化数值
registerElementAnnotatedBy({
  name: KC_NUMBER_FORMAT,
  onAdd(element) {
    // 读取 data-kcNumberFormat 模板并调用 formatNumber 格式化当前值
    const formatValue = () => {
      const format = element.getAttribute(`data-${KC_NUMBER_FORMAT}`);
      element.value = formatNumber(element.value, format);
    };

    element.addEventListener("keyup", formatValue);

    formatValue();

    // 元素移除时注销 keyup 监听器
    return () => element.removeEventListener("keyup", formatValue);
  },
});
