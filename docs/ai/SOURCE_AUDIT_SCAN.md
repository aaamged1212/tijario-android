# Remaining source audit scan

## Force unwraps
None.

## Package-level print helper usage
None.

## Empty or silent catches
app/src/main/java/app/tijario/domain/DocumentCalculator.kt:87:        } catch (e: Exception) {
app/src/main/java/app/tijario/domain/DocumentCalculator.kt:97:        } catch (e: Exception) {
app/src/main/java/app/tijario/features/sync/SyncWorker.kt:26:        } catch (error: Exception) {
app/src/main/java/app/tijario/features/business/logo/LogoAssetManager.kt:44:        } catch (e: Exception) {
app/src/main/java/app/tijario/features/business/logo/LogoAssetManager.kt:93:                } catch (e: Exception) {
app/src/main/java/app/tijario/features/business/logo/LogoAssetManager.kt:120:        } catch (e: Exception) {
app/src/main/java/app/tijario/features/documents/pdf/LocalPdfGenerator.kt:156:                } catch (_: Exception) {
app/src/main/java/app/tijario/features/documents/export/DocumentDownloadManager.kt:59:        } catch (error: Throwable) {
app/src/main/java/app/tijario/ui/screens/FormScreens.kt:548:                                } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/FormScreens.kt:612:                            } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/FormScreens.kt:781:            } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/FormScreens.kt:1048:                                } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/FormScreens.kt:1114:                } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/FormScreens.kt:1538:                        } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/FormScreens.kt:2627:            } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/DocumentDetailScreen.kt:217:                            } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/DocumentDetailScreen.kt:382:                                    } catch (_: ActivityNotFoundException) {
app/src/main/java/app/tijario/ui/screens/DocumentDetailScreen.kt:384:                                    } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/AuthScreens.kt:251:                                } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/AuthScreens.kt:486:                                } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/AuthScreens.kt:822:                                        } catch (otpEx: Exception) {
app/src/main/java/app/tijario/ui/screens/AuthScreens.kt:836:                                } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/AuthScreens.kt:870:                                    } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/AuthScreens.kt:1000:                                    } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/AuthScreens.kt:1058:                } catch (e: Exception) { if (language == AppLanguage.AR) "السعودية" else "Saudi Arabia" },
app/src/main/java/app/tijario/ui/screens/AuthScreens.kt:1344:                                } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/SettingsScreens.kt:382:            } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/SettingsScreens.kt:1046:                                } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:1035:                                    } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:1306:                            } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:1734:                                            } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:1755:                                            } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:1912:                            } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:2370:            } catch (_: ActivityNotFoundException) {
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:2372:            } catch (_: Exception) {
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:2404:            } catch (_: Exception) {
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:2727:                                        } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:3022:            } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:3103:                            } catch (e: Exception) {
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:3119:                            } catch (e: Exception) {
app/src/main/java/app/tijario/ui/state/AuthViewModel.kt:69:            } catch (e: Exception) {
app/src/main/java/app/tijario/ui/state/AuthViewModel.kt:163:            } catch (e: Exception) {
app/src/main/java/app/tijario/ui/state/AuthViewModel.kt:189:            } catch (_: Exception) {
app/src/main/java/app/tijario/data/repository/TijarioRepository.kt:879:        } catch (e: Exception) {
app/src/main/java/app/tijario/data/repository/TijarioRepository.kt:963:        } catch (e: Exception) {
app/src/main/java/app/tijario/data/repository/TijarioRepository.kt:996:        } catch (e: Exception) {
app/src/main/java/app/tijario/data/repository/TijarioRepository.kt:1810:                } catch (error: Exception) {

## Kotlin files by line count
  29048 total
   4798 app/src/main/java/app/tijario/ui/screens/FormScreens.kt
   3407 app/src/main/java/app/tijario/ui/screens/CoreScreens.kt
   2548 app/src/main/java/app/tijario/ui/screens/SettingsScreens.kt
   2277 app/src/main/java/app/tijario/data/repository/TijarioRepository.kt
   1548 app/src/main/java/app/tijario/features/ai/AiScreens.kt
   1547 app/src/main/java/app/tijario/ui/screens/AuthScreens.kt
    975 app/src/main/java/app/tijario/ui/TijarioApp.kt
    635 app/src/main/java/app/tijario/config/Localization.kt
    634 app/src/main/java/app/tijario/data/local/TijarioEntities.kt
    617 app/src/main/java/app/tijario/data/remote/BackendApiClient.kt
    529 app/src/main/java/app/tijario/data/remote/ApiContracts.kt
    476 app/src/main/java/app/tijario/ui/screens/DocumentDetailScreen.kt
    442 app/src/main/java/app/tijario/features/notifications/NotificationsScreens.kt
    404 app/src/main/java/app/tijario/data/local/TijarioDatabase.kt
    378 app/src/main/java/app/tijario/features/documents/template/DocumentHtmlRenderer.kt
    366 app/src/main/java/app/tijario/ui/state/TijarioDataViewModel.kt
    336 app/src/main/java/app/tijario/ui/components/TijarioComponents.kt
    280 app/src/main/java/app/tijario/features/billing/GooglePlayBillingRepository.kt
    269 app/src/main/java/app/tijario/features/documents/pdf/LocalPdfGenerator.kt
    258 app/src/main/java/app/tijario/data/local/TijarioDao.kt
    252 app/src/main/java/app/tijario/ui/screens/ChangePasswordScreen.kt
    230 app/src/main/java/app/tijario/features/ai/AiViewModel.kt
    217 app/src/main/java/app/tijario/features/documents/template/DocumentTemplateRegistry.kt
    209 app/src/main/java/app/tijario/ui/state/AuthViewModel.kt
    203 app/src/main/java/app/tijario/ui/state/FormState.kt
    194 app/src/main/java/app/tijario/features/documents/preview/DocumentPreviewWebView.kt
    193 app/src/main/java/app/tijario/ui/components/StoreLogoSupport.kt
    193 app/src/main/java/app/tijario/config/AppPreferences.kt
    191 app/src/main/java/app/tijario/features/documents/ui/DocumentTemplatePicker.kt
    190 app/src/main/java/app/tijario/features/billing/BillingViewModel.kt
    183 app/src/main/java/app/tijario/data/model/TijarioModels.kt
    160 app/src/main/java/app/tijario/features/notifications/NotificationsViewModel.kt
    160 app/src/main/java/app/tijario/features/notifications/NotificationsRepository.kt
    160 app/src/main/java/app/tijario/domain/DashboardStatsCalculator.kt
    150 app/src/main/java/app/tijario/domain/LocalizedErrorMapper.kt
    140 app/src/main/java/app/tijario/ui/components/GoogleSignInButton.kt
    139 app/src/main/java/app/tijario/features/documents/mapper/SavedDocumentRenderMapper.kt
    137 app/src/main/java/app/tijario/ui/theme/Type.kt
    127 app/src/main/java/app/tijario/domain/DocumentCalculator.kt

## MainActivity compatibility facade usage
app/src/main/java/app/tijario/features/notifications/NotificationsViewModel.kt:78:                            errorMessage = if (MainActivity.currentLanguage == AppLanguage.AR) {
app/src/main/java/app/tijario/features/ai/AiViewModel.kt:179:        val mapped = LocalizedErrorMapper.map(null, error.message, MainActivity.currentLanguage)
app/src/main/java/app/tijario/features/ai/AiViewModel.kt:184:        Localization.getString("ai_limit_reached", MainActivity.currentLanguage)
app/src/main/java/app/tijario/features/ai/AiViewModel.kt:186:    private fun localizedReplyError(): String = if (MainActivity.currentLanguage == AppLanguage.AR) {
app/src/main/java/app/tijario/features/ai/AiViewModel.kt:192:    private fun localizedCaptionError(): String = if (MainActivity.currentLanguage == AppLanguage.AR) {
app/src/main/java/app/tijario/features/ai/AiViewModel.kt:198:    private fun localizedRefineError(): String = if (MainActivity.currentLanguage == AppLanguage.AR) {
app/src/main/java/app/tijario/features/ai/AiViewModel.kt:204:    private fun localizedReportError(): String = if (MainActivity.currentLanguage == AppLanguage.AR) {
app/src/main/java/app/tijario/features/ai/AiViewModel.kt:210:    private fun localizedReportSuccess(): String = if (MainActivity.currentLanguage == AppLanguage.AR) {
app/src/main/java/app/tijario/features/ai/AiViewModel.kt:216:    private fun localizedOfflineMessage(): String = if (MainActivity.currentLanguage == AppLanguage.AR) {
app/src/main/java/app/tijario/features/ai/AiScreens.kt:103:    val isDark = MainActivity.isDarkMode
app/src/main/java/app/tijario/ui/TijarioApp.kt:199:                notificationsViewModel.syncTopic(MainActivity.currentLanguage)
app/src/main/java/app/tijario/ui/TijarioApp.kt:219:            val authDeepLinkTarget = MainActivity.authDeepLinkTarget
app/src/main/java/app/tijario/ui/TijarioApp.kt:232:                    MainActivity.consumeAuthDeepLinkTarget()
app/src/main/java/app/tijario/ui/TijarioApp.kt:327:            LaunchedEffect(dataUiState.userId, MainActivity.currentLanguage) {
app/src/main/java/app/tijario/ui/TijarioApp.kt:328:                notificationsViewModel.syncTopic(MainActivity.currentLanguage)
app/src/main/java/app/tijario/ui/TijarioApp.kt:342:                    language = MainActivity.currentLanguage,
app/src/main/java/app/tijario/ui/TijarioApp.kt:359:                        notificationsViewModel.syncTopic(MainActivity.currentLanguage)
app/src/main/java/app/tijario/ui/TijarioApp.kt:450:                            val selectedNavAccent = if (MainActivity.isDarkMode) Color(0xFF14B8A6) else Color(0xFF0D9488)
app/src/main/java/app/tijario/ui/TijarioApp.kt:903:                else -> LocalizedErrorMapper.map(null, state.message, MainActivity.currentLanguage)
app/src/main/java/app/tijario/ui/screens/AuthScreens.kt:79:            MainActivity.currentLanguage =
app/src/main/java/app/tijario/ui/screens/AuthScreens.kt:81:            AppPreferences.setLanguage(context, MainActivity.currentLanguage)
app/src/main/java/app/tijario/ui/screens/SettingsScreens.kt:908:                            MainActivity.currentLanguage = AppLanguage.AR
app/src/main/java/app/tijario/ui/screens/SettingsScreens.kt:918:                            MainActivity.currentLanguage = AppLanguage.EN
app/src/main/java/app/tijario/ui/screens/SettingsScreens.kt:1020:                                    text = if (MainActivity.isDarkMode) t("theme_dark") else t("theme_light"),
app/src/main/java/app/tijario/ui/screens/SettingsScreens.kt:1028:                            checked = MainActivity.isDarkMode,
app/src/main/java/app/tijario/ui/screens/SettingsScreens.kt:1030:                                MainActivity.isDarkMode = it
app/src/main/java/app/tijario/ui/screens/SettingsScreens.kt:1639:    val isDarkTheme = MainActivity.isDarkMode
app/src/main/java/app/tijario/ui/screens/SettingsScreens.kt:2203:    val isDarkTheme = MainActivity.isDarkMode
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:3156:                                MainActivity.currentLanguage = AppLanguage.AR
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:3159:                                Text(t("language_arabic"), color = if (MainActivity.currentLanguage == AppLanguage.AR) MaterialTheme.colorScheme.primary else Color.Gray)
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:3162:                                MainActivity.currentLanguage = AppLanguage.EN
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:3165:                                Text(t("language_english"), color = if (MainActivity.currentLanguage == AppLanguage.EN) MaterialTheme.colorScheme.primary else Color.Gray)
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:3177:                            checked = MainActivity.isDarkMode,
app/src/main/java/app/tijario/ui/screens/CoreScreens.kt:3179:                                MainActivity.isDarkMode = it
app/src/main/java/app/tijario/ui/state/TijarioDataViewModel.kt:156:                    Localization.getString("billing_plan_refresh_failed", MainActivity.currentLanguage)
app/src/main/java/app/tijario/ui/state/AuthViewModel.kt:86:                        LocalizedErrorMapper.map(null, e.message, MainActivity.currentLanguage)
app/src/main/java/app/tijario/ui/state/AuthViewModel.kt:103:                            Localization.getString("error_after_verification_check", MainActivity.currentLanguage)
app/src/main/java/app/tijario/ui/state/AuthViewModel.kt:112:                        Localization.getString("google_login_error", MainActivity.currentLanguage)
app/src/main/java/app/tijario/ui/state/AuthViewModel.kt:120:                        Localization.getString("error_network", MainActivity.currentLanguage)
app/src/main/java/app/tijario/ui/state/AuthViewModel.kt:165:                    LocalizedErrorMapper.map(null, e.message, MainActivity.currentLanguage)
app/src/main/java/app/tijario/config/AppRuntimeState.kt:10: * Persistent preferences are restored whenever the process creates MainActivity.

## Inline stdout/stacktrace usage
None.
