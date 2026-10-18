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
    val selectedCharacter: String = "sparky",
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
        val validChar = if (selectedCharacter in listOf("sparky", "luna", "robobit", "sunny", "komodo")) selectedCharacter else "sparky"
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
    val emoji: String,
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
        emoji = "🗺️",
        titleId = "Petualangan",
        titleEn = "Adventure",
        titleEs = "Aventura",
        titleFr = "Aventure",
        titleHi = "रोमांच",
        descId = "5 tantangan seru dengan petunjuk visual & bintang!",
        descEn = "5 fun puzzles with visual hints & golden stars!",
        descEs = "¡5 acertijos con pistas visuales y estrellas!",
        descFr = "5 énigmes avec indices visuels et étoiles !",
        descHi = "जादुई संकेतों और सितारों के साथ 5 पहेलियाँ!",
        colorHex = 0xFF4F46E5,
        questionCount = 5,
        timeLimitSeconds = 0,
        bonusStars = 0
    ),
    STAR_BLITZ(
        id = "star_blitz",
        emoji = "⚡",
        titleId = "Kilat Bintang",
        titleEn = "Star Blitz",
        titleEs = "Relámpago",
        titleFr = "Éclair",
        titleHi = "सुपर स्पीड",
        descId = "Tantangan cepat 60 detik dengan bonus bintang ekstra!",
        descEn = "60-second speed challenge with bonus stars!",
        descEs = "¡Reto rápido de 60 segundos con estrellas extra!",
        descFr = "Défi rapide de 60 secondes avec étoiles bonus !",
        descHi = "बोनस सितारों के साथ 60-सेकंड की तेज़ चुनौती!",
        colorHex = 0xFFFF6B35,
        questionCount = 6,
        timeLimitSeconds = 60,
        bonusStars = 2
    ),
    ZEN_PRACTICE(
        id = "zen_practice",
        emoji = "🌈",
        titleId = "Latihan Santai",
        titleEn = "Zen Practice",
        titleEs = "Modo Zen",
        titleFr = "Mode Zen",
        titleHi = "आरामदायक",
        descId = "Belajar santai tanpa batas waktu + ketuk objek untuk menghitung!",
        descEn = "Relaxed learning with tap-to-count visual helpers!",
        descEs = "¡Aprendizaje relajado contando objetos visuales!",
        descFr = "Apprentissage calme avec aide visuelle interactive !",
        descHi = "बिना समय सीमा के विजुअल वस्तुओं को टैप करके सीखें!",
        colorHex = 0xFF10B981,
        questionCount = 4,
        timeLimitSeconds = 0,
        bonusStars = 1
    ),
    DAILY_QUEST(
        id = "daily_quest",
        emoji = "🏆",
        titleId = "Misi Harian",
        titleEn = "Daily Quest",
        titleEs = "Misión Diaria",
        titleFr = "Quête du Jour",
        titleHi = "दैनिक मिशन",
        descId = "Misi spesial hari ini untuk hadiah +3 bintang emas!",
        descEn = "Special daily challenge for +3 bonus golden stars!",
        descEs = "¡Misión especial diaria por +3 estrellas doradas!",
        descFr = "Mission spéciale du jour pour +3 étoiles dorées !",
        descHi = "+3 बोनस सुनहरे सितारों के लिए विशेष दैनिक मिशन!",
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
    val iconEmoji: String,
    val titleId: String,
    val titleEn: String,
    val titleEs: String,
    val titleFr: String,
    val titleHi: String
) {
    AGES_4_6(
        code = "4-6",
        iconEmoji = "🌱",
        titleId = "4–6 Tahun",
        titleEn = "Ages 4–6",
        titleEs = "4–6 Años",
        titleFr = "4–6 Ans",
        titleHi = "4–6 वर्ष"
    ),
    AGES_7_9(
        code = "7-9",
        iconEmoji = "🚀",
        titleId = "7–9 Tahun",
        titleEn = "Ages 7–9",
        titleEs = "7–9 Años",
        titleFr = "7–9 Ans",
        titleHi = "7–9 वर्ष"
    ),
    AGES_10_13(
        code = "10-13",
        iconEmoji = "⚡",
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
    val emoji: String,
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
        emoji = "🦎",
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
        emoji = "🦊",
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
        emoji = "🦉",
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
        emoji = "🤖",
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
        emoji = "🦁",
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
            entries.find { it.id == id } ?: SPARKY
    }
}

enum class MiniGameType(
    val id: String,
    val emoji: String,
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
        emoji = "🦁",
        accentColorHex = 0xFFFF7043,
        isMath = true,
        titleId = "Safari Matematika",
        titleEn = "Math Safari",
        titleEs = "Safari Matemático",
        titleFr = "Safari Maths",
        titleHi = "गणित सफारी",
        subtitleId = "Ketuk hewan untuk menghitung, menjumlah, dan mengalikan!",
        subtitleEn = "Tap animals to count, add, subtract & multiply!",
        subtitleEs = "¡Toca animales para contar, sumar y multiplicar!",
        subtitleFr = "Touche les animaux pour compter et calculer !",
        subtitleHi = "गिनने, जोड़ने और गुणा करने के लिए जानवरों को टैप करें!"
    ),
    PATTERN_PORTAL(
        id = "pattern_portal",
        emoji = "🔮",
        accentColorHex = 0xFF7E57C2,
        isMath = false,
        titleId = "Portal Pola Ajaib",
        titleEn = "Pattern Portal",
        titleEs = "Portal de Patrones",
        titleFr = "Portail des Motifs",
        titleHi = "पैटर्न पोर्टल",
        subtitleId = "Lengkapi urutan kristal bercahaya untuk membuka portal!",
        subtitleEn = "Complete glowing crystal sequences to open the portal!",
        subtitleEs = "¡Completa las secuencias mágicas de cristal!",
        subtitleFr = "Complète les séquences magiques de cristal !",
        subtitleHi = "जादुई क्रिस्टल अनुक्रम पूरा करें!"
    ),
    NUMBER_BRIDGE(
        id = "number_bridge",
        emoji = "🌉",
        accentColorHex = 0xFF26A69A,
        isMath = true,
        titleId = "Jembatan Angka",
        titleEn = "Number Bridge",
        titleEs = "Puente Numérico",
        titleFr = "Pont des Nombres",
        titleHi = "नंबर ब्रिज",
        subtitleId = "Temukan batu loncatan angka yang hilang untuk menyeberang!",
        subtitleEn = "Find missing stepping stones & skip-count across!",
        subtitleEs = "¡Encuentra los escalones perdidos en el puente!",
        subtitleFr = "Trouve les pierres manquantes sur le pont !",
        subtitleHi = "पुल पार करने के लिए गायब संख्याएँ खोजें!"
    ),
    LOGIC_MATRIX(
        id = "logic_matrix",
        emoji = "🧩",
        accentColorHex = 0xFFEC407A,
        isMath = false,
        titleId = "Matriks Logika",
        titleEn = "Logic Matrix",
        titleEs = "Matriz Lógica",
        titleFr = "Matrice Logique",
        titleHi = "लॉजिक मैट्रिक्स",
        subtitleId = "Cocokkan petunjuk visual dan pecahkan teka-teki harta!",
        subtitleEn = "Match visual clues and solve the treasure grid!",
        subtitleEs = "¡Une pistas visuales y resuelve el enigma!",
        subtitleFr = "Associe les indices et résous la grille !",
        subtitleHi = "सुराग मिलाएं और पहेली सुलझाएं!"
    ),
    SHAPE_GALAXY(
        id = "shape_galaxy",
        emoji = "🪐",
        accentColorHex = 0xFF29B6F6,
        isMath = true,
        titleId = "Galaksi Bentuk",
        titleEn = "Shape Galaxy",
        titleEs = "Galaxia Geométrica",
        titleFr = "Galaxie des Formes",
        titleHi = "आकृति गैलेक्सी",
        subtitleId = "Hitung titik sudut bercahaya & sisi bangun geometri luar angkasa!",
        subtitleEn = "Count glowing vertices & sides on animated space polygons!",
        subtitleEs = "¡Cuenta las esquinas brillantes y lados geométricos!",
        subtitleFr = "Compte les sommets lumineux et les côtés géométriques !",
        subtitleHi = "चमकती ज्यामितीय आकृतियों के कोने और भुजाएँ गिनें!"
    ),
    MEMORY_TREASURE(
        id = "memory_treasure",
        emoji = "💎",
        accentColorHex = 0xFFFFB300,
        isMath = false,
        titleId = "Memori Harta Karun",
        titleEn = "Memory Treasure",
        titleEs = "Tesoro de Memoria",
        titleFr = "Trésor de Mémoire",
        titleHi = "मेमोरी खजाना",
        subtitleId = "Ingat urutan permata pulau dan tebak isi peti rahasia!",
        subtitleEn = "Remember the island gems and unlock the secret chest!",
        subtitleEs = "¡Recuerda las gemas y abre el cofre secreto!",
        subtitleFr = "Mémorise les joyaux et ouvre le coffre secret !",
        subtitleHi = "रत्नों को याद रखें और गुप्त तिजोरी खोलें!"
    ),
    COSMIC_BALANCE(
        id = "cosmic_balance",
        emoji = "⚖️",
        accentColorHex = 0xFF5C6BC0,
        isMath = true,
        titleId = "Timbangan Kosmik",
        titleEn = "Cosmic Balance",
        titleEs = "Balanza Cósmica",
        titleFr = "Balance Cosmique",
        titleHi = "कॉस्मिक तराजू",
        subtitleId = "Seimbangkan timbangan bintang dengan jumlah beban yang tepat!",
        subtitleEn = "Balance the animated star scale with the missing weight!",
        subtitleEs = "¡Equilibra la balanza estelar animada!",
        subtitleFr = "Équilibre la balance stellaire animée !",
        subtitleHi = "एनिमेटेड तराजू पर सितारों का संतुलन बनाएं!"
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
    val emoji: String,
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
    val allBadges: List<BadgeDefinition> = listOf(
        BadgeDefinition(
            id = "curious_buddy",
            emoji = "🌟",
            colorHex = 0xFFFFB300,
            titleId = "Sahabat Penasaran",
            titleEn = "Curious Buddy",
            titleEs = "Amigo Curioso",
            titleFr = "Ami Curieux",
            titleHi = "जिज्ञासु दोस्त",
            descId = "Memulai petualangan seru di Pulau WonderQuest!",
            descEn = "Joined WonderQuest Island!",
            descEs = "¡Comenzó la aventura en la isla!",
            descFr = "A rejoint l'île WonderQuest !",
            descHi = "वंडरक्वेस्ट द्वीप की यात्रा शुरू की!"
        ),
        BadgeDefinition(
            id = "first_quest",
            emoji = "🗺️",
            colorHex = 0xFF26A69A,
            titleId = "Penjelajah Pertama",
            titleEn = "First Explorer",
            titleEs = "Primer Explorador",
            titleFr = "Premier Explorateur",
            titleHi = "पहला खोजकर्ता",
            descId = "Menyelesaikan tantangan belajar pertamamu!",
            descEn = "Completed your very first learning quest!",
            descEs = "¡Completaste tu primera misión!",
            descFr = "Première quête terminée !",
            descHi = "पहला मिशन पूरा किया!"
        ),
        BadgeDefinition(
            id = "star_collector",
            emoji = "⭐",
            colorHex = 0xFFFF7043,
            titleId = "Kolektor Bintang",
            titleEn = "Star Collector",
            titleEs = "Coleccionista Estelar",
            titleFr = "Collectionneur d'Étoiles",
            titleHi = "स्टार कलेक्टर",
            descId = "Mengumpulkan 10 bintang emas berkilau!",
            descEn = "Collected 10+ golden stars!",
            descEs = "¡Coleccionaste 10 estrellas!",
            descFr = "10 étoiles dorées gagnées !",
            descHi = "10 सुनहरे सितारे जमा किए!"
        ),
        BadgeDefinition(
            id = "math_whiz",
            emoji = "🔢",
            colorHex = 0xFF42A5F5,
            titleId = "Juara Matematika",
            titleEn = "Math Whiz",
            titleEs = "Genio Matemático",
            titleFr = "As des Maths",
            titleHi = "गणित चैंपियन",
            descId = "Meraih 100+ poin di petualangan Matematika!",
            descEn = "Reached 100+ Math points!",
            descEs = "¡Alcanzaste 100 puntos en Matemáticas!",
            descFr = "100 points en Maths !",
            descHi = "गणित में 100+ अंक प्राप्त किए!"
        ),
        BadgeDefinition(
            id = "logic_master",
            emoji = "🧠",
            colorHex = 0xFF7E57C2,
            titleId = "Ahli Logika",
            titleEn = "Logic Master",
            titleEs = "Maestro Lógico",
            titleFr = "Maître Logique",
            titleHi = "तर्क मास्टर",
            descId = "Meraih 100+ poin di Portal Pola & Matriks Logika!",
            descEn = "Reached 100+ Logic points!",
            descEs = "¡Alcanzaste 100 puntos en Lógica!",
            descFr = "100 points en Logique !",
            descHi = "तर्क में 100+ अंक प्राप्त किए!"
        ),
        BadgeDefinition(
            id = "super_streak",
            emoji = "🔥",
            colorHex = 0xFFEC407A,
            titleId = "Sempurna 100%",
            titleEn = "Super Streak",
            titleEs = "Súper Racha",
            titleFr = "Super Série",
            titleHi = "सुपर स्ट्रीक",
            descId = "Menjawab semua pertanyaan dengan benar dalam satu level!",
            descEn = "Earned a perfect score in a mini-game!",
            descEs = "¡Puntaje perfecto en una misión!",
            descFr = "Score parfait dans un jeu !",
            descHi = "एक खेल में पूरे अंक पाए!"
        ),
        BadgeDefinition(
            id = "galaxy_explorer",
            emoji = "🛸",
            colorHex = 0xFF00ACC1,
            titleId = "Kapten Galaksi",
            titleEn = "Galaxy Captain",
            titleEs = "Capitán Galáctico",
            titleFr = "Capitaine Galactique",
            titleHi = "गैलेक्सी कैप्टन",
            descId = "Menaklukkan tantangan Galaksi Bentuk atau Timbangan Kosmik!",
            descEn = "Conquered Shape Galaxy or Cosmic Balance!",
            descEs = "¡Conquistaste Galaxia Geométrica o Balanza Cósmica!",
            descFr = "A conquis Galaxie des Formes ou Balance Cosmique !",
            descHi = "आकृति गैलेक्सी या कॉस्मिक तराजू जीता!"
        ),
        BadgeDefinition(
            id = "island_champion",
            emoji = "👑",
            colorHex = 0xFF66BB6A,
            titleId = "Legenda Pulau",
            titleEn = "Island Legend",
            titleEs = "Campeón de la Isla",
            titleFr = "Champion de l'Île",
            titleHi = "द्वीप चैंपियन",
            descId = "Menyelesaikan 5 level petualangan di Pulau WonderQuest!",
            descEn = "Completed 5+ levels across the island!",
            descEs = "¡Completaste 5 niveles en la isla!",
            descFr = "5 niveaux terminés sur l'île !",
            descHi = "द्वीप पर 5 लेवल पूरे किए!"
        )
    )
}
