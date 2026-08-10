// field-config/index.ts — field-config 目录对外导出入口。

// field-config 对外入口，保持 provider-modal 对 `./field-config` 的导入路径不变。
// 避免调用方改为深层路径导入。

export { FACTORIES_WITH_BASE_URL } from './generic-api-key-config';
export { getProviderConfig } from './get-provider-config';
