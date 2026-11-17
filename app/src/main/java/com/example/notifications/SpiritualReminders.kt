package com.example.notifications

import java.util.Locale

data class SpiritualReminder(
    val title: String,
    val verseOrQuote: String,
    val reflection: String
) {
    val fullBody: String
        get() = "$verseOrQuote\n$reflection"
}

object SpiritualReminders {
    private data class Entry(
        val titlePrefix: String,
        val verseOrQuote: String,
        val reflection: String
    )

    private val entries = listOf(
        Entry(
            titlePrefix = "آية اليوم",
            verseOrQuote = "﴿وَقُل رَّبِّ زِدْنِي عِلْمًا﴾ — سورة طه (114)",
            reflection = "اجعل لك اليوم نصيباً من العلم النافع، ولو درساً قصيراً يُنير قلبك."
        ),
        Entry(
            titlePrefix = "سكينة القلب",
            verseOrQuote = "﴿أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ﴾ — سورة الرعد (28)",
            reflection = "خصّص دقائق هادئة لذكر الله وسماع ما يثبّت فؤادك."
        ),
        Entry(
            titlePrefix = "رفعةُ أهل العلم",
            verseOrQuote = "﴿يَرْفَعِ اللَّهُ الَّذِينَ آمَنُوا مِنكُمْ وَالَّذِينَ أُوتُوا الْعِلْمَ دَرَجَاتٍ﴾ — سورة المجادلة (11)",
            reflection = "كل خطوةٍ في طريق العلم تقرّبك من الله وترفع قدرك."
        ),
        Entry(
            titlePrefix = "بشارةُ يُسر",
            verseOrQuote = "﴿فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ الْعُسْرِ يُسْرًا﴾ — سورة الشرح (5-6)",
            reflection = "أحسن الظن بربك وتوكّل عليه، واملأ وقتك بما ينفعك."
        ),
        Entry(
            titlePrefix = "نورُ الهداية",
            verseOrQuote = "«مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ» — رواه مسلم",
            reflection = "واصل رحلتك اليومية في العلم والمدارسة ولو بدقائق معدودة."
        ),
        Entry(
            titlePrefix = "شكرٌ وذكر",
            verseOrQuote = "﴿فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ﴾ — سورة البقرة (152)",
            reflection = "أعظمُ ما تفتتح به يومك أو تختمه: ذكرُ الله وتعلّمُ دينه."
        ),
        Entry(
            titlePrefix = "دعاءُ الثبات",
            verseOrQuote = "﴿رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً﴾ — سورة آل عمران (8)",
            reflection = "أحيِ قلبك اليوم بموعظةٍ حسنة أو مقطعٍ يجدّد إيمانك."
        ),
        Entry(
            titlePrefix = "أحبّ الأعمال",
            verseOrQuote = "«أَحَبُّ الأَعْمَالِ إِلَى اللَّهِ أَدْوَمُهَا وَإِنْ قَلَّ» — متفق عليه",
            reflection = "الاستمرار على درسٍ واحدٍ يومياً يبني علماً راسخاً وقلباً حيّاً."
        ),
        Entry(
            titlePrefix = "خيرُ الزاد",
            verseOrQuote = "﴿وَتَزَوَّدُوا فَإِنَّ خَيْرَ الزَّادِ التَّقْوَىٰ﴾ — سورة البقرة (197)",
            reflection = "تزوّد اليوم بآيةٍ تتدبّرها أو نصيحةٍ تعمل بها."
        ),
        Entry(
            titlePrefix = "كفايةُ المتوكل",
            verseOrQuote = "﴿وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ﴾ — سورة الطلاق (3)",
            reflection = "استعن بالله في يومك كله وافتح قلبك لنور الذكر والعلم."
        ),
        Entry(
            titlePrefix = "زادُ البصيرة",
            verseOrQuote = "﴿قُلْ هَلْ يَسْتَوِي الَّذِينَ يَعْلَمُونَ وَالَّذِينَ لَا يَعْلَمُونَ﴾ — سورة الزمر (9)",
            reflection = "استثمر دقائق من وقتك الآن في الاستماع إلى درسٍ نافع."
        ),
        Entry(
            titlePrefix = "توجيهُ القلب",
            verseOrQuote = "﴿إِنَّ فِي ذَٰلِكَ لَذِكْرَىٰ لِمَن كَانَ لَهُ قَلْبٌ أَوْ أَلْقَى السَّمْعَ وَهُوَ شَهِيدٌ﴾ — سورة ق (37)",
            reflection = "أقبل بقلبك وسمعك على آيات الله ومواعظ أهل العلم."
        )
    )

    val count: Int get() = entries.size

    fun reminderForDay(dayIndex: Int, rawUserName: String = ""): SpiritualReminder {
        val entry = entries[dayIndex.mod(entries.size)]
        val name = rawUserName.trim()
        val title = if (name.isEmpty()) {
            "${entry.titlePrefix} 🌙"
        } else {
            "${entry.titlePrefix} يا $name 🌙"
        }
        return SpiritualReminder(
            title = title,
            verseOrQuote = entry.verseOrQuote,
            reflection = entry.reflection
        )
    }

    fun formatTimeArabic(hour: Int, minute: Int): String {
        val clampedHour = hour.coerceIn(0, 23)
        val clampedMinute = minute.coerceIn(0, 59)
        val period = if (clampedHour < 12) "ص" else "م"
        val hour12 = when {
            clampedHour == 0 -> 12
            clampedHour > 12 -> clampedHour - 12
            else -> clampedHour
        }
        return String.format(Locale.ENGLISH, "%02d:%02d %s", hour12, clampedMinute, period)
    }
}
