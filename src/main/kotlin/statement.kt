import java.text.NumberFormat
import java.util.*
import kotlin.math.*

data class StatementLine(val name: String,
                         val amount: Int,
                         val volumeCredits: Int,
                         val audience: Int)

data class StatementData(val customer: String, val lines: List<StatementLine>) {
    val totalAmount = lines.map {it.amount}.sum()
    val totalVolumeCredits = lines.map {it.volumeCredits}.sum()
}

fun statement(invoice: Invoice, plays: List<Play>): String {

    fun findPlay(performance: Performance): Play {
        return plays.find { it.id == performance.playID } ?: throw Exception("Unknown play: ${performance.playID}")
    }

    val stmtData = StatementData(invoice.customer, invoice.performances.map {
        val play = findPlay(it)
        StatementLine(play.name, amountFor(it, play),
                      volumeCreditsFor(it, play),it.audience)
    })

    return prepareStatement(stmtData)
}

private fun prepareStatement(stmtData: StatementData): String {
    var result = "Statement for ${stmtData.customer}\n"
    for (line in stmtData.lines) {
        result += " ${line.name}: ${asUSD(line.amount)} (${line.audience} seats)\n"
    }
    result += "Amount owed is ${asUSD(stmtData.totalAmount)}\n"
    result += "You earned ${stmtData.totalVolumeCredits} credits\n"
    return result
}

private fun asUSD(thisAmount: Int) =
    NumberFormat.getCurrencyInstance(Locale.US).format(thisAmount / 100)

private fun volumeCreditsFor(performance: Performance, play: Play): Int {
    var result = max(performance.audience - 30, 0)
    // add extra credit for every ten comedy attendees
    if (play.type == PlayType.COMEDY)
        result += floor(performance.audience / 5.0).toInt()
    // print line for this order
    return result
}

private fun amountFor(perf: Performance, play: Play): Int {
    var result: Int
    when (play.type) {
        PlayType.TRAGEDY -> {
            result = 40000
            if (perf.audience > 30) {
                result += 1000 * (perf.audience - 30)
            }
        }
        PlayType.COMEDY -> {
            result = 30000
            if (perf.audience > 20) {
                result += 10000 + 500 * (perf.audience - 20)
            }
            result += 300 * perf.audience
        }
        PlayType.OTHER -> throw Exception("Unknown type: ${play.type}")
    }
    return result
}