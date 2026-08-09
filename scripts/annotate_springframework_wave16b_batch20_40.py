#!/usr/bin/env python3
"""Chinese-annotate Spring Framework 7.0.8 wave-16b batch [20:40]."""
from __future__ import annotations
import importlib.util, json, re, shutil, subprocess, sys
from pathlib import Path
ROOT = Path("/workspace")
VER = ROOT / "springframework/7.0.8"
ORIGINAL, ANALYZED, QUEUE, SCRIPTS = VER/"original", VER/"analyzed", VER/"_reports/class-queue", ROOT/"scripts"
BATCH_FILES = [ln.strip() for ln in Path("/tmp/sf_w16b.txt").read_text().splitlines() if ln.strip()]
FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}
def _load(name, attr):
    spec = importlib.util.spec_from_file_location(name.replace(".py",""), SCRIPTS/name)
    mod = importlib.util.module_from_spec(spec); spec.loader.exec_module(mod)
    return getattr(mod, attr)
for m,a in [
    ("wave16b_replacements_tx_exceptions.py","TX_EXCEPTIONS_REPLACEMENTS"),
    ("wave16b_replacements_tx_enums_parsers.py","TX_ENUMS_PARSERS_REPLACEMENTS"),
    ("wave16b_replacements_tx_annotation_config.py","TX_ANNOTATION_CONFIG_REPLACEMENTS"),
    ("wave16b_replacements_tx_enable_configurer.py","TX_ENABLE_CONFIGURER_REPLACEMENTS"),
]:
    FILE_REPLACEMENTS.update(_load(m,a))
def apply_replacements(text, reps):
    for old,new in reps:
        if old not in text: raise ValueError(f"Pattern not found: {old[:100]}")
        text = text.replace(old,new,1)
    return text
def main():
    failures=[]; ok=0
    for rel in BATCH_FILES:
        name=Path(rel).name; src=ORIGINAL/rel; dst=ANALYZED/rel
        dst.parent.mkdir(parents=True, exist_ok=True); shutil.copy2(src,dst)
        try:
            text=apply_replacements(dst.read_text(encoding="utf-8"), FILE_REPLACEMENTS[name])
            cn=len(re.findall(r"[\u4e00-\u9fff]", text))
            if cn<10 or "Licensed under the Apache License" not in text: raise ValueError(f"cn={cn}")
            dst.write_text(text,encoding="utf-8"); ok+=1; print(f"OK {rel} cn={cn}")
        except Exception as e: failures.append(f"{rel}: {e}"); print(f"FAIL {rel}: {e}")
    if ok==len(BATCH_FILES) and not failures:
        subprocess.run([sys.executable,str(SCRIPTS/"mark_batch_done.py"),"--project","springframework","--version","7.0.8","--note","wave16b",*BATCH_FILES],check=True)
        batch=json.loads((QUEUE/"batch.json").read_text())
        batch["done"]=len([x for x in (QUEUE/"done.txt").read_text().splitlines() if x.strip()])
        batch["remaining_pending"]=len([x for x in (QUEUE/"pending.txt").read_text().splitlines() if x.strip()])
        (QUEUE/"batch.json").write_text(json.dumps(batch,ensure_ascii=False,indent=2)+"\n")
    print(json.dumps({"ok":ok,"failures":failures})); return 1 if failures else 0
if __name__=="__main__": raise SystemExit(main())
