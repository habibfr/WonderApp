package com.example.data

import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ChildProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val childName: String = "Penjelajah Kecil",
    val ageGroup: String = "7-9",
    val selectedCharacter: String = "komodo",
    val languageCode: String = "id",
    val darkMode: Boolean = false,
    val parentPin: String = "1234",
    val totalStars: Long = 0L,
    val completedLevelsCount: Long = 0L,
    val mathScore: Long = 0L,
    val logicScore: Long = 0L,
    val streakDays: Long = 1L,
    val unlockedBadges: List<String> = listOf("curious_buddy"),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreatePayload(ownerUid: String): Map<String, Any> {
        val validLang = if (languageCode in listOf("id", "en", "es", "fr", "hi")) languageCode else "id"
        val validChar = if (selectedCharacter in listOf("sparky", "luna", "robobit", "sunny", "komodo")) selectedCharacter else "komodo"
        val validAge = if (ageGroup in listOf("4-6", "7-9", "10-13")) ageGroup else "7-9"
        return mapOf(
            "userId" to ownerUid,
            "childName" to childName.trim().take(40).ifEmpty { "Penjelajah Kecil" },
            "ageGroup" to validAge,
            "selectedCharacter" to validChar,
            "languageCode" to validLang,
            "darkMode" to darkMode,
            "parentPin" to parentPin.filter { it.isDigit() }.padEnd(4, '0').take(4),
            "totalStars" to totalStars.coerceIn(0L, 100000L),
            "completedLevelsCount" to completedLevelsCount.coerceIn(0L, 50000L),
            "mathScore" to mathScore.coerceIn(0L, 1000000L),
            "logicScore" to logicScore.coerceIn(0L, 1000000L),
            "streakDays" to streakDays.coerceIn(0L, 3650L),
            "unlockedBadges" to unlockedBadges.distinct().take(20),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

@IgnoreExtraProperties
data class GameSessionRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val gameType: String = "math_safari",
    val ageGroup: String = "7-9",
    val levelNumber: Long = 1L,
    val starsEarned: Long = 3L,
    val correctAnswers: Long = 5L,
    val totalQuestions: Long = 5L,
    val durationSeconds: Long = 30L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreatePayload(ownerUid: String): Map<String, Any> {
        val validAge = if (ageGroup in listOf("4-6", "7-9", "10-13")) ageGroup else "7-9"
        val totalQ = totalQuestions.coerceIn(1L, 20L)
        val correctQ = correctAnswers.coerceIn(0L, totalQ)
        return mapOf(
            "userId" to ownerUid,
            "gameType" to MiniGameType.fromId(gameType).id,
            "ageGroup" to validAge,
            "levelNumber" to levelNumber.coerceIn(1L, 50L),
            "starsEarned" to starsEarned.coerceIn(1L, 3L),
            "correctAnswers" to correctQ,
            "totalQuestions" to totalQ,
            "durationSeconds" to durationSeconds.coerceIn(1L, 3600L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

enum class BuddyReaction {
    IDLE,
    CELEBRATING,
    DANCING,
    HIGH_FIVE,
    SPARKLE,
    EATING_TREAT,
    MAGIC_WAND,
    THINKING,
    SLEEPY
}

enum class AgeGroupConfig(
    val code: String,
    val iconKey: String,
    val titleId: String,
    val titleEn: String,
    val titleEs: String,
    val titleFr: String,
    val titleHi: String
) {
    EARLY(
        code = "4-6",
        iconKey = "early",
        titleId = "4-6 Tahun • Tunas",
        titleEn = "Ages 4-6 • Sprouts",
        titleEs = "4-6 Años • Semillas",
        titleFr = "4-6 Ans • Bourgeons",
        titleHi = "4-6 वर्ष • अंकुर"
    ),
    JUNIOR(
        code = "7-9",
        iconKey = "junior",
        titleId = "7-9 Tahun • Penjelajah",
        titleEn = "Ages 7-9 • Rangers",
        titleEs = "7-9 Años • Exploradores",
        titleFr = "7-9 Ans • Rangers",
        titleHi = "7-9 वर्ष • रेंजर्स"
    ),
    SENIOR(
        code = "10-13",
        iconKey = "senior",
        titleId = "10-13 Tahun • Master",
        titleEn = "Ages 10-13 • Wizards",
        titleEs = "10-13 Años • Magos",
        titleFr = "10-13 Ans • Mages",
        titleHi = "10-13 वर्ष • जादूगर"
    );

    fun localizedTitle(lang: String): String = when (lang) {
        "en" -> titleEn
        "es" -> titleEs
        "fr" -> titleFr
        "hi" -> titleHi
        else -> titleId
    }

    companion object {
        fun fromCode(code: String): AgeGroupConfig = entries.find { it.code == code } ?: JUNIOR
    }
}

enum class GamePlayMode(
    val id: String,
    val iconKey: String,
    val colorHex: Long,
    val questionCount: Int,
    val timeLimitSeconds: Int,
    val bonusStars: Int,
    val titleId: String,
    val titleEn: String,
    val titleEs: String,
    val titleFr: String,
    val titleHi: String
) {
    ADVENTURE(
        id = "adventure",
        iconKey = "explore",
        colorHex = 0xFF0284C7,
        questionCount = 5,
        timeLimitSeconds = 0,
        bonusStars = 0,
        titleId = "Petualangan",
        titleEn = "Adventure",
        titleEs = "Aventura",
        titleFr = "Aventure",
        titleHi = "साहसिक"
    ),
    STAR_BLITZ(
        id = "star_blitz",
        iconKey = "bolt",
        colorHex = 0xFFE05347,
        questionCount = 5,
        timeLimitSeconds = 45,
        bonusStars = 1,
        titleId = "Kilat Bintang",
        titleEn = "Star Blitz",
        titleEs = "Rayo Estelar",
        titleFr = "Éclair",
        titleHi = "स्टार ब्लिट्ज"
    ),
    ZEN_PRACTICE(
        id = "zen_practice",
        iconKey = "spa",
        colorHex = 0xFF10B981,
        questionCount = 4,
        timeLimitSeconds = 0,
        bonusStars = 0,
        titleId = "Latihan Santai",
        titleEn = "Zen Practice",
        titleEs = "Práctica Zen",
        titleFr = "Mode Zen",
        titleHi = "शांत अभ्यास"
    ),
    DAILY_QUEST(
        id = "daily_quest",
        iconKey = "trophy",
        colorHex = 0xFF7C3AED,
        questionCount = 6,
        timeLimitSeconds = 0,
        bonusStars = 2,
        titleId = "Misi Harian",
        titleEn = "Daily Quest",
        titleEs = "Misión Diaria",
        titleFr = "Quête du Jour",
        titleHi = "दैनिक मिशन"
    );

    fun localizedTitle(lang: String): String = when (lang) {
        "en" -> titleEn
        "es" -> titleEs
        "fr" -> titleFr
        "hi" -> titleHi
        else -> titleId
    }
}

enum class BuddyCharacter(
    val id: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val nameId: String,
    val nameEn: String,
    val nameEs: String,
    val nameFr: String,
    val nameHi: String,
    val titleId: String,
    val titleEn: String,
    val titleEs: String,
    val titleFr: String,
    val titleHi: String
) {
    KOMODO(
        id = "komodo",
        primaryColorHex = 0xFF10B981,
        secondaryColorHex = 0xFFFFD95A,
        nameId = "Koko Komodo",
        nameEn = "Koko the Dragon",
        nameEs = "Dragón Koko",
        nameFr = "Koko le Dragon",
        nameHi = "कोको ड्रैगन",
        titleId = "Penjaga Pulau Nusantara",
        titleEn = "Emerald Island Guardian",
        titleEs = "Guardián de la Isla",
        titleFr = "Gardien de l'Île",
        titleHi = "द्वीप रक्षक"
    ),
    SPARKY(
        id = "sparky",
        primaryColorHex = 0xFFFF7F73,
        secondaryColorHex = 0xFFFFD95A,
        nameId = "Sparky si Rubah",
        nameEn = "Sparky the Fox",
        nameEs = "Zorrito Sparky",
        nameFr = "Sparky le Renard",
        nameHi = "स्पार्की लोमड़ी",
        titleId = "Penjelajah Angka Ceria",
        titleEn = "Number Trailblazer",
        titleEs = "Explorador de Números",
        titleFr = "Éclaireur des Nombres",
        titleHi = "संख्या खोजकर्ता"
    ),
    LUNA(
        id = "luna",
        primaryColorHex = 0xFFA98BFA,
        secondaryColorHex = 0xFF72DDA8,
        nameId = "Luna Burung Hantu",
        nameEn = "Luna the Owl",
        nameEs = "Búho Luna",
        nameFr = "Luna la Chouette",
        nameHi = "लूना उल्लू",
        titleId = "Ahli Pola Bintang",
        titleEn = "Pattern Alchemist",
        titleEs = "Alquimista de Patrones",
        titleFr = "Alchimiste des Motifs",
        titleHi = "पैटर्न जादूगरनी"
    ),
    ROBOBIT(
        id = "robobit",
        primaryColorHex = 0xFF55C7F7,
        secondaryColorHex = 0xFFFFD95A,
        nameId = "Robo-Bit Pintar",
        nameEn = "Robo-Bit",
        nameEs = "Robo-Bit",
        nameFr = "Robo-Bit",
        nameHi = "रोबो-बिट",
        titleId = "Sahabat Sirkuit Logika",
        titleEn = "Logic Circuit Buddy",
        titleEs = "Amigo de Circuitos Lógicos",
        titleFr = "Copain des Circuits Logiques",
        titleHi = "लॉजिक रोबोट मित्र"
    ),
    SUNNY(
        id = "sunny",
        primaryColorHex = 0xFFF59E0B,
        secondaryColorHex = 0xFFFF7F73,
        nameId = "Sunny Singa Bintang",
        nameEn = "Sunny Star",
        nameEs = "Estrella Sunny",
        nameFr = "Étoile Sunny",
        nameHi = "सनी तारा",
        titleId = "Kapten Semangat Kosmik",
        titleEn = "Cosmic Cheer Captain",
        titleEs = "Capitán de Alegría Cósmica",
        titleFr = "Capitaine de Joie Cosmique",
        titleHi = "चमकता तारा कप्तान"
    );

    fun localizedName(lang: String): String = when (lang) {
        "en" -> nameEn
        "es" -> nameEs
        "fr" -> nameFr
        "hi" -> nameHi
        else -> nameId
    }

    fun localizedTitle(lang: String): String = when (lang) {
        "en" -> titleEn
        "es" -> titleEs
        "fr" -> titleFr
        "hi" -> titleHi
        else -> titleId
    }

    companion object {
        fun fromId(id: String): BuddyCharacter = entries.find { it.id == id } ?: KOMODO
    }
}

enum class MiniGameType(
    val id: String,
    val isMath: Boolean,
    val iconKey: String,
    val drawableRes: Int,
    val accentColorHex: Long,
    val titleId: String,
    val titleEn: String,
    val titleEs: String,
    val titleFr: String,
    val titleHi: String,
    val subtitleId: String,
    val subtitleEn: String,
    val subtitleEs: String,
    val subtitleFr: String,
    val subtitleHi: String
) {
    MATH_SAFARI(
        id = "math_safari",
        isMath = true,
        iconKey = "calculate",
        drawableRes = R.drawable.ic_game_math_safari,
        accentColorHex = 0xFF10B981,
        titleId = "Safari Matematika",
        titleEn = "Math Safari",
        titleEs = "Safari Matemático",
        titleFr = "Safari Mathématique",
        titleHi = "गणित सफारी",
        subtitleId = "Hitung buah, bintang, dan kristal ajaib di pulau tropis!",
        subtitleEn = "Count, add, subtract and multiply across enchanted islands!",
        subtitleEs = "¡Cuenta, suma, resta y multiplica en islas encantadas!",
        subtitleFr = "Compte, additionne, soustrais et multiplie sur les îles !",
        subtitleHi = "जादुई द्वीपों पर गिनें, जोड़ें, घटाएं और गुणा करें!"
    ),
    PATTERN_PORTAL(
        id = "pattern_portal",
        isMath = false,
        iconKey = "extension",
        drawableRes = R.drawable.ic_game_pattern_portal,
        accentColorHex = 0xFF7C3AED,
        titleId = "Portal Pola Ajaib",
        titleEn = "Pattern Portal",
        titleEs = "Portal de Patrones",
        titleFr = "Portail des Motifs",
        titleHi = "पैटर्न पोर्टल",
        subtitleId = "Lengkapi urutan bentuk geometri warna-warni dan deret angka!",
        subtitleEn = "Unlock glowing sequences of shapes, colors and numbers!",
        subtitleEs = "¡Descubre secuencias mágicas de formas, colores y números!",
        subtitleFr = "Complète les suites magiques de formes et de couleurs !",
        subtitleHi = "आकृतियों, रंगों और संख्याओं के जादुई क्रम को पूरा करें!"
    ),
    NUMBER_BRIDGE(
        id = "number_bridge",
        isMath = true,
        iconKey = "bridge",
        drawableRes = R.drawable.ic_game_number_bridge,
        accentColorHex = 0xFFF59E0B,
        titleId = "Jembatan Angka",
        titleEn = "Missing Bridge",
        titleEs = "Puente Numérico",
        titleFr = "Pont des Nombres",
        titleHi = "संख्या पुल",
        subtitleId = "Temukan batu angka yang hilang untuk menyeberangi jembatan pelangi!",
        subtitleEn = "Find the missing stone to balance the equation bridge!",
        subtitleEs = "¡Encuentra la piedra que falta para cruzar el puente!",
        subtitleFr = "Trouve la pierre manquante pour équilibrer le pont !",
        subtitleHi = "समीकरण पुल को पार करने के लिए सही संख्या ढूंढें!"
    ),
    LOGIC_MATRIX(
        id = "logic_matrix",
        isMath = false,
        iconKey = "psychology",
        drawableRes = R.drawable.ic_game_logic_matrix,
        accentColorHex = 0xFFE05347,
        titleId = "Detektif Logika",
        titleEn = "Odd-One & Riddles",
        titleEs = "Acertijos Lógicos",
        titleFr = "Énigmes & Intrus",
        titleHi = "तर्क पहेलियाँ",
        subtitleId = "Temukan benda unik dan pecahkan teka-teki logika cerdas!",
        subtitleEn = "Spot the rule-breaker and solve clever brain teasers!",
        subtitleEs = "¡Descubre al intruso y resuelve acertijos ingeniosos!",
        subtitleFr = "Trouve l'intrus et résous des casse-têtes malins !",
        subtitleHi = "अलग विकल्प पहचानें और मजेदार दिमागी पहेलियाँ सुलझाएं!"
    ),
    SHAPE_GALAXY(
        id = "shape_galaxy",
        isMath = true,
        iconKey = "shapes",
        drawableRes = R.drawable.ic_game_pattern_portal,
        accentColorHex = 0xFF0284C7,
        titleId = "Galaksi Geometri",
        titleEn = "Shape Galaxy",
        titleEs = "Galaxia Geométrica",
        titleFr = "Galaxie des Formes",
        titleHi = "आकृति गैलेक्सी",
        subtitleId = "Hitung sudut dan sisi bangun datar yang berputar di angkasa!",
        subtitleEn = "Count glowing corners and sides of spinning cosmic polygons!",
        subtitleEs = "¡Cuenta las esquinas brillantes de polígonos espaciales!",
        subtitleFr = "Compte les sommets lumineux des polygones cosmiques !",
        subtitleHi = "अंतरिक्ष में घूमती आकृतियों के कोने और भुजाएँ गिनें!"
    ),
    MEMORY_TREASURE(
        id = "memory_treasure",
        isMath = false,
        iconKey = "diamond",
        drawableRes = R.drawable.ic_game_logic_matrix,
        accentColorHex = 0xFFEC4899,
        titleId = "Harta Memori",
        titleEn = "Memory Treasure",
        titleEs = "Tesoro de Memoria",
        titleFr = "Trésor de Mémoire",
        titleHi = "स्मृति खजाना",
        subtitleId = "Ingat urutan kristal dan permata yang muncul di peti harta!",
        subtitleEn = "Remember the secret crystal sequence inside the treasure chest!",
        subtitleEs = "¡Recuerda el orden de los cristales en el cofre del tesoro!",
        subtitleFr = "Mémorise l'ordre des cristaux dans le coffre au trésor !",
        subtitleHi = "खजाने की पेटी में रत्नों का सही क्रम याद रखें!"
    ),
    COSMIC_BALANCE(
        id = "cosmic_balance",
        isMath = true,
        iconKey = "balance",
        drawableRes = R.drawable.ic_game_number_bridge,
        accentColorHex = 0xFF0D9488,
        titleId = "Timbangan Kosmik",
        titleEn = "Cosmic Balance",
        titleEs = "Balanza Cósmica",
        titleFr = "Balance Cosmique",
        titleHi = "कॉस्मिक तराजू",
        subtitleId = "Seimbangkan timbangan bintang dengan memilih beban angka yang tepat!",
        subtitleEn = "Balance the cosmic scale by choosing the exact star weight!",
        subtitleEs = "¡Equilibra la balanza eligiendo el peso estelar exacto!",
        subtitleFr = "Équilibre la balance cosmique avec le bon poids étoilé !",
        subtitleHi = "सही भार चुनकर कॉस्मिक तराजू को संतुलित करें!"
    );

    fun localizedTitle(lang: String): String = when (lang) {
        "en" -> titleEn
        "es" -> titleEs
        "fr" -> titleFr
        "hi" -> titleHi
        else -> titleId
    }

    fun localizedSubtitle(lang: String): String = when (lang) {
        "en" -> subtitleEn
        "es" -> subtitleEs
        "fr" -> subtitleFr
        "hi" -> subtitleHi
        else -> subtitleId
    }

    companion object {
        fun fromId(id: String): MiniGameType = entries.find { it.id == id } ?: MATH_SAFARI
    }
}

data class BadgeDefinition(
    val id: String,
    val iconKey: String,
    val drawableRes: Int,
    val colorHex: Long,
    val titleId: String,
    val titleEn: String,
    val titleEs: String,
    val titleFr: String,
    val titleHi: String,
    val descId: String,
    val descEn: String,
    val descEs: String,
    val descFr: String,
    val descHi: String
) {
    fun localizedTitle(lang: String): String = when (lang) {
        "en" -> titleEn
        "es" -> titleEs
        "fr" -> titleFr
        "hi" -> titleHi
        else -> titleId
    }

    fun localizedDesc(lang: String): String = when (lang) {
        "en" -> descEn
        "es" -> descEs
        "fr" -> descFr
        "hi" -> descHi
        else -> descId
    }
}

object BadgeCatalog {
    val allBadges = listOf(
        BadgeDefinition(
            id = "curious_buddy",
            iconKey = "star",
            drawableRes = R.drawable.ic_badge_star_medal,
            colorHex = 0xFFF59E0B,
            titleId = "Sahabat Penasaran",
            titleEn = "Curious Buddy",
            titleEs = "Amigo Curioso",
            titleFr = "Ami Curieux",
            titleHi = "जिज्ञासु मित्र",
            descId = "Bergabung di Pulau WonderQuest dan bertemu sahabat belajarmu!",
            descEn = "Joined WonderQuest Island and met your animated learning buddy!",
            descEs = "¡Te uniste a WonderQuest y conociste a tu compañero animado!",
            descFr = "Tu as rejoint l'Île WonderQuest et rencontré ton compagnon !",
            descHi = "वंडरक्वेस्ट में शामिल हुए और अपने दोस्त से मिले!"
        ),
        BadgeDefinition(
            id = "first_quest",
            iconKey = "explore",
            drawableRes = R.drawable.ic_badge_rocket_quest,
            colorHex = 0xFF10B981,
            titleId = "Petualang Pertama",
            titleEn = "First Quest Complete",
            titleEs = "Primera Misión",
            titleFr = "Première Quête",
            titleHi = "पहली जीत",
            descId = "Menyelesaikan level permainan edukasi pertamamu dengan berani!",
            descEn = "Completed your very first educational mini-game level!",
            descEs = "¡Completaste tu primer nivel educativo!",
            descFr = "Tu as terminé ton tout premier niveau éducatif !",
            descHi = "अपना पहला शैक्षिक मिनी-गेम स्तर पूरा किया!"
        ),
        BadgeDefinition(
            id = "star_collector",
            iconKey = "workspace_premium",
            drawableRes = R.drawable.ic_badge_star_medal,
            colorHex = 0xFFEAB308,
            titleId = "Kolektor Bintang",
            titleEn = "Star Collector",
            titleEs = "Coleccionista de Estrellas",
            titleFr = "Collectionneur d'Étoiles",
            titleHi = "स्टार कलेक्टर",
            descId = "Mengumpulkan 10 atau lebih bintang emas di Pulau WonderQuest!",
            descEn = "Collected 10 or more golden stars across WonderQuest Island!",
            descEs = "¡Coleccionaste 10 o más estrellas doradas!",
            descFr = "Tu as collectionné 10 étoiles dorées ou plus !",
            descHi = "10 या अधिक सुनहरे सितारे एकत्र किए!"
        ),
        BadgeDefinition(
            id = "math_whiz",
            iconKey = "calculate",
            drawableRes = R.drawable.ic_game_math_safari,
            colorHex = 0xFF0284C7,
            titleId = "Ahli Matematika",
            titleEn = "Math Safari Whiz",
            titleEs = "Genio Matemático",
            titleFr = "Prodige des Maths",
            titleHi = "गणित उस्ताद",
            descId = "Meraih 100+ poin dari tantangan berhitung dan jembatan angka!",
            descEn = "Scored 100+ points in Math Safari and Missing Bridge!",
            descEs = "¡Obtuviste más de 100 puntos en juegos de matemáticas!",
            descFr = "Plus de 100 points gagnés dans les défis mathématiques !",
            descHi = "गणित के खेलों में 100+ अंक हासिल किए!"
        ),
        BadgeDefinition(
            id = "logic_master",
            iconKey = "psychology",
            drawableRes = R.drawable.ic_game_pattern_portal,
            colorHex = 0xFF7C3AED,
            titleId = "Master Logika",
            titleEn = "Logic Crystal Master",
            titleEs = "Maestro de la Lógica",
            titleFr = "Maître de la Logique",
            titleHi = "तर्क सम्राट",
            descId = "Meraih 100+ poin dari Portal Pola dan Detektif Logika!",
            descEn = "Scored 100+ points in Pattern Portal and Logic Riddles!",
            descEs = "¡Más de 100 puntos en patrones y acertijos lógicos!",
            descFr = "Plus de 100 points en logique et suites magiques !",
            descHi = "पैटर्न और तर्क पहेलियों में 100+ अंक प्राप्त किए!"
        ),
        BadgeDefinition(
            id = "super_streak",
            iconKey = "bolt",
            drawableRes = R.drawable.ic_badge_crown_ace,
            colorHex = 0xFFE05347,
            titleId = "Juara Sempurna",
            titleEn = "Super Streak Ace",
            titleEs = "Racha Perfecta",
            titleFr = "Série Parfaite",
            titleHi = "सुपर स्ट्रीक",
            descId = "Menjawab semua pertanyaan dengan benar (3 Bintang Penuh)!",
            descEn = "Answered every question correctly in a level for 3 full stars!",
            descEs = "¡Respondiste todas las preguntas correctamente en un nivel!",
            descFr = "Tu as répondu correctement à toutes les questions d'un niveau !",
            descHi = "एक स्तर के सभी प्रश्नों के सही उत्तर दिए!"
        ),
        BadgeDefinition(
            id = "island_champion",
            iconKey = "trophy",
            drawableRes = R.drawable.ic_badge_trophy_cup,
            colorHex = 0xFF0D9488,
            titleId = "Kampiun Pulau",
            titleEn = "Island Champion",
            titleEs = "Campeón de la Isla",
            titleFr = "Champion de l'Île",
            titleHi = "द्वीप चैंपियन",
            descId = "Menyelesaikan 5 level petualangan belajar di Pulau WonderQuest!",
            descEn = "Completed 5 or more learning adventure levels on the Island!",
            descEs = "¡Completaste 5 o más niveles de aventura en la isla!",
            descFr = "Tu as terminé 5 niveaux d'aventure ou plus sur l'île !",
            descHi = "द्वीप पर 5 या अधिक स्तर पूरे किए!"
        ),
        BadgeDefinition(
            id = "galaxy_explorer",
            iconKey = "shapes",
            drawableRes = R.drawable.ic_badge_globe_world,
            colorHex = 0xFF6366F1,
            titleId = "Penjelajah Galaksi",
            titleEn = "Galaxy Explorer",
            titleEs = "Explorador Galáctico",
            titleFr = "Explorateur Galactique",
            titleHi = "गैलेक्सी खोजकर्ता",
            descId = "Menaklukkan tantangan Galaksi Geometri atau Timbangan Kosmik!",
            descEn = "Conquered a quest in Shape Galaxy or Cosmic Balance!",
            descEs = "¡Completaste un desafío en Galaxia Geométrica o Balanza Cósmica!",
            descFr = "Tu as réussi un défi dans Galaxie des Formes ou Balance Cosmique !",
            descHi = "आकृति गैलेक्सी या कॉस्मिक तराजू में जीत हासिल की!"
        )
    )
}
