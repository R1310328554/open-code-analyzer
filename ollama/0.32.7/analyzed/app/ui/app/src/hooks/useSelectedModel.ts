/**
 * 当前选中模型：按显存推荐默认项、云端/Turbo 迁移、聊天历史恢复与列表合并。
 */
import { useEffect, useMemo, useRef } from "react";
import { useQuery } from "@tanstack/react-query";
import { useModels } from "./useModels";
import { useChat } from "./useChats";
import { useSettings } from "./useSettings.ts";
import { Model } from "@/gotypes";
import { getTotalVRAM } from "@/utils/vram.ts";
import { getInferenceCompute } from "@/api";
import { useCloudStatus } from "./useCloudStatus";

/** 按总显存（GB）推荐默认模型名称。 */
export function recommendDefaultModel(totalVRAM: number): string {
  const vram = Math.max(0, Number(totalVRAM) || 0);

  if (vram < 6) {
    return "gemma3:1b";
  } else if (vram < 16) {
    return "gemma3:4b";
  }
  return "gpt-oss:20b";
}

/** 解析并同步 settings 中的选中模型，必要时自动切换或从聊天历史恢复。 */
export function useSelectedModel(currentChatId?: string, searchQuery?: string) {
  const { settings, setSettings } = useSettings();
  const { data: models = [], isLoading } = useModels(searchQuery || "");
  const { cloudDisabled } = useCloudStatus();
  const { data: chatData, isLoading: isChatLoading } = useChat(
    currentChatId && currentChatId !== "new" ? currentChatId : "",
  );

  const { data: inferenceComputeResponse } = useQuery({
    queryKey: ["inferenceCompute"],
    queryFn: getInferenceCompute,
    enabled: !settings.selectedModel, // 尚未选中模型时才拉取推理设备信息
    // Only fetch if no model is selected
  });

  const inferenceComputes = inferenceComputeResponse?.inferenceComputes || [];

  const totalVRAM = useMemo(
    () => getTotalVRAM(inferenceComputes),
    [inferenceComputes],
  );

  const recommendedModel = useMemo(
    () => recommendDefaultModel(totalVRAM),
    [totalVRAM],
  );

  // 记录已为哪个聊天恢复过历史模型，避免重复写入 settings
  // Track which chat we've already restored the model for
  const restoredChatRef = useRef<string | null>(null);

  const selectedModel: Model | null = useMemo(() => {
    // 云端禁用时若仍选中 *cloud 模型，回退到本地推荐默认项。
    // If cloud is disabled and selected model ends with cloud, switch to a local default.
    if (cloudDisabled && settings.selectedModel?.endsWith("cloud")) {
      return (
        models.find((m) => m.model === recommendedModel) ||
        models.find((m) => !m.isCloud()) ||
        models.find((m) => m.digest === undefined || m.digest === "") ||
        models[0] ||
        null
      );
    }

    // Turbo 迁移：启用 turbo 且选中基础模型时，自动切换到对应 cloud 后缀模型。
    // Migration logic: if turboEnabled is true and selectedModel is a base model,
    // migrate to the cloud version and disable turboEnabled permanently
    // TODO: remove this logic in a future release
    const baseModelsToMigrate = [
      "gpt-oss:20b",
      "gpt-oss:120b",
      "deepseek-v3.1:671b",
      "qwen3-coder:480b",
    ];
    const shouldMigrate =
      !cloudDisabled &&
      settings.turboEnabled &&
      baseModelsToMigrate.includes(settings.selectedModel);

    if (shouldMigrate) {
      const cloudModel = `${settings.selectedModel}cloud`;
      return (
        models.find((m) => m.model === cloudModel) ||
        new Model({
          model: cloudModel,
          cloud: true,
          ollama_host: false,
        })
      );
    }

    return (
      models.find((m) => m.model === settings.selectedModel) ||
      (settings.selectedModel &&
        new Model({
          model: settings.selectedModel,
          cloud: settings.selectedModel.endsWith("cloud"),
          ollama_host: false,
        })) ||
      null
    );
  }, [
    models,
    settings.selectedModel,
    cloudDisabled,
    recommendedModel,
  ]);

  useEffect(() => {
    if (!selectedModel) return;

    if (
      cloudDisabled &&
      settings.selectedModel?.endsWith("cloud") &&
      selectedModel.model !== settings.selectedModel
    ) {
      setSettings({ SelectedModel: selectedModel.model });
    }

    if (
      !cloudDisabled &&
      settings.turboEnabled &&
      selectedModel.model !== settings.selectedModel
    ) {
      setSettings({ SelectedModel: selectedModel.model, TurboEnabled: false });
    }
  }, [
    selectedModel,
    cloudDisabled,
    settings.selectedModel,
  ]);

  // 聊天详情加载后，从最近一条带 model 的消息恢复选中模型
  // Set model from chat history when chat data loads
  useEffect(() => {
    // 仅在有有效 chatId（非 new）时执行恢复逻辑
    // Only run this effect if we have a valid currentChatId
    if (!currentChatId || currentChatId === "new") {
      return;
    }

    if (
      chatData?.chat?.messages &&
      !isChatLoading &&
      restoredChatRef.current !== currentChatId
    ) {
      // 从最新消息向前查找该聊天最近使用的模型
      // Find the most recent model used in this chat
      const messages = [...chatData.chat.messages].reverse();
      for (const message of messages) {
        if (message.model) {
          const chatModelName = message.model;

          if (chatModelName !== settings.selectedModel) {
            setSettings({ SelectedModel: chatModelName });
          }

          // 标记该聊天已处理，避免 effect 重复写入
          // Mark this chat as restored
          restoredChatRef.current = currentChatId;
          return;
        }
      }
      // 即使未找到 model 也标记已处理，防止无限重试
      // Mark this chat as processed even if no model was found
      restoredChatRef.current = currentChatId;
    }
  }, [
    currentChatId,
    chatData,
    isChatLoading,
    settings.selectedModel,
    setSettings,
  ]);

  // 首次加载且 settings 无选中模型时，按推荐/本地/云端优先级设默认
  // On initial load, if no model is selected, set default model
  useEffect(() => {
    if (
      isLoading ||
      inferenceComputes.length === 0 ||
      models.length === 0 ||
      settings.selectedModel
    ) {
      return;
    }

    const defaultModel =
      models.find((m) => m.model === recommendedModel) ||
      (cloudDisabled
        ? models.find((m) => !m.isCloud())
        : models.find((m) => m.isCloud())) ||
      models.find((m) => m.digest === undefined || m.digest === "") ||
      models[0];

    if (defaultModel) {
      setSettings({ SelectedModel: defaultModel.model });
    }
  }, [
    isLoading,
    inferenceComputes.length,
    models.length,
    settings.selectedModel,
    cloudDisabled,
  ]);

  // 若选中模型不在列表中（如仅存在于 settings），合并进返回列表
  // Add the selected model to the models list if it's not already there
  const allModels = useMemo(() => {
    if (!selectedModel || models.find((m) => m.model === selectedModel.model)) {
      return models;
    }

    return [...models, selectedModel];
  }, [models, selectedModel]);

  return {
    selectedModel,
    setSettings,
    models: allModels,
    loading: isLoading,
  };
}
