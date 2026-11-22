package com.example.support

import java.util.Locale

enum class KnowledgeCategory(val displayTitleBn: String) {
    APP_FEATURES("অ্যাপের ফিচার ও হিসাব"),
    INCOME_EXPENSE_LEDGER("আয়-ব্যয়, কাস্টমার ও সাপ্লায়ার হিসাব"),
    DUE_COLLECTION_QR("বাকি আদায়, তাগাদা ও বাংলা QR"),
    REPORTS_AND_EXPORTS("আর্থিক রিপোর্ট ও PDF"),
    SETTINGS_AND_MEMBERSHIP("সেটিংস, পারমিশন ও গোল্ড মেম্বারশিপ"),
    LOGIN_AND_AUTH("লগইন, OTP ও Firebase অথেনটিকেশন"),
    BACKUP_AND_SYNC("ডেটা ব্যাকআপ, রিস্টোর ও ক্লাউড সিঙ্ক"),
    SECURITY_AND_PRIVACY("নিরাপত্তা ও তথ্য সুরক্ষা"),
    OFFICIAL_HELPLINE("অফিশিয়াল হেল্পলাইন"),
}

data class VerifiedKnowledgeEntry(
    val id: String,
    val category: KnowledgeCategory,
    val titleBn: String,
    val verifiedSummaryBn: String,
    val verifiedStepsBn: List<String>,
    val keywords: List<String>,
    val caveatBn: String? = null,
)

enum class KbCoverage {
    VERIFIED_MATCH,
    PARTIAL_MATCH,
    UNVERIFIED_OR_UNKNOWN,
}

data class KnowledgeRetrievalResult(
    val coverage: KbCoverage,
    val matchedEntries: List<VerifiedKnowledgeEntry>,
    val formattedContextForModel: String,
)

object VerifiedKnowledgeBase {

    val entries: List<VerifiedKnowledgeEntry> = listOf(
        VerifiedKnowledgeEntry(
            id = "kb_ledger_customer_supplier",
            category = KnowledgeCategory.INCOME_EXPENSE_LEDGER,
            titleBn = "কাস্টমার ও সাপ্লায়ার হিসাব (টালি খাতা)",
            verifiedSummaryBn = "হোম স্ক্রিনে কাস্টমার ও সাপ্লায়ার আলাদাভাবে দেখা যায়। প্রতি কাস্টমার/সাপ্লায়ারের প্রোফাইলে 'দিলাম/বেচা' এবং 'পেলাম' হিসাব সংরক্ষণ হয়।",
            verifiedStepsBn = listOf(
                "হোম স্ক্রিনের নিচের ডানদিকের '+' (নতুন কাস্টমার/সাপ্লায়ার) বাটনে ট্যাপ করে নাম ও ফোন নম্বর দিয়ে অ্যাকাউন্ট যোগ করুন।",
                "কোনো কাস্টমার বা সাপ্লায়ারের নামে ট্যাপ করে ডিটেইলস স্ক্রিনে যান।",
                "বাকি বিক্রি বা টাকা দিলে 'দিলাম/বেচা' এবং টাকা পেলে 'পেলাম' ঘরে টাকার অঙ্ক, বিবরণ ও তারিখ দিয়ে 'নিশ্চিত করুন' চাপুন।",
                "হোম স্ক্রিনের 'মোট পাবো' ও 'মোট দেবো' কার্ডে সব হিসাবের সারসংক্ষেপ স্বয়ংক্রিয়ভাবে আপডেট হয়।",
            ),
            keywords = listOf(
                "হিসাব", "কাস্টমার", "সাপ্লায়ার", "টালি", "দিলাম", "পেলাম", "বেচা", "যোগ", "এন্ট্রি",
                "পাবো", "দেবো", "দেনা", "পাওনা", "customer", "supplier", "ledger", "tally", "due", "entry", "add"
            ),
        ),
        VerifiedKnowledgeEntry(
            id = "kb_cashbook_income_expense",
            category = KnowledgeCategory.INCOME_EXPENSE_LEDGER,
            titleBn = "ক্যাশবক্স (দৈনিক আয় ও ব্যয় হিসাব)",
            verifiedSummaryBn = "বটম বার বা হোমের 'ক্যাশবাক্স' মেনু থেকে নগদ ক্যাশ-ইন (আয়) ও ক্যাশ-আউট (খরচ) আলাদাভাবে হিসাব রাখা যায়।",
            verifiedStepsBn = listOf(
                "হোম বা বটম নেভিগেশন থেকে 'ক্যাশবাক্স' (Cashbook) ট্যাবে যান।",
                "আয়ের জন্য 'ক্যাশ ইন / আয়' এবং খরচের জন্য 'ক্যাশ আউট / খরচ' নির্বাচন করুন।",
                "টাকার অঙ্ক ও বিবরণ লিখে সেভ করলে আজকের আয়, আজকের খরচ ও বর্তমান ক্যাশ ব্যালেন্স হিসাব হয়।",
            ),
            keywords = listOf(
                "ক্যাশ", "ক্যাশবক্স", "ক্যাশবাক্স", "আয়", "ব্যয়", "খরচ", "বেচা", "নগদ",
                "cash", "cashbook", "income", "expense", "cash in", "cash out"
            ),
        ),
        VerifiedKnowledgeEntry(
            id = "kb_stock_notes_multibiz",
            category = KnowledgeCategory.APP_FEATURES,
            titleBn = "স্টক হিসাব, ব্যবসার নোট ও মাল্টি ব্যবসা",
            verifiedSummaryBn = "অ্যাপে পণ্যের স্টক (স্টক হিসাব), জরুরি নোট (ব্যবসার নোট) এবং একাধিক দোকান পরিচালনার (মাল্টি ব্যবসা) সুবিধা রয়েছে।",
            verifiedStepsBn = listOf(
                "হোম গ্রিডের 'স্টক হিসাব' থেকে পণ্যের নাম, পরিমাণ ও দাম যোগ/আপডেট করা যায় (কম স্টক অ্যালার্ট সহ)।",
                "'ব্যবসার নোট' অপশনে দোকানের দৈনন্দিন দরকারি নোট লিখে রাখা যায়।",
                "'মাল্টি ব্যবসা' থেকে একই অ্যাকাউন্টে একাধিক ব্যবসা/দোকান তৈরি ও পরিবর্তন করা যায়।",
            ),
            keywords = listOf(
                "স্টক", "পণ্য", "নোট", "মাল্টি", "দোকান", "ব্যবসা", "ফিচার", "ব্যবহার", "সুবিধা",
                "stock", "inventory", "notes", "multi", "business", "features", "how to use"
            ),
        ),
        VerifiedKnowledgeEntry(
            id = "kb_due_collection_tagada_qr",
            category = KnowledgeCategory.DUE_COLLECTION_QR,
            titleBn = "বাকি আদায়, তাগাদা SMS ও হিসাবি বাংলা QR",
            verifiedSummaryBn = "কাস্টমারের বাকি আদায়ের জন্য 'তাগাদা SMS', 'গ্রুপ তাগাদা' এবং 'হিসাব পে / কিউআর কোড' ফিচার অ্যাপে রয়েছে।",
            verifiedStepsBn = listOf(
                "কাস্টমারের পেজ বা হোমের 'গ্রুপ তাগাদা' আইকন থেকে বকেয়া আদায়ের তাগাদা SMS পাঠানো যায় (SMS ব্যালেন্স থাকা সাপেক্ষে)।",
                "অতিরিক্ত SMS প্রয়োজন হলে 'SMS প্যাকেজ' মেনু থেকে প্যাকেজ নেওয়া যায়।",
                "বটম বারের 'কিউআর কোড' বা 'হিসাব পে' অপশন থেকে ব্যবসার QR কোড দেখানো বা স্ক্যান করে লেনদেন রেকর্ড করা যায়।",
            ),
            keywords = listOf(
                "তাগাদা", "গ্রুপ তাগাদা", "বাকি", "বকেয়া", "আদায়", "মেসেজ", "sms", "qr", "কিউআর",
                "হিসাব পে", "পেমেন্ট", "tagada", "payment", "collection", "remind"
            ),
        ),
        VerifiedKnowledgeEntry(
            id = "kb_reports_pdf",
            category = KnowledgeCategory.REPORTS_AND_EXPORTS,
            titleBn = "আর্থিক রিপোর্ট ও PDF বিবরণী",
            verifiedSummaryBn = "অ্যাপে 'রিপোর্ট' ও 'মালিকের রিপোর্ট' অপশনের মাধ্যমে লেনদেনের সারসংক্ষেপ দেখা এবং PDF ফাইল তৈরি ও শেয়ার করা যায়।",
            verifiedStepsBn = listOf(
                "মেনু, ড্রয়ার বা হোম থেকে 'রিপোর্ট' অপশনে প্রবেশ করুন।",
                "নির্দিষ্ট কাস্টমার, সময়কাল বা ব্যবসার সামগ্রিক আয়-ব্যয় ও পাওনা-দেনার তালিকা দেখুন।",
                "PDF এক্সপোর্ট বা শেয়ার বাটনে ট্যাপ করে রিপোর্টটি সংরক্ষণ বা পাঠাতে পারবেন।",
            ),
            keywords = listOf(
                "রিপোর্ট", "বিবরণী", "পিডিএফ", "pdf", "হিসাবের তালিকা", "মালিকের রিপোর্ট",
                "report", "reports", "statement", "export", "analytics"
            ),
        ),
        VerifiedKnowledgeEntry(
            id = "kb_settings_permissions_gold",
            category = KnowledgeCategory.SETTINGS_AND_MEMBERSHIP,
            titleBn = "সেটিংস, পারমিশন সেন্টার ও গোল্ড মেম্বারশিপ",
            verifiedSummaryBn = "সেটিংস থেকে দশমিক প্রদর্শন, নোটিফিকেশন, PIN লক, ভাষা (বাংলা/English), পারমিশন সেন্টার এবং গোল্ড মেম্বারশিপ (৳১৯৯/মাস) নিয়ন্ত্রণ করা যায়।",
            verifiedStepsBn = listOf(
                "সেটিংস পেজে গিয়ে 'টাকার অঙ্কে দশমিক', 'নোটিফিকেশন সাউন্ড', 'PIN পরিবর্তন' ও 'পিন ছাড়া এন্ট্রি রেকর্ড' চালু বা বন্ধ করা যায়।",
                "'অ্যাপ পারমিশন সেন্টার' থেকে Camera, Contacts, Location, Notifications ও Phone অনুমতির আসল অবস্থা দেখা ও নিয়ন্ত্রণ করা যায়।",
                "গোল্ড মেম্বারশিপের (৳১৯৯/মাস) সুবিধা: ১০০ টি ফ্রি মাসিক SMS, আনলিমিটেড অ্যানালিটিক্স রিপোর্ট এবং প্রায়োরিটি কাস্টমার সাপোর্ট।",
            ),
            keywords = listOf(
                "সেটিংস", "পিন", "লক", "ভাষা", "পারমিশন", "অনুমতি", "গোল্ড", "প্রিমিয়াম", "প্রিমিয়াম", "মেম্বারশিপ",
                "মেম্বারশিপ ফি", "গোল্ড দাম", "প্রিমিয়াম দাম", "দশমিক", "সাউন্ড",
                "settings", "permission", "pin lock", "gold membership", "premium", "membership price", "language"
            ),
        ),
        VerifiedKnowledgeEntry(
            id = "kb_login_otp_registration",
            category = KnowledgeCategory.LOGIN_AND_AUTH,
            titleBn = "লগইন, রেজিস্ট্রেশন ও OTP সমস্যা সমাধান",
            verifiedSummaryBn = "অ্যাপে মোবাইল নম্বর (OTP অটো-ফিল) এবং Google Sign-In (Credential Manager) উভয় মাধ্যমে লগইন করা যায়।",
            verifiedStepsBn = listOf(
                "সঠিক ১১-ডিজিটের বাংলাদেশি মোবাইল নম্বর (`01XXXXXXXXX`) দিয়েছেন কিনা যাচাই করুন এবং ফোনের ইন্টারনেট সংযোগ চালু রাখুন।",
                "Google Sign-In ব্যবহার করলে ফোনের Settings -> Google অ্যাকাউন্টে লগইন করা আছে এবং Google Play Services আপডেটেড আছে কিনা দেখুন।",
                "OTP মেসেজ অটো-ফিল না হলে SMS ইনবক্স থেকে ৬-ডিজিট কোডটি দেখে ম্যানুয়ালি লিখে 'ভেরিফাই' চাপুন।",
                "একবার লগইন না হলে অ্যাপটি বন্ধ করে আবার খুলে পুনরায় চেষ্টা করুন; তারপরও না হলে হেল্পলাইনে (+8809638517411) যোগাযোগ করুন।",
            ),
            keywords = listOf(
                "লগইন", "সাইন ইন", "রেজিস্ট্রেশন", "অ্যাকাউন্ট", "ওটিপি", "otp", "কোড", "খুলছে না",
                "login", "signin", "sign in", "register", "registration", "verify", "phone"
            ),
        ),
        VerifiedKnowledgeEntry(
            id = "kb_firebase_token_appcheck",
            category = KnowledgeCategory.LOGIN_AND_AUTH,
            titleBn = "Firebase Authentication ও Invalid Token / App Check ত্রুটি",
            verifiedSummaryBn = "Invalid Token, Expired Token বা 403/অথেনটিকেশন এরর সাধারণত পুরোনো সেশন টোকেন, ফোনের ভুল তারিখ/সময় বা নেটওয়ার্ক/Google Play Services সমস্যার কারণে হয়।",
            verifiedStepsBn = listOf(
                "ফোনের তারিখ ও সময় (Date & Time) 'Automatic / Network-provided' অবস্থায় আছে কিনা পরীক্ষা করুন (সময় ভুল থাকলে টোকেন অবৈধ হতে পারে)।",
                "সেশন টোকেন রিফ্রেশ করতে অ্যাপের Settings থেকে 'লগ আউট' করে আবার Google Sign-In দিয়ে নতুন করে লগইন করুন।",
                "ফোনের Google Play Services এবং অ্যাপের ইন্টারনেট সংযোগ স্থিতিশীল আছে কিনা যাচাই করুন।",
                "এই ধাপগুলোর পরও যদি Invalid Token / Permission Denied দেখায়, তবে আমাদের কারিগরি যাচাইয়ের জন্য হেল্পলাইনে (+8809638517411) যোগাযোগ করুন।",
            ),
            keywords = listOf(
                "firebase", "ফায়ারবেস", "টোকেন", "token", "invalid token", "expired", "403", "app check",
                "অথেনটিকেশন", "authentication", "permission denied", "unauthenticated", "এরর", "error", "সার্ভার"
            ),
            caveatBn = "বিঃদ্রঃ কাস্টমার কেবল ফলাফল যাচাই করার পরেই সমস্যাটি সমাধান হয়েছে ধরা যাবে; অ্যাপ থেকে সরাসরি টোকেন বা সার্ভার লগ পরীক্ষা না করে নিশ্চিত সমাধান দাবি করা যাবে না।",
        ),
        VerifiedKnowledgeEntry(
            id = "kb_backup_restore_sync",
            category = KnowledgeCategory.BACKUP_AND_SYNC,
            titleBn = "ডেটা ব্যাকআপ, রিস্টোর ও ক্লাউড সিঙ্ক",
            verifiedSummaryBn = "অ্যাপে Google Sign-In যুক্ত থাকলে ক্লাউড Firestore সিঙ্ক কাজ করে এবং হোম/সেটিংস থেকে JSON ব্যাকআপ এক্সপোর্ট ও ক্যাশ ক্লিয়ার করা যায়।",
            verifiedStepsBn = listOf(
                "স্বয়ংক্রিয় ক্লাউড সিঙ্কের জন্য নিশ্চিত করুন আপনি Google Sign-In দিয়ে লগইন আছেন (সেটিংসে 'Google ক্লাউড ব্যাকআপ সংযোগ করুন' দেখালে সেটিতে ট্যাপ করুন)।",
                "অফলাইনে সংরক্ষণের জন্য হোমের 'ক্লাউড ব্যাকআপ' বা সেটিংসের 'ডাটা ব্যাকআপ ও এক্সপোর্ট' থেকে `hisaber_khata_backup.json` ফাইল তৈরি ও শেয়ার করতে পারবেন।",
                "কোনো ডিভাইসে ডেটা সিঙ্ক আটকে গেলে ইন্টারনেট সংযোগ পরীক্ষা করে একই Google অ্যাকাউন্টে পুনরায় লগইন করুন।",
                "সতর্কতা: 'ক্লিয়ার ব্যাকআপ' কেবল ডিভাইসের অস্থায়ী `hisaber_khata_backup.json` ক্যাশ ফাইল পরিষ্কার করে; ক্লাউড ডেটা রিকভারি নিশ্চিত না হলে হেল্পলাইনে (+8809638517411) যোগাযোগ করুন।",
            ),
            keywords = listOf(
                "ব্যাকআপ", "সিঙ্ক", "রিস্টোর", "ডেটা", "ডাটা", "ক্লাউড", "গুগল", "হারায়", "হারিয়ে", "ফিরিয়ে",
                "backup", "restore", "sync", "cloud", "export", "json", "data"
            ),
        ),
        VerifiedKnowledgeEntry(
            id = "kb_security_privacy_rules",
            category = KnowledgeCategory.SECURITY_AND_PRIVACY,
            titleBn = "নিরাপত্তা, পাসওয়ার্ড ও তথ্যের গোপনীয়তা",
            verifiedSummaryBn = "Firestore নিরাপত্তা নিয়ম অনুযায়ী প্রতিটি ব্যবহারকারী কেবল নিজের `userId` ভিত্তিক ডেটা দেখতে ও সম্পাদনা করতে পারেন। সাপোর্ট চ্যাট কখনো OTP, PIN, পাসওয়ার্ড বা টোকেন চায় না বা প্রকাশ করে না।",
            verifiedStepsBn = listOf(
                "৪-ডিজিট অ্যাপ PIN, OTP এবং Google অ্যাকাউন্টের পাসওয়ার্ড বা টোকেন কখনো কারো সাথে শেয়ার করবেন না।",
                "অন্য কোনো গ্রাহক বা দোকানের আর্থিক হিসাব এই সাপোর্ট সিস্টেমের মাধ্যমে দেখা বা পরিবর্তন করা সম্ভব নয়।",
                "হিসাবে কোনো পরিবর্তনের জন্য সরাসরি আপনার নিজের লগইন করা অ্যাকাউন্ট থেকে অনুমোদিত এন্ট্রি করতে হবে।",
            ),
            keywords = listOf(
                "সুরক্ষা", "নিরাপত্তা", "পাসওয়ার্ড", "গোপন", "প্রাইভেসি", "হ্যাক", "অন্য কাস্টমার", "পিন", "টোকেন",
                "security", "privacy", "password", "protect", "safe", "hack", "unauthorized"
            ),
        ),
        VerifiedKnowledgeEntry(
            id = "kb_official_helpline",
            category = KnowledgeCategory.OFFICIAL_HELPLINE,
            titleBn = "অফিশিয়াল মানব সাপোর্ট হেল্পলাইন",
            verifiedSummaryBn = "জটিল বা অমীমাংসিত কারিগরি ও অ্যাকাউন্ট সমস্যার জন্য হিসাবের খাতার একমাত্র অফিশিয়াল সাপোর্ট হেল্পলাইন নম্বর: $SUPPORT_HELPLINE_NUMBER।",
            verifiedStepsBn = listOf(
                "সরাসরি মানব সহায়তার প্রয়োজনে 'হেল্পলাইনে যোগাযোগ করুন' বাটনে ট্যাপ করে $SUPPORT_HELPLINE_NUMBER নম্বরে কল করুন।",
            ),
            keywords = listOf(
                "হেল্পলাইন", "হেল্প লাইন", "কল করুন", "মানব সাপোর্ট", "প্রতিনিধি", "যোগাযোগ", "কাস্টমার সাপোর্ট", "অভিযোগ",
                "helpline", "phone call", "human support", "agent", "contact", "escalation"
            ),
        ),
    )

    private val genericStopWords = setOf(
        "দাম", "কত", "কী", "কি", "হয়", "হয়", "আছে", "করব", "করবো", "করে", "থেকে", "জন্য", "এবং",
        "price", "cost", "how", "what", "the", "and", "for", "with", "can", "you", "does",
    )

    fun retrieveRelevantEntries(
        query: String,
        detectedCategory: SupportIssueCategory? = null,
        maxEntries: Int = 3,
    ): KnowledgeRetrievalResult {
        val normalized = normalizeBengaliAndEnglish(query)
        val tokens = normalized.split(Regex("[\\s,?।.!:/\\-]+"))
            .filter { it.length >= 2 && it !in genericStopWords }
            .distinct()

        val preferredCategories = when (detectedCategory) {
            SupportIssueCategory.AUTH_LOGIN -> setOf(KnowledgeCategory.LOGIN_AND_AUTH)
            SupportIssueCategory.FIREBASE_TOKEN_APPCHECK -> setOf(KnowledgeCategory.LOGIN_AND_AUTH, KnowledgeCategory.SECURITY_AND_PRIVACY)
            SupportIssueCategory.BACKUP_SYNC -> setOf(KnowledgeCategory.BACKUP_AND_SYNC)
            SupportIssueCategory.PAYMENT_QR -> setOf(KnowledgeCategory.DUE_COLLECTION_QR)
            SupportIssueCategory.ACCOUNTING_LEDGER -> setOf(KnowledgeCategory.INCOME_EXPENSE_LEDGER, KnowledgeCategory.REPORTS_AND_EXPORTS)
            SupportIssueCategory.SETTINGS_PERMISSIONS -> setOf(KnowledgeCategory.SETTINGS_AND_MEMBERSHIP, KnowledgeCategory.APP_FEATURES)
            SupportIssueCategory.ACCOUNT_PRIVACY_RISK -> setOf(KnowledgeCategory.SECURITY_AND_PRIVACY)
            SupportIssueCategory.GENERAL_APP_INFO -> setOf(KnowledgeCategory.APP_FEATURES, KnowledgeCategory.INCOME_EXPENSE_LEDGER)
            SupportIssueCategory.UNKNOWN_OR_UNVERIFIED, null -> emptySet()
        }

        val scored = entries.map { entry ->
            var score = 0
            val matchedTokens = mutableSetOf<String>()
            val normalizedKeywords = entry.keywords.map { normalizeBengaliAndEnglish(it) }

            normalizedKeywords.forEach { nKw ->
                val exactHit = if (nKw.all { it in 'a'..'z' || it in '0'..'9' || it == ' ' }) {
                    Regex("\\b${Regex.escape(nKw)}\\b").containsMatchIn(normalized)
                } else {
                    normalized.contains(nKw)
                }
                if (exactHit) {
                    score += if (nKw.contains(" ")) 6 else 4
                }
            }

            tokens.forEach { t ->
                if (t.length >= 3 && t !in matchedTokens && normalizedKeywords.any { kw -> kw.contains(t) }) {
                    matchedTokens += t
                    score += 2
                }
            }

            if (score > 0 && entry.category in preferredCategories) {
                score += 3
            }
            entry to score
        }.filter { it.second >= 4 }
            .sortedByDescending { it.second }
            .take(maxEntries)
            .map { it.first }

        val coverage = when {
            scored.isNotEmpty() && scored.first().keywords.any { kw ->
                val nKw = normalizeBengaliAndEnglish(kw)
                if (nKw.all { it in 'a'..'z' || it in '0'..'9' || it == ' ' }) {
                    Regex("\\b${Regex.escape(nKw)}\\b").containsMatchIn(normalized)
                } else {
                    normalized.contains(nKw)
                }
            } -> KbCoverage.VERIFIED_MATCH
            scored.isNotEmpty() -> KbCoverage.PARTIAL_MATCH
            else -> KbCoverage.UNVERIFIED_OR_UNKNOWN
        }

        val contextBlock = if (scored.isEmpty()) {
            "KB_MATCH: NONE (এই প্রশ্নের সাথে সরাসরি মিল থাকা কোনো যাচাইকৃত নলেজ বেস তথ্য পাওয়া যায়নি। কোনো তথ্য বানিয়ে বা অনুমান করে বলবে না; নিশ্চিতকরণ প্রয়োজন বলে জানিয়ে প্রয়োজনে হেল্পলাইন $SUPPORT_HELPLINE_NUMBER-এ যোগাযোগের পরামর্শ দাও।)"
        } else {
            buildString {
                appendLine("VERIFIED_KNOWLEDGE_BASE_ENTRIES (${scored.size}):")
                scored.forEachIndexed { idx, e ->
                    appendLine("${idx + 1}. [${e.id}] ${e.titleBn}")
                    appendLine("   তথ্য: ${e.verifiedSummaryBn}")
                    e.verifiedStepsBn.forEachIndexed { sIdx, step ->
                        appendLine("   ধাপ ${sIdx + 1}: $step")
                    }
                    if (!e.caveatBn.isNullOrBlank()) {
                        appendLine("   সতর্কতা: ${e.caveatBn}")
                    }
                }
            }.trimEnd()
        }

        return KnowledgeRetrievalResult(
            coverage = coverage,
            matchedEntries = scored,
            formattedContextForModel = contextBlock,
        )
    }

    fun normalizeBengaliAndEnglish(raw: String): String {
        return raw.trim()
            .lowercase(Locale.ENGLISH)
            // Common Bengali spelling variations (য়/য়, ড়/ড়, ং/ঙ, ও/উ variants)
            .replace("য়", "য়")
            .replace("ড়", "ড়")
            .replace("ঢ়", "ঢ়")
            .replace("হিসাবি", "হিসাব")
            .replace("হিসাবের", "হিসাব")
            .replace("অ্যাপস", "অ্যাপ")
            .replace("বকেয়া", "বকি")
            .replace("বকেয়া", "বকি")
            .replace("বাকি", "বকি")
            .replace("ক্যাশবাক্স", "ক্যাশবক্স")
            .replace("প্রিমিয়াম", "প্রিমিয়াম")
            .replace("ডাটা", "ডেটা")
            .replace("ব্যাকআপ", "ব্যাকআপ")
            .replace("টোকেন", "টোকেন")
            .replace("ফায়ারবেস", "ফায়ারবেস")
    }
}
