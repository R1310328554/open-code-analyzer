# Copyright 2025 the HuggingFace Inc. team. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 跳过测试扫描：统计各模型对 common/generate 测试的 skip 覆盖情况


import argparse
import json
import re
from pathlib import Path


REPO_ROOT = Path().cwd()

COMMON_TEST_FILES: list[tuple[Path, str]] = [
    (REPO_ROOT / "tests/test_modeling_common.py", "common"),
    (REPO_ROOT / "tests/generation/test_utils.py", "GenerationMixin"),
]

MODELS_DIR = REPO_ROOT / "tests/models"


# get_common_tests：从公共测试文件提取 test_* 函数名及来源标签
def get_common_tests(file_paths_with_origin: list[tuple[Path, str]]) -> dict[str, str]:
    """Extract all common test function names (e.g., 'test_forward')."""
    tests_with_origin: dict[str, str] = {}
    for file_path, origin_tag in file_paths_with_origin:
        if not file_path.is_file():
            continue
        content = file_path.read_text(encoding="utf-8")
        for test_name in re.findall(r"^\s*def\s+(test_[A-Za-z0-9_]+)", content, re.MULTILINE):
            tests_with_origin[test_name] = origin_tag
    return tests_with_origin


# get_models_and_test_files：枚举 tests/models 下所有 test_modeling_*.py
def get_models_and_test_files(models_dir: Path) -> tuple[list[str], list[Path]]:
    if not models_dir.is_dir():
        raise FileNotFoundError(f"Models directory not found at {models_dir}")
    test_files: list[Path] = sorted(models_dir.rglob("test_modeling_*.py"))
    model_names: list[str] = sorted({file_path.parent.name for file_path in test_files})
    return model_names, test_files


# _extract_reason_from_decorators：从 @skip 装饰器块解析 skip reason 字符串
def _extract_reason_from_decorators(decorators_block: str) -> str:
    """Extracts the reason string from a decorator block, if any."""
    reason_match = re.search(r'reason\s*=\s*["\'](.*?)["\']', decorators_block)
    if reason_match:
        return reason_match.group(1)
    reason_match = re.search(r'\((?:.*?,\s*)?["\'](.*?)["\']\)', decorators_block)
    if reason_match:
        return reason_match.group(1)
    return decorators_block.strip().split("\n")[-1].strip()


# extract_test_info：解析单文件内各 test 的 RAN/SKIPPED 状态与原因
def extract_test_info(file_content: str) -> dict[str, tuple[str, str]]:
    """
    Parse a test file once and return a mapping of test functions to their
    status and skip reason, e.g. {'test_forward': ('SKIPPED', 'too slow')}.
    """
    result: dict[str, tuple[str, str]] = {}
    pattern = re.compile(r"((?:^\s*@.*?\n)*?)^\s*def\s+(test_[A-Za-z0-9_]+)\b", re.MULTILINE)
    for decorators_block, test_name in pattern.findall(file_content):
        if "skip" in decorators_block:
            result[test_name] = ("SKIPPED", _extract_reason_from_decorators(decorators_block))
        else:
            result[test_name] = ("RAN", "")
    return result


# build_model_overrides：汇总各模型 test 覆盖/跳过信息为嵌套字典
def build_model_overrides(model_test_files: list[Path]) -> dict[str, dict[str, tuple[str, str]]]:
    """Return *model_name → {test_name → (status, reason)}* mapping."""
    model_overrides: dict[str, dict[str, tuple[str, str]]] = {}
    for file_path in model_test_files:
        model_name = file_path.parent.name
        file_content = file_path.read_text(encoding="utf-8")
        model_overrides.setdefault(model_name, {}).update(extract_test_info(file_content))
    return model_overrides


# save_json：将扫描结果写入 JSON 文件
def save_json(obj: dict, output_path: Path) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(obj, indent=2), encoding="utf-8")


# summarize_single_test：打印并返回单个 common test 的 skip 统计
def summarize_single_test(
    test_name: str,
    model_names: list[str],
    model_overrides: dict[str, dict[str, tuple[str, str]]],
) -> dict[str, object]:
    """Print a concise terminal summary for *test_name* and return the raw data."""
    models_ran, models_skipped, reasons_for_skipping = [], [], []
    for model_name in model_names:
        status, reason = model_overrides.get(model_name, {}).get(test_name, ("RAN", ""))
        if status == "SKIPPED":
            models_skipped.append(model_name)
            reasons_for_skipping.append(f"{model_name}: {reason}")
        else:
            models_ran.append(model_name)

    total_models = len(model_names)
    skipped_ratio = len(models_skipped) / total_models if total_models else 0.0

    print(f"\n== {test_name} ==")
    print(f"Ran    : {len(models_ran)}/{total_models}")
    print(f"Skipped : {len(models_skipped)}/{total_models} ({skipped_ratio:.1%})")
    for reason_entry in reasons_for_skipping[:10]:
        print(f" - {reason_entry}")
    if len(reasons_for_skipping) > 10:
        print(" - ...")

    return {
        "models_ran": sorted(models_ran),
        "models_skipped": sorted(models_skipped),
        "skipped_proportion": round(skipped_ratio, 4),
        "reasons_skipped": sorted(reasons_for_skipping),
    }


# summarize_all_tests：聚合全部 common test 的 skip 比例与原因
def summarize_all_tests(
    tests_with_origin: dict[str, str],
    model_names: list[str],
    model_overrides: dict[str, dict[str, tuple[str, str]]],
) -> dict[str, object]:
    """Return aggregated data for every discovered common test."""
    results: dict[str, object] = {}
    total_models = len(model_names)
    test_names = list(tests_with_origin)

    print(f"[INFO] Aggregating {len(test_names)} tests...")
    for index, test_fn in enumerate(test_names, 1):
        print(f"  ({index}/{len(test_names)}) {test_fn}", end="\r")
        models_ran, models_skipped, reasons_for_skipping = [], [], []
        for model_name in model_names:
            status, reason = model_overrides.get(model_name, {}).get(test_fn, ("RAN", ""))
            if status == "SKIPPED":
                models_skipped.append(model_name)
                reasons_for_skipping.append(f"{model_name}: {reason}")
            else:
                models_ran.append(model_name)

        skipped_ratio = len(models_skipped) / total_models if total_models else 0.0
        results[test_fn] = {
            "origin": tests_with_origin[test_fn],
            "models_ran": sorted(models_ran),
            "models_skipped": sorted(models_skipped),
            "skipped_proportion": round(skipped_ratio, 4),
            "reasons_skipped": sorted(reasons_for_skipping),
        }
    print("\n[INFO] Scan complete.")
    return results


# main：CLI 入口，支持单测或全量扫描并输出 JSON
def main() -> None:
    parser = argparse.ArgumentParser(
        description="Scan model tests for overridden or skipped common or generate tests.",
    )
    parser.add_argument(
        "--output_dir",
        default=".",
        help="Directory for JSON output (default: %(default)s)",
    )
    parser.add_argument(
        "--test_method_name",
        help="Scan only this test method (single‑test mode)",
    )
    args = parser.parse_args()

    output_dir = Path(args.output_dir).expanduser()
    test_method_name = args.test_method_name

    tests_with_origin = get_common_tests(COMMON_TEST_FILES)
    if test_method_name:
        tests_with_origin = {test_method_name: tests_with_origin.get(test_method_name, "unknown")}

    model_names, model_test_files = get_models_and_test_files(MODELS_DIR)
    print(f"[INFO] Parsing {len(model_test_files)} model test files once each...")
    model_overrides = build_model_overrides(model_test_files)

    if test_method_name:
        data = summarize_single_test(test_method_name, model_names, model_overrides)
        json_path = output_dir / f"scan_{test_method_name}.json"
    else:
        data = summarize_all_tests(tests_with_origin, model_names, model_overrides)
        json_path = output_dir / "all_tests_scan_result.json"
    save_json(data, json_path)
    print(f"\n[INFO] JSON saved to {json_path.resolve()}")


if __name__ == "__main__":
    main()
