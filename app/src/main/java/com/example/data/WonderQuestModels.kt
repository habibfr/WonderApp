package com.example.data

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
    val claimedRewardBadges: List<String> = listOf("curious_buddy"),
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
            "claimedRewardBadges" to claimedRewardBadges.distinct().take(20),
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
        val validGame = if (gameType in MiniGameType.entries.map { it.id }) gameType else "math_safari"
        val validAge = if (ageGroup in listOf("4-6", "7-9", "10-13")) ageGroup else "7-9"
        val safeTotal = totalQuestions.coerceIn(1L, 20L)
        val safeCorrect = correctAnswers.coerceIn(0L, safeTotal)
        return mapOf(
            "userId" to ownerUid,
            "gameType" to validGame,
            "ageGroup" to validAge,
            "levelNumber" to levelNumber.coerceIn(1L, 50L),
            "starsEarned" to starsEarned.coerceIn(1L, 3L),
            "correctAnswers" to safeCorrect,
            "totalQuestions" to safeTotal,
            "durationSeconds" to durationSeconds.coerceIn(1L, 3600L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

enum class GamePlayMode(
    val id: String,
    val iconKey: String,
    val titleId: String,
    val titleEn: String,
    val titleEs: String,
    val titleFr: String,
    val titleHi: String,
    val descId: String,
    val descEn: String,
    val descEs: String,
    val descFr: String,
    val descHi: String,
    val colorHex: Long,
    val questionCount: Int,
    val timeLimitSeconds: Int,
    val bonusStars: Int
) {
    ADVENTURE(
        id = "adventure",
        iconKey = "explore",
        titleId = "Petualangan",
        titleEn = "Adventure",
        titleEs = "Aventura",
        titleFr = "Aventure",
        titleHi = "रोमांच",
        descId = "5 tantangan terpandu dengan petunjuk visual dan penghargaan bintang.",
        descEn = "5 guided challenges with visual hints and star rewards.",
        descEs = "5 acertijos con pistas visuales y recompensas.",
        descFr = "5 défis guidés avec indices visuels et étoiles.",
        descHi = "मार्गदर्शित चुनौतियों और सितारों के साथ 5 पहेलियाँ।",
        colorHex = 0xFF4F46E5,
        questionCount = 5,
        timeLimitSeconds = 0,
        bonusStars = 0
    ),
    STAR_BLITZ(
        id = "star_blitz",
        iconKey = "bolt",
        titleId = "Kilat Bintang",
        titleEn = "Star Blitz",
        titleEs = "Relámpago",
        titleFr = "Éclair",
        titleHi = "सुपर स्पीड",
        descId = "Tantangan kecepatan 60 detik dengan bonus bintang ekstra.",
        descEn = "60-second speed challenge with extra bonus stars.",
        descEs = "Reto rápido de 60 segundos con estrellas extra.",
        descFr = "Défi rapide de 60 secondes avec étoiles bonus.",
        descHi = "बोनस सितारों के साथ 60-सेकंड की तेज़ चुनौती।",
        colorHex = 0xFFFF6B35,
        questionCount = 6,
        timeLimitSeconds = 60,
        bonusStars = 2
    ),
    ZEN_PRACTICE(
        id = "zen_practice",
        iconKey = "spa",
        titleId = "Latihan Santai",
        titleEn = "Zen Practice",
        titleEs = "Modo Zen",
        titleFr = "Mode Zen",
        titleHi = "आरामदायक",
        descId = "Belajar santai tanpa batas waktu dengan bantuan hitung visual interaktif.",
        descEn = "Relaxed learning with interactive visual counting counters.",
        descEs = "Aprendizaje relajado con conteo visual interactivo.",
        descFr = "Apprentissage calme avec aide au comptage visuel.",
        descHi = "बिना समय सीमा के विजुअल वस्तुओं से सीखें।",
        colorHex = 0xFF10B981,
        questionCount = 4,
        timeLimitSeconds = 0,
        bonusStars = 1
    ),
    DAILY_QUEST(
        id = "daily_quest",
        iconKey = "trophy",
        titleId = "Misi Harian",
        titleEn = "Daily Quest",
        titleEs = "Misión Diaria",
        titleFr = "Quête du Jour",
        titleHi = "दैनिक मिशन",
        descId = "Tantangan spesial harian untuk meraih hadiah bintang emas ekstra.",
        descEn = "Special daily challenge for bonus golden stars.",
        descEs = "Misión especial diaria por estrellas doradas extra.",
        descFr = "Mission spéciale du jour pour des étoiles dorées bonus.",
        descHi = "बोनस सुनहरे सितारों के लिए विशेष दैनिक मिशन।",
        colorHex = 0xFFE91E63,
        questionCount = 5,
        timeLimitSeconds = 90,
        bonusStars = 3
    );

    fun localizedTitle(lang: String): String = when (lang) {
        "en" -> titleEn
        "es" -> titleEs
        "fr" -> titleFr
        "hi" -> titleHi
        else -> titleId
    }

    fun localizedDescription(lang: String): String = when (lang) {
        "en" -> descEn
        "es" -> descEs
        "fr" -> descFr
        "hi" -> descHi
        else -> descId
    }
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
    AGES_4_6(
        code = "4-6",
        iconKey = "early",
        titleId = "4–6 Tahun",
        titleEn = "Ages 4–6",
        titleEs = "4–6 Años",
        titleFr = "4–6 Ans",
        titleHi = "4–6 वर्ष"
    ),
    AGES_7_9(
        code = "7-9",
        iconKey = "junior",
        titleId = "7–9 Tahun",
        titleEn = "Ages 7–9",
        titleEs = "7–9 Años",
        titleFr = "7–9 Ans",
        titleHi = "7–9 वर्ष"
    ),
    AGES_10_13(
        code = "10-13",
        iconKey = "senior",
        titleId = "10–13 Tahun",
        titleEn = "Ages 10–13",
        titleEs = "10–13 Años",
        titleFr = "10–13 Ans",
        titleHi = "10–13 वर्ष"
    );

    fun localizedTitle(lang: String): String = when (lang) {
        "en" -> titleEn
        "es" -> titleEs
        "fr" -> titleFr
        "hi" -> titleHi
        else -> titleId
    }

    companion object {
        fun fromCode(code: String): AgeGroupConfig =
            entries.find { it.code == code } ?: AGES_7_9
    }
}

enum class BuddyReaction {
    IDLE,
    DANCING,
    CELEBRATING,
    THINKING,
    HIGH_FIVE,
    SPARKLE,
    EATING_TREAT,
    MAGIC_WAND,
    SLEEPY
}

enum class BuddyCharacter(
    val id: String,
    val iconKey: String,
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
        iconKey = "dragon",
        primaryColorHex = 0xFF00897B,
        secondaryColorHex = 0xFFFFCA28,
        nameId = "Bima Komodo",
        nameEn = "Bima Komodo",
        nameEs = "Bima Dragón",
        nameFr = "Bima Dragon",
        nameHi = "बीमा कोमोडो",
        titleId = "Penjaga Pulau Nusantara",
        titleEn = "Emerald Island Guardian",
        titleEs = "Guardián de la Isla",
        titleFr = "Gardien de l'Île",
        titleHi = "द्वीप रक्षक मित्र"
    ),
    SPARKY(
        id = "sparky",
        iconKey = "fox",
        primaryColorHex = 0xFFFF7043,
        secondaryColorHex = 0xFFFFCA28,
        nameId = "Sparky Rubah",
        nameEn = "Sparky Fox",
        nameEs = "Zorro Sparky",
        nameFr = "Renard Sparky",
        nameHi = "स्पार्की लोमड़ी",
        titleId = "Penjelajah Bintang",
        titleEn = "Star Explorer",
        titleEs = "Explorador Estelar",
        titleFr = "Explorateur d'Étoiles",
        titleHi = "स्टार एक्सप्लोरर"
    ),
    LUNA(
        id = "luna",
        iconKey = "owl",
        primaryColorHex = 0xFF7E57C2,
        secondaryColorHex = 0xFF26C6DA,
        nameId = "Luna Burung Hantu",
        nameEn = "Luna Owl",
        nameEs = "Búho Luna",
        nameFr = "Chouette Luna",
        nameHi = "लूना उल्लू",
        titleId = "Penjaga Pola Ajaib",
        titleEn = "Pattern Wizard",
        titleEs = "Maga de Patrones",
        titleFr = "Magicienne des Motifs",
        titleHi = "पैटर्न जादूगर"
    ),
    ROBOBIT(
        id = "robobit",
        iconKey = "robot",
        primaryColorHex = 0xFF26A69A,
        secondaryColorHex = 0xFF42A5F5,
        nameId = "RoboBit Pintar",
        nameEn = "RoboBit",
        nameEs = "RoboBit",
        nameFr = "RoboBit",
        nameHi = "रोबोबिट",
        titleId = "Insinyur Logika",
        titleEn = "Logic Engineer",
        titleEs = "Ingeniero Lógico",
        titleFr = "Ingénieur Logique",
        titleHi = "लॉजिक इंजीनियर"
    ),
    SUNNY(
        id = "sunny",
        iconKey = "lion",
        primaryColorHex = 0xFFFFB300,
        secondaryColorHex = 0xFFEC407A,
        nameId = "Sunny Singa",
        nameEn = "Sunny Lion",
        nameEs = "León Sunny",
        nameFr = "Lion Sunny",
        nameHi = "सनी शेर",
        titleId = "Kapten Angka Berani",
        titleEn = "Number Captain",
        titleEs = "Capitán Numérico",
        titleFr = "Capitaine des Nombres",
        titleHi = "नंबर कैप्टन"
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
        fun fromId(id: String): BuddyCharacter =
            entries.find { it.id == id } ?: KOMODO
    }
}

enum class MiniGameType(
    val id: String,
    val iconKey: String,
    val accentColorHex: Long,
    val isMath: Boolean,
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
        iconKey = "calculate",
        accentColorHex = 0xFFFF7043,
        isMath = true,
        titleId = "Safari Matematika",
        titleEn = "Math Safari",
        titleEs = "Safari Matemático",
        titleFr = "Safari Maths",
        titleHi = "गणित सफारी",
        subtitleId = "Ketuk kristal visual untuk menghitung, menjumlah, dan mengalikan.",
        subtitleEn = "Tap visual crystals to count, add, subtract & multiply.",
        subtitleEs = "Toca cristales para contar, sumar y multiplicar.",
        subtitleFr = "Touche les cristaux pour compter et calculer.",
        subtitleHi = "गिनने, जोड़ने और गुणा करने के लिए विजुअल क्रिस्टल टैप करें।"
    ),
    PATTERN_PORTAL(
        id = "pattern_portal",
        iconKey = "extension",
        accentColorHex = 0xFF7E57C2,
        isMath = false,
        titleId = "Portal Pola Ajaib",
        titleEn = "Pattern Portal",
        titleEs = "Portal de Patrones",
        titleFr = "Portail des Motifs",
        titleHi = "पैटर्न पोर्टल",
        subtitleId = "Lengkapi urutan kristal bercahaya untuk membuka portal ruang.",
        subtitleEn = "Complete glowing crystal sequences to open the portal.",
        subtitleEs = "Completa las secuencias mágicas de cristal.",
        subtitleFr = "Complète les séquences magiques de cristal.",
        subtitleHi = "जादुई क्रिस्टल अनुक्रम पूरा करें।"
    ),
    NUMBER_BRIDGE(
        id = "number_bridge",
        iconKey = "alt_route",
        accentColorHex = 0xFF26A69A,
        isMath = true,
        titleId = "Jembatan Angka",
        titleEn = "Number Bridge",
        titleEs = "Puente Numérico",
        titleFr = "Pont des Nombres",
        titleHi = "नंबर ब्रिज",
        subtitleId = "Temukan batu loncatan angka yang hilang untuk menyeberang sungai.",
        subtitleEn = "Find missing stepping stones & skip-count across.",
        subtitleEs = "Encuentra los escalones perdidos en el puente.",
        subtitleFr = "Trouve les pierres manquantes sur le pont.",
        subtitleHi = "पुल पार करने के लिए गायब संख्याएँ खोजें।"
    ),
    LOGIC_MATRIX(
        id = "logic_matrix",
        iconKey = "psychology",
        accentColorHex = 0xFFEC407A,
        isMath = false,
        titleId = "Matriks Logika",
        titleEn = "Logic Matrix",
        titleEs = "Matriz Lógica",
        titleFr = "Matrice Logique",
        titleHi = "लॉजिक मैट्रिक्स",
        subtitleId = "Cocokkan petunjuk visual dan pecahkan teka-teki logika ruang.",
        subtitleEn = "Match visual clues and solve the logic matrix grid.",
        subtitleEs = "Une pistas visuales y resuelve el enigma.",
        subtitleFr = "Associe les indices et résous la grille logique.",
        subtitleHi = "सुराग मिलाएं और पहेली सुलझाएं।"
    ),
    SHAPE_GALAXY(
        id = "shape_galaxy",
        iconKey = "category",
        accentColorHex = 0xFF29B6F6,
        isMath = true,
        titleId = "Galaksi Bentuk",
        titleEn = "Shape Galaxy",
        titleEs = "Galaxia Geométrica",
        titleFr = "Galaxie des Formes",
        titleHi = "आकृति गैलेक्सी",
        subtitleId = "Hitung titik sudut bercahaya dan sisi bangun geometri poligon.",
        subtitleEn = "Count glowing vertices and sides on space polygons.",
        subtitleEs = "Cuenta las esquinas brillantes y lados geométricos.",
        subtitleFr = "Compte les sommets lumineux et les côtés géométriques.",
        subtitleHi = "ज्यामितीय आकृतियों के कोने और भुजाएँ गिनें।"
    ),
    MEMORY_TREASURE(
        id = "memory_treasure",
        iconKey = "diamond",
        accentColorHex = 0xFFFFB300,
        isMath = false,
        titleId = "Memori Harta Karun",
        titleEn = "Memory Treasure",
        titleEs = "Tesoro de Memoria",
        titleFr = "Trésor de Mémoire",
        titleHi = "मेमोरी खजाना",
        subtitleId = "Ingat urutan permata pulau dan tebak isi peti rahasia.",
        subtitleEn = "Remember island gem sequences and unlock the secret chest.",
        subtitleEs = "Recuerda las gemas y abre el cofre secreto.",
        subtitleFr = "Mémorise les joyaux et ouvre le coffre secret.",
        subtitleHi = "रत्नों को याद रखें और गुप्त तिजोरी खोलें।"
    ),
    COSMIC_BALANCE(
        id = "cosmic_balance",
        iconKey = "balance",
        accentColorHex = 0xFF5C6BC0,
        isMath = true,
        titleId = "Timbangan Kosmik",
        titleEn = "Cosmic Balance",
        titleEs = "Balanza Cósmica",
        titleFr = "Balance Cosmique",
        titleHi = "कॉस्मिक तराजू",
        subtitleId = "Seimbangkan timbangan bintang dengan jumlah beban yang presisi.",
        subtitleEn = "Balance the star scale with the missing weight.",
        subtitleEs = "Equilibra la balanza estelar con el peso faltante.",
        subtitleFr = "Équilibre la balance stellaire avec le poids manquant.",
        subtitleHi = "तराजू पर सितारों का सटीक संतुलन बनाएं।"
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
        fun fromId(id: String): MiniGameType =
            entries.find { it.id == id } ?: MATH_SAFARI
    }
}

data class BadgeDefinition(
    val id: String,
    val iconKey: String,
    val colorHex: Long,
    val targetMetric: String,
    val targetValue: Long,
    val rewardBonusStars: Long,
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

    fun currentProgress(profile: ChildProfile): Long = when (targetMetric) {
        "math" -> profile.mathScore
        "logic" -> profile.logicScore
        "stars" -> profile.totalStars
        "levels" -> profile.completedLevelsCount
        "streak" -> profile.streakDays
        "galaxy" -> if (profile.unlockedBadges.contains(id)) 1L else 0L
        else -> if (profile.unlockedBadges.contains(id)) 1L else 0L
    }
}

data class AchievementReward(
    val badge: BadgeDefinition,
    val bonusStars: Long,
    val isNewUnlock: Boolean
)

object BadgeCatalog {
    val allBadges: List<BadgeDefinition> = listOf(
        BadgeDefinition(
            id = "curious_buddy",
            iconKey = "star",
            colorHex = 0xFFFFB300,
            targetMetric = "start",
            targetValue = 1L,
            rewardBonusStars = 1L,
            titleId = "Sahabat Penasaran",
            titleEn = "Curious Buddy",
            titleEs = "Amigo Curioso",
            titleFr = "Ami Curieux",
            titleHi = "जिज्ञासु दोस्त",
            descId = "Memulai petualangan belajar di Pulau WonderQuest.",
            descEn = "Started learning journey on WonderQuest Island.",
            descEs = "Inició la aventura en la isla WonderQuest.",
            descFr = "A commencé l'aventure sur l'île WonderQuest.",
            descHi = "वंडरक्वेस्ट द्वीप पर सीखने की यात्रा शुरू की।"
        ),
        BadgeDefinition(
            id = "first_quest",
            iconKey = "explore",
            colorHex = 0xFF26A69A,
            targetMetric = "levels",
            targetValue = 1L,
            rewardBonusStars = 2L,
            titleId = "Penjelajah Pertama",
            titleEn = "First Explorer",
            titleEs = "Primer Explorador",
            titleFr = "Premier Explorateur",
            titleHi = "पहला खोजकर्ता",
            descId = "Selesaikan 1 level tantangan matematika atau logika.",
            descEn = "Complete 1 math or logic challenge level.",
            descEs = "Completa 1 nivel de desafío matemático o lógico.",
            descFr = "Termine 1 niveau de défi mathématique ou logique.",
            descHi = "1 गणित या तर्क चुनौती स्तर पूरा करें।"
        ),
        BadgeDefinition(
            id = "star_collector",
            iconKey = "military_tech",
            colorHex = 0xFFFF7043,
            targetMetric = "stars",
            targetValue = 10L,
            rewardBonusStars = 3L,
            titleId = "Kolektor Bintang Emas",
            titleEn = "Star Collector",
            titleEs = "Coleccionista Estelar",
            titleFr = "Collectionneur d'Étoiles",
            titleHi = "स्टार कलेक्टर",
            descId = "Raih 10 bintang emas dari berbagai permainan.",
            descEn = "Earn 10 golden stars across all learning games.",
            descEs = "Gana 10 estrellas doradas en todos los juegos.",
            descFr = "Gagne 10 étoiles dorées dans les jeux.",
            descHi = "सभी खेलों में 10 सुनहरे सितारे प्राप्त करें।"
        ),
        BadgeDefinition(
            id = "math_whiz",
            iconKey = "calculate",
            colorHex = 0xFF42A5F5,
            targetMetric = "math",
            targetValue = 100L,
            rewardBonusStars = 3L,
            titleId = "Pakar Matematika",
            titleEn = "Math Whiz",
            titleEs = "Genio Matemático",
            titleFr = "As des Maths",
            titleHi = "गणित चैंपियन",
            descId = "Kumpulkan 100 poin di wahana Safari, Jembatan, atau Timbangan.",
            descEn = "Score 100+ points in Math Safari, Bridge, or Balance.",
            descEs = "Alcanza 100+ puntos en retos matemáticos.",
            descFr = "Marque 100+ points dans les défis mathématiques.",
            descHi = "गणित के खेलों में 100+ अंक प्राप्त करें।"
        ),
        BadgeDefinition(
            id = "logic_master",
            iconKey = "psychology",
            colorHex = 0xFF7E57C2,
            targetMetric = "logic",
            targetValue = 100L,
            rewardBonusStars = 3L,
            titleId = "Master Logika",
            titleEn = "Logic Master",
            titleEs = "Maestro Lógico",
            titleFr = "Maître Logique",
            titleHi = "तर्क मास्टर",
            descId = "Kumpulkan 100 poin di Portal Pola, Matriks, atau Memori.",
            descEn = "Score 100+ points in Pattern, Matrix, or Memory.",
            descEs = "Alcanza 100+ puntos en lógica y patrones.",
            descFr = "Marque 100+ points en logique et suites de formes.",
            descHi = "तर्क और पैटर्न खेलों में 100+ अंक प्राप्त करें।"
        ),
        BadgeDefinition(
            id = "super_streak",
            iconKey = "workspace_premium",
            colorHex = 0xFFEC407A,
            targetMetric = "streak",
            targetValue = 1L,
            rewardBonusStars = 2L,
            titleId = "Akurasi Sempurna",
            titleEn = "Perfect Streak",
            titleEs = "Racha Perfecta",
            titleFr = "Série Parfaite",
            titleHi = "सटीक विजय",
            descId = "Jawab semua tantangan dengan benar dalam satu putaran.",
            descEn = "Answer all questions correctly in a single level.",
            descEs = "Responde todos los acertijos correctamente en un nivel.",
            descFr = "Réponds correctement à toutes les questions d'un niveau.",
            descHi = "एक स्तर में सभी प्रश्नों का सही उत्तर दें।"
        ),
        BadgeDefinition(
            id = "galaxy_explorer",
            iconKey = "category",
            colorHex = 0xFF00ACC1,
            targetMetric = "galaxy",
            targetValue = 1L,
            rewardBonusStars = 3L,
            titleId = "Kapten Geometri Galaksi",
            titleEn = "Galaxy Geometry Captain",
            titleEs = "Capitán Geométrico",
            titleFr = "Capitaine Géométrie",
            titleHi = "ज्यामिति कप्तान",
            descId = "Selesaikan misi di Galaksi Bentuk atau Timbangan Kosmik.",
            descEn = "Complete a mission in Shape Galaxy or Cosmic Balance.",
            descEs = "Completa una misión en Galaxia o Balanza.",
            descFr = "Réussis une mission dans Galaxie des Formes ou Balance.",
            descHi = "आकृति गैलेक्सी या कॉस्मिक तराजू का मिशन पूरा करें।"
        ),
        BadgeDefinition(
            id = "island_champion",
            iconKey = "champion",
            colorHex = 0xFF66BB6A,
            targetMetric = "levels",
            targetValue = 5L,
            rewardBonusStars = 5L,
            titleId = "Legenda Pulau",
            titleEn = "Island Legend",
            titleEs = "Campeón de la Isla",
            titleFr = "Champion de l'Île",
            titleHi = "द्वीप चैंपियन",
            descId = "Selesaikan 5 level petualangan di Pulau WonderQuest.",
            descEn = "Complete 5 adventure levels across the island.",
            descEs = "Completa 5 niveles en la isla.",
            descFr = "Termine 5 niveaux sur l'île WonderQuest.",
            descHi = "द्वीप पर 5 स्तर पूरे करें।"
        )
    )

    fun findById(id: String): BadgeDefinition? = allBadges.find { it.id == id }
}
