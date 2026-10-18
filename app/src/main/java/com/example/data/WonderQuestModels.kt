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
    val childName: String = "Explorer",
    val ageGroup: String = "7-9",
    val selectedCharacter: String = "sparky",
    val languageCode: String = "en",
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
        return mapOf(
            "userId" to ownerUid,
            "childName" to childName.trim().take(40).ifEmpty { "Explorer" },
            "ageGroup" to ageGroup,
            "selectedCharacter" to selectedCharacter,
            "languageCode" to languageCode,
            "darkMode" to darkMode,
            "parentPin" to parentPin.filter { it.isDigit() }.padEnd(4, '0').take(4),
            "totalStars" to totalStars,
            "completedLevelsCount" to completedLevelsCount,
            "mathScore" to mathScore,
            "logicScore" to logicScore,
            "streakDays" to streakDays,
            "unlockedBadges" to unlockedBadges.take(20),
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
        return mapOf(
            "userId" to ownerUid,
            "gameType" to gameType,
            "ageGroup" to ageGroup,
            "levelNumber" to levelNumber.coerceIn(1L, 50L),
            "starsEarned" to starsEarned.coerceIn(1L, 3L),
            "correctAnswers" to correctAnswers.coerceIn(0L, 20L),
            "totalQuestions" to totalQuestions.coerceIn(1L, 20L),
            "durationSeconds" to durationSeconds.coerceIn(1L, 3600L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

enum class AgeBracket(val code: String, val labelEn: String, val labelEs: String, val labelFr: String, val labelHi: String) {
    EARLY("4-6", "Ages 4–6 • Early Sprouts", "4–6 años • Semillitas", "4–6 ans • Petits Bourgeons", "4–6 वर्ष • नन्हे अंकुर"),
    MIDDLE("7-9", "Ages 7–9 • Junior Rangers", "7–9 años • Exploradores", "7–9 ans • Jeunes Rangers", "7–9 वर्ष • जूनियर रेंजर्स"),
    OLDER("10-13", "Ages 10–13 • Master Wizards", "10–13 años • Magos Maestros", "10–13 ans • Maîtres Mages", "10–13 वर्ष • मास्टर जादूगर");

    fun label(lang: String): String = when (lang) {
        "es" -> labelEs
        "fr" -> labelFr
        "hi" -> labelHi
        else -> labelEn
    }

    companion object {
        fun fromCode(code: String): AgeBracket = entries.find { it.code == code } ?: MIDDLE
    }
}

enum class BuddyCharacter(
    val id: String,
    val nameEn: String,
    val nameEs: String,
    val nameFr: String,
    val nameHi: String,
    val roleEn: String,
    val roleEs: String,
    val roleFr: String,
    val roleHi: String,
    val cheerEn: String,
    val cheerEs: String,
    val cheerFr: String,
    val cheerHi: String,
    val primaryColorHex: Long,
    val accentColorHex: Long
) {
    SPARKY(
        id = "sparky",
        nameEn = "Sparky the Fox",
        nameEs = "Zorrito Sparky",
        nameFr = "Sparky le Renard",
        nameHi = "स्पार्की लोमड़ी",
        roleEn = "Number Trailblazer",
        roleEs = "Explorador de Números",
        roleFr = "Éclaireur des Nombres",
        roleHi = "संख्या खोजकर्ता",
        cheerEn = "Yip-yip! Tap me to jump for joy! Let's solve puzzles together!",
        cheerEs = "¡Yip-yip! ¡Tócame para saltar de alegría! ¡Resolvamos acertijos!",
        cheerFr = "Youpi ! Touche-moi pour sauter de joie ! Résolvons des énigmes !",
        cheerHi = "यिप-यिप! खुशी से उछलने के लिए मुझे टैप करें! चलो पहेलियाँ सुलझाएं!",
        primaryColorHex = 0xFFFF7A30,
        accentColorHex = 0xFF00B4D8
    ),
    LUNA(
        id = "luna",
        nameEn = "Luna the Owl",
        nameEs = "Búho Luna",
        nameFr = "Luna la Chouette",
        nameHi = "लूना उल्लू",
        roleEn = "Pattern Alchemist",
        roleEs = "Alquimista de Patrones",
        roleFr = "Alchimiste des Motifs",
        roleHi = "पैटर्न जादूगरनी",
        cheerEn = "Hoo-ray! My magic feathers glow when you spot the secret pattern!",
        cheerEs = "¡Hurra! ¡Mis plumas mágicas brillan cuando descubres el patrón!",
        cheerFr = "Hourra ! Mes plumes magiques brillent quand tu trouves le motif !",
        cheerHi = "हुर्रे! जब आप गुप्त पैटर्न ढूंढते हैं तो मेरे जादुई पंख चमकते हैं!",
        primaryColorHex = 0xFF8B5CF6,
        accentColorHex = 0xFF34D399
    ),
    ROBOBIT(
        id = "robobit",
        nameEn = "Robo-Bit",
        nameEs = "Robo-Bit",
        nameFr = "Robo-Bit",
        nameHi = "रोबो-बिट",
        roleEn = "Logic Circuit Buddy",
        roleEs = "Amigo de Circuitos Lógicos",
        roleFr = "Copain des Circuits Logiques",
        roleHi = "लॉजिक रोबोट मित्र",
        cheerEn = "BEEP-BOOP! Brain circuits charged at 100%! Ready for a logic quest?",
        cheerEs = "¡BIP-BUP! ¡Circuitos al 100%! ¿Listo para una misión lógica?",
        cheerFr = "BIP-BOOP ! Circuits chargés à 100 % ! Prêt pour un défi logique ?",
        cheerHi = "बीप-बूप! दिमागी सर्किट 100% चार्ज! क्या आप लॉजिक मिशन के लिए तैयार हैं?",
        primaryColorHex = 0xFF0EA5E9,
        accentColorHex = 0xFFFBBF24
    ),
    SUNNY(
        id = "sunny",
        nameEn = "Sunny Star",
        nameEs = "Estrella Sunny",
        nameFr = "Étoile Sunny",
        nameHi = "सनी तारा",
        roleEn = "Cosmic Cheer Captain",
        roleEs = "Capitán de Alegría Cósmica",
        roleFr = "Capitaine de Joie Cosmique",
        roleHi = "चमकता तारा कप्तान",
        cheerEn = "Twinkle-twinkle! Every mistake is just a stepping stone to a star!",
        cheerEs = "¡Brilla-brilla! ¡Cada intento te acerca más a una nueva estrella!",
        cheerFr = "Scintille ! Chaque essai te rapproche d'une nouvelle étoile !",
        cheerHi = "ट्विंकल-ट्विंकल! हर कोशिश आपको एक नए सितारे के करीब ले जाती है!",
        primaryColorHex = 0xFFF59E0B,
        accentColorHex = 0xFFEC4899
    );

    fun displayName(lang: String): String = when (lang) {
        "es" -> nameEs
        "fr" -> nameFr
        "hi" -> nameHi
        else -> nameEn
    }

    fun role(lang: String): String = when (lang) {
        "es" -> roleEs
        "fr" -> roleFr
        "hi" -> roleHi
        else -> roleEn
    }

    fun cheer(lang: String): String = when (lang) {
        "es" -> cheerEs
        "fr" -> cheerFr
        "hi" -> cheerHi
        else -> cheerEn
    }

    companion object {
        fun fromId(id: String): BuddyCharacter = entries.find { it.id == id } ?: SPARKY
    }
}

enum class MiniGameType(
    val id: String,
    val isMath: Boolean,
    val titleEn: String,
    val titleEs: String,
    val titleFr: String,
    val titleHi: String,
    val descEn: String,
    val descEs: String,
    val descFr: String,
    val descHi: String,
    val colorHex: Long
) {
    MATH_SAFARI(
        id = "math_safari",
        isMath = true,
        titleEn = "Math Safari",
        titleEs = "Safari Matemático",
        titleFr = "Safari Mathématique",
        titleHi = "गणित सफारी",
        descEn = "Count, add, subtract & multiply across enchanted islands!",
        descEs = "¡Cuenta, suma, resta y multiplica en islas encantadas!",
        descFr = "Compte, additionne, soustrais et multiplie sur les îles !",
        descHi = "जादुई द्वीपों पर गिनें, जोड़ें, घटाएं और गुणा करें!",
        colorHex = 0xFF10B981
    ),
    PATTERN_PORTAL(
        id = "pattern_portal",
        isMath = false,
        titleEn = "Pattern Portal",
        titleEs = "Portal de Patrones",
        titleFr = "Portail des Motifs",
        titleHi = "पैटर्न पोर्टल",
        descEn = "Unlock glowing sequences of shapes, colors & symbols!",
        descEs = "¡Descubre secuencias mágicas de formas, colores y símbolos!",
        descFr = "Complète les suites magiques de formes et de couleurs !",
        descHi = "आकृतियों, रंगों और प्रतीकों के जादुई क्रम को पूरा करें!",
        colorHex = 0xFF8B5CF6
    ),
    NUMBER_BRIDGE(
        id = "number_bridge",
        isMath = true,
        titleEn = "Missing Bridge",
        titleEs = "Puente Numérico",
        titleFr = "Pont des Nombres",
        titleHi = "संख्या पुल",
        descEn = "Find the missing stone to balance the equation bridge!",
        descEs = "¡Encuentra la piedra que falta para cruzar el puente!",
        descFr = "Trouve la pierre manquante pour équilibrer le pont !",
        descHi = "समीकरण पुल को पार करने के लिए सही संख्या ढूंढें!",
        colorHex = 0xFFF59E0B
    ),
    LOGIC_MATRIX(
        id = "logic_matrix",
        isMath = false,
        titleEn = "Odd-One & Riddles",
        titleEs = "Acertijos Lógicos",
        titleFr = "Énigmes & Intrus",
        titleHi = "तर्क पहेलियाँ",
        descEn = "Spot the rule-breaker and solve clever brain teasers!",
        descEs = "¡Descubre al intruso y resuelve acertijos ingeniosos!",
        descFr = "Trouve l'intrus et résous des casse-têtes malins !",
        descHi = "अलग विकल्प पहचानें और मजेदार दिमागी पहेलियाँ सुलझाएं!",
        colorHex = 0xFFEC4899
    );

    fun title(lang: String): String = when (lang) {
        "es" -> titleEs
        "fr" -> titleFr
        "hi" -> titleHi
        else -> titleEn
    }

    fun description(lang: String): String = when (lang) {
        "es" -> descEs
        "fr" -> descFr
        "hi" -> descHi
        else -> descEn
    }

    companion object {
        fun fromId(id: String): MiniGameType = entries.find { it.id == id } ?: MATH_SAFARI
    }
}

data class RewardBadge(
    val id: String,
    val iconEmoji: String,
    val titleEn: String,
    val titleEs: String,
    val titleFr: String,
    val titleHi: String,
    val descEn: String,
    val descEs: String,
    val descFr: String,
    val descHi: String,
    val colorHex: Long
) {
    fun title(lang: String): String = when (lang) {
        "es" -> titleEs
        "fr" -> titleFr
        "hi" -> titleHi
        else -> titleEn
    }

    fun description(lang: String): String = when (lang) {
        "es" -> descEs
        "fr" -> descFr
        "hi" -> descHi
        else -> descEn
    }
}

val ALL_REWARD_BADGES = listOf(
    RewardBadge(
        id = "curious_buddy",
        iconEmoji = "🌟",
        titleEn = "Curious Buddy",
        titleEs = "Amigo Curioso",
        titleFr = "Ami Curieux",
        titleHi = "जिज्ञासु मित्र",
        descEn = "Joined WonderQuest & met your animated learning buddy!",
        descEs = "¡Te uniste a WonderQuest y conociste a tu amigo animado!",
        descFr = "Tu as rejoint WonderQuest et rencontré ton compagnon !",
        descHi = "वंडरक्वेस्ट में शामिल हुए और अपने दोस्त से मिले!",
        colorHex = 0xFFF59E0B
    ),
    RewardBadge(
        id = "first_star",
        iconEmoji = "🚀",
        titleEn = "First Quest Complete",
        titleEs = "Primera Misión",
        titleFr = "Première Quête",
        titleHi = "पहली जीत",
        descEn = "Completed your very first educational mini-game level!",
        descEs = "¡Completaste tu primer nivel educativo!",
        descFr = "Tu as terminé ton tout premier niveau éducatif !",
        descHi = "अपना पहला शैक्षिक मिनी-गेम स्तर पूरा किया!",
        colorHex = 0xFF10B981
    ),
    RewardBadge(
        id = "perfect_three",
        iconEmoji = "👑",
        titleEn = "Triple Star Ace",
        titleEs = "As de Tres Estrellas",
        titleFr = "Champion 3 Étoiles",
        titleHi = "तीन सितारा चैंपियन",
        descEn = "Earned a perfect 3-star rating on any mini-game level!",
        descEs = "¡Obtuviste 3 estrellas perfectas en un nivel!",
        descFr = "Tu as obtenu 3 étoiles parfaites dans un niveau !",
        descHi = "किसी भी स्तर पर पूरे 3 सितारे प्राप्त किए!",
        colorHex = 0xFFEC4899
    ),
    RewardBadge(
        id = "math_whiz",
        iconEmoji = "🧮",
        titleEn = "Math Safari Whiz",
        titleEs = "Genio Matemático",
        titleFr = "Prodige des Maths",
        titleHi = "गणित उस्ताद",
        descEn = "Scored 100+ points in Math Safari & Missing Bridge!",
        descEs = "¡Obtuviste más de 100 puntos en juegos de matemáticas!",
        descFr = "Plus de 100 points gagnés dans les défis mathématiques !",
        descHi = "गणित के खेलों में 100+ अंक हासिल किए!",
        colorHex = 0xFF0EA5E9
    ),
    RewardBadge(
        id = "logic_master",
        iconEmoji = "🔮",
        titleEn = "Logic Crystal Master",
        titleEs = "Maestro de la Lógica",
        titleFr = "Maître de la Logique",
        titleHi = "तर्क सम्राट",
        descEn = "Scored 100+ points in Pattern Portal & Logic Riddles!",
        descEs = "¡Más de 100 puntos en patrones y acertijos lógicos!",
        descFr = "Plus de 100 points en logique et suites magiques !",
        descHi = "पैटर्न और तर्क पहेलियों में 100+ अंक प्राप्त किए!",
        colorHex = 0xFF8B5CF6
    ),
    RewardBadge(
        id = "island_champion",
        iconEmoji = "🏆",
        titleEn = "Island Champion",
        titleEs = "Campeón de la Isla",
        titleFr = "Champion de l'Île",
        titleHi = "द्वीप चैंपियन",
        descEn = "Collected 15 or more golden stars across levels!",
        descEs = "¡Coleccionaste 15 o más estrellas doradas!",
        descFr = "Tu as collectionné 15 étoiles dorées ou plus !",
        descHi = "सभी स्तरों में 15 या अधिक सुनहरे सितारे एकत्र किए!",
        colorHex = 0xFFEAB308
    ),
    RewardBadge(
        id = "speed_solver",
        iconEmoji = "⚡",
        titleEn = "Lightning Thinker",
        titleEs = "Pensador Relámpago",
        titleFr = "Penseur Éclair",
        titleHi = "तेज़ दिमाग",
        descEn = "Finished a level in under 25 seconds with 3 stars!",
        descEs = "¡Nivel completado en menos de 25 segundos con 3 estrellas!",
        descFr = "Niveau terminé en moins de 25 secondes avec 3 étoiles !",
        descHi = "25 सेकंड से कम समय में 3 सितारों के साथ स्तर पूरा किया!",
        colorHex = 0xFF06B6D4
    ),
    RewardBadge(
        id = "polyglot",
        iconEmoji = "🌍",
        titleEn = "Global Classroom Explorer",
        titleEs = "Explorador Multilingüe",
        titleFr = "Explorateur Polyglotte",
        titleHi = "बहुभाषी खोजकर्ता",
        descEn = "Explored learning in multiple classroom languages!",
        descEs = "¡Exploraste el aprendizaje en varios idiomas!",
        descFr = "Tu as exploré l'apprentissage en plusieurs langues !",
        descHi = "कई भाषाओं में सीखने का अनुभव किया!",
        colorHex = 0xFF14B8A6
    )
)
