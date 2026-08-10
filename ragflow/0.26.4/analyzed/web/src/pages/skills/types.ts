/**
 * skills/types.ts — Skill Space 领域类型：Skill、Space、校验与 UI 组件 props。
 */

// Skill Space 核心类型定义

// ============================================================================
// 核心 Skill 实体
// ============================================================================

/** 单个 Skill：含文件树、元数据、版本列表及内部 folderId。 */
export interface Skill {
  id: string; // Skill 名称作为 ID，与搜索结果一致
  name: string;
  description: string;
  source_type: 'local' | 'git' | 'central' | 'search';
  source_ref?: string;
  central_path?: string;
  created_at: number;
  updated_at: number;
  files: SkillFileEntry[];
  metadata?: SkillMetadata;
  versions?: string[]; // 可用版本号列表（semver 目录结构）
  _folderId?: string; // 内部字段：文件系统文件夹 ID
}

export interface SkillSpace {
  id: string;
  name: string;
  folder_id?: string;
  create_time?: number;
}

export interface SkillFileEntry {
  name: string;
  path: string;
  is_dir: boolean;
  size: number;
  content?: string;
  contentType?: string;
}

// ============================================================================
// Skill 元数据（frontmatter）
// ============================================================================

export interface SkillMetadata {
  // Basic fields
  name?: string;
  description?: string;
  version?: string;
  author?: string;
  tags?: string[];
  tools?: string[];

  // Legacy fields for backward compatibility
  [key: string]: any;
}

// ============================================================================
// 上传/更新 API 载荷
// ============================================================================

export interface SkillUploadPayload {
  name: string;
  description?: string;
  files: { path: string; content: string }[];
}

export interface SkillUpdatePayload {
  id: string;
  description?: string;
  metadata?: SkillMetadata;
}

// ============================================================================
// 校验结果与字段错误
// ============================================================================

export interface SkillValidationResult {
  valid: boolean;
  error?: string;
  details?: string;
  name?: string;
  description?: string;
}

export interface ValidationError {
  field: string;
  message: string;
}

// ============================================================================
// 列表/详情/上传等 UI 组件 props
// ============================================================================

export type ViewMode = 'grid' | 'list';

export interface SkillCardProps {
  skill: Skill;
  onView: (skill: Skill) => void;
  onDelete: (skillId: string, skillName: string, folderId?: string) => void;
  formatRelative: (timestamp: number) => string;
}

export interface SkillDetailProps {
  skill: Skill | null;
  open: boolean;
  onClose: () => void;
  getFileContent: (
    skillId: string,
    filePath: string,
    version?: string,
  ) => Promise<string | null>;
  getVersionFiles?: (
    skillId: string,
    version: string,
  ) => Promise<SkillFileEntry[]>;
}

export interface UploadModalProps {
  open: boolean;
  onCancel: () => void;
  onUpload: (name: string, version: string, files: File[]) => Promise<boolean>;
  loading?: boolean;
}

// ============================================================================
// 技能语义搜索配置与结果
// ============================================================================

export interface FieldWeight {
  enabled: boolean;
  weight: number;
}

export interface FieldConfig {
  name: FieldWeight;
  tags: FieldWeight;
  description: FieldWeight;
  content: FieldWeight;
}

// Re-export SkillSearchConfig from service to ensure consistency
export { SkillSearchConfig } from '@/services/skill-space-service';

export interface SkillSearchResult {
  skill_id: string;
  name: string;
  description: string;
  tags: string[];
  score: number;
  bm25_score?: number;
  vector_score?: number;
}

export interface SkillSearchResponse {
  results: SkillSearchResult[];
  total: number;
  query: string;
  search_type: string;
}

export interface SearchConfigModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  config?: SkillSearchConfig;
  onSave: (config: SkillSearchConfig) => Promise<boolean>;
  onReindex?: (embdId: string) => Promise<boolean>;
  loading?: boolean;
}
