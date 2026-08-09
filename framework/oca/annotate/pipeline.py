from __future__ import annotations

import json
import os
from pathlib import Path

from ..static.complexity import ComplexityHit
from ..util.fs import write_text
from ..util.paths import ProjectLayout
from .java_annotator import AnnotationPlan, annotate_java_tree, load_plan


def _maybe_llm_enrich_plan(
    layout: ProjectLayout,
    plan: AnnotationPlan,
    complex_hits: list[ComplexityHit],
) -> AnnotationPlan:
    """若配置了 API Key，可在此扩展 LLM 批注。当前默认不强制联网调用。"""
    enabled = os.environ.get("OCA_LLM", "").lower() in {"1", "true", "yes"}
    if not enabled:
        return plan
    # 预留：读取 prompts/ 并调用模型。保持框架可扩展且默认离线可用。
    print("[oca] OCA_LLM 已开启，但当前环境未绑定具体 Provider；沿用计划/启发式注释。")
    return plan


def run_annotation_pipeline(
    layout: ProjectLayout,
    complex_hits: list[ComplexityHit],
    *,
    plan_path: Path | None = None,
    annotate_fields: bool = True,
    max_files: int | None = None,
) -> dict:
    plan_path = plan_path or (layout.reports / "annotation-plan.json")
    plan = load_plan(plan_path if plan_path.exists() else None)
    plan = _maybe_llm_enrich_plan(layout, plan, complex_hits)
    stats = annotate_java_tree(
        layout.analyzed,
        plan=plan,
        complex_hits=complex_hits,
        annotate_fields=annotate_fields,
        max_files=max_files,
    )
    write_text(
        layout.reports / "annotation-stats.json",
        json.dumps(stats, ensure_ascii=False, indent=2),
    )
    return stats
