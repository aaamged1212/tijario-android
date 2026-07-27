package app.tijario.domain

import app.tijario.config.AppLanguage
import app.tijario.config.Localization

object LocalizedErrorMapper {
    private val codeToKey = mapOf(
        "DOCUMENT_LIMIT_REACHED" to "doc_error_limit_reached",
        "document_limit_reached" to "doc_error_limit_reached",
        "QUOTA_LIMIT_EXCEEDED" to "doc_error_limit_reached",
        "quota_limit_exceeded" to "doc_error_limit_reached",
        "AI_LIMIT_REACHED" to "ai_limit_reached",
        "TEMPLATE_NOT_ALLOWED" to "doc_error_template_not_allowed",
        "template_not_allowed" to "doc_error_template_not_allowed",
        "ai_limit_reached" to "ai_limit_reached",
        "ai_generation_failed" to "doc_error_unexpected",
        "ai_reply_failed" to "doc_error_unexpected",
        "ai_caption_failed" to "doc_error_unexpected",
        "ai_refine_failed" to "doc_error_unexpected",
        "ai_report_failed" to "doc_error_unexpected",
        "PLAN_REQUIRED" to "doc_error_plan_required",
        "invalid_document_input" to "doc_error_invalid_input",
        "invalid_document_type" to "doc_error_invalid_input",
        "invalid_document_total" to "doc_error_total_negative",
        "document_customer_invalid" to "doc_error_invalid_input",
        "document_items_invalid" to "doc_error_invalid_input",
        "document_template_invalid" to "doc_error_template_not_allowed",
        "document_number_collision" to "doc_error_save_failed",
        "document_create_failed" to "doc_error_save_failed",
        "document_update_failed" to "doc_error_update_failed",
        "out_of_stock" to "out_of_stock",
        "insufficient_stock" to "insufficient_stock",
        "SERVER_SAVE_FAILED" to "doc_error_server_save_failed",
        "server_save_failed" to "doc_error_server_save_failed",
        "SERVER_UPDATE_FAILED" to "doc_error_server_update_failed",
        "server_update_failed" to "doc_error_server_update_failed",
        "invalid_api_response" to "doc_error_server_error",
        "document_items_delete_failed" to "doc_error_server_error",
        "document_delete_failed" to "doc_error_server_error",
        "document_not_found" to "doc_error_document_not_found",
        "product_load_failed" to "doc_error_product_load_failed",
        "saved_product_not_found" to "doc_error_saved_product_not_found",
        "customer_load_failed" to "doc_error_customer_load_failed",
        "customer_create_failed" to "doc_error_customer_create_failed",
        "customer_update_failed" to "doc_error_customer_update_failed",
        "business_settings_load_failed" to "doc_error_server_error",
        "business_settings_save_failed" to "doc_error_server_error",
        "server_error" to "doc_error_server_error",
        "server_admin_client_unavailable" to "doc_error_server_admin_client_unavailable",
        "select_customer_first" to "select_customer_first",
        "add_one_item_min" to "add_one_item_min",
        "enter_item_details_correctly" to "enter_item_details_correctly",
        "MISSING_DOCUMENT_ITEMS" to "doc_error_missing_document_items",
        "missing_document_items" to "doc_error_missing_document_items",
        "SYNC_FAILED" to "doc_error_sync_failed",
        "sync_failed" to "doc_error_sync_failed",
        "BLOCKED_BY_PLAN" to "doc_error_blocked_by_plan",
        "blocked_by_plan" to "doc_error_blocked_by_plan",
        "CONFLICT" to "doc_error_conflict",
        "conflict" to "doc_error_conflict",
        "CUSTOMER_LIMIT_REACHED" to "doc_error_customer_limit_reached",
        "PRODUCT_LIMIT_REACHED" to "doc_error_product_limit_reached",
        "customer_limit_reached" to "doc_error_customer_limit_reached",
        "product_limit_reached" to "doc_error_product_limit_reached",
        "delete_failed" to "doc_error_server_error",
        "unauthorized" to "error_session_expired",
        "unauthenticated" to "error_session_expired",
        "invalid_current_password" to "change_password_invalid_current",
        "password_mismatch" to "validation_password_mismatch",
        "billing_verification_failed" to "billing_verification_failed",
        "billing_ownership_conflict" to "billing_ownership_conflict",
        "billing_unavailable" to "billing_unavailable",
        "billing_product_unavailable" to "billing_product_unavailable",
        "billing_products_load_failed" to "billing_products_load_failed",
        "billing_no_active_purchases" to "billing_no_active_purchases",
        "billing_no_purchase_returned" to "billing_no_purchase_returned",
        "billing_google_play_failed" to "billing_google_play_failed",
        "billing_missing_product_data" to "billing_missing_product_data",
        "billing_restore_failed" to "billing_restore_failed",
        "billing_purchase_pending" to "billing_purchase_pending",
        "billing_plan_refresh_failed" to "billing_unavailable",
        "notifications_refresh_failed" to "doc_error_unexpected",
        "ENTITLEMENT_INITIALIZATION_REQUIRED" to "account_initialization_retry",
        "OFFLINE_LEASE_REQUIRED" to "offline_lease_required",
        "ENTITLEMENT_SIGNATURE_INVALID" to "account_initialization_invalid",
        "ENTITLEMENT_EXPIRED" to "account_initialization_invalid",
        "DEVICE_NOT_REGISTERED" to "account_initialization_retry",
        "NETWORK_UNAVAILABLE" to "account_initialization_retry",
        "LOCAL_DATABASE_WRITE_FAILED" to "save_settings_error",
        "INVALID_ONBOARDING_FIELDS" to "save_settings_error",
        "backup_installation_not_registered" to "backup_installation_not_registered",
        "backup_key_unavailable" to "backup_key_unavailable",
        "backup_device_key_invalid" to "backup_device_key_invalid",
        "server_configuration_error" to "backup_server_configuration_error",
    )

    private val messagePatterns = linkedMapOf(
        "select a customer first" to "select_customer_first",
        "add at least one item first" to "add_one_item_min",
        "enter item details correctly" to "enter_item_details_correctly",
        "quota limit exceeded" to "doc_error_limit_reached",
        "template is not available" to "doc_error_template_not_allowed",
        "document customer invalid" to "doc_error_invalid_input",
        "document items invalid" to "doc_error_invalid_input",
        "document template invalid" to "doc_error_template_not_allowed",
        "document type invalid" to "doc_error_invalid_input",
        "document number collision" to "doc_error_save_failed",
        "customer limit reached" to "doc_error_customer_limit_reached",
        "product limit reached" to "doc_error_product_limit_reached",
        "session expired" to "error_session_expired",
        "unexpected error" to "doc_error_unexpected",
        "could not verify your current plan" to "doc_error_plan_required",
        "could not save the document" to "doc_error_save_failed",
        "could not update the document" to "doc_error_update_failed",
        "server save failed" to "doc_error_server_save_failed",
        "server update failed" to "doc_error_server_update_failed",
        "missing document items" to "doc_error_missing_document_items",
        "sync failed" to "doc_error_sync_failed",
        "blocked by plan" to "doc_error_blocked_by_plan",
        "conflict" to "doc_error_conflict",
        "product or service not found" to "doc_error_saved_product_not_found",
        "saved product not found" to "doc_error_saved_product_not_found",
        "out of stock" to "out_of_stock",
        "exceeds available stock" to "insufficient_stock",
    )

    fun map(code: String?, message: String?, language: AppLanguage, operation: String? = null): String {
        val key = resolveKey(code, message)
        if (key != null) {
            return Localization.getString(key, language)
        }

        val sanitizedMessage = message?.trim().orEmpty()
        if (sanitizedMessage.isNotBlank()) {
            return Localization.getString("doc_error_unexpected", language)
        }

        return when (language) {
            AppLanguage.AR -> Localization.getString("doc_error_unexpected", language)
            AppLanguage.EN -> Localization.getString("doc_error_unexpected", language)
        }
    }

    private fun resolveKey(code: String?, message: String?): String? {
        val normalizedCode = code?.trim().orEmpty()
        if (normalizedCode.isNotBlank()) {
            codeToKey[normalizedCode]?.let { return it }
            codeToKey[normalizedCode.uppercase()]?.let { return it }
        }

        val normalizedMessage = message?.trim().orEmpty().lowercase()
        if (normalizedMessage.isBlank()) return null

        messagePatterns.forEach { (needle, key) ->
            if (normalizedMessage.contains(needle)) {
                return key
            }
        }

        return null
    }
}
