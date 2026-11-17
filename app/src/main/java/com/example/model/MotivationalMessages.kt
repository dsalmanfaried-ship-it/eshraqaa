package com.example.model

object MotivationalProvider {
    fun buildMessages(
        rawUserName: String,
        watchedCount: Int,
        totalCount: Int,
        savedCount: Int
    ): List<String> {
        val name = rawUserName.trim().ifEmpty { "طالب العلم" }
        val remaining = (totalCount - watchedCount).coerceAtLeast(0)

        val progressLine = when {
            watchedCount == 0 ->
                "ابدأ رحلتك اليوم يا $name 🌿 — درسٌ واحد قد يفتح لك باباً من النور والسكينة."
            watchedCount >= totalCount && totalCount > 0 ->
                "ما شاء الله يا $name! أتممت جميع الدروس ($totalCount) — زادك الله علماً وثباتاً 🏆"
            else ->
                "بارك الله في همتك يا $name ✨ — أتممت $watchedCount درساً، وبقي أمامك $remaining درساً نافعاً."
        }

        val favoritesLine = if (savedCount > 0) {
            "لديك $savedCount درساً في مفضلتك يا $name ♥ — راجع منها ما يثبّت قلبك اليوم."
        } else {
            "كل كلمةٍ طيبةٍ تسمعها يا $name هي زادٌ لقلبك وبصيرةٌ في طريقك إلى الله 🌙."
        }

        return listOf(
            progressLine,
            "من سلك طريقاً يلتمس فيه علماً سهّل الله له به طريقاً إلى الجنة — واصل المسير يا $name.",
            "تذكّر يا $name: أحبّ الأعمال إلى الله أدومها وإن قلّ، ولو مقطعاً قصيراً كل يوم 🌿.",
            "افتح قلبك لنور القرآن والموعظة اليوم يا $name 📖، فرب آيةٍ أو كلمةٍ تُحيي قلباً.",
            favoritesLine,
            "استثمارك لدقائق من يومك في العلم يا $name هو بركةٌ في عمرك ورزقك وتوفيقك ✨."
        )
    }

    fun personalizedWatchedToast(rawUserName: String): String {
        val name = rawUserName.trim()
        return if (name.isEmpty()) "اتسجل كمشاهَد ✓" else "أحسنت يا $name! اتسجل كمشاهَد ✓"
    }

    fun personalizedFavoriteToast(rawUserName: String, added: Boolean): String {
        val name = rawUserName.trim()
        return if (added) {
            if (name.isEmpty()) "تمت الإضافة إلى المفضلة ♥" else "اختيار موفق يا $name ♥"
        } else {
            "تمت الإزالة من المفضلة"
        }
    }
}
