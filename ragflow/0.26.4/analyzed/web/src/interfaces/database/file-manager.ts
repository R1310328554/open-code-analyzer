// file-manager.ts — 文件管理器：文件/文件夹实体与列表 API 响应。

/** 租户文件实体：存储路径、关联知识库与父文件夹。 */
export interface IFile {
  create_date: string;
  create_time: number;
  created_by: string;
  id: string;
  kbs_info: { kb_id: string; kb_name: string }[];
  location: string;
  name: string;
  parent_id: string;
  size: number;
  tenant_id: string;
  type: string;
  update_date: string;
  update_time: number;
  source_type: string;
  has_child_folder?: boolean;
}

/** 文件夹实体：层级 parent_id 与租户归属。 */
export interface IFolder {
  create_date: string;
  create_time: number;
  created_by: string;
  id: string;
  location: string;
  name: string;
  parent_id: string;
  size: number;
  tenant_id: string;
  type: string;
  update_date: string;
  update_time: number;
  source_type: string;
}

/** 文件列表 API 响应：当前目录文件、父文件夹与总数。 */
export type IFetchFileListResult = {
  files: IFile[];
  parent_folder: IFolder;
  total: number;
};
