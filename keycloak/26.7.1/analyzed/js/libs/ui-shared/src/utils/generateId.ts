/** 生成 0–999 之间的随机整数 ID，用于 DOM aria 关联等轻量场景 */
export const generateId = () => Math.floor(Math.random() * 1000);
