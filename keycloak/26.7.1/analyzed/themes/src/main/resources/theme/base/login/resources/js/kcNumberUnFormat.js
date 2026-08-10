// @ts-check
// 登录主题：表单提交前将本地化数字格式还原为原始数值字符串。
import { formatNumber } from "./common.js";
import { registerElementAnnotatedBy } from "./userProfile.js";

// data-kcNumberUnFormat 属性名常量
const KC_NUMBER_UNFORMAT = 'kcNumberUnFormat';

// 注册带 data-kcNumberUnFormat 标记的输入框，在提交时反格式化数值
registerElementAnnotatedBy({
    name: KC_NUMBER_UNFORMAT,
    onAdd(element) {
        // 监听页面上所有表单的 submit 事件
        for (let form of document.forms) {
            form.addEventListener('submit', (event) => {
                const rawFormat = element.getAttribute(`data-${KC_NUMBER_UNFORMAT}`);
                if (rawFormat) {
                    // 按原始格式模板将显示值转换回服务端可解析的格式
                    element.value = formatNumber(element.value, rawFormat);
                }
            });
        }
    },
});
