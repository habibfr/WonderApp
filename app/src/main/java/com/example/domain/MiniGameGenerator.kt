package com.example.domain

import com.example.data.AgeGroupConfig
import com.example.data.GamePlayMode
import com.example.data.MiniGameType

enum class VisualSceneKind {
    COUNTING_SAFARI,
    SEQUENCE_CRYSTALS,
    STEPPING_BRIDGE,
    MATRIX_GRID,
    POLYGON_GALAXY,
    MEMORY_CHEST,
    BALANCE_BEAM
}

data class VisualCountGroup(
    val symbol: String,
    val count: Int,
    val label: String,
    val accentHex: Long
)

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
    val sceneKind: VisualSceneKind = VisualSceneKind.COUNTING_SAFARI,
    val leftGroup: VisualCountGroup? = null,
    val rightGroup: VisualCountGroup? = null,
    val operatorSymbol: String = "+",
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
        val total = playMode.questionCount
        return (1..total).map { idx ->
            when (gameType) {
                MiniGameType.MATH_SAFARI -> buildMathSafariQuestion(idx, total, ageGroup, levelNumber, languageCode)
                MiniGameType.PATTERN_PORTAL -> buildPatternPortalQuestion(idx, total, ageGroup, levelNumber, languageCode)
                MiniGameType.NUMBER_BRIDGE -> buildNumberBridgeQuestion(idx, total, ageGroup, levelNumber, languageCode)
                MiniGameType.LOGIC_MATRIX -> buildLogicMatrixQuestion(idx, total, ageGroup, levelNumber, languageCode)
                MiniGameType.SHAPE_GALAXY -> buildShapeGalaxyQuestion(idx, total, ageGroup, levelNumber, languageCode)
                MiniGameType.MEMORY_TREASURE -> buildMemoryTreasureQuestion(idx, total, ageGroup, levelNumber, languageCode)
                MiniGameType.COSMIC_BALANCE -> buildCosmicBalanceQuestion(idx, total, ageGroup, levelNumber, languageCode)
            }
        }
    }

    private fun buildMathSafariQuestion(
        index: Int,
        total: Int,
        ageGroup: AgeGroupConfig,
        level: Int,
        lang: String
    ): MiniGameQuestion {
        val safariTokens = listOf(
            "●" to 0xFFFFB300L,
            "■" to 0xFF26A69AL,
            "▲" to 0xFFFF7043L,
            "★" to 0xFF7E57C2L,
            "◆" to 0xFFEC407AL,
            "✦" to 0xFF66BB6AL
        )
        val (tokenSymbol, colorHex) = safariTokens[(index + level) % safariTokens.size]

        return when (ageGroup) {
            AgeGroupConfig.AGES_4_6 -> {
                val a = ((index + level) % 4) + 1
                val b = (index % 3) + 1
                val answer = a + b
                val options = makeNumericOptions(answer, minVal = 1)
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = localizedMathPrompt(lang, isAddition = true, symbol = tokenSymbol),
                    visualEquationOrPattern = "$a $tokenSymbol  +  $b $tokenSymbol  =  ?",
                    visualTokens = List(a) { tokenSymbol } + listOf("+") + List(b) { tokenSymbol },
                    options = options.map { it.toString() },
                    optionIcons = emptyList(),
                    correctIndex = options.indexOf(answer),
                    hintText = localizedHintCount(lang, a, b, answer),
                    sceneKind = VisualSceneKind.COUNTING_SAFARI,
                    leftGroup = VisualCountGroup(tokenSymbol, a, "$a", colorHex),
                    rightGroup = VisualCountGroup(tokenSymbol, b, "$b", colorHex),
                    operatorSymbol = "+"
                )
            }
            AgeGroupConfig.AGES_7_9 -> {
                val isAdd = index % 2 == 1
                val a = (level * 2) + index + 3
                val b = (index % 4) + 2
                val answer = if (isAdd) a + b else a - b
                val op = if (isAdd) "+" else "−"
                val options = makeNumericOptions(answer, minVal = 1)
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = localizedMathPrompt(lang, isAddition = isAdd, symbol = tokenSymbol),
                    visualEquationOrPattern = "$a  $op  $b  =  ?",
                    visualTokens = List(a.coerceAtMost(6)) { tokenSymbol } + listOf(op) + List(b.coerceAtMost(5)) { tokenSymbol },
                    options = options.map { it.toString() },
                    optionIcons = emptyList(),
                    correctIndex = options.indexOf(answer),
                    hintText = localizedHintStartFrom(lang, a, b, isAdd, answer),
                    sceneKind = VisualSceneKind.COUNTING_SAFARI,
                    leftGroup = VisualCountGroup(tokenSymbol, a.coerceAtMost(8), "$a", colorHex),
                    rightGroup = VisualCountGroup(tokenSymbol, b.coerceAtMost(8), "$b", colorHex),
                    operatorSymbol = op
                )
            }
            AgeGroupConfig.AGES_10_13 -> {
                val a = (index + 2) + (level % 4)
                val b = (index % 4) + 2
                val c = index
                val answer = (a * b) + c
                val options = makeNumericOptions(answer, minVal = 4)
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = localizedOrderOfOpsPrompt(lang),
                    visualEquationOrPattern = "($a × $b) + $c = ?",
                    visualTokens = listOf("$a × $b", "+", "$c", "=", "?"),
                    options = options.map { it.toString() },
                    optionIcons = emptyList(),
                    correctIndex = options.indexOf(answer),
                    hintText = "$a × $b = ${a * b}, + $c = $answer",
                    sceneKind = VisualSceneKind.COUNTING_SAFARI,
                    leftGroup = VisualCountGroup(tokenSymbol, a.coerceAtMost(7), "$a×$b", colorHex),
                    rightGroup = VisualCountGroup("★", c.coerceAtMost(6), "+$c", 0xFFFFB300L),
                    operatorSymbol = "×+"
                )
            }
        }
    }

    private fun buildPatternPortalQuestion(
        index: Int,
        total: Int,
        ageGroup: AgeGroupConfig,
        level: Int,
        lang: String
    ): MiniGameQuestion {
        val patternSets = listOf(
            listOf("▲", "■", "●"),
            listOf("★", "◆", "✦"),
            listOf("1", "2", "3"),
            listOf("A", "B", "C"),
            listOf("●", "▲", "★"),
            listOf("◆", "■", "✦")
        )
        val set = patternSets[(index + level) % patternSets.size]
        return when (ageGroup) {
            AgeGroupConfig.AGES_4_6 -> {
                val a = set[0]
                val b = set[1]
                val sequence = listOf(a, b, a, b, a, "?")
                val correct = b
                val options = listOf(a, b, set[2], "▼").distinct()
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = localizedPatternPrompt(lang),
                    visualEquationOrPattern = sequence.joinToString("  →  "),
                    visualTokens = sequence,
                    options = options,
                    optionIcons = emptyList(),
                    correctIndex = options.indexOf(correct),
                    hintText = "$a, $b, $a, $b ... → $correct",
                    sceneKind = VisualSceneKind.SEQUENCE_CRYSTALS
                )
            }
            AgeGroupConfig.AGES_7_9 -> {
                val a = set[0]
                val b = set[1]
                val c = set[2]
                val sequence = listOf(a, b, c, a, b, "?")
                val correct = c
                val options = listOf(c, a, b, "●")
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = localizedPatternPrompt(lang),
                    visualEquationOrPattern = sequence.joinToString("  •  "),
                    visualTokens = sequence,
                    options = options,
                    optionIcons = emptyList(),
                    correctIndex = options.indexOf(correct),
                    hintText = "$a → $b → $c",
                    sceneKind = VisualSceneKind.SEQUENCE_CRYSTALS
                )
            }
            AgeGroupConfig.AGES_10_13 -> {
                val step = index + 2
                val start = level * 2
                val seqNums = listOf(start, start + step, start + step * 2, start + step * 3)
                val answer = start + step * 4
                val options = makeNumericOptions(answer, minVal = 2)
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = localizedPatternPrompt(lang),
                    visualEquationOrPattern = "${seqNums.joinToString(" , ")} , ?",
                    visualTokens = seqNums.map { it.toString() } + listOf("?"),
                    options = options.map { it.toString() },
                    optionIcons = emptyList(),
                    correctIndex = options.indexOf(answer),
                    hintText = "+$step → ${seqNums.last()} + $step = $answer",
                    sceneKind = VisualSceneKind.SEQUENCE_CRYSTALS
                )
            }
        }
    }

    private fun buildNumberBridgeQuestion(
        index: Int,
        total: Int,
        ageGroup: AgeGroupConfig,
        level: Int,
        lang: String
    ): MiniGameQuestion {
        val step = when (ageGroup) {
            AgeGroupConfig.AGES_4_6 -> 1
            AgeGroupConfig.AGES_7_9 -> (index % 3) + 2
            AgeGroupConfig.AGES_10_13 -> (index % 4) + 4
        }
        val start = level + index
        val stones = listOf(
            start,
            start + step,
            start + step * 2,
            start + step * 3,
            start + step * 4
        )
        val missingPos = 2
        val answer = stones[missingPos]
        val displayStones = stones.mapIndexed { idx, value ->
            if (idx == missingPos) "?" else value.toString()
        }
        val options = makeNumericOptions(answer, minVal = 1)
        return MiniGameQuestion(
            index = index,
            total = total,
            prompt = localizedBridgePrompt(lang, step),
            visualEquationOrPattern = displayStones.joinToString("  ──  "),
            visualTokens = displayStones,
            options = options.map { it.toString() },
            optionIcons = emptyList(),
            correctIndex = options.indexOf(answer),
            hintText = "${stones[1]} + $step = $answer",
            sceneKind = VisualSceneKind.STEPPING_BRIDGE
        )
    }

    private fun buildLogicMatrixQuestion(
        index: Int,
        total: Int,
        ageGroup: AgeGroupConfig,
        level: Int,
        lang: String
    ): MiniGameQuestion {
        return when (ageGroup) {
            AgeGroupConfig.AGES_4_6 -> {
                val pairs = listOf(
                    Triple("Kucing", "Ikan", listOf("Ikan", "Mobil", "Topi", "Jam")),
                    Triple("Lebah", "Madu", listOf("Madu", "Sepatu", "Sofa", "Klip")),
                    Triple("Burung", "Sarang", listOf("Sarang", "Papan", "Es", "Gitar")),
                    Triple("Monyet", "Pisang", listOf("Pisang", "Senter", "Bata", "Roda")),
                    Triple("Kelinci", "Wortel", listOf("Wortel", "Buku", "Kacamata", "Sendok"))
                )
                val chosen = pairs[(index + level) % pairs.size]
                val shuffled = chosen.third.shuffled(java.util.Random((index * 31 + level).toLong()))
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = localizedLogicPairPrompt(lang, chosen.first),
                    visualEquationOrPattern = "${chosen.first}  ➔  ?",
                    visualTokens = listOf(chosen.first, "➔", "?"),
                    options = shuffled,
                    optionIcons = emptyList(),
                    correctIndex = shuffled.indexOf(chosen.second),
                    hintText = "${chosen.first} + ${chosen.second}",
                    sceneKind = VisualSceneKind.MATRIX_GRID
                )
            }
            AgeGroupConfig.AGES_7_9 -> {
                val shapes = listOf("●", "■", "▲")
                val target = shapes[(index + level) % shapes.size]
                val others = shapes.filter { it != target }
                val options = listOf(target, others[0], others[1], "◆")
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = localizedMatrixRowPrompt(lang),
                    visualEquationOrPattern = "${others[0]}   ${others[1]}   ?",
                    visualTokens = listOf(others[0], others[1], "?"),
                    options = options,
                    optionIcons = emptyList(),
                    correctIndex = 0,
                    hintText = "● + ■ + ▲ = $target",
                    sceneKind = VisualSceneKind.MATRIX_GRID
                )
            }
            AgeGroupConfig.AGES_10_13 -> {
                val base = index + level + 2
                val mult = (index % 3) + 2
                val topA = base
                val topB = base * mult
                val botA = base + 3
                val answer = botA * mult
                val options = makeNumericOptions(answer, minVal = 4)
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = localizedMatrixGridPrompt(lang),
                    visualEquationOrPattern = "[ $topA ➔ $topB ]   •   [ $botA ➔ ? ]",
                    visualTokens = listOf("$topA ➔ $topB", "×$mult", "$botA ➔ ?"),
                    options = options.map { it.toString() },
                    optionIcons = emptyList(),
                    correctIndex = options.indexOf(answer),
                    hintText = "× $mult: $botA × $mult = $answer",
                    sceneKind = VisualSceneKind.MATRIX_GRID
                )
            }
        }
    }

    private fun buildShapeGalaxyQuestion(
        index: Int,
        total: Int,
        ageGroup: AgeGroupConfig,
        level: Int,
        lang: String
    ): MiniGameQuestion {
        val shapesData = listOf(
            Triple(3, "▲", mapOf("id" to "Segitiga", "en" to "Triangle", "es" to "Triángulo", "fr" to "Triangle", "hi" to "त्रिभुज")),
            Triple(4, "■", mapOf("id" to "Persegi", "en" to "Square", "es" to "Cuadrado", "fr" to "Carré", "hi" to "वर्ग")),
            Triple(5, "⬠", mapOf("id" to "Segi Lima", "en" to "Pentagon", "es" to "Pentágono", "fr" to "Pentagone", "hi" to "पंचभुज")),
            Triple(6, "⬡", mapOf("id" to "Segi Enam", "en" to "Hexagon", "es" to "Hexágono", "fr" to "Hexagone", "hi" to "षट्भुज")),
            Triple(8, "✦", mapOf("id" to "Segi Delapan", "en" to "Octagon", "es" to "Octágono", "fr" to "Octogone", "hi" to "अष्टभुज"))
        )
        val picked = shapesData[(index + level) % shapesData.size]
        val sides = picked.first
        val shapeSymbol = picked.second
        val shapeName = picked.third[lang] ?: picked.third["id"]!!

        return when (ageGroup) {
            AgeGroupConfig.AGES_4_6, AgeGroupConfig.AGES_7_9 -> {
                val options = listOf(sides, sides + 1, (sides - 1).coerceAtLeast(3), sides + 2).distinct()
                    .let { if (it.size < 4) (it + listOf(7, 9)).distinct().take(4) else it }
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = localizedShapePrompt(lang, shapeName),
                    visualEquationOrPattern = "$shapeSymbol  $shapeName  =  ? Sisi / Sudut",
                    visualTokens = List(sides.coerceAtMost(6)) { "✦" } + listOf(shapeSymbol),
                    options = options.map { it.toString() },
                    optionIcons = emptyList(),
                    correctIndex = options.indexOf(sides),
                    hintText = localizedShapeHint(lang, shapeName, sides),
                    sceneKind = VisualSceneKind.POLYGON_GALAXY,
                    polygonSides = sides
                )
            }
            AgeGroupConfig.AGES_10_13 -> {
                val sideLength = (index % 4) + 2
                val perimeter = sides * sideLength
                val options = makeNumericOptions(perimeter, minVal = 6)
                MiniGameQuestion(
                    index = index,
                    total = total,
                    prompt = localizedPerimeterPrompt(lang, shapeName, sides, sideLength),
                    visualEquationOrPattern = "$shapeName ($sides × $sideLength) = ?",
                    visualTokens = listOf(shapeSymbol, "$sides sisi", "× $sideLength", "= ?"),
                    options = options.map { it.toString() },
                    optionIcons = emptyList(),
                    correctIndex = options.indexOf(perimeter),
                    hintText = "$sides × $sideLength = $perimeter",
                    sceneKind = VisualSceneKind.POLYGON_GALAXY,
                    polygonSides = sides
                )
            }
        }
    }

    private fun buildMemoryTreasureQuestion(
        index: Int,
        total: Int,
        ageGroup: AgeGroupConfig,
        level: Int,
        lang: String
    ): MiniGameQuestion {
        val treasureSets = listOf(
            listOf("★", "◆", "●", "▲"),
            listOf("A", "B", "C", "D"),
            listOf("1", "2", "3", "4"),
            listOf("✦", "■", "▼", "✚"),
            listOf("Alpha", "Beta", "Gamma", "Delta")
        )
        val set = treasureSets[(index + level) % treasureSets.size]
        val cardCount = when (ageGroup) {
            AgeGroupConfig.AGES_4_6 -> 3
            AgeGroupConfig.AGES_7_9 -> 4
            AgeGroupConfig.AGES_10_13 -> 4
        }
        val cards = set.take(cardCount)
        val hiddenPos = (index + level) % cards.size
        val correctGem = cards[hiddenPos]
        val displayTokens = cards.mapIndexed { idx, item ->
            if (idx == hiddenPos) "[ ? ]" else item
        }
        return MiniGameQuestion(
            index = index,
            total = total,
            prompt = localizedMemoryPrompt(lang, hiddenPos + 1, cards),
            visualEquationOrPattern = displayTokens.joinToString("   "),
            visualTokens = cards,
            options = set,
            optionIcons = emptyList(),
            correctIndex = set.indexOf(correctGem),
            hintText = "${cards.joinToString(" • ")} → $correctGem",
            sceneKind = VisualSceneKind.MEMORY_CHEST,
            polygonSides = hiddenPos
        )
    }

    private fun buildCosmicBalanceQuestion(
        index: Int,
        total: Int,
        ageGroup: AgeGroupConfig,
        level: Int,
        lang: String
    ): MiniGameQuestion {
        val rightKnown = when (ageGroup) {
            AgeGroupConfig.AGES_4_6 -> (index % 3) + 1
            AgeGroupConfig.AGES_7_9 -> (index % 4) + 2
            AgeGroupConfig.AGES_10_13 -> (index % 5) + 4
        }
        val missingWeight = when (ageGroup) {
            AgeGroupConfig.AGES_4_6 -> (level % 3) + 1
            AgeGroupConfig.AGES_7_9 -> (level % 4) + 2
            AgeGroupConfig.AGES_10_13 -> (level % 6) + 3
        }
        val leftTotal = rightKnown + missingWeight
        val options = makeNumericOptions(missingWeight, minVal = 1)
        return MiniGameQuestion(
            index = index,
            total = total,
            prompt = localizedBalancePrompt(lang, leftTotal, rightKnown),
            visualEquationOrPattern = "[ $leftTotal ★ ]  =  [ $rightKnown ★ + ? ]",
            visualTokens = listOf("$leftTotal", "=", "$rightKnown + ?"),
            options = options.map { it.toString() },
            optionIcons = emptyList(),
            correctIndex = options.indexOf(missingWeight),
            hintText = "$leftTotal − $rightKnown = $missingWeight",
            sceneKind = VisualSceneKind.BALANCE_BEAM,
            balanceLeftWeight = leftTotal,
            balanceRightKnownWeight = rightKnown
        )
    }

    private fun makeNumericOptions(correct: Int, minVal: Int): List<Int> {
        val candidates = linkedSetOf(correct)
        val offsets = listOf(1, -1, 2, -2, 3, 4)
        for (offset in offsets) {
            val candidate = (correct + offset).coerceAtLeast(minVal)
            candidates.add(candidate)
            if (candidates.size == 4) break
        }
        var extra = correct + 5
        while (candidates.size < 4) {
            candidates.add(extra++)
        }
        return candidates.toList().sorted()
    }

    private fun localizedMathPrompt(lang: String, isAddition: Boolean, symbol: String): String = when (lang) {
        "en" -> if (isAddition) "Count & tap the $symbol tokens! How many altogether?" else "How many $symbol tokens remain?"
        "es" -> if (isAddition) "¡Cuenta y toca los símbolos $symbol! ¿Cuántos hay en total?" else "¿Cuántos $symbol quedan?"
        "fr" -> if (isAddition) "Compte et touche les symboles $symbol ! Combien au total ?" else "Combien de $symbol restent ?"
        "hi" -> if (isAddition) "$symbol को गिनें! कुल कितने हैं?" else "कितने $symbol बचे हैं?"
        else -> if (isAddition) "Ketuk & hitung simbol $symbol di bawah! Berapa jumlah semuanya?" else "Berapa sisa simbol $symbol di pulau?"
    }

    private fun localizedOrderOfOpsPrompt(lang: String): String = when (lang) {
        "en" -> "Solve the Safari Crystal equation!"
        "es" -> "¡Resuelve la ecuación de cristal!"
        "fr" -> "Résous l'équation magique !"
        "hi" -> "सफारी समीकरण हल करें!"
        else -> "Selesaikan persamaan Kristal Safari ini!"
    }

    private fun localizedHintCount(lang: String, a: Int, b: Int, answer: Int): String = when (lang) {
        "en" -> "Tap each icon to count: $a + $b = $answer!"
        "es" -> "Toca cada icono: $a + $b = $answer"
        "fr" -> "Touche chaque icône : $a + $b = $answer"
        "hi" -> "प्रत्येक चित्र को टैप करें: $a + $b = $answer"
        else -> "Ketuk gambarnya satu per satu: $a + $b = $answer!"
    }

    private fun localizedHintStartFrom(lang: String, a: Int, b: Int, isAdd: Boolean, ans: Int): String = when (lang) {
        "en" -> if (isAdd) "Start at $a and count up $b steps → $ans" else "Start at $a and count back $b steps → $ans"
        "es" -> if (isAdd) "Empieza en $a y suma $b → $ans" else "Empieza en $a y resta $b → $ans"
        "fr" -> if (isAdd) "Commence à $a et ajoute $b → $ans" else "Commence à $a et enlève $b → $ans"
        "hi" -> if (isAdd) "$a में $b जोड़ें → $ans" else "$a में से $b घटाएं → $ans"
        else -> if (isAdd) "Mulai dari $a lalu maju $b langkah → $ans" else "Mulai dari $a lalu mundur $b langkah → $ans"
    }

    private fun localizedPatternPrompt(lang: String): String = when (lang) {
        "en" -> "Which crystal icon completes the magic portal?"
        "es" -> "¿Qué cristal completa el portal mágico?"
        "fr" -> "Quel cristal complète le portail magique ?"
        "hi" -> "जादुई पोर्टल को कौन सा चिन्ह पूरा करता है?"
        else -> "Simbol kristal mana yang melengkapi portal ajaib ini?"
    }

    private fun localizedBridgePrompt(lang: String, step: Int): String = when (lang) {
        "en" -> "Hop by +$step across the bridge! Find the missing stone:"
        "es" -> "¡Salta de +$step en +$step por el puente! Encuentra la piedra:"
        "fr" -> "Saute de +$step en +$step sur le pont ! Trouve la pierre :"
        "hi" -> "+$step के अंतर से पुल पार करें! सही पत्थर चुनें:"
        else -> "Lompat +$step di setiap batu jembatan! Berapa angka batu yang hilang?"
    }

    private fun localizedLogicPairPrompt(lang: String, item: String): String = when (lang) {
        "en" -> "Which favorite treasure belongs with $item?"
        "es" -> "¿Qué pareja le corresponde a $item?"
        "fr" -> "Quel élément va avec $item ?"
        "hi" -> "$item के साथ कौन सी चीज़ मेल खाती है?"
        else -> "Pasangan mana yang paling cocok untuk $item?"
    }

    private fun localizedMatrixRowPrompt(lang: String): String = when (lang) {
        "en" -> "Every row needs ●, ■, and ▲. Which shape is missing?"
        "es" -> "Cada fila necesita ●, ■ y ▲. ¿Cuál falta?"
        "fr" -> "Chaque ligne a ●, ■ et ▲. Laquelle manque ?"
        "hi" -> "हर पंक्ति में ●, ■ और ▲ हैं। कौन सा गायब है?"
        else -> "Setiap baris memiliki ●, ■, dan ▲. Bentuk mana yang belum ada?"
    }

    private fun localizedMatrixGridPrompt(lang: String): String = when (lang) {
        "en" -> "Find the secret multiplier rule to unlock the vault!"
        "es" -> "¡Descubre la regla secreta para abrir el cofre!"
        "fr" -> "Trouve la règle secrète pour ouvrir le coffre !"
        "hi" -> "तिजोरी खोलने के लिए गुप्त नियम खोजें!"
        else -> "Temukan pola perkalian rahasia untuk membuka peti!"
    }

    private fun localizedShapePrompt(lang: String, shapeName: String): String = when (lang) {
        "en" -> "Count the glowing corners on the $shapeName! How many sides does it have?"
        "es" -> "¡Cuenta las esquinas brillantes del $shapeName! ¿Cuántos lados tiene?"
        "fr" -> "Compte les sommets lumineux du $shapeName ! Combien de côtés ?"
        "hi" -> "$shapeName के चमकते कोनों को गिनें! इसमें कितनी भुजाएँ हैं?"
        else -> "Hitung titik sudut bercahaya pada $shapeName! Berapa jumlah sisinya?"
    }

    private fun localizedShapeHint(lang: String, shapeName: String, sides: Int): String = when (lang) {
        "en" -> "A $shapeName has $sides glowing sides and $sides corners!"
        "es" -> "Un $shapeName tiene $sides lados."
        "fr" -> "Un $shapeName possède $sides côtés."
        "hi" -> "$shapeName में $sides भुजाएँ होती हैं।"
        else -> "Bangun $shapeName memiliki $sides sisi dan $sides titik sudut!"
    }

    private fun localizedPerimeterPrompt(lang: String, shapeName: String, sides: Int, len: Int): String = when (lang) {
        "en" -> "Each of the $sides sides of this $shapeName is $len units. Total perimeter?"
        "es" -> "Cada uno de los $sides lados del $shapeName mide $len. ¿Perímetro total?"
        "fr" -> "Chacun des $sides côtés du $shapeName mesure $len. Périmètre total ?"
        "hi" -> "$shapeName की $sides भुजाओं में प्रत्येक $len है। कुल परिमाप?"
        else -> "Setiap dari $sides sisi $shapeName panjangnya $len. Berapa keliling totalnya?"
    }

    private fun localizedMemoryPrompt(lang: String, pos: Int, cards: List<String>): String = when (lang) {
        "en" -> "Treasure Chest #$pos is hiding a gem from [${cards.joinToString(" ")}]! Which one is in slot #$pos?"
        "es" -> "¡El cofre #$pos esconde un tesoro de [${cards.joinToString(" ")}]! ¿Cuál está en #$pos?"
        "fr" -> "Le coffre #$pos cache un trésor de [${cards.joinToString(" ")}] ! Lequel est en #$pos ?"
        "hi" -> "तिजोरी #$pos में [${cards.joinToString(" ")}] में से कौन सा रत्न छिपा है?"
        else -> "Peti Harta ke-$pos menyimpan simbol dari [${cards.joinToString(" ")}]! Manakah isi peti ke-$pos?"
    }

    private fun localizedBalancePrompt(lang: String, leftTotal: Int, rightKnown: Int): String = when (lang) {
        "en" -> "Balance the Cosmic Scale! Left has $leftTotal, Right has $rightKnown. How many more needed to balance?"
        "es" -> "¡Equilibra la balanza! Izquierda: $leftTotal, Derecha: $rightKnown. ¿Cuánto falta?"
        "fr" -> "Équilibre la balance ! Gauche : $leftTotal, Droite : $rightKnown. Combien manque-t-il ?"
        "hi" -> "तराजू संतुलित करें! बाएं $leftTotal, दाएं $rightKnown हैं। और कितने चाहिए?"
        else -> "Seimbangkan Timbangan Kosmik! Kiri = $leftTotal, Kanan = $rightKnown. Berapa beban lagi agar seimbang?"
    }
}
