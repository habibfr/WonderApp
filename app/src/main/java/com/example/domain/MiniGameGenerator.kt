package com.example.domain

import com.example.data.AgeGroupConfig
import com.example.data.GamePlayMode
import com.example.data.MiniGameType
import kotlin.random.Random

enum class VisualSceneKind {
    STANDARD,
    SAFARI_COUNT,
    PATTERN_PORTAL,
    NUMBER_BRIDGE,
    LOGIC_MATRIX,
    MEMORY_DECK,
    POLYGON_GALAXY,
    BALANCE_BEAM
}

data class MiniGameQuestion(
    val index: Int,
    val total: Int,
    val prompt: String,
    val visualEquationOrPattern: String,
    val visualTokens: List<String>,
    val options: List<String>,
    val optionIcons: List<String> = emptyList(),
    val correctIndex: Int,
    val hintText: String,
    val sceneKind: VisualSceneKind = VisualSceneKind.STANDARD,
    val polygonSides: Int = 3,
    val balanceLeftWeight: Int = 5,
    val balanceRightKnownWeight: Int = 2
)

object MiniGameGenerator {

    fun generateQuestions(
        gameType: MiniGameType,
        ageGroup: AgeGroupConfig,
        levelNumber: Int,
        languageCode: String,
        playMode: GamePlayMode = GamePlayMode.ADVENTURE
    ): List<MiniGameQuestion> {
        val total = playMode.questionCount.coerceAtLeast(3)
        val seed = (gameType.id.hashCode() * 31) + (ageGroup.code.hashCode() * 17) + (levelNumber * 101)
        val rng = Random(seed)

        return (1..total).map { idx ->
            when (gameType) {
                MiniGameType.MATH_SAFARI -> buildMathSafari(idx, total, ageGroup, levelNumber, languageCode, rng)
                MiniGameType.PATTERN_PORTAL -> buildPatternPortal(idx, total, ageGroup, levelNumber, languageCode, rng)
                MiniGameType.NUMBER_BRIDGE -> buildNumberBridge(idx, total, ageGroup, levelNumber, languageCode, rng)
                MiniGameType.LOGIC_MATRIX -> buildLogicMatrix(idx, total, ageGroup, levelNumber, languageCode, rng)
                MiniGameType.SHAPE_GALAXY -> buildShapeGalaxy(idx, total, ageGroup, levelNumber, languageCode, rng)
                MiniGameType.MEMORY_TREASURE -> buildMemoryTreasure(idx, total, ageGroup, levelNumber, languageCode, rng)
                MiniGameType.COSMIC_BALANCE -> buildCosmicBalance(idx, total, ageGroup, levelNumber, languageCode, rng)
            }
        }
    }

    private fun buildMathSafari(
        index: Int,
        total: Int,
        ageGroup: AgeGroupConfig,
        levelNumber: Int,
        lang: String,
        rng: Random
    ): MiniGameQuestion {
        val visualItems = listOf("ITEM_APPLE", "ITEM_STAR", "ITEM_BUTTERFLY", "ITEM_CRYSTAL")
        val itemToken = visualItems[(index + levelNumber) % visualItems.size]

        return when (ageGroup) {
            AgeGroupConfig.EARLY -> {
                val a = rng.nextInt(1, 5 + (levelNumber % 3))
                val b = rng.nextInt(1, 5)
                val sum = a + b
                val opts = buildNumericOptions(sum, rng, minVal = 1)
                val tokens = List(a) { itemToken } + listOf("+") + List(b) { itemToken }
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = when (lang) {
                        "en" -> "Count and add the shapes! How many altogether? ($a + $b)"
                        "es" -> "¡Cuenta y suma las figuras! ¿Cuántas hay en total? ($a + $b)"
                        "fr" -> "Compte et additionne les formes ! Combien au total ? ($a + $b)"
                        "hi" -> "आकृतियों को गिनें और जोड़ें! कुल कितने हैं? ($a + $b)"
                        else -> "Hitung dan jumlahkan gambar di bawah! Berapa semuanya? ($a + $b)"
                    },
                    visualEquationOrPattern = "$a + $b = ?",
                    visualTokens = tokens,
                    options = opts,
                    correctIndex = opts.indexOf(sum.toString()).coerceAtLeast(0),
                    hintText = when (lang) {
                        "en" -> "Tap each icon below to count from 1 up to $sum!"
                        "es" -> "Toca cada figura para contar desde $a y sumar $b más."
                        "fr" -> "Touche chaque icône pour compter jusqu'à $sum."
                        "hi" -> "एक-एक करके गिनने के लिए नीचे की आकृतियों को टैप करें!"
                        else -> "Ketuk setiap gambar satu per satu mulai dari $a lalu tambah $b lagi!"
                    },
                    sceneKind = VisualSceneKind.SAFARI_COUNT
                )
            }
            AgeGroupConfig.JUNIOR -> {
                val isMultiply = index % 2 == 0
                if (isMultiply) {
                    val a = rng.nextInt(2, 6 + (levelNumber % 4))
                    val b = rng.nextInt(2, 6)
                    val ans = a * b
                    val opts = buildNumericOptions(ans, rng, minVal = 2)
                    val tokens = List((a * b).coerceAtMost(12)) { itemToken }
                    MiniGameQuestion(
                        index = index,
                        total = total,
                        prompt = when (lang) {
                            "en" -> "$a magic groups with $b crystals each. How many in total?"
                            "es" -> "$a grupos mágicos con $b cristales cada uno. ¿Cuántos en total?"
                            "fr" -> "$a groupes magiques de $b cristaux chacun. Combien au total ?"
                            "hi" -> "$a समूह, प्रत्येक में $b रत्न। कुल कितने हैं?"
                            else -> "Ada $a kelompok kristal dengan masing-masing $b kristal. Berapa totalnya?"
                        },
                        visualEquationOrPattern = "$a × $b = ?",
                        visualTokens = tokens,
                        options = opts,
                        correctIndex = opts.indexOf(ans.toString()).coerceAtLeast(0),
                        hintText = when (lang) {
                            "en" -> "Think of $a × $b as adding $b together $a times!"
                            "es" -> "Suma $b un total de $a veces ($a × $b)."
                            "fr" -> "Additionne $b exactement $a fois ($a × $b)."
                            "hi" -> "$b को $a बार जोड़ें ($a × $b)।"
                            else -> "Ingat: $a × $b sama dengan menjumlahkan angka $b sebanyak $a kali!"
                        },
                        sceneKind = VisualSceneKind.SAFARI_COUNT
                    )
                } else {
                    val a = rng.nextInt(12, 26 + levelNumber)
                    val b = rng.nextInt(4, 11)
                    val ans = a - b
                    val opts = buildNumericOptions(ans, rng, minVal = 1)
                    MiniGameQuestion(
                        index = index,
                        total = total,
                        prompt = when (lang) {
                            "en" -> "Solve the island subtraction quest: $a - $b = ?"
                            "es" -> "Resuelve la resta de la isla: $a - $b = ?"
                            "fr" -> "Résous la soustraction de l'île : $a - $b = ?"
                            "hi" -> "द्वीप का घटाव हल करें: $a - $b = ?"
                            else -> "Pecahkan pengurangan pulau: $a - $b = ?"
                        },
                        visualEquationOrPattern = "$a - $b = ?",
                        visualTokens = List(8) { itemToken },
                        options = opts,
                        correctIndex = opts.indexOf(ans.toString()).coerceAtLeast(0),
                        hintText = when (lang) {
                            "en" -> "Count backward $b steps starting from $a!"
                            "es" -> "Cuenta hacia atrás $b pasos desde $a."
                            "fr" -> "Recule de $b à partir de $a."
                            "hi" -> "$a में से $b कदम पीछे गिनें।"
                            else -> "Hitung mundur sebanyak $b langkah dari angka $a!"
                        },
                        sceneKind = VisualSceneKind.SAFARI_COUNT
                    )
                }
            }
            AgeGroupConfig.SENIOR -> {
                val a = rng.nextInt(3, 9)
                val b = rng.nextInt(3, 8)
                val c = rng.nextInt(4, 15)
                val ans = (a * b) + c
                val opts = buildNumericOptions(ans, rng, minVal = 5)
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = when (lang) {
                        "en" -> "Master Wizard Expression: ($a × $b) + $c = ?"
                        "es" -> "Orden de operaciones: ($a × $b) + $c = ?"
                        "fr" -> "Priorité des opérations : ($a × $b) + $c = ?"
                        "hi" -> "संक्रियाओं का क्रम: ($a × $b) + $c = ?"
                        else -> "Tantangan Master Operasi Hitung: ($a × $b) + $c = ?"
                    },
                    visualEquationOrPattern = "($a × $b) + $c = ?",
                    visualTokens = listOf("ITEM_STAR", "ITEM_CRYSTAL", "ITEM_STAR", "ITEM_CRYSTAL"),
                    options = opts,
                    correctIndex = opts.indexOf(ans.toString()).coerceAtLeast(0),
                    hintText = when (lang) {
                        "en" -> "First multiply $a × $b = ${a * b}, then add $c!"
                        "es" -> "Multiplica primero $a × $b = ${a * b}, luego suma $c."
                        "fr" -> "Calcule d'abord $a × $b = ${a * b}, puis ajoute $c."
                        "hi" -> "पहले गुणा करें $a × $b = ${a * b}, फिर $c जोड़ें।"
                        else -> "Kalikan dulu $a × $b = ${a * b}, lalu tambahkan $c!"
                    },
                    sceneKind = VisualSceneKind.SAFARI_COUNT
                )
            }
        }
    }

    private fun buildPatternPortal(
        index: Int,
        total: Int,
        ageGroup: AgeGroupConfig,
        levelNumber: Int,
        lang: String,
        rng: Random
    ): MiniGameQuestion {
        return when (ageGroup) {
            AgeGroupConfig.EARLY -> {
                val pairs = listOf(
                    "SHAPE_RED_CIRCLE" to "SHAPE_BLUE_SQUARE",
                    "SHAPE_GOLD_STAR" to "SHAPE_GREEN_TRIANGLE",
                    "SHAPE_PURPLE_DIAMOND" to "SHAPE_GOLD_STAR",
                    "ITEM_SUN" to "ITEM_MOON"
                )
                val (first, second) = pairs[(index + levelNumber) % pairs.size]
                val seq = listOf(first, second, first, second, first, "?")
                val allShapes = listOf(
                    "SHAPE_RED_CIRCLE",
                    "SHAPE_BLUE_SQUARE",
                    "SHAPE_GOLD_STAR",
                    "SHAPE_GREEN_TRIANGLE",
                    "SHAPE_PURPLE_DIAMOND",
                    "ITEM_SUN",
                    "ITEM_MOON"
                )
                val distractors = allShapes.filter { it != second }.shuffled(rng).take(3)
                val rawOpts = (listOf(second) + distractors).shuffled(rng)
                val displayOpts = rawOpts.map { tokenLabel(it, lang) }
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = when (lang) {
                        "en" -> "Which shape comes next in the glowing Pattern Portal?"
                        "es" -> "¿Qué figura geométrica sigue en el portal mágico?"
                        "fr" -> "Quelle forme géométrique vient ensuite dans le portail ?"
                        "hi" -> "जादुई पैटर्न में आगे कौन सी आकृति आएगी?"
                        else -> "Bentuk apa yang muncul selanjutnya di Portal Pola Ajaib?"
                    },
                    visualEquationOrPattern = "${tokenLabel(first, lang)} • ${tokenLabel(second, lang)} • ${tokenLabel(first, lang)} • ?",
                    visualTokens = seq,
                    options = displayOpts,
                    optionIcons = rawOpts,
                    correctIndex = rawOpts.indexOf(second).coerceAtLeast(0),
                    hintText = when (lang) {
                        "en" -> "Look at the alternating pattern: first shape, second shape, first shape..."
                        "es" -> "Observa cómo las dos figuras se turnan una y otra vez."
                        "fr" -> "Regarde comment les deux formes alternent l'une après l'autre."
                        "hi" -> "देखें कि दोनों आकृतियाँ बारी-बारी से आ रही हैं।"
                        else -> "Perhatikan polanya bergantian: bentuk pertama, bentuk kedua, bentuk pertama..."
                    },
                    sceneKind = VisualSceneKind.PATTERN_PORTAL
                )
            }
            AgeGroupConfig.JUNIOR -> {
                val step = rng.nextInt(2, 6)
                val start = rng.nextInt(2, 10)
                val seq = (0..3).map { (start + it * step).toString() } + listOf("?")
                val correct = start + 4 * step
                val opts = buildNumericOptions(correct, rng, minVal = 1)
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = when (lang) {
                        "en" -> "Find the next number to unlock the Pattern Portal:"
                        "es" -> "Completa la secuencia numérica del portal:"
                        "fr" -> "Complète la suite numérique du portail :"
                        "hi" -> "पोर्टल की संख्या श्रृंखला पूरी करें:"
                        else -> "Temukan angka berikutnya untuk membuka Portal Pola:"
                    },
                    visualEquationOrPattern = seq.joinToString("  •  "),
                    visualTokens = seq,
                    options = opts,
                    correctIndex = opts.indexOf(correct.toString()).coerceAtLeast(0),
                    hintText = when (lang) {
                        "en" -> "Each stone increases by +$step! Add $step to ${start + 3 * step}."
                        "es" -> "Cada paso suma +$step al número anterior."
                        "fr" -> "On ajoute +$step à chaque étape."
                        "hi" -> "हर अगली संख्या में +$step जोड़ा गया है।"
                        else -> "Setiap langkah bertambah +$step! Tambahkan $step ke ${start + 3 * step}."
                    },
                    sceneKind = VisualSceneKind.PATTERN_PORTAL
                )
            }
            AgeGroupConfig.SENIOR -> {
                val base = rng.nextInt(2, 8)
                val n1 = base
                val n2 = n1 + 2
                val n3 = n2 + 4
                val n4 = n3 + 6
                val correct = n4 + 8
                val seq = listOf("$n1", "$n2", "$n3", "$n4", "?")
                val opts = buildNumericOptions(correct, rng, minVal = 5)
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = when (lang) {
                        "en" -> "Growing-Step Sequence: What number completes the portal?"
                        "es" -> "Patrón de diferencias crecientes: ¿qué número sigue?"
                        "fr" -> "Suite à écarts croissants : quel nombre suit ?"
                        "hi" -> "बढ़ते अंतर का पैटर्न: अगली संख्या क्या होगी?"
                        else -> "Pola Lompatan Bertambah: Angka berapa yang melengkapi portal?"
                    },
                    visualEquationOrPattern = seq.joinToString("  •  "),
                    visualTokens = seq,
                    options = opts,
                    correctIndex = opts.indexOf(correct.toString()).coerceAtLeast(0),
                    hintText = when (lang) {
                        "en" -> "Look at the gaps: +2, +4, +6... so the next jump is +8!"
                        "es" -> "Los saltos crecen: +2, +4, +6... ¡el siguiente salto es +8!"
                        "fr" -> "Les écarts augmentent : +2, +4, +6... le prochain est +8 !"
                        "hi" -> "अंतर बढ़ रहा है: +2, +4, +6... अगला अंतर +8 होगा!"
                        else -> "Lihat selisihnya: +2, +4, +6... jadi lompatan berikutnya adalah +8!"
                    },
                    sceneKind = VisualSceneKind.PATTERN_PORTAL
                )
            }
        }
    }

    private fun buildNumberBridge(
        index: Int,
        total: Int,
        ageGroup: AgeGroupConfig,
        levelNumber: Int,
        lang: String,
        rng: Random
    ): MiniGameQuestion {
        val missing = when (ageGroup) {
            AgeGroupConfig.EARLY -> rng.nextInt(1, 6)
            AgeGroupConfig.JUNIOR -> rng.nextInt(3, 12 + levelNumber)
            AgeGroupConfig.SENIOR -> rng.nextInt(4, 15 + levelNumber)
        }
        val known = rng.nextInt(2, 9)
        val sum = missing + known
        val opts = buildNumericOptions(missing, rng, minVal = 1)
        return MiniGameQuestion(
            index = index,
            total = total,
            prompt = when (lang) {
                "en" -> "Find the missing bridge stone! [ ? ] + $known = $sum"
                "es" -> "¿Qué número falta para cruzar el puente? [ ? ] + $known = $sum"
                "fr" -> "Quel nombre manque sur le pont ? [ ? ] + $known = $sum"
                "hi" -> "पुल पार करने के लिए कौन सी संख्या चाहिए? [ ? ] + $known = $sum"
                else -> "Temukan batu angka yang hilang di jembatan! [ ? ] + $known = $sum"
            },
            visualEquationOrPattern = "[ ? ] + $known = $sum",
            visualTokens = List(missing.coerceAtMost(8)) { "ITEM_STAR" },
            options = opts,
            correctIndex = opts.indexOf(missing.toString()).coerceAtLeast(0),
            hintText = when (lang) {
                "en" -> "Subtract $sum - $known to discover the missing bridge stone!"
                "es" -> "Calcula $sum - $known para hallar la piedra faltante."
                "fr" -> "Calcule $sum - $known pour trouver la pierre."
                "hi" -> "सही पत्थर खोजने के लिए $sum - $known करें।"
                else -> "Hitung $sum - $known untuk menemukan angka batu jembatan!"
            },
            sceneKind = VisualSceneKind.NUMBER_BRIDGE
        )
    }

    private fun buildLogicMatrix(
        index: Int,
        total: Int,
        ageGroup: AgeGroupConfig,
        levelNumber: Int,
        lang: String,
        rng: Random
    ): MiniGameQuestion {
        val variant = (index + levelNumber) % 3
        return when (variant) {
            0 -> {
                val rawTokens = listOf("SHAPE_RED_CIRCLE", "SHAPE_BLUE_SQUARE", "ITEM_SUN", "ITEM_APPLE")
                val labels = rawTokens.map { tokenLabel(it, lang) }
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = when (lang) {
                        "en" -> "Spot the Odd-One-Out! Which shape has 4 square corners?"
                        "es" -> "¿Cuál figura tiene 4 esquinas rectas en lugar de ser redonda?"
                        "fr" -> "Quelle forme a 4 coins droits au lieu d'être ronde ?"
                        "hi" -> "इनमें से कौन सी आकृति गोल न होकर 4 कोनों वाली है?"
                        else -> "Detektif Logika: Manakah bentuk yang punya 4 sudut kotak?"
                    },
                    visualEquationOrPattern = labels.joinToString("  •  "),
                    visualTokens = rawTokens,
                    options = labels,
                    optionIcons = rawTokens,
                    correctIndex = 1,
                    hintText = when (lang) {
                        "en" -> "Look for the blue square with 4 equal straight sides!"
                        "es" -> "Busca el cuadrado azul con cuatro lados iguales."
                        "fr" -> "Cherche le carré bleu avec quatre côtés égaux."
                        "hi" -> "चार बराबर भुजाओं वाले नीले वर्ग को चुनें।"
                        else -> "Pilih Kotak Biru yang memiliki 4 sisi lurus sama panjang!"
                    },
                    sceneKind = VisualSceneKind.LOGIC_MATRIX
                )
            }
            1 -> {
                val rawTokens = listOf("ITEM_APPLE", "ITEM_BUTTERFLY", "ITEM_CAR", "ITEM_TREE")
                val labels = rawTokens.map { tokenLabel(it, lang) }
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = when (lang) {
                        "en" -> "Which one has WHEELS to roll along the road?"
                        "es" -> "¿Cuál tiene RUEDAS para rodar por el camino?"
                        "fr" -> "Lequel a des ROUES pour rouler sur la route ?"
                        "hi" -> "इनमें से किसमें सड़क पर चलने के लिए पहिए हैं?"
                        else -> "Manakah benda yang memiliki RODA untuk melaju di jalan?"
                    },
                    visualEquationOrPattern = labels.joinToString("  •  "),
                    visualTokens = rawTokens,
                    options = labels,
                    optionIcons = rawTokens,
                    correctIndex = 2,
                    hintText = when (lang) {
                        "en" -> "Tap the playful blue car with two round wheels!"
                        "es" -> "Busca el carrito azul con ruedas."
                        "fr" -> "Cherche la petite voiture bleue avec des roues."
                        "hi" -> "पहियों वाली नीली गाड़ी को पहचानें।"
                        else -> "Pilih Mobil Biru yang punya dua roda bundar!"
                    },
                    sceneKind = VisualSceneKind.LOGIC_MATRIX
                )
            }
            else -> {
                val evenTarget = rng.nextInt(2, 9) * 2
                val odds = listOf(evenTarget + 1, evenTarget + 3, evenTarget + 5)
                val opts = (listOf(evenTarget) + odds).shuffled(rng).map { it.toString() }
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = when (lang) {
                        "en" -> "Logic Detective: Which of these numbers is EVEN (divisible by 2)?"
                        "es" -> "Acertijo Lógico: ¿Cuál de estos números es PAR?"
                        "fr" -> "Énigme Logique : Lequel de ces nombres est PAIR ?"
                        "hi" -> "तर्क पहेली: इनमें से कौन सी संख्या सम (Even) है?"
                        else -> "Detektif Angka: Manakah di antara angka ini yang GENAP (habis dibagi 2)?"
                    },
                    visualEquationOrPattern = opts.joinToString("  •  "),
                    visualTokens = opts,
                    options = opts,
                    correctIndex = opts.indexOf(evenTarget.toString()).coerceAtLeast(0),
                    hintText = when (lang) {
                        "en" -> "Even numbers can be split into two equal halves (ending in 0, 2, 4, 6, or 8)!"
                        "es" -> "Los números pares terminan en 0, 2, 4, 6 u 8."
                        "fr" -> "Les nombres pairs se terminent par 0, 2, 4, 6 ou 8."
                        "hi" -> "सम संख्याएं 2 से पूरी तरह विभाजित हो जाती हैं।"
                        else -> "Angka genap bisa dibagi dua sama rata (berakhiran 0, 2, 4, 6, atau 8)!"
                    },
                    sceneKind = VisualSceneKind.LOGIC_MATRIX
                )
            }
        }
    }

    private fun buildShapeGalaxy(
        index: Int,
        total: Int,
        ageGroup: AgeGroupConfig,
        levelNumber: Int,
        lang: String,
        rng: Random
    ): MiniGameQuestion {
        val sidesList = listOf(3, 4, 5, 6, 8)
        val sides = sidesList[(index + levelNumber) % sidesList.size]
        val opts = buildNumericOptions(sides, rng, minVal = 3)
        val shapeName = polygonName(sides, lang)
        return MiniGameQuestion(
            index = index,
            total = total,
            prompt = when (lang) {
                "en" -> "Count the glowing corners on this spinning $shapeName! How many sides & corners?"
                "es" -> "¡Cuenta las esquinas brillantes de este $shapeName! ¿Cuántos lados tiene?"
                "fr" -> "Compte les sommets lumineux de ce $shapeName ! Combien de côtés ?"
                "hi" -> "इस चमकती आकृति ($shapeName) के कितने कोने और भुजाएँ हैं?"
                else -> "Hitung titik sudut bercahaya pada $shapeName ini! Ada berapa sisi & sudutnya?"
            },
            visualEquationOrPattern = shapeName,
            visualTokens = List(sides) { "SHAPE_GOLD_STAR" },
            options = opts,
            correctIndex = opts.indexOf(sides.toString()).coerceAtLeast(0),
            hintText = when (lang) {
                "en" -> "Tap the $sides golden stars below or count the glowing yellow corners on the shape!"
                "es" -> "Cuenta los $sides puntos amarillos en las esquinas de la figura."
                "fr" -> "Compte les $sides points dorés sur les coins de la forme."
                "hi" -> "आकृति के $sides पीले चमकते कोनों को एक-एक करके गिनें!"
                else -> "Ketuk $sides bintang emas di bawah atau hitung titik kuning pada sudut bangun!"
            },
            sceneKind = VisualSceneKind.POLYGON_GALAXY,
            polygonSides = sides
        )
    }

    private fun buildMemoryTreasure(
        index: Int,
        total: Int,
        ageGroup: AgeGroupConfig,
        levelNumber: Int,
        lang: String,
        rng: Random
    ): MiniGameQuestion {
        val gems = listOf(
            "SHAPE_RED_CIRCLE",
            "SHAPE_BLUE_SQUARE",
            "SHAPE_GOLD_STAR",
            "SHAPE_GREEN_TRIANGLE",
            "SHAPE_PURPLE_DIAMOND"
        )
        val g1 = gems[(index + levelNumber) % gems.size]
        val g2 = gems[(index + levelNumber + 1) % gems.size]
        val g3 = gems[(index + levelNumber + 2) % gems.size]
        val sequence = listOf(g1, g2, g3, g1)
        val rawOpts = listOf(g1, g2, g3, gems[(index + levelNumber + 3) % gems.size]).distinct()
            .let { if (it.size < 4) (it + gems).distinct().take(4) else it.take(4) }
        val displayOpts = rawOpts.map { tokenLabel(it, lang) }
        return MiniGameQuestion(
            index = index,
            total = total,
            prompt = when (lang) {
                "en" -> "Which treasure gem appeared BOTH first and last in the chest?"
                "es" -> "¿Qué gema apareció TANTO al principio como al final del cofre?"
                "fr" -> "Quelle gemme apparaît au début ET à la fin du coffre ?"
                "hi" -> "कौन सा रत्न खजाने में सबसे पहले और सबसे अंत दोनों में आया है?"
                else -> "Permata manakah yang muncul di urutan PERTAMA sekaligus TERAKHIR?"
            },
            visualEquationOrPattern = sequence.joinToString("  •  ") { tokenLabel(it, lang) },
            visualTokens = sequence,
            options = displayOpts,
            optionIcons = rawOpts,
            correctIndex = rawOpts.indexOf(g1).coerceAtLeast(0),
            hintText = when (lang) {
                "en" -> "Look at the very first gem on the left and the last gem on the right!"
                "es" -> "Mira la primera gema a la izquierda y la última a la derecha."
                "fr" -> "Regarde la toute première gemme à gauche et la dernière à droite."
                "hi" -> "सबसे बाएं और सबसे दाएं रत्न को ध्यान से देखें!"
                else -> "Perhatikan permata paling kiri (pertama) dan paling kanan (terakhir)!"
            },
            sceneKind = VisualSceneKind.MEMORY_DECK
        )
    }

    private fun buildCosmicBalance(
        index: Int,
        total: Int,
        ageGroup: AgeGroupConfig,
        levelNumber: Int,
        lang: String,
        rng: Random
    ): MiniGameQuestion {
        val rightKnown = rng.nextInt(2, 6 + (levelNumber % 4))
        val missingWeight = rng.nextInt(2, 8 + (levelNumber % 5))
        val leftTotal = rightKnown + missingWeight
        val opts = buildNumericOptions(missingWeight, rng, minVal = 1)
        return MiniGameQuestion(
            index = index,
            total = total,
            prompt = when (lang) {
                "en" -> "Balance the Cosmic Scale! Left pan has $leftTotal, Right pan has $rightKnown + [ ? ]"
                "es" -> "¡Equilibra la Balanza Cósmica! Izquierda = $leftTotal, Derecha = $rightKnown + [ ? ]"
                "fr" -> "Équilibre la Balance Cosmique ! Gauche = $leftTotal, Droite = $rightKnown + [ ? ]"
                "hi" -> "कॉस्मिक तराजू संतुलित करें! बायां = $leftTotal, दायां = $rightKnown + [ ? ]"
                else -> "Seimbangkan Timbangan Kosmik! Kiri = $leftTotal, Kanan = $rightKnown + [ ? ]"
            },
            visualEquationOrPattern = "$leftTotal  =  $rightKnown + [ ? ]",
            visualTokens = List(missingWeight.coerceAtMost(8)) { "ITEM_CRYSTAL" },
            options = opts,
            correctIndex = opts.indexOf(missingWeight.toString()).coerceAtLeast(0),
            hintText = when (lang) {
                "en" -> "Count the $missingWeight crystals below: $leftTotal - $rightKnown = $missingWeight!"
                "es" -> "Resta $leftTotal - $rightKnown = $missingWeight para equilibrar la balanza."
                "fr" -> "Calcule $leftTotal - $rightKnown = $missingWeight pour équilibrer."
                "hi" -> "तराजू बराबर करने के लिए $leftTotal में से $rightKnown घटाएं ($missingWeight)!"
                else -> "Hitung $missingWeight kristal di bawah: $leftTotal - $rightKnown = $missingWeight!"
            },
            sceneKind = VisualSceneKind.BALANCE_BEAM,
            balanceLeftWeight = leftTotal,
            balanceRightKnownWeight = rightKnown
        )
    }

    private fun polygonName(sides: Int, lang: String): String = when (sides) {
        3 -> when (lang) {
            "en" -> "Triangle"
            "es" -> "Triángulo"
            "fr" -> "Triangle"
            "hi" -> "त्रिभुज"
            else -> "Segitiga"
        }
        4 -> when (lang) {
            "en" -> "Square"
            "es" -> "Cuadrado"
            "fr" -> "Carré"
            "hi" -> "वर्ग"
            else -> "Persegi"
        }
        5 -> when (lang) {
            "en" -> "Pentagon"
            "es" -> "Pentágono"
            "fr" -> "Pentagone"
            "hi" -> "पंचभुज"
            else -> "Segi Lima"
        }
        6 -> when (lang) {
            "en" -> "Hexagon"
            "es" -> "Hexágono"
            "fr" -> "Hexagone"
            "hi" -> "षट्भुज"
            else -> "Segi Enam"
        }
        else -> when (lang) {
            "en" -> "Octagon"
            "es" -> "Octágono"
            "fr" -> "Octogone"
            "hi" -> "अष्टभुज"
            else -> "Segi Delapan"
        }
    }

    fun tokenLabel(token: String, lang: String): String = when (token) {
        "SHAPE_RED_CIRCLE" -> when (lang) {
            "en" -> "Red Circle"
            "es" -> "Círculo Rojo"
            "fr" -> "Cercle Rouge"
            "hi" -> "लाल वृत्त"
            else -> "Lingkaran Merah"
        }
        "SHAPE_BLUE_SQUARE" -> when (lang) {
            "en" -> "Blue Square"
            "es" -> "Cuadrado Azul"
            "fr" -> "Carré Bleu"
            "hi" -> "नीला वर्ग"
            else -> "Kotak Biru"
        }
        "SHAPE_GOLD_STAR" -> when (lang) {
            "en" -> "Gold Star"
            "es" -> "Estrella Dorada"
            "fr" -> "Étoile d'Or"
            "hi" -> "सुनहरा तारा"
            else -> "Bintang Emas"
        }
        "SHAPE_GREEN_TRIANGLE" -> when (lang) {
            "en" -> "Green Triangle"
            "es" -> "Triángulo Verde"
            "fr" -> "Triangle Vert"
            "hi" -> "हरा त्रिभुज"
            else -> "Segitiga Hijau"
        }
        "SHAPE_PURPLE_DIAMOND" -> when (lang) {
            "en" -> "Purple Gem"
            "es" -> "Gema Violeta"
            "fr" -> "Gemme Violette"
            "hi" -> "बैंगनी रत्न"
            else -> "Permata Ungu"
        }
        "ITEM_SUN" -> when (lang) {
            "en" -> "Bright Sun"
            "es" -> "Sol Brillante"
            "fr" -> "Soleil"
            "hi" -> "सूरज"
            else -> "Matahari"
        }
        "ITEM_MOON" -> when (lang) {
            "en" -> "Crescent Moon"
            "es" -> "Luna"
            "fr" -> "Lune"
            "hi" -> "चाँद"
            else -> "Bulan Sabit"
        }
        "ITEM_APPLE" -> when (lang) {
            "en" -> "Red Berry"
            "es" -> "Fruta Roja"
            "fr" -> "Pomme Rouge"
            "hi" -> "लाल फल"
            else -> "Buah Apel"
        }
        "ITEM_BUTTERFLY" -> when (lang) {
            "en" -> "Butterfly"
            "es" -> "Mariposa"
            "fr" -> "Papillon"
            "hi" -> "तितली"
            else -> "Kupu-Kupu"
        }
        "ITEM_CAR" -> when (lang) {
            "en" -> "Blue Car"
            "es" -> "Auto Azul"
            "fr" -> "Voiture Bleue"
            "hi" -> "नीली गाड़ी"
            else -> "Mobil Biru"
        }
        "ITEM_TREE" -> when (lang) {
            "en" -> "Pine Tree"
            "es" -> "Árbol"
            "fr" -> "Sapin"
            "hi" -> "पेड़"
            else -> "Pohon Hijau"
        }
        "ITEM_CRYSTAL" -> when (lang) {
            "en" -> "Magic Crystal"
            "es" -> "Cristal Mágico"
            "fr" -> "Cristal Magique"
            "hi" -> "जादुई रत्न"
            else -> "Kristal Ajaib"
        }
        "ITEM_STAR" -> when (lang) {
            "en" -> "Star"
            "es" -> "Estrella"
            "fr" -> "Étoile"
            "hi" -> "तारा"
            else -> "Bintang"
        }
        else -> token
    }

    private fun buildNumericOptions(correct: Int, rng: Random, minVal: Int = 0): List<String> {
        val set = mutableSetOf(correct)
        val offsets = listOf(-3, -2, -1, 1, 2, 3, 4, 5).shuffled(rng)
        for (offset in offsets) {
            if (set.size >= 4) break
            val candidate = correct + offset
            if (candidate >= minVal && candidate != correct) {
                set.add(candidate)
            }
        }
        var fallback = correct + 1
        while (set.size < 4) {
            set.add(fallback++)
        }
        return set.toList().shuffled(rng).map { it.toString() }
    }
}
