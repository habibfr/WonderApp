package com.example.domain

import com.example.data.AgeBracket
import com.example.data.MiniGameType
import kotlin.random.Random

data class PuzzleQuestion(
    val index: Int,
    val title: String,
    val prompt: String,
    val visualEquationOrTokens: List<String>,
    val options: List<String>,
    val correctOption: String,
    val hint: String
)

object MiniGameGenerator {

    fun generateLevelQuestions(
        gameType: MiniGameType,
        ageBracket: AgeBracket,
        levelNumber: Int,
        lang: String,
        questionCount: Int = 5
    ): List<PuzzleQuestion> {
        val seed = (gameType.id.hashCode() * 31) + (ageBracket.code.hashCode() * 17) + (levelNumber * 101)
        val rng = Random(seed)
        return (1..questionCount).map { idx ->
            when (gameType) {
                MiniGameType.MATH_SAFARI -> buildMathSafariQuestion(idx, ageBracket, levelNumber, lang, rng)
                MiniGameType.PATTERN_PORTAL -> buildPatternPortalQuestion(idx, ageBracket, levelNumber, lang, rng)
                MiniGameType.NUMBER_BRIDGE -> buildNumberBridgeQuestion(idx, ageBracket, levelNumber, lang, rng)
                MiniGameType.LOGIC_MATRIX -> buildLogicMatrixQuestion(idx, ageBracket, levelNumber, lang, rng)
            }
        }
    }

    private fun buildMathSafariQuestion(
        index: Int,
        ageBracket: AgeBracket,
        levelNumber: Int,
        lang: String,
        rng: Random
    ): PuzzleQuestion {
        val emojis = listOf("🍎", "🦋", "🌟", "🐢", "🍓", "🎈", "🦊", "🦉")
        val emoji = emojis[(index + levelNumber) % emojis.size]

        return when (ageBracket) {
            AgeBracket.EARLY -> {
                val a = rng.nextInt(1, 5 + (levelNumber % 3))
                val b = rng.nextInt(1, 5)
                val sum = a + b
                val tokens = List(a) { emoji } + listOf("➕") + List(b) { emoji }
                val prompt = when (lang) {
                    "es" -> "¡Cuenta y suma! ¿Cuántos $emoji hay en total? ($a + $b)"
                    "fr" -> "Compte et additionne ! Combien de $emoji en tout ? ($a + $b)"
                    "hi" -> "गिनें और जोड़ें! कुल कितने $emoji हैं? ($a + $b)"
                    else -> "Count and add! How many $emoji altogether? ($a + $b)"
                }
                val hint = when (lang) {
                    "es" -> "Cuenta primero $a y luego sigue contando $b más."
                    "fr" -> "Compte d'abord $a puis ajoute encore $b."
                    "hi" -> "पहले $a गिनें और फिर उसमें $b और जोड़ें।"
                    else -> "Start at $a and count up $b more one by one!"
                }
                PuzzleQuestion(
                    index = index,
                    title = "$a + $b = ?",
                    prompt = prompt,
                    visualEquationOrTokens = tokens,
                    options = numericOptions(sum, rng, minVal = 1),
                    correctOption = sum.toString(),
                    hint = hint
                )
            }
            AgeBracket.MIDDLE -> {
                val isMultiply = index % 2 == 0
                if (isMultiply) {
                    val a = rng.nextInt(2, 6 + (levelNumber % 4))
                    val b = rng.nextInt(2, 6)
                    val ans = a * b
                    val prompt = when (lang) {
                        "es" -> "$a grupos mágicos de $b $emoji cada uno. ¿Cuántos en total?"
                        "fr" -> "$a groupes magiques de $b $emoji chacun. Combien au total ?"
                        "hi" -> "$emoji के $a समूह, प्रत्येक में $b। कुल कितने हैं?"
                        else -> "$a magic groups with $b $emoji in each group. How many in total?"
                    }
                    val hint = when (lang) {
                        "es" -> "Suma $b un total de $a veces ($a × $b)."
                        "fr" -> "Additionne $b exactement $a fois ($a × $b)."
                        "hi" -> "$b को $a बार जोड़ें ($a × $b)।"
                        else -> "Think of $a × $b as adding $b together $a times!"
                    }
                    PuzzleQuestion(
                        index = index,
                        title = "$a × $b = ?",
                        prompt = prompt,
                        visualEquationOrTokens = listOf("$a × $b", emoji, "✨"),
                        options = numericOptions(ans, rng, minVal = 2),
                        correctOption = ans.toString(),
                        hint = hint
                    )
                } else {
                    val a = rng.nextInt(12, 28 + levelNumber)
                    val b = rng.nextInt(4, 11)
                    val ans = a - b
                    val prompt = when (lang) {
                        "es" -> "Resuelve la resta de la isla: $a − $b = ?"
                        "fr" -> "Résous la soustraction de l'île : $a − $b = ?"
                        "hi" -> "द्वीप का घटाव हल करें: $a − $b = ?"
                        else -> "Solve the island subtraction quest: $a − $b = ?"
                    }
                    val hint = when (lang) {
                        "es" -> "Resta primero las unidades y luego las decenas de $a."
                        "fr" -> "Recule de $b à partir de $a."
                        "hi" -> "$a में से $b कदम पीछे गिनें।"
                        else -> "Count backward $b steps starting from $a!"
                    }
                    PuzzleQuestion(
                        index = index,
                        title = "$a − $b = ?",
                        prompt = prompt,
                        visualEquationOrTokens = listOf("$a", "➖", "$b", "🟰", "❓"),
                        options = numericOptions(ans, rng, minVal = 1),
                        correctOption = ans.toString(),
                        hint = hint
                    )
                }
            }
            AgeBracket.OLDER -> {
                val a = rng.nextInt(3, 9)
                val b = rng.nextInt(3, 8)
                val c = rng.nextInt(4, 15)
                val ans = (a * b) + c
                val prompt = when (lang) {
                    "es" -> "Orden de operaciones: ($a × $b) + $c = ?"
                    "fr" -> "Priorité des opérations : ($a × $b) + $c = ?"
                    "hi" -> "संक्रियाओं का क्रम: ($a × $b) + $c = ?"
                    else -> "Master Wizard Expression: ($a × $b) + $c = ?"
                }
                val hint = when (lang) {
                    "es" -> "Multiplica primero $a × $b = ${a * b}, luego suma $c."
                    "fr" -> "Calcule d'abord $a × $b = ${a * b}, puis ajoute $c."
                    "hi" -> "पहले गुणा करें $a × $b = ${a * b}, फिर $c जोड़ें।"
                    else -> "First multiply $a × $b = ${a * b}, then add $c!"
                }
                PuzzleQuestion(
                    index = index,
                    title = "($a × $b) + $c = ?",
                    prompt = prompt,
                    visualEquationOrTokens = listOf("($a × $b)", "➕", "$c", "🟰", "❓"),
                    options = numericOptions(ans, rng, minVal = 5),
                    correctOption = ans.toString(),
                    hint = hint
                )
            }
        }
    }

    private fun buildPatternPortalQuestion(
        index: Int,
        ageBracket: AgeBracket,
        levelNumber: Int,
        lang: String,
        rng: Random
    ): PuzzleQuestion {
        return when (ageBracket) {
            AgeBracket.EARLY -> {
                val pairs = listOf(
                    listOf("🔴", "🔵"),
                    listOf("🌞", "🌙"),
                    listOf("⭐", "🌈"),
                    listOf("🍎", "🍌"),
                    listOf("🐸", "🦋")
                )
                val pair = pairs[(index + levelNumber) % pairs.size]
                val sequence = listOf(pair[0], pair[1], pair[0], pair[1], pair[0], "❓")
                val correct = pair[1]
                val distractors = listOf("🟣", "🍀", "💎").filter { it != correct }
                val options = (listOf(correct) + distractors).shuffled(rng)
                val prompt = when (lang) {
                    "es" -> "¿Qué símbolo sigue en el portal mágico?"
                    "fr" -> "Quel symbole vient ensuite dans le portail ?"
                    "hi" -> "जादुई पैटर्न में आगे कौन सा चिन्ह आएगा?"
                    else -> "What comes next in the glowing Pattern Portal?"
                }
                val hint = when (lang) {
                    "es" -> "Observa cómo se alternan: ${pair[0]} y luego ${pair[1]}."
                    "fr" -> "Regarde l'alternance : ${pair[0]} puis ${pair[1]}."
                    "hi" -> "देखें कि ${pair[0]} के बाद हमेशा ${pair[1]} आता है।"
                    else -> "Look closely! After every ${pair[0]} comes ${pair[1]}!"
                }
                PuzzleQuestion(
                    index = index,
                    title = sequence.joinToString("  "),
                    prompt = prompt,
                    visualEquationOrTokens = sequence,
                    options = options,
                    correctOption = correct,
                    hint = hint
                )
            }
            AgeBracket.MIDDLE -> {
                val step = rng.nextInt(2, 6)
                val start = rng.nextInt(2, 10)
                val seq = (0..3).map { (start + it * step).toString() } + listOf("❓")
                val correct = start + 4 * step
                val prompt = when (lang) {
                    "es" -> "Completa la secuencia numérica del portal:"
                    "fr" -> "Complète la suite numérique du portail :"
                    "hi" -> "पोर्टल की संख्या श्रृंखला पूरी करें:"
                    else -> "Find the next number to unlock the Pattern Portal:"
                }
                val hint = when (lang) {
                    "es" -> "Cada paso suma +$step al número anterior."
                    "fr" -> "On ajoute +$step à chaque étape."
                    "hi" -> "हर अगली संख्या में +$step जोड़ा गया है।"
                    else -> "Each stone increases by +$step! Add $step to ${start + 3 * step}."
                }
                PuzzleQuestion(
                    index = index,
                    title = seq.joinToString(" → "),
                    prompt = prompt,
                    visualEquationOrTokens = seq,
                    options = numericOptions(correct, rng, minVal = 1),
                    correctOption = correct.toString(),
                    hint = hint
                )
            }
            AgeBracket.OLDER -> {
                // Growing difference sequence: +2, +4, +6, +8...
                val base = rng.nextInt(2, 8)
                val n1 = base
                val n2 = n1 + 2
                val n3 = n2 + 4
                val n4 = n3 + 6
                val correct = n4 + 8
                val seq = listOf("$n1", "$n2", "$n3", "$n4", "❓")
                val prompt = when (lang) {
                    "es" -> "Patrón de diferencias crecientes: ¿qué número sigue?"
                    "fr" -> "Suite à écarts croissants : quel nombre suit ?"
                    "hi" -> "बढ़ते अंतर का पैटर्न: अगली संख्या क्या होगी?"
                    else -> "Growing-Step Sequence: What number completes the portal?"
                }
                val hint = when (lang) {
                    "es" -> "Los saltos crecen: +2, +4, +6... ¡el siguiente salto es +8!"
                    "fr" -> "Les écarts augmentent : +2, +4, +6... le prochain est +8 !"
                    "hi" -> "अंतर बढ़ रहा है: +2, +4, +6... अगला अंतर +8 होगा!"
                    else -> "Look at the gaps: +2, +4, +6... so the next jump is +8!"
                }
                PuzzleQuestion(
                    index = index,
                    title = seq.joinToString(" → "),
                    prompt = prompt,
                    visualEquationOrTokens = seq,
                    options = numericOptions(correct, rng, minVal = 5),
                    correctOption = correct.toString(),
                    hint = hint
                )
            }
        }
    }

    private fun buildNumberBridgeQuestion(
        index: Int,
        ageBracket: AgeBracket,
        levelNumber: Int,
        lang: String,
        rng: Random
    ): PuzzleQuestion {
        return when (ageBracket) {
            AgeBracket.EARLY -> {
                val missing = rng.nextInt(1, 6)
                val known = rng.nextInt(1, 5)
                val total = missing + known
                val prompt = when (lang) {
                    "es" -> "¿Qué número falta para cruzar el puente? [ ? ] + $known = $total"
                    "fr" -> "Quel nombre manque sur le pont ? [ ? ] + $known = $total"
                    "hi" -> "पुल पार करने के लिए कौन सी संख्या चाहिए? [ ? ] + $known = $total"
                    else -> "Find the missing bridge stone! [ ? ] + $known = $total"
                }
                val hint = when (lang) {
                    "es" -> "¿Cuánto le falta a $known para llegar a $total? ($total − $known)"
                    "fr" -> "Combien faut-il ajouter à $known pour faire $total ?"
                    "hi" -> "$known में कितना जोड़ें कि $total बन जाए? ($total − $known)"
                    else -> "Count up from $known until you reach $total!"
                }
                PuzzleQuestion(
                    index = index,
                    title = "[ ? ] + $known = $total",
                    prompt = prompt,
                    visualEquationOrTokens = listOf("🌉", "❓", "➕", "$known", "🟰", "$total"),
                    options = numericOptions(missing, rng, minVal = 1),
                    correctOption = missing.toString(),
                    hint = hint
                )
            }
            AgeBracket.MIDDLE -> {
                val missing = rng.nextInt(3, 12 + levelNumber)
                val known = rng.nextInt(4, 12)
                val total = missing + known
                val prompt = when (lang) {
                    "es" -> "Equilibra el puente: $known + [ ? ] = $total"
                    "fr" -> "Équilibre le pont : $known + [ ? ] = $total"
                    "hi" -> "पुल को संतुलित करें: $known + [ ? ] = $total"
                    else -> "Balance the equation bridge: $known + [ ? ] = $total"
                }
                val hint = when (lang) {
                    "es" -> "Calcula $total − $known para hallar la piedra faltante."
                    "fr" -> "Calcule $total − $known pour trouver la pierre."
                    "hi" -> "सही पत्थर खोजने के लिए $total − $known करें।"
                    else -> "Subtract $total − $known to discover the missing bridge stone!"
                }
                PuzzleQuestion(
                    index = index,
                    title = "$known + [ ? ] = $total",
                    prompt = prompt,
                    visualEquationOrTokens = listOf("$known", "➕", "❓", "🟰", "$total"),
                    options = numericOptions(missing, rng, minVal = 1),
                    correctOption = missing.toString(),
                    hint = hint
                )
            }
            AgeBracket.OLDER -> {
                val x = rng.nextInt(2, 9)
                val mult = rng.nextInt(2, 6)
                val add = rng.nextInt(3, 12)
                val rhs = mult * x + add
                val prompt = when (lang) {
                    "es" -> "Resuelve el misterio algebraico: ($mult × ❓) + $add = $rhs"
                    "fr" -> "Résous l'équation du pont : ($mult × ❓) + $add = $rhs"
                    "hi" -> "बीजगणित पहेली सुलझाएं: ($mult × ❓) + $add = $rhs"
                    else -> "Solve for the mystery crystal ❓: ($mult × ❓) + $add = $rhs"
                }
                val hint = when (lang) {
                    "es" -> "Resta $rhs − $add = ${rhs - add}, luego divide entre $mult."
                    "fr" -> "Fais $rhs − $add = ${rhs - add}, puis divise par $mult."
                    "hi" -> "पहले $rhs − $add = ${rhs - add} करें, फिर $mult से भाग दें।"
                    else -> "First subtract $add from $rhs (${rhs - add}), then divide by $mult!"
                }
                PuzzleQuestion(
                    index = index,
                    title = "($mult × ❓) + $add = $rhs",
                    prompt = prompt,
                    visualEquationOrTokens = listOf("${mult}×❓", "➕", "$add", "🟰", "$rhs"),
                    options = numericOptions(x, rng, minVal = 1),
                    correctOption = x.toString(),
                    hint = hint
                )
            }
        }
    }

    private fun buildLogicMatrixQuestion(
        index: Int,
        ageBracket: AgeBracket,
        levelNumber: Int,
        lang: String,
        rng: Random
    ): PuzzleQuestion {
        val variant = (index + levelNumber) % 4
        return when (ageBracket) {
            AgeBracket.EARLY -> {
                when (variant) {
                    0 -> PuzzleQuestion(
                        index = index,
                        title = "🍎  🍌  🚗  🍓",
                        prompt = when (lang) {
                            "es" -> "¿Cuál NO es una fruta para comer?"
                            "fr" -> "Lequel N'EST PAS un fruit qui se mange ?"
                            "hi" -> "इनमें से कौन सा खाने वाला फल नहीं है?"
                            else -> "Spot the Odd-One-Out! Which one is NOT a fruit?"
                        },
                        visualEquationOrTokens = listOf("🍎", "🍌", "🚗", "🍓"),
                        options = listOf("🍎", "🍌", "🚗", "🍓"),
                        correctOption = "🚗",
                        hint = when (lang) {
                            "es" -> "¡Tres son frutas deliciosas y uno tiene ruedas!"
                            "fr" -> "Trois sont des fruits et l'un a des roues !"
                            "hi" -> "तीन मीठे फल हैं और एक पहियों वाली गाड़ी है!"
                            else -> "Three are yummy fruits, and one has four wheels!"
                        }
                    )
                    1 -> PuzzleQuestion(
                        index = index,
                        title = "🐶  🐱  🐰  🌵",
                        prompt = when (lang) {
                            "es" -> "¿Cuál NO es un animalito?"
                            "fr" -> "Lequel N'EST PAS un animal ?"
                            "hi" -> "इनमें से कौन एक जानवर नहीं है?"
                            else -> "Which one is a plant instead of a furry animal?"
                        },
                        visualEquationOrTokens = listOf("🐶", "🐱", "🐰", "🌵"),
                        options = listOf("🐶", "🐱", "🐰", "🌵"),
                        correctOption = "🌵",
                        hint = when (lang) {
                            "es" -> "Busca la planta verde del desierto."
                            "fr" -> "Cherche la plante verte du désert."
                            "hi" -> "हरे रंग के कैक्टस पौधे को पहचानें।"
                            else -> "Look for the prickly green cactus plant!"
                        }
                    )
                    2 -> PuzzleQuestion(
                        index = index,
                        title = "✈️  🦅  🦋  ⚓",
                        prompt = when (lang) {
                            "es" -> "¿Cuál NO vuela por el cielo?"
                            "fr" -> "Lequel NE vole PAS dans le ciel ?"
                            "hi" -> "इनमें से कौन आकाश में नहीं उड़ता?"
                            else -> "Which one stays deep under the ocean instead of flying?"
                        },
                        visualEquationOrTokens = listOf("✈️", "🦅", "🦋", "⚓"),
                        options = listOf("✈️", "🦅", "🦋", "⚓"),
                        correctOption = "⚓",
                        hint = when (lang) {
                            "es" -> "El ancla se hunde en el mar."
                            "fr" -> "L'ancre va au fond de l'eau."
                            "hi" -> "जहाज का लंगर पानी के नीचे जाता है।"
                            else -> "An anchor sinks down into the water!"
                        }
                    )
                    else -> PuzzleQuestion(
                        index = index,
                        title = "2  •  5  •  9  •  1",
                        prompt = when (lang) {
                            "es" -> "¿Cuál es el número MÁS GRANDE?"
                            "fr" -> "Quel est le PLUS GRAND nombre ?"
                            "hi" -> "इनमें से सबसे बड़ी संख्या कौन सी है?"
                            else -> "Number Detective: Which number is the BIGGEST?"
                        },
                        visualEquationOrTokens = listOf("2️⃣", "5️⃣", "9️⃣", "1️⃣"),
                        options = listOf("2", "5", "9", "1"),
                        correctOption = "9",
                        hint = when (lang) {
                            "es" -> "El 9 está casi al llegar al 10."
                            "fr" -> "9 est juste avant 10."
                            "hi" -> "9 संख्या 10 के सबसे करीब और सबसे बड़ी है।"
                            else -> "9 is right next to 10—the biggest of the group!"
                        }
                    )
                }
            }
            AgeBracket.MIDDLE -> {
                val evenTarget = rng.nextInt(2, 9) * 2
                val odds = listOf(evenTarget + 1, evenTarget + 3, evenTarget + 5)
                val opts = (listOf(evenTarget) + odds).shuffled(rng).map { it.toString() }
                PuzzleQuestion(
                    index = index,
                    title = opts.joinToString("  •  "),
                    prompt = when (lang) {
                        "es" -> "Acertijo Lógico: ¿Cuál de estos números es PAR (divisible por 2)?"
                        "fr" -> "Énigme Logique : Lequel de ces nombres est PAIR ?"
                        "hi" -> "तर्क पहेली: इनमें से कौन सी संख्या सम (Even) है?"
                        else -> "Logic Detective: Which of these numbers is EVEN (divisible by 2)?"
                    },
                    visualEquationOrTokens = opts.map { "🔮$it" },
                    options = opts,
                    correctOption = evenTarget.toString(),
                    hint = when (lang) {
                        "es" -> "Los números pares terminan en 0, 2, 4, 6 u 8."
                        "fr" -> "Les nombres pairs se terminent par 0, 2, 4, 6 ou 8."
                        "hi" -> "सम संख्याएं 2 से पूरी तरह विभाजित हो जाती हैं (अंत में 0, 2, 4, 6, 8)।"
                        else -> "Even numbers can be split into two equal halves (ending in 0, 2, 4, 6, or 8)!"
                    }
                )
            }
            AgeBracket.OLDER -> {
                val primes = listOf(11, 13, 17, 19, 23, 29)
                val targetPrime = primes[(index + levelNumber) % primes.size]
                val composites = listOf(12, 15, 21, 25, 27, 28).shuffled(rng).take(3)
                val opts = (listOf(targetPrime) + composites).shuffled(rng).map { it.toString() }
                PuzzleQuestion(
                    index = index,
                    title = opts.joinToString("  •  "),
                    prompt = when (lang) {
                        "es" -> "Lógica Maestra: ¿Cuál de estos es un NÚMERO PRIMO?"
                        "fr" -> "Logique Maître : Lequel de ces nombres est un NOMBRE PREMIER ?"
                        "hi" -> "मास्टर तर्क: इनमें से कौन सी एक अभाज्य संख्या (Prime Number) है?"
                        else -> "Master Logic Riddle: Which of these is a PRIME number?"
                    },
                    visualEquationOrTokens = opts.map { "⚡$it" },
                    options = opts,
                    correctOption = targetPrime.toString(),
                    hint = when (lang) {
                        "es" -> "Un número primo solo se puede dividir exactamente entre 1 y sí mismo."
                        "fr" -> "Un nombre premier n'est divisible que par 1 et par lui-même."
                        "hi" -> "अभाज्य संख्या केवल 1 और स्वयं से विभाजित होती है।"
                        else -> "A prime number has only two factors: 1 and itself!"
                    }
                )
            }
        }
    }

    private fun numericOptions(correct: Int, rng: Random, minVal: Int = 0): List<String> {
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
