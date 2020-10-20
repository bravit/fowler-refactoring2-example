import java.text.NumberFormat
import java.util.*
import kotlin.math.*

fun statement(invoice: Invoice, plays: List<Play>): String {
    var totalAmount = 0
    var totalVolumeCredits = 0
    var result = "Statement for ${invoice.customer}\n"

    fun findPlay(performance: Performance): Play {
        return plays.find { it.id == performance.playID } ?: throw Exception("Unknown play: ${performance.playID}")
    }

    for(perf in invoice.performances) {
        val play = findPlay(perf)
        val thisAmount = amountFor(perf, play)
        // add volume credits
        totalVolumeCredits += volumeCreditsFor(perf, play)
        result += " ${play.name}: ${asUSD(thisAmount)} (${perf.audience} seats)\n"
        totalAmount += thisAmount
    }
    result += "Amount owed is ${asUSD(totalAmount)}\n"
    result += "You earned $totalVolumeCredits credits\n"
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