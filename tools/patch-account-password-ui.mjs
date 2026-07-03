import fs from "node:fs";

const file = "app/src/main/java/app/tijario/ui/screens/SettingsScreens.kt";
let text = fs.readFileSync(file, "utf8");

function replaceOnce(oldValue, newValue, label) {
  const index = text.indexOf(oldValue);
  if (index < 0) throw new Error(`Missing patch target: ${label}`);
  text = text.slice(0, index) + newValue + text.slice(index + oldValue.length);
}

replaceOnce(
  "    var showDeleteConfirm by remember { mutableStateOf(false) }\n",
  "    var showDeleteConfirm by remember { mutableStateOf(false) }\n    var showChangePasswordDialog by remember { mutableStateOf(false) }\n",
  "dialog state",
);

const accountStart = text.indexOf("fun AccountSettingsScreen(");
if (accountStart < 0) throw new Error("AccountSettingsScreen not found");
const scaffold = text.indexOf("    Scaffold(\n", accountStart);
if (scaffold < 0) throw new Error("AccountSettingsScreen scaffold not found");

const dialog = `    ChangePasswordDialog(\n        visible = showChangePasswordDialog,\n        onDismiss = { showChangePasswordDialog = false },\n        onSuccess = { message ->\n            scope.launch { snackbarHostState.showSnackbar(message) }\n        },\n    )\n\n`;
text = text.slice(0, scaffold) + dialog + text.slice(scaffold);

const cardStart = text.indexOf("            // Change Password Row", accountStart);
const cardEnd = text.indexOf("            // Manual subscription sync support action", cardStart);
if (cardStart < 0 || cardEnd < 0) throw new Error("Legacy password card not found");

const card = `            // Change Password Row\n            Card(\n                modifier = Modifier\n                    .fillMaxWidth()\n                    .clickable { showChangePasswordDialog = true },\n                shape = RoundedCornerShape(16.dp),\n                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),\n                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)\n            ) {\n                Row(\n                    modifier = Modifier.padding(16.dp).fillMaxWidth(),\n                    verticalAlignment = Alignment.CenterVertically,\n                    horizontalArrangement = Arrangement.SpaceBetween\n                ) {\n                    Row(\n                        verticalAlignment = Alignment.CenterVertically,\n                        horizontalArrangement = Arrangement.spacedBy(12.dp)\n                    ) {\n                        Surface(\n                            color = Color(0xFFE6F4EA),\n                            shape = RoundedCornerShape(8.dp),\n                            modifier = Modifier.size(36.dp)\n                        ) {\n                            Box(contentAlignment = Alignment.Center) {\n                                Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF137333), modifier = Modifier.size(18.dp))\n                            }\n                        }\n                        Column(modifier = Modifier.weight(1f)) {\n                            Text(t("change_password"), fontWeight = FontWeight.Bold, fontSize = 14.sp)\n                            Text(t("update_password_desc"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)\n                        }\n                    }\n                }\n            }\n`;

text = text.slice(0, cardStart) + card + text.slice(cardEnd);
text = text.replace("import app.tijario.data.remote.ResetPasswordRequest\n", "");
text = text.replace("    val sendingPasswordLinkMsg = t(\"sending_password_link\")\n", "");
text = text.replace("    val passwordResetSentMsg = t(\"password_reset_link_sent\")\n", "");
text = text.replace("    val passwordResetFailedMsg = t(\"password_reset_link_failed\")\n", "");
text = text.replace("    var isPasswordResetLoading by remember { mutableStateOf(false) }\n", "");

if (text.includes("requestPasswordReset(ResetPasswordRequest")) {
  throw new Error("Legacy reset-link action still exists");
}

fs.writeFileSync(file, text, "utf8");
console.log("Account password UI patched.");
