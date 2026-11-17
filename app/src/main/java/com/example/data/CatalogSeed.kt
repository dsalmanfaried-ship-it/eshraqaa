package com.example.data

import com.example.data.local.VideoEntity
import com.example.model.CategoryMapper
import com.example.model.YouTubeUrlParser

private data class SeedEntry(
    val scholarIndex: Int,
    val scholar: String,
    val topic: String,
    val title: String,
    val url: String,
    val isShort: Boolean = false
)

object CatalogSeed {
    val defaultScholars: List<String> = listOf(
        "دكتور أحمد العربي",
        "أمجد سمير",
        "سمير مصطفى",
        "الشيخ محمد الغليظ",
        "ياسر ممدوح",
        "حازم شومان",
        "شريف علي",
        "عبد العزيز الطريفي"
    )

    private val entries: List<SeedEntry> = listOf(
        // 0: دكتور أحمد العربي
        SeedEntry(0, "دكتور أحمد العربي", "تزكية وإيمانيات", "كيف نصل إلى القلب السليم؟", "https://www.youtube.com/watch?v=ZWraKKwNfpE"),
        SeedEntry(0, "دكتور أحمد العربي", "تزكية وإيمانيات", "الأخلاء يومئذ بعضهم لبعض عدو إلا المتقين", "https://www.youtube.com/watch?v=xwywIkJF6HE"),
        SeedEntry(0, "دكتور أحمد العربي", "تزكية وإيمانيات", "هذا هو القلب — مقطع مؤثر", "https://www.youtube.com/watch?v=sAbgN3H32bw"),
        SeedEntry(0, "دكتور أحمد العربي", "تزكية وإيمانيات", "لمثل هذا فأعدوا", "https://www.youtube.com/watch?v=ZRFrXCvydog", true),
        SeedEntry(0, "دكتور أحمد العربي", "تزكية وإيمانيات", "فاهم 38 — اعتزال إبراهيم عليه السلام", "https://www.youtube.com/watch?v=si994Z9BAr8"),
        SeedEntry(0, "دكتور أحمد العربي", "القرآن", "أنا صاحبك القرآن.. كيف يكون القرآن صاحبي؟", "https://www.youtube.com/watch?v=lkX6mcCD3CI"),
        SeedEntry(0, "دكتور أحمد العربي", "القرآن", "فاهم 37 — عن القرآن الكريم", "https://www.youtube.com/watch?v=hK-9Be0prrA"),
        SeedEntry(0, "دكتور أحمد العربي", "القرآن", "هذا هو القرآن — مقطع رائع", "https://www.youtube.com/watch?v=XC-JV2342ic"),
        SeedEntry(0, "دكتور أحمد العربي", "القرآن", "تأثير القرآن", "https://www.youtube.com/watch?v=8jlOpkjCNzM", true),
        SeedEntry(0, "دكتور أحمد العربي", "القرآن", "كذلك لنثبت به فؤادك", "https://www.youtube.com/watch?v=ssao8w-8_Pg", true),
        SeedEntry(0, "دكتور أحمد العربي", "الصلاة ورمضان", "فاهم 36 — أول ما يحاسب به العبد الصلاة", "https://www.youtube.com/watch?v=63_AOCldyXo"),
        SeedEntry(0, "دكتور أحمد العربي", "الصلاة ورمضان", "ماذا أريد من رمضان؟", "https://www.youtube.com/watch?v=9TjlLsX9iA0"),
        SeedEntry(0, "دكتور أحمد العربي", "أدعية ومناجاة", "دعاء يذيب قساوة القلب", "https://www.youtube.com/watch?v=ZFXEXTRhOTQ", true),
        SeedEntry(0, "دكتور أحمد العربي", "أدعية ومناجاة", "نفر إليك منا فاعف عنا", "https://www.youtube.com/watch?v=dTiPXjPwj-4", true),
        SeedEntry(0, "دكتور أحمد العربي", "أدعية ومناجاة", "أنت أحق من ذُكر", "https://www.youtube.com/watch?v=U6L4ZeIuCu4", true),
        SeedEntry(0, "دكتور أحمد العربي", "أدعية ومناجاة", "مناجاة الله عز وجل والأُنس به", "https://www.youtube.com/watch?v=Tr9eZ-M6mUk"),

        // 1: أمجد سمير
        SeedEntry(1, "أمجد سمير", "تزكية ويقين", "اجعل يقينك قوياً.. يريك الله المعجزات", "https://www.youtube.com/watch?v=AjipD2d2rWo"),
        SeedEntry(1, "أمجد سمير", "تزكية ويقين", "حقيقة الدنيا؟", "https://www.youtube.com/watch?v=pqOdVMSO_xE"),
        SeedEntry(1, "أمجد سمير", "تزكية ويقين", "كيف تهرب من ظلمات الدنيا؟", "https://www.youtube.com/watch?v=TlAzTpwh2Cw"),
        SeedEntry(1, "أمجد سمير", "تزكية ويقين", "من هم الصالحين؟", "https://www.youtube.com/watch?v=e0yMlaICASA"),
        SeedEntry(1, "أمجد سمير", "حياة وسلوك", "كيف سرقت المقارنة سعادتك دون أن تشعر؟", "https://www.youtube.com/watch?v=5uMX_lW1Myw"),
        SeedEntry(1, "أمجد سمير", "حياة وسلوك", "سبب الخمول والكسل والتشتت وإدمان التليفون", "https://www.youtube.com/watch?v=YcT0yM_fHeU"),
        SeedEntry(1, "أمجد سمير", "حياة وسلوك", "كيف يوازن المسلم بين العمل والعبادة؟", "https://www.youtube.com/watch?v=b5_sznU0Pm4"),
        SeedEntry(1, "أمجد سمير", "حياة وسلوك", "ليلة رأس السنة", "https://www.youtube.com/watch?v=EC4esPiVeVw"),
        SeedEntry(1, "أمجد سمير", "التوبة والرجوع", "قصة توبتي", "https://www.youtube.com/watch?v=X18eDcft4G4"),
        SeedEntry(1, "أمجد سمير", "التوبة والرجوع", "رسالة لكل من أرهقته العودة للمعاصي بعد التوبة", "https://www.youtube.com/watch?v=LELh3kHmyTw"),
        SeedEntry(1, "أمجد سمير", "التوبة والرجوع", "كلمتين هيطمنوك ويريحوا قلبك", "https://www.youtube.com/watch?v=kwRY_bleW2w"),
        SeedEntry(1, "أمجد سمير", "الصلاة", "أسرار الصلاة", "https://www.youtube.com/watch?v=1473X0E1Cqk"),
        SeedEntry(1, "أمجد سمير", "قصص القرآن والصحابة", "قصة غريبة", "https://www.youtube.com/watch?v=Rmp0Azp7OAo", true),
        SeedEntry(1, "أمجد سمير", "قصص القرآن والصحابة", "تأملات في قصة أصحاب السبت", "https://www.youtube.com/watch?v=wyXbLGlynbQ"),
        SeedEntry(1, "أمجد سمير", "قصص القرآن والصحابة", "واجه الدنيا بقلب لا يخاف — أبو ذر الغفاري", "https://www.youtube.com/watch?v=lsZrPXB7VsE"),
        SeedEntry(1, "أمجد سمير", "قصص القرآن والصحابة", "قصة سليمان عليه السلام مع الهدهد", "https://www.youtube.com/watch?v=oThH4gXdW0k"),

        // 2: سمير مصطفى
        SeedEntry(2, "سمير مصطفى", "رقائق وتوبة", "من يسألني فأعطيه؟ ومن يتوب اليوم فأغفر له؟", "https://www.youtube.com/watch?v=GhCy5yqHhd4"),
        SeedEntry(2, "سمير مصطفى", "رقائق وتوبة", "موعظة تهز القلوب", "https://www.youtube.com/watch?v=090VEfM9LOw"),
        SeedEntry(2, "سمير مصطفى", "رقائق وتوبة", "أحسن الظن بالله ولا تستسلم", "https://www.youtube.com/watch?v=jrffu7M7Uk0"),
        SeedEntry(2, "سمير مصطفى", "رقائق وتوبة", "كيف نتخفف من ذنوبنا؟", "https://www.youtube.com/watch?v=S5_3QUFkZV0"),
        SeedEntry(2, "سمير مصطفى", "قصص ومواعظ", "مسجد الصحابة — وصية راهب", "https://www.youtube.com/watch?v=m6S4vPy0P54"),
        SeedEntry(2, "سمير مصطفى", "قصص ومواعظ", "أبصر طريقك ج1", "https://www.youtube.com/watch?v=y2ZDAIyxzNM"),
        SeedEntry(2, "سمير مصطفى", "الموت والآخرة", "من مشاعر الموت وألم الفراق", "https://www.youtube.com/watch?v=oUFSYNcNEMU"),
        SeedEntry(2, "سمير مصطفى", "الموت والآخرة", "خطبة النذير العريان (ذكر الموت)", "https://www.youtube.com/watch?v=bhFBiMrQA2o"),
        SeedEntry(2, "سمير مصطفى", "الصلاة والخشوع", "اقف انتباه", "https://www.youtube.com/watch?v=-FSkQOm1PDA"),
        SeedEntry(2, "سمير مصطفى", "الصلاة والخشوع", "ستعشق الصلاة — الخشوع وقوة التوحيد", "https://www.youtube.com/watch?v=TInVMFxX2PI"),

        // 3: الشيخ محمد الغليظ
        SeedEntry(3, "الشيخ محمد الغليظ", "القرآن", "القرآن — مقطع مؤثر", "https://www.youtube.com/watch?v=gtKC6Xa_RnU"),
        SeedEntry(3, "الشيخ محمد الغليظ", "القرآن", "كل يوم آية (18)", "https://www.youtube.com/watch?v=yhgqGgXTbfI"),
        SeedEntry(3, "الشيخ محمد الغليظ", "التوبة والهداية", "مهما وقعت أو انتكست افتكر إنه يبدأ ويعيد", "https://www.youtube.com/watch?v=NnvG_Ahz-Vk"),
        SeedEntry(3, "الشيخ محمد الغليظ", "التوبة والهداية", "مشكلته في الدين المؤثر في الحياة", "https://www.youtube.com/watch?v=uGWRj9-N3BU"),
        SeedEntry(3, "الشيخ محمد الغليظ", "بداية الطريق", "من أين أبدأ؟ (1)", "https://www.youtube.com/watch?v=Bgjz1VxODjY"),
        SeedEntry(3, "الشيخ محمد الغليظ", "الصلاة", "إنها الصلاة — أستحي من الله", "https://www.youtube.com/watch?v=mH1RMAhwDa8"),
        SeedEntry(3, "الشيخ محمد الغليظ", "الصلاة", "لو مبتصليش أو بتقطع في الصلاة", "https://www.youtube.com/watch?v=LUhi8GoeBGw"),
        SeedEntry(3, "الشيخ محمد الغليظ", "الدعاء", "روشتة ابن القيم لاستجابة الدعاء", "https://www.youtube.com/watch?v=wvU6jCXKdv4"),
        SeedEntry(3, "الشيخ محمد الغليظ", "الدعاء", "أنا مش لوحدي — معك الملك", "https://www.youtube.com/watch?v=S_FXjpy2FVk"),
        SeedEntry(3, "الشيخ محمد الغليظ", "الدعاء", "دعاء فيه كل اللي نفسك فيه", "https://www.youtube.com/watch?v=7zwh51ysJts"),
        SeedEntry(3, "الشيخ محمد الغليظ", "أسرة وزواج", "هل مبدأ حقي وحقك ينفع في الزواج؟", "https://www.youtube.com/watch?v=PkOmS0EYEhw"),
        SeedEntry(3, "الشيخ محمد الغليظ", "أسرة وزواج", "نصيحة لكل زوجة صاحب رسالة حقيقية", "https://www.youtube.com/watch?v=VBWZMrxkJxk"),
        SeedEntry(3, "الشيخ محمد الغليظ", "أسرة وزواج", "إيه صفات الزوج الصالح؟", "https://www.youtube.com/watch?v=dlByIzPPRR0"),
        SeedEntry(3, "الشيخ محمد الغليظ", "قصص مؤثرة", "بيده الأمر — قصة مع مادة السيوتيكال", "https://www.youtube.com/watch?v=3m3Nb7O7vgs"),
        SeedEntry(3, "الشيخ محمد الغليظ", "قصص مؤثرة", "الله يراك — قصة هادفة", "https://www.youtube.com/watch?v=fl5gzROdu0Q"),
        SeedEntry(3, "الشيخ محمد الغليظ", "قصص مؤثرة", "مفيش تاني", "https://www.youtube.com/watch?v=MRfO8IfuFE8"),
        SeedEntry(3, "الشيخ محمد الغليظ", "قوائم تشغيل", "فيديوهات مميزة للشيخ محمد الغليظ", "https://www.youtube.com/playlist?list=PL2Sst6o2QU5ShcUdVN6WmSOeCF1ijGfLb"),

        // 4: ياسر ممدوح
        SeedEntry(4, "ياسر ممدوح", "أسرة وعلاقات", "خش هتجيبك!", "https://www.youtube.com/watch?v=M7ZqHNnTQs0"),
        SeedEntry(4, "ياسر ممدوح", "أسرة وعلاقات", "اتطلقتي يا فالحة", "https://www.youtube.com/watch?v=34_Bj67N6Fk"),
        SeedEntry(4, "ياسر ممدوح", "أسرة وعلاقات", "الخديعة", "https://www.youtube.com/watch?v=mIA00ifqbSg", true),
        SeedEntry(4, "ياسر ممدوح", "أسرة وعلاقات", "مش مقتنعة بالحجاب", "https://www.youtube.com/watch?v=qm-63nnpA6g"),
        SeedEntry(4, "ياسر ممدوح", "إيمان وصلاة", "بدأت أشك في الدين", "https://www.youtube.com/watch?v=RIaaFH6d6tA"),
        SeedEntry(4, "ياسر ممدوح", "إيمان وصلاة", "لو بصلي بس!", "https://www.youtube.com/watch?v=byJfW3Wb5ms"),
        SeedEntry(4, "ياسر ممدوح", "إيمان وصلاة", "العكاك والملاك", "https://www.youtube.com/watch?v=OY5uHXTnCxQ"),
        SeedEntry(4, "ياسر ممدوح", "إيمان وصلاة", "هو ده قرآن؟؟", "https://www.youtube.com/watch?v=O--nWTmxNd4"),
        SeedEntry(4, "ياسر ممدوح", "إيمان وصلاة", "الذين يظنون أنهم ملاقوا ربهم", "https://www.youtube.com/watch?v=xNufizcaMFQ", true),
        SeedEntry(4, "ياسر ممدوح", "قصص وسير", "تعرف يعني إيه ست بجد — السيدة عائشة", "https://www.youtube.com/watch?v=zT4PXuZJdxw"),
        SeedEntry(4, "ياسر ممدوح", "قوائم تشغيل", "فيديو الجمعة — كل الحلقات", "https://www.youtube.com/playlist?list=PLVdjsGOClzyHqLF_bgbxdPxAKHpGdkqx2"),

        // 5: حازم شومان
        SeedEntry(5, "حازم شومان", "تحفيز ونجاح", "أسباب النجاح في الدنيا والدين — طريقة تفكيرك", "https://www.youtube.com/watch?v=-pmRKYcbFI4"),
        SeedEntry(5, "حازم شومان", "تحفيز ونجاح", "أكتر فيديو تحفيزي ممكن تشوفه", "https://www.youtube.com/watch?v=J71rHj7gyCU", true),
        SeedEntry(5, "حازم شومان", "تحفيز ونجاح", "حزنك وهم", "https://www.youtube.com/watch?v=4t3_HOTG0JY"),
        SeedEntry(5, "حازم شومان", "قصص الصحابة", "﴿وينقلب إلى أهله مسرورًا﴾ — قصة أبي سلمة وأم سلمة", "https://www.youtube.com/watch?v=Mw_xZ101Xps"),
        SeedEntry(5, "حازم شومان", "التوبة", "والله هنقدر نتوب يا شباب", "https://www.youtube.com/watch?v=-63VeI1gKXg"),
        SeedEntry(5, "حازم شومان", "التوبة", "مهما بعدت — الرجوع إلى الله", "https://www.youtube.com/watch?v=nCNooKzdfA0"),
        SeedEntry(5, "حازم شومان", "التوبة", "فيديو تحفيزي عن التوبة والندم", "https://www.youtube.com/watch?v=2JkQUvroXRc"),
        SeedEntry(5, "حازم شومان", "التوبة", "التوبة — مقطع جميل", "https://www.youtube.com/watch?v=YZebJPi9Qo8"),
        SeedEntry(5, "حازم شومان", "حكاية آية وقصص", "شفيعًا لأصحابه — حكاية آية", "https://www.youtube.com/watch?v=RcIs_XYBDws"),
        SeedEntry(5, "حازم شومان", "حكاية آية وقصص", "قصة مؤثرة في فضل قراءة القرآن", "https://www.youtube.com/watch?v=lJivOl23G6U"),
        SeedEntry(5, "حازم شومان", "حكاية آية وقصص", "عجائب الصلاة على رسول الله ﷺ — ج2", "https://www.youtube.com/watch?v=wcroQmXWtGM"),
        SeedEntry(5, "حازم شومان", "تزكية وثقة بالله", "حقيقة الدنيا", "https://www.youtube.com/watch?v=DFCrbwzTp4Q"),
        SeedEntry(5, "حازم شومان", "تزكية وثقة بالله", "الثقة في الله", "https://www.youtube.com/watch?v=5VQUEPuDYok"),
        SeedEntry(5, "حازم شومان", "أسرة", "الفجوة بين الآباء والأبناء", "https://www.youtube.com/watch?v=l9j708-JDIE"),
        SeedEntry(5, "حازم شومان", "أسرة", "كلمة مؤثرة عن الحياة الزوجية", "https://www.youtube.com/watch?v=lVVahYXUlsU"),
        SeedEntry(5, "حازم شومان", "قوائم تشغيل", "مقاطع الصلاة", "https://www.youtube.com/playlist?list=PLVPp4tYPys9O-Vlcp8dnmaShe6eZ2Y3Uq"),
        SeedEntry(5, "حازم شومان", "قوائم تشغيل", "خطب الجمعة", "https://www.youtube.com/playlist?list=PLVPp4tYPys9MxsCWyA5z60h55qMTheKjz"),

        // 6: شريف علي
        SeedEntry(6, "شريف علي", "واهدِ قلبي — تزكية", "٥- ثمرات الخوف", "https://www.youtube.com/watch?v=mQxALzCnlNU"),
        SeedEntry(6, "شريف علي", "واهدِ قلبي — تزكية", "٢٦- حُب الدنيا", "https://www.youtube.com/watch?v=xtBD4GoaYtE"),
        SeedEntry(6, "شريف علي", "واهدِ قلبي — تزكية", "٧- الذكر يدفع الهم والغم", "https://www.youtube.com/watch?v=2VHpBxoBwNE"),
        SeedEntry(6, "شريف علي", "مجالس الشباب", "مُعاملة الكريم", "https://www.youtube.com/watch?v=_8q-7fyCyyU"),
        SeedEntry(6, "شريف علي", "مجالس الشباب", "لا تفتحه!", "https://www.youtube.com/watch?v=jXgwAEfAsVw"),
        SeedEntry(6, "شريف علي", "مجالس الشباب", "أين المفر…؟", "https://www.youtube.com/watch?v=VZbnsz1M8Es"),
        SeedEntry(6, "شريف علي", "مجالس الشباب", "﴿ثُمَّ يَتُوبُونَ مِن قَرِيبٍ﴾", "https://www.youtube.com/watch?v=yLGZvdlggWY"),
        SeedEntry(6, "شريف علي", "مجالس الشباب", "علمني نوحٌ عليه السلام", "https://www.youtube.com/watch?v=dKxkqEFYuUU"),
        SeedEntry(6, "شريف علي", "مجالس الشباب", "شفاء القلب في زمن الفتن — من سورة يوسف", "https://www.youtube.com/watch?v=StsGAgA37fY"),
        SeedEntry(6, "شريف علي", "القرآن", "﴿قَدْ أَفْلَحَ الْمُؤْمِنُونَ﴾", "https://www.youtube.com/watch?v=kUcfwqp_2gg"),
        SeedEntry(6, "شريف علي", "التوبة", "نعمة التوبة — تخيّل حياتك بلا توبة", "https://www.youtube.com/watch?v=oZ6Paggql-A"),
        SeedEntry(6, "شريف علي", "التوبة", "رسالة لكل من أرهقته ذنوب الخلوات", "https://www.youtube.com/watch?v=DXlyygjcRA8"),

        // 7: عبد العزيز الطريفي
        SeedEntry(7, "عبد العزيز الطريفي", "إيمانيات وتزكية", "الصحابة.. الجيل الذي غيّر العالم", "https://www.youtube.com/watch?v=-2nKpmmaS6I"),
        SeedEntry(7, "عبد العزيز الطريفي", "إيمانيات وتزكية", "الدنيا ليست لك (الابتلاء)", "https://www.youtube.com/watch?v=M4JC2uvYBYs"),
        SeedEntry(7, "عبد العزيز الطريفي", "الصلاة", "كيف تحافظ على الصلاة؟", "https://www.youtube.com/watch?v=z9MMPhdIe2U"),
        SeedEntry(7, "عبد العزيز الطريفي", "الصلاة", "المحافظة على الصلاة", "https://www.youtube.com/watch?v=RmdjSW8_Rl4"),
        SeedEntry(7, "عبد العزيز الطريفي", "الصلاة", "حكم تارك الصلاة", "https://www.youtube.com/watch?v=feF28UkkJLs"),
        SeedEntry(7, "عبد العزيز الطريفي", "إيمان وعقيدة", "الإيمان قول وعمل واعتقاد", "https://www.youtube.com/watch?v=eNPCx1VXzuw"),
        SeedEntry(7, "عبد العزيز الطريفي", "إيمان وعقيدة", "الزيغ موجود في القلب قبل النظر", "https://www.youtube.com/watch?v=HzJZxOiBN20"),
        SeedEntry(7, "عبد العزيز الطريفي", "إيمان وعقيدة", "الصراع بين الكفر والإيمان", "https://www.youtube.com/watch?v=o9NEC6ijrSw", true)
    )

    fun buildEntities(): List<VideoEntity> {
        return entries.mapIndexedNotNull { idx, entry ->
            val parsed = YouTubeUrlParser.parse(entry.url) ?: return@mapIndexedNotNull null
            VideoEntity(
                key = parsed.key,
                kindCode = parsed.kind.code,
                youtubeId = parsed.id,
                title = entry.title,
                scholar = entry.scholar,
                scholarIndex = entry.scholarIndex,
                topic = entry.topic,
                category = CategoryMapper.categoryOf(entry.topic),
                isShort = entry.isShort,
                isCustom = false,
                isWatched = false,
                isSaved = false,
                sortOrder = idx
            )
        }
    }
}
