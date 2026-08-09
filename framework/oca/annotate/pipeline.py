from __future__ import annotations

import json
import os
from pathlib import Path

from ..static.complexity import ComplexityHit
from ..util.fs import write_text
from ..util.paths import ProjectLayout
from .java_annotator import AnnotationPlan, annotate_java_tree, load_plan
from .translator import ZhTranslator
from .zh_localize import localize_tree


def _maybe_llm_enrich_plan(
    layout: ProjectLayout,
    plan: AnnotationPlan,
    complex_hits: list[ComplexityHit],
) -> AnnotationPlan:
    enabled = os.environ.get("OCA_LLM", "").lower() in {"1", "true", "yes"}
    if not enabled:
        return plan
    print("[oca] OCA_LLM 已开启，但当前环境未绑定具体 Provider；沿用计划/启发式注释。")
    return plan


def run_zh_localize_pipeline(
    layout: ProjectLayout,
    *,
    modules: list[str] | None = None,
    max_files: int | None = None,
    only_globs: list[str] | None = None,
) -> dict:
    """主路径：把 analyzed 中英文注释译为中文，并补齐字段/方法中文注释。"""
    cache = layout.reports / "translate-cache.json"
    tr = ZhTranslator(cache)
    stats = localize_tree(
        layout.analyzed,
        tr,
        modules=modules,
        max_files=max_files,
        only_globs=only_globs,
    )
    payload = {
        "files": stats.files,
        "javadocs": stats.javadocs,
        "line_comments": stats.line_comments,
        "fields_added": stats.fields_added,
        "methods_added": stats.methods_added,
        "skipped": stats.skipped,
        "errors": stats.errors[:50],
    }
    write_text(layout.reports / "zh-localize-stats.json", json.dumps(payload, ensure_ascii=False, indent=2))
    return payload


def run_annotation_pipeline(
    layout: ProjectLayout,
    complex_hits: list[ComplexityHit],
    *,
    plan_path: Path | None = None,
    annotate_fields: bool = True,
    max_files: int | None = None,
) -> dict:
    """旧路径：追加 OCA 解析块（保留兼容）。"""
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
