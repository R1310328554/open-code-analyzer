/**
 * skill-space-service.ts — Skill Space 后端 API 封装：空间 CRUD、检索配置、搜索与索引。
 */

import api from '@/utils/api';
import request from '@/utils/request';

/** Skill Space 实体：租户下的技能集合及其嵌入/重排配置。 */
export interface SkillSpace {
  id: string;
  tenant_id: string;
  name: string;
  folder_id: string;
  description?: string;
  embd_id?: string;
  rerank_id?: string;
  top_k?: number;
  status?: string;
  create_time?: number;
  update_time?: string;
}

/** 创建 Skill Space 的请求体。 */
export interface CreateSpaceRequest {
  name: string;
  description?: string;
  embd_id?: string;
  rerank_id?: string;
}

/** 更新 Skill Space 的可选字段。 */
export interface UpdateSpaceRequest {
  name?: string;
  description?: string;
  embd_id?: string;
  rerank_id?: string;
  top_k?: number;
}

/** 技能搜索配置：向量权重、相似度阈值、字段映射与 top_k。 */
export interface SkillSearchConfig {
  id: string;
  tenant_id: string;
  space_id: string;
  embd_id: string;
  vector_similarity_weight: number;
  similarity_threshold: number;
  field_config: Record<string, any>;
  rerank_id?: string;
  tenant_rerank_id?: number;
  top_k: number;
  index_version: string;
  status: string;
  create_time?: number;
  update_time?: string;
}

/** 提交/更新搜索配置的请求体。 */
export interface UpdateConfigRequest {
  tenant_id?: string;
  space_id?: string;
  embd_id: string;
  vector_similarity_weight: number;
  similarity_threshold: number;
  field_config: Record<string, any>;
  rerank_id?: string;
  top_k: number;
}

/** 技能语义/BM25 混合搜索请求。 */
export interface SearchRequest {
  tenant_id?: string;
  space_id?: string;
  query: string;
  page?: number;
  page_size?: number;
}

/** 搜索结果：技能列表、总分与检索类型。 */
export interface SearchResult {
  skills: Array<{
    skill_id: string;
    folder_id: string;
    name: string;
    description: string;
    tags: string[];
    score: number;
    bm25_score?: number;
    vector_score?: number;
    index_version?: string;
  }>;
  total: number;
  query: string;
  search_type: string;
}

/** 待索引的单条技能元数据与正文。 */
export interface SkillInfo {
  id: string;
  folder_id: string;
  name: string;
  description: string;
  tags: string[];
  content: string;
}

/** 批量索引技能到向量库的 payload。 */
export interface IndexSkillsRequest {
  tenant_id?: string;
  space_id?: string;
  skills: SkillInfo[];
  embd_id?: string;
}

/** Skill Space REST 客户端：统一 request 包装与 code===0 校验。 */
class SkillSpaceService {
  /** 通用 HTTP 请求：解析 data 字段并在业务失败时抛错。 */
  private async request<T>(
    method: string,
    url: string,
    data?: any,
    params?: any,
  ): Promise<T> {
    const response: any = await request(url, {
      method: method as any,
      data,
      params,
    });

    const jsonData = response?.data ?? response;

    if (jsonData?.code !== 0) {
      throw new Error(jsonData?.message || 'Request failed');
    }

    return jsonData.data;
  }

  // ==================== Skill Space 空间管理 ====================

  /** 列出当前租户下全部 Skill Space。 */
  // List all skill spaces
  async listSpaces(): Promise<{ spaces: SkillSpace[]; total: number }> {
    return await this.request<{ spaces: SkillSpace[]; total: number }>(
      'GET',
      api.skillSpaces,
    );
  }

  /** 创建新的 Skill Space。 */
  // Create a new skill space
  async createSpace(request: CreateSpaceRequest): Promise<SkillSpace> {
    return await this.request<SkillSpace>('POST', api.skillSpaces, request);
  }

  /** 按 spaceId 获取空间详情。 */
  // Get a skill space by ID
  async getSpace(spaceId: string): Promise<SkillSpace> {
    return await this.request<SkillSpace>('GET', api.skillSpace(spaceId));
  }

  /** 更新空间名称、描述或嵌入/重排模型。 */
  // Update a skill space
  async updateSpace(
    spaceId: string,
    request: UpdateSpaceRequest,
  ): Promise<SkillSpace> {
    return await this.request<SkillSpace>(
      'PUT',
      api.skillSpace(spaceId),
      request,
    );
  }

  /** 删除指定 Skill Space。 */
  // Delete a skill space
  async deleteSpace(spaceId: string): Promise<void> {
    await this.request<void>('DELETE', api.skillSpace(spaceId));
  }

  /** 通过文件系统 folder_id 反查关联的 Skill Space。 */
  // Get space by folder ID
  async getSpaceByFolder(folderId: string): Promise<SkillSpace> {
    return await this.request<SkillSpace>('GET', api.skillSpaceByFolder, null, {
      folder_id: folderId,
    });
  }

  // ==================== 技能搜索配置 ====================

  /** 读取搜索配置（可按 space_id / embd_id 过滤）。 */
  // Get skill search config
  async getConfig(
    spaceId?: string,
    embdId?: string,
  ): Promise<SkillSearchConfig> {
    const params: Record<string, string> = {};
    if (spaceId) params.space_id = spaceId;
    if (embdId) params.embd_id = embdId;

    return await this.request<SkillSearchConfig>(
      'GET',
      api.skillConfig,
      null,
      params,
    );
  }

  /** 保存或覆盖搜索配置。 */
  // Update skill search config
  async updateConfig(request: UpdateConfigRequest): Promise<SkillSearchConfig> {
    return await this.request<SkillSearchConfig>(
      'POST',
      api.skillConfig,
      request,
    );
  }

  // ==================== 技能搜索 ====================

  /** 对 Skill Space 执行混合检索。 */
  // Search skills
  async search(request: SearchRequest): Promise<SearchResult> {
    return await this.request<SearchResult>('POST', api.skillSearch, request);
  }

  // ==================== 技能索引 ====================

  /** 将技能内容写入向量索引。 */
  // Index skills
  async indexSkills(
    request: IndexSkillsRequest,
  ): Promise<{ indexed_count: number }> {
    return await this.request<{ indexed_count: number }>(
      'POST',
      api.skillIndex,
      request,
    );
  }

  /** 从索引中移除单条技能。 */
  // Delete skill index
  async deleteSkillIndex(skillId: string, spaceId?: string): Promise<void> {
    const params: Record<string, string> = { skill_id: skillId };
    if (spaceId) params.space_id = spaceId;

    await this.request<void>('DELETE', api.skillIndex, null, params);
  }

  /** 全量重建 Skill Space 索引。 */
  // Reindex all skills
  async reindex(request: IndexSkillsRequest): Promise<any> {
    return await this.request<any>('POST', api.skillReindex, request);
  }
}

/** 全局 Skill Space 服务单例。 */
export const skillSpaceService = new SkillSpaceService();
export default skillSpaceService;
