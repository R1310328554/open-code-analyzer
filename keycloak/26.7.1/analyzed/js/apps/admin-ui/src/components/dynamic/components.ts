import type { ConfigPropertyRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/authenticatorConfigInfoRepresentation";
import { FunctionComponent } from "react";

import { BooleanComponent } from "./BooleanComponent";
import { ClientSelectComponent } from "./ClientSelectComponent";
import { ClaimDisplayComponent } from "./ClaimDisplayComponent";
import { IdentityProviderMultiSelectComponent } from "./IdentityProviderMultiSelectComponent";
import { FileComponent } from "./FileComponent";
import { GroupComponent } from "./GroupComponent";
import { ListComponent } from "./ListComponent";
import { MapComponent } from "./MapComponent";
import { MultiValuedListComponent } from "./MultivaluedListComponent";
import { MultiValuedStringComponent } from "./MultivaluedStringComponent";
import { PasswordComponent } from "./PasswordComponent";
import { RoleComponent } from "./RoleComponent";
import { ScriptComponent } from "./ScriptComponent";
import { StringComponent } from "./StringComponent";
import { TextComponent } from "./TextComponent";
import { UrlComponent } from "./UrlComponent";
import { UserProfileAttributeListComponent } from "./UserProfileAttributeListComponent";
import { IntComponent } from "./IntComponent";
import { NumberComponent } from "./NumberComponent";

/** 动态表单字段通用属性：继承认证器配置元数据，并补充 UI 交互回调。 */
export type ComponentProps = Omit<ConfigPropertyRepresentation, "type"> & {
  isDisabled?: boolean;
  isNew?: boolean;
  stringify?: boolean;
  convertToName: (name: string) => string;
  onSearch?: (search: string) => void;
};

/** 数值类字段额外支持 min/max 约束。 */
export type NumberComponentProps = ComponentProps & {
  min?: number;
  max?: number;
};

/** 服务端 ConfigPropertyRepresentation.type 与前端 React 组件的一一对应键。 */
type ComponentType =
  | "String"
  | "Text"
  | "Integer"
  | "Number"
  | "boolean"
  | "List"
  | "Role"
  | "Script"
  | "Map"
  | "Group"
  | "MultivaluedList"
  | "ClientList"
  | "IdentityProviderMultiList"
  | "UserProfileAttributeList"
  | "MultivaluedString"
  | "File"
  | "Password"
  | "Url"
  | "ClaimDisplay";

/**
 * 认证器/组件配置表单使用的动态控件注册表。
 * 根据 API 返回的 type 字段选择对应输入组件（字符串、角色、脚本、映射等）。
 */
export const COMPONENTS: {
  [index in ComponentType]: FunctionComponent<ComponentProps>;
} = {
  String: StringComponent,
  Text: TextComponent,
  boolean: BooleanComponent,
  Integer: IntComponent,
  Number: NumberComponent,
  List: ListComponent,
  Role: RoleComponent,
  Script: ScriptComponent,
  Map: MapComponent,
  Group: GroupComponent,
  ClientList: ClientSelectComponent,
  IdentityProviderMultiList: IdentityProviderMultiSelectComponent,
  UserProfileAttributeList: UserProfileAttributeListComponent,
  MultivaluedList: MultiValuedListComponent,
  MultivaluedString: MultiValuedStringComponent,
  File: FileComponent,
  Password: PasswordComponent,
  Url: UrlComponent,
  ClaimDisplay: ClaimDisplayComponent,
} as const;

/** 运行时校验字符串是否为已注册的组件类型。 */
export const isValidComponentType = (value: string): value is ComponentType =>
  value in COMPONENTS;
