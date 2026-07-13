from __future__ import annotations
from pathlib import Path
import sys

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()

main_path = ROOT / "app/src/main/java/app/tijario/MainActivity.kt"
main = main_path.read_text(encoding="utf-8")

for unused in [
    "import androidx.compose.runtime.getValue\n",
    "import androidx.compose.runtime.mutableStateOf\n",
    "import androidx.compose.runtime.setValue\n",
]:
    main = main.replace(unused, "")

if "import app.tijario.config.AppRuntimeState\n" not in main:
    main = main.replace(
        "import app.tijario.config.AppPreferences\n",
        "import app.tijario.config.AppPreferences\nimport app.tijario.config.AppRuntimeState\n",
    )

old_companion = '''class MainActivity : ComponentActivity() {
    companion object {
        var currentLanguage by mutableStateOf(AppLanguage.AR)
        var isDarkMode by mutableStateOf(false)
        var authDeepLinkTarget by mutableStateOf<String?>(null)
            private set

        fun consumeAuthDeepLinkTarget() {
            authDeepLinkTarget = null
        }
    }
'''
new_class = '''class MainActivity : ComponentActivity() {
'''
if old_companion in main:
    main = main.replace(old_companion, new_class, 1)
elif new_class not in main:
    raise RuntimeError("MainActivity companion block not found")

main = main.replace(
    '''        currentLanguage = AppPreferences.getLanguage(applicationContext)
        isDarkMode = AppPreferences.getDarkMode(applicationContext)
        setContent {
            TijarioTheme(darkTheme = isDarkMode, language = currentLanguage) {
                val layoutDirection = if (currentLanguage == AppLanguage.AR) {''',
    '''        AppRuntimeState.restorePreferences(applicationContext)
        setContent {
            TijarioTheme(
                darkTheme = AppRuntimeState.isDarkMode,
                language = AppRuntimeState.currentLanguage,
            ) {
                val layoutDirection = if (AppRuntimeState.currentLanguage == AppLanguage.AR) {''',
    1,
)
main = main.replace(
    "                    LocalLanguage provides currentLanguage",
    "                    LocalLanguage provides AppRuntimeState.currentLanguage",
    1,
)
main = main.replace(
    '''        authDeepLinkTarget = uri.getQueryParameter("next")?.takeIf { it.startsWith("/") } ?: "/login"''',
    '''        AppRuntimeState.setAuthDeepLinkTarget(
            uri.getQueryParameter("next")?.takeIf { it.startsWith("/") } ?: "/login",
        )''',
    1,
)
main_path.write_text(main, encoding="utf-8")

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

remaining = []
for path in (ROOT / "app/src/main/java").rglob("*.kt"):
    for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if "MainActivity." in line:
            remaining.append(f"{path.relative_to(ROOT)}:{number}:{line.strip()}")
if remaining:
    raise RuntimeError("Remaining MainActivity global references:\n" + "\n".join(remaining))

print("MainActivity global state moved to AppRuntimeState")
