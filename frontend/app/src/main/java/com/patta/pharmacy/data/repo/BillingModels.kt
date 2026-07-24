package com.patta.pharmacy.data.repo

import kotlin.math.floor

/**
 * One line in the current bill. [mrpPaise] is the per-PACK MRP (GST-inclusive).
 * When [perTablet] is on (loose sale), the price and stock impact are computed
 * per tablet using [unitsPerPack].
 */
data class CartLine(
    val medicineId: String,
    val name: String,
    val salt: String,
    val batchId: String,
    val batchNo: String,
    val expiryYm: Int,
    val packType: String,
    val gstPercent: Int,
    val mrpPaise: Long,
    val unitsPerPack: Int,
    val allowLooseSale: Boolean,
    val isScheduleH1: Boolean = false,
    val dosage: String = "",
    val perTablet: Boolean = false,
    val qty: Int,
    val availablePacks: Double,
) {
    /** Price of one sold unit (strip or tablet), in paise. */
    val ratePaise: Long
        get() = if (perTablet && unitsPerPack > 0) Math.round(mrpPaise.toDouble() / unitsPerPack) else mrpPaise

    /** How many packs one sold unit consumes (1 strip = 1 pack, 1 tablet = 1/N pack). */
    val packsPerUnit: Double
        get() = if (perTablet && unitsPerPack > 0) 1.0 / unitsPerPack else 1.0

    val unitLabel: String get() = if (perTablet) "Tab" else packType

    val lineTotalPaise: Long get() = ratePaise * qty

    /** Stock consumed by this line, in packs (may be fractional for loose sale). */
    val packsSold: Double get() = qty * packsPerUnit

    /** Max sellable count in the current unit, given available stock. */
    val maxQty: Int
        get() = if (perTablet && unitsPerPack > 0) floor(availablePacks * unitsPerPack).toInt()
        else floor(availablePacks).toInt()
}

data class BillTotals(
    val subtotalPaise: Long,     // taxable value (total − GST)
    val gstPaise: Long,          // GST component contained within the MRP
    val totalPaise: Long,        // what the customer pays
)

/** GST is included in MRP, so we back it out of each line rather than add it on top. */
fun computeTotals(lines: List<CartLine>, discountPaise: Long = 0): BillTotals {
    val gross = lines.sumOf { it.lineTotalPaise }
    val total = (gross - discountPaise).coerceAtLeast(0)
    val gst = lines.sumOf { line ->
        val lt = line.lineTotalPaise
        Math.round(lt * line.gstPercent.toDouble() / (100 + line.gstPercent))
    }
    val subtotal = (total - gst).coerceAtLeast(0)
    return BillTotals(subtotalPaise = subtotal, gstPaise = gst, totalPaise = total)
}

/** Snapshot of a saved bill, used to render/share a PDF after checkout. */
data class CompletedBill(
    val billNo: String,
    val dateTimeMillis: Long,
    val paymentMode: String,
    val lines: List<CartLine>,
    val totals: BillTotals,
    val customerPhone: String = "",   // prefilled for udhaar bills so WhatsApp is one tap
)
