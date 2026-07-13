from __future__ import annotations
from pathlib import Path
import sys

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
main_path = ROOT / "app/src/main/java/app/tijario/MainActivity.kt"

for path in (ROOT / "app/src/main/java").rglob("*.kt"):
    if path == main_path or path.name == "AppRuntimeState.kt":
        continue
    source = path.read_text(encoding="utf-8")
    if "MainActivity." not in source:
        continue

    source = source.replace("MainActivity.currentLanguage", "AppRuntimeState.currentLanguage")
    source = source.replace("MainActivity.isDarkMode", "AppRuntimeState.isDarkMode")
    source = source.replace("MainActivity.authDeepLinkTarget", "AppRuntimeState.authDeepLinkTarget")
    source = source.replace("MainActivity.consumeAuthDeepLinkTarget()", "AppRuntimeState.consumeAuthDeepLinkTarget()")

    if "AppRuntimeState." in source and "import app.tijario.config.AppRuntimeState" not in source:
        if "import app.tijario.MainActivity\n" in source:
            source = source.replace(
                "import app.tijario.MainActivity\n",
                "import app.tijario.config.AppRuntimeState\n",
                1,
            )
        else:
            package_end = source.index("\n", source.index("package ")) + 1
            source = source[:package_end] + "\nimport app.tijario.config.AppRuntimeState\n" + source[package_end:]
    source = source.replace("import app.tijario.MainActivity\n", "")
    path.write_text(source, encoding="utf-8")

main = main_path.read_text(encoding="utf-8")
start = main.find("    /**\n     * Compatibility facade")
end_marker = "\n\n    override fun onCreate"
if start >= 0:
    end = main.find(end_marker, start)
    if end < 0:
        raise RuntimeError("MainActivity compatibility facade end not found")
    main = main[:start] + "    override fun onCreate" + main[end + len(end_marker):]
main_path.write_text(main, encoding="utf-8")

remaining = []
for path in (ROOT / "app/src/main/java").rglob("*.kt"):
    if path.name == "AppRuntimeState.kt":
        continue
    for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if "MainActivity." in line:
            remaining.append(f"{path.relative_to(ROOT)}:{line_number}:{line.strip()}")
if remaining:
    raise RuntimeError("Remaining MainActivity facade references:\n" + "\n".join(remaining))

print("MainActivity compatibility facade removed")
