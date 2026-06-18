package app.tijario.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage { AR, EN }

val LocalLanguage = compositionLocalOf { AppLanguage.AR }

object Localization {
    private val translations = mapOf(
        "app_name" to mapOf(AppLanguage.AR to "تيجاريو", AppLanguage.EN to "Tijario"),
        "app_slogan" to mapOf(AppLanguage.AR to "أدر تجارتك وفواتيرك بذكاء وسرعة", AppLanguage.EN to "Manage your business and invoices smartly"),

        // Navigation / Tabs
        "tab_home" to mapOf(AppLanguage.AR to "الرئيسية", AppLanguage.EN to "Home"),
        "tab_customers" to mapOf(AppLanguage.AR to "العملاء", AppLanguage.EN to "Customers"),
        "tab_products" to mapOf(AppLanguage.AR to "المنتجات", AppLanguage.EN to "Products"),
        "tab_documents" to mapOf(AppLanguage.AR to "المستندات", AppLanguage.EN to "Documents"),
        "tab_ai" to mapOf(AppLanguage.AR to "الذكاء الاصطناعي", AppLanguage.EN to "AI Tools"),
        "tab_account" to mapOf(AppLanguage.AR to "الحساب", AppLanguage.EN to "Account"),

        // Login Screen
        "login_title" to mapOf(AppLanguage.AR to "تسجيل الدخول", AppLanguage.EN to "Login"),
        "login_subtitle" to mapOf(AppLanguage.AR to "ادخل إلى حساب تجاري لمتابعة إدارة متجرك.", AppLanguage.EN to "Sign in to manage your store and invoices."),
        "email" to mapOf(AppLanguage.AR to "البريد الإلكتروني", AppLanguage.EN to "Email"),
        "password" to mapOf(AppLanguage.AR to "كلمة المرور", AppLanguage.EN to "Password"),
        "forgot_password" to mapOf(AppLanguage.AR to "نسيت كلمة المرور؟", AppLanguage.EN to "Forgot Password?"),
        "btn_login" to mapOf(AppLanguage.AR to "دخول", AppLanguage.EN to "Login"),
        "or" to mapOf(AppLanguage.AR to "أو", AppLanguage.EN to "Or"),
        "google_login" to mapOf(AppLanguage.AR to "تسجيل الدخول باستخدام Google", AppLanguage.EN to "Sign in with Google"),
        "google_register" to mapOf(AppLanguage.AR to "التسجيل باستخدام Google", AppLanguage.EN to "Sign up with Google"),
        "no_account" to mapOf(AppLanguage.AR to "ليس لديك حساب؟ ", AppLanguage.EN to "Don't have an account? "),
        "create_account" to mapOf(AppLanguage.AR to "أنشئ حساباً جديداً", AppLanguage.EN to "Create an account"),

        // Register Screen
        "register_title" to mapOf(AppLanguage.AR to "إنشاء حساب جديد", AppLanguage.EN to "Register"),
        "register_subtitle" to mapOf(AppLanguage.AR to "انضم إلى تيجاريو وابدأ في تنظيم أعمالك اليوم", AppLanguage.EN to "Join Tijario and organize your business today"),
        "fullname" to mapOf(AppLanguage.AR to "الاسم الكامل", AppLanguage.EN to "Full Name"),
        "already_have_account" to mapOf(AppLanguage.AR to "لديك حساب بالفعل؟ ", AppLanguage.EN to "Already have an account? "),

        // Forgot Password
        "reset_password_title" to mapOf(AppLanguage.AR to "استعادة كلمة المرور", AppLanguage.EN to "Reset Password"),
        "reset_password_subtitle" to mapOf(AppLanguage.AR to "سنرسل لك رابطاً لإعادة تعيين كلمة المرور", AppLanguage.EN to "We will send you a reset link to your email"),
        "btn_send_reset" to mapOf(AppLanguage.AR to "إرسال رابط الاستعادة", AppLanguage.EN to "Send Reset Link"),
        "back_to_login" to mapOf(AppLanguage.AR to "العودة لتسجيل الدخول", AppLanguage.EN to "Back to Login"),
        "reset_success" to mapOf(AppLanguage.AR to "تم إرسال رابط استعادة كلمة المرور بنجاح إلى بريدك الإلكتروني.", AppLanguage.EN to "Reset link has been successfully sent to your email."),

        // Onboarding / Shop Setup
        "onboarding_title" to mapOf(AppLanguage.AR to "تهيئة حساب متجرك", AppLanguage.EN to "Setup Your Store"),
        "onboarding_subtitle" to mapOf(AppLanguage.AR to "أدخل بيانات نشاطك التجاري لبدء إصدار الفواتير", AppLanguage.EN to "Enter business details to start issuing documents"),
        "shop_name" to mapOf(AppLanguage.AR to "اسم المتجر / النشاط التجاري", AppLanguage.EN to "Store / Business Name"),
        "whatsapp_phone" to mapOf(AppLanguage.AR to "رقم واتساب للتواصل", AppLanguage.EN to "WhatsApp Phone Number"),
        "country" to mapOf(AppLanguage.AR to "الدولة", AppLanguage.EN to "Country"),
        "currency" to mapOf(AppLanguage.AR to "العملة", AppLanguage.EN to "Currency"),
        "btn_save_continue" to mapOf(AppLanguage.AR to "حفظ وإكمال الإعداد", AppLanguage.EN to "Save & Continue"),

        // Dashboard
        "welcome" to mapOf(AppLanguage.AR to "أهلاً بك في تيجاريو 👋", AppLanguage.EN to "Welcome to Tijario 👋"),
        "dash_subtitle" to mapOf(AppLanguage.AR to "متابعة سريعة لأداء عملك التجاري اليوم", AppLanguage.EN to "Quick overview of your business performance"),
        "financial_summary" to mapOf(AppLanguage.AR to "الملخص المالي العام", AppLanguage.EN to "Financial Summary"),
        "this_month" to mapOf(AppLanguage.AR to "هذا الشهر", AppLanguage.EN to "This Month"),
        "paid_invoices" to mapOf(AppLanguage.AR to "الفواتير المدفوعة", AppLanguage.EN to "Paid Invoices"),
        "pending_quotes" to mapOf(AppLanguage.AR to "عروض الأسعار المعلقة", AppLanguage.EN to "Pending Quotes"),
        "quick_actions" to mapOf(AppLanguage.AR to "إجراءات سريعة", AppLanguage.EN to "Quick Actions"),
        "btn_new_invoice" to mapOf(AppLanguage.AR to "فاتورة جديدة", AppLanguage.EN to "New Invoice"),
        "btn_new_quote" to mapOf(AppLanguage.AR to "عرض سعر جديد", AppLanguage.EN to "New Quote"),
        "btn_add_customer" to mapOf(AppLanguage.AR to "إضافة عميل", AppLanguage.EN to "Add Customer"),

        // Customers Screen
        "customers_title" to mapOf(AppLanguage.AR to "العملاء", AppLanguage.EN to "Customers"),
        "customers_subtitle" to mapOf(AppLanguage.AR to "سجل العملاء وإدارة الحسابات المسجلة", AppLanguage.EN to "Customer registry and registered accounts"),
        "search_placeholder" to mapOf(AppLanguage.AR to "البحث عن عميل...", AppLanguage.EN to "Search for a customer..."),

        // Documents Screen
        "documents_title" to mapOf(AppLanguage.AR to "المستندات", AppLanguage.EN to "Documents"),
        "documents_subtitle" to mapOf(AppLanguage.AR to "استعراض وإدارة جميع فواتيرك وعروض الأسعار", AppLanguage.EN to "View and manage all your invoices and quotes"),
        "doc_status_paid" to mapOf(AppLanguage.AR to "مدفوعة", AppLanguage.EN to "Paid"),
        "doc_status_draft" to mapOf(AppLanguage.AR to "مسودة", AppLanguage.EN to "Draft"),
        "doc_status_expired" to mapOf(AppLanguage.AR to "منتهية", AppLanguage.EN to "Expired"),
        "section_invoices" to mapOf(AppLanguage.AR to "قسم الفواتير", AppLanguage.EN to "Invoices Section"),
        "section_quotes" to mapOf(AppLanguage.AR to "قسم عروض الأسعار", AppLanguage.EN to "Quotes Section"),
        "btn_create_invoice" to mapOf(AppLanguage.AR to "إنشاء فاتورة جديدة", AppLanguage.EN to "Create New Invoice"),
        "btn_create_quote" to mapOf(AppLanguage.AR to "إنشاء عرض سعر جديد", AppLanguage.EN to "Create New Quote"),

        // AI Tools Screen
        "ai_title" to mapOf(AppLanguage.AR to "مساعد تيجاريو الذكي (AI)", AppLanguage.EN to "Tijario AI Assistant"),
        "ai_subtitle" to mapOf(AppLanguage.AR to "صياغة ردود العملاء وكتابة الأوصاف لمنتجاتك بلمح البصر", AppLanguage.EN to "Draft replies and write product captions in a flash"),
        "ai_card_title" to mapOf(AppLanguage.AR to "صياغة رد ذكي للعملاء", AppLanguage.EN to "Draft Smart Reply"),
        "ai_input_placeholder" to mapOf(AppLanguage.AR to "الصق رسالة العميل هنا...", AppLanguage.EN to "Paste client message here..."),
        "btn_generate_reply" to mapOf(AppLanguage.AR to "توليد الرد الذكي", AppLanguage.EN to "Generate Smart Reply"),
        "ai_suggestion" to mapOf(AppLanguage.AR to "الرد المقترح:", AppLanguage.EN to "Suggested Reply:"),
        "tab_ai_reply" to mapOf(AppLanguage.AR to "رد ذكي", AppLanguage.EN to "Smart Reply"),
        "tab_ai_caption" to mapOf(AppLanguage.AR to "كابشن ذكي", AppLanguage.EN to "Smart Caption"),
        "ai_caption_card_title" to mapOf(AppLanguage.AR to "كتابة كابشن ذكي للمنتجات", AppLanguage.EN to "Generate Smart Caption"),
        "ai_caption_input_placeholder" to mapOf(AppLanguage.AR to "اكتب تفاصيل أو مميزات المنتج هنا...", AppLanguage.EN to "Type product details or features here..."),
        "btn_generate_caption" to mapOf(AppLanguage.AR to "توليد الكابشن الذكي", AppLanguage.EN to "Generate Smart Caption"),
        "ai_caption_suggestion" to mapOf(AppLanguage.AR to "الكابشن المقترح:", AppLanguage.EN to "Suggested Caption:"),

        // Account Screen
        "account_title" to mapOf(AppLanguage.AR to "الحساب والاستخدام", AppLanguage.EN to "Account & Usage"),
        "settings_lang" to mapOf(AppLanguage.AR to "لغة التطبيق", AppLanguage.EN to "App Language"),
        "settings_theme" to mapOf(AppLanguage.AR to "مظهر التطبيق (Theme)", AppLanguage.EN to "App Theme"),
        "theme_light" to mapOf(AppLanguage.AR to "فاتح", AppLanguage.EN to "Light"),
        "theme_dark" to mapOf(AppLanguage.AR to "داكن", AppLanguage.EN to "Dark"),
        "account_pro_badge" to mapOf(AppLanguage.AR to "الباقة الاحترافية (Pro)", AppLanguage.EN to "Pro Plan"),
        "monthly_limits" to mapOf(AppLanguage.AR to "الاستهلاك والحدود الشهرية", AppLanguage.EN to "Monthly Limits & Usage"),
        "limit_docs" to mapOf(AppLanguage.AR to "عروض الأسعار والفواتير المتبقية", AppLanguage.EN to "Remaining Quotes & Invoices"),
        "limit_ai" to mapOf(AppLanguage.AR to "عمليات الذكاء الاصطناعي (AI)", AppLanguage.EN to "AI Operations"),
        "store_premium" to mapOf(AppLanguage.AR to "متجر تيجاريو الفاخر", AppLanguage.EN to "Tijario Premium Store"),

        // Form Screens
        "form_client_info" to mapOf(AppLanguage.AR to "بيانات العميل المستهدف", AppLanguage.EN to "Target Customer Details"),
        "form_items_info" to mapOf(AppLanguage.AR to "تفاصيل البنود والأسعار", AppLanguage.EN to "Items & Pricing Details"),
        "form_item_name" to mapOf(AppLanguage.AR to "اسم البند / الخدمة", AppLanguage.EN to "Item / Service Name"),
        "form_quantity" to mapOf(AppLanguage.AR to "الكمية", AppLanguage.EN to "Quantity"),
        "form_unit_price" to mapOf(AppLanguage.AR to "سعر الوحدة", AppLanguage.EN to "Unit Price"),
        "form_discount" to mapOf(AppLanguage.AR to "الخصم", AppLanguage.EN to "Discount"),
        "form_extra_fees" to mapOf(AppLanguage.AR to "رسوم إضافية", AppLanguage.EN to "Extra Fees"),
        "btn_save_doc" to mapOf(AppLanguage.AR to "توليد وحفظ المستند", AppLanguage.EN to "Generate & Save Document"),
        "btn_save_customer" to mapOf(AppLanguage.AR to "حفظ بيانات العميل", AppLanguage.EN to "Save Customer Data"),
        "btn_save_settings" to mapOf(AppLanguage.AR to "حفظ التغييرات", AppLanguage.EN to "Save Changes"),
        "city" to mapOf(AppLanguage.AR to "المدينة", AppLanguage.EN to "City"),
        "notes" to mapOf(AppLanguage.AR to "ملاحظات إضافية", AppLanguage.EN to "Additional Notes"),
        "terms" to mapOf(AppLanguage.AR to "الشروط والأحكام الافتراضية للفواتير", AppLanguage.EN to "Default Invoice Terms"),
        "btn_back" to mapOf(AppLanguage.AR to "رجوع", AppLanguage.EN to "Back"),
        "config_req" to mapOf(AppLanguage.AR to "إعدادات الاتصال مطلوبة", AppLanguage.EN to "Connection Settings Required"),

        // Products & Services Screen
        "products_title" to mapOf(AppLanguage.AR to "المنتجات والخدمات", AppLanguage.EN to "Products & Services"),
        "products_subtitle" to mapOf(AppLanguage.AR to "إدارة مخزون منتجاتك والخدمات التي تقدمها", AppLanguage.EN to "Manage inventory of your products and services"),
        "search_products" to mapOf(AppLanguage.AR to "البحث عن منتج أو خدمة...", AppLanguage.EN to "Search for a product or service..."),
        "btn_add_product" to mapOf(AppLanguage.AR to "إضافة منتج أو خدمة", AppLanguage.EN to "Add Product/Service"),
        "product_name" to mapOf(AppLanguage.AR to "اسم المنتج / الخدمة", AppLanguage.EN to "Product/Service Name"),
        "product_description" to mapOf(AppLanguage.AR to "الوصف", AppLanguage.EN to "Description"),
        "product_price" to mapOf(AppLanguage.AR to "السعر", AppLanguage.EN to "Price"),
        "product_kind" to mapOf(AppLanguage.AR to "النوع", AppLanguage.EN to "Type"),
        "kind_product" to mapOf(AppLanguage.AR to "منتج", AppLanguage.EN to "Product"),
        "kind_service" to mapOf(AppLanguage.AR to "خدمة", AppLanguage.EN to "Service"),
        "btn_save_product" to mapOf(AppLanguage.AR to "حفظ المنتج/الخدمة", AppLanguage.EN to "Save Product/Service"),
    )

    fun getString(key: String, lang: AppLanguage): String {
        return translations[key]?.get(lang) ?: key
    }
}

@Composable
fun t(key: String): String {
    val lang = LocalLanguage.current
    return Localization.getString(key, lang)
}
