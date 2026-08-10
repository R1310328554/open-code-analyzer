import type ComponentTypeRepresentation from "./componentTypeRepresentation.js";
import type { ConfigPropertyRepresentation } from "./configPropertyRepresentation.js";
import FeatureRepresentation from "./featureRepresentation.js";
import type PasswordPolicyTypeRepresentation from "./passwordPolicyTypeRepresentation.js";
import type ProfileInfoRepresentation from "./profileInfoRepresentation.js";
import type ProtocolMapperRepresentation from "./protocolMapperRepresentation.js";
import type SystemInfoRepresentation from "./systemInfoRepersantation.js";

/**
 * 服务器运行时信息：SPI 提供者、主题、协议映射器类型及系统资源概况。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_serverinforepresentation
 */
export interface ServerInfoRepresentation {
  /** 操作系统与 JVM 系统信息 */
  systemInfo?: SystemInfoRepresentation;
  /** CPU 信息 */
  cpuInfo?: CpuInfoRepresentation;
  /** 内存使用概况 */
  memoryInfo?: MemoryInfoRepresentation;
  /** 当前运行 Profile（如 community、product） */
  profileInfo?: ProfileInfoRepresentation;
  /** 已启用/可用的功能特性列表 */
  features?: FeatureRepresentation[];
  /** 加密提供者及支持的算法/密钥库类型 */
  cryptoInfo?: CryptoInfoRepresentation;
  /** 按主题类型（login、account、admin、email）分组的主题列表 */
  themes?: { [index: string]: ThemeInfoRepresentation[] };
  /** 社交登录提供者 ID 与显示名映射 */
  socialProviders?: { [index: string]: string }[];
  /** 身份联邦提供者 ID 与显示名映射 */
  identityProviders?: { [index: string]: string }[];
  /** 客户端导入器 ID 与显示名映射 */
  clientImporters?: { [index: string]: string }[];
  /** 按 SPI 名称分组的提供者信息 */
  providers?: { [index: string]: SpiInfoRepresentation };
  /** 按协议分组的协议映射器类型定义 */
  protocolMapperTypes?: { [index: string]: ProtocolMapperTypeRepresentation[] };
  /** 按协议分组的内置协议映射器 */
  builtinProtocolMappers?: { [index: string]: ProtocolMapperRepresentation[] };
  /** 按协议分组的客户端安装/适配器配置模板 */
  clientInstallations?: { [index: string]: ClientInstallationRepresentation[] };
  /** 按组件类型分组的组件类型元信息 */
  componentTypes?: { [index: string]: ComponentTypeRepresentation[] };
  /** 支持的密码策略类型列表 */
  passwordPolicies?: PasswordPolicyTypeRepresentation[];
  /** 枚举类型名称到可选值列表的映射（供 Admin UI 使用） */
  enums?: { [index: string]: string[] };
  /** 参数化 OAuth Scope 类型定义 */
  parameterizedScopeTypes?: ParameterizedScopeTypeRepresentation[];
}

/** 参数化 Scope 类型：支持动态 Scope 参数 */
export interface ParameterizedScopeTypeRepresentation {
  /** Scope 类型名称 */
  name: string;
  /** 同一请求中是否可重复出现 */
  repeatable: boolean;
}

/** 主题元信息 */
export interface ThemeInfoRepresentation {
  /** 主题名称 */
  name: string;
  /** 支持的语言区域列表 */
  locales?: string[];
  /** 主题描述 */
  description?: string;
}

/** SPI 模块信息：是否内部实现及可用提供者 */
export interface SpiInfoRepresentation {
  /** 是否为 Keycloak 内部 SPI（非扩展） */
  internal: boolean;
  /** 该 SPI 下已注册的提供者及其顺序 */
  providers: { [index: string]: ProviderRepresentation };
}

/** SPI 提供者实例信息 */
export interface ProviderRepresentation {
  /** 加载顺序（数值越小优先级越高） */
  order: number;
  /** 运行时运维信息键值对 */
  operationalInfo?: Record<string, string>;
}

/** 客户端适配器/安装配置下载项 */
export interface ClientInstallationRepresentation {
  /** 安装配置 ID */
  id: string;
  /** 所属协议 */
  protocol: string;
  /** 是否仅提供下载（不可在线预览） */
  downloadOnly: boolean;
  /** Admin UI 显示类型 */
  displayType: string;
  /** 帮助说明文本 */
  helpText: string;
  /** 下载文件名 */
  filename: string;
  /** HTTP 媒体类型 */
  mediaType: string;
}

/** JVM 内存使用信息 */
export interface MemoryInfoRepresentation {
  /** 总内存（字节） */
  total: number;
  /** 格式化的总内存字符串 */
  totalFormated: string;
  /** 已用内存（字节） */
  used: number;
  /** 格式化的已用内存字符串 */
  usedFormated: string;
  /** 空闲内存（字节） */
  free: number;
  /** 空闲内存百分比 */
  freePercentage: number;
  /** 格式化的空闲内存字符串 */
  freeFormated: string;
}

/** CPU 信息 */
export interface CpuInfoRepresentation {
  /** 可用处理器核心数 */
  processorCount: number;
}

/** 协议映射器类型元信息（供 Admin UI 创建映射器时使用） */
export interface ProtocolMapperTypeRepresentation {
  /** 映射器类型 ID */
  id: string;
  /** 显示名称 */
  name: string;
  /** 所属类别 */
  category: string;
  /** 帮助说明 */
  helpText: string;
  /** Admin UI 排序优先级 */
  priority: number;
  /** 可配置属性定义列表 */
  properties: ConfigPropertyRepresentation[];
}

/** 加密子系统信息 */
export interface CryptoInfoRepresentation {
  /** 当前加密提供者名称 */
  cryptoProvider: string;
  /** 支持的密钥库类型 */
  supportedKeystoreTypes: string[];
  /** 客户端签名支持的 symmetric 算法 */
  clientSignatureSymmetricAlgorithms: string[];
  /** 客户端签名支持的 asymmetric 算法 */
  clientSignatureAsymmetricAlgorithms: string[];
}
