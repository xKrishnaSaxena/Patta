package com.patta.pharmacy.data.repo

/**
 * One line being entered on a purchase (supplier) invoice.
 * ratePaise is the ex-GST purchase rate per pack; scheme discount and free goods
 * are what make the true landed cost differ from the sticker rate.
 */
data class PurchaseLine(
    val medicineId: String?,        // null => create a new medicine on save
    val medicineName: String,
    val salt: String = "",
    val batchNo: String,
    val expiryYm: Int,
    val qtyPacks: Double,
    val freeQtyPacks: Double,
    val ratePaise: Long,
    val schemeDiscPercent: Double,
    val gstPercent: Int,
    val mrpPaise: Long,
)

data class LineCost(
    val taxablePaise: Long,         // rate*qty after scheme discount (ex-GST)
    val gstPaise: Long,
    val lineTotalPaise: Long,       // taxable + GST = what the shop pays
    val landedCostPaise: Long,      // per-pack real cost (spread over qty + free)
    val marginPercent: Double,      // on MRP
)

/** The core Phase-2 calculation: real landed cost + margin from scheme + free goods. */
fun computeLineCost(line: PurchaseLine): LineCost {
    val gross = line.ratePaise * line.qtyPacks
    val taxable = gross * (1 - line.schemeDiscPercent / 100.0)
    val gst = taxable * line.gstPercent / 100.0
    val lineTotal = taxable + gst
    val totalPacks = line.qtyPacks + line.freeQtyPacks
    val landed = if (totalPacks > 0) lineTotal / totalPacks else 0.0
    val margin = if (line.mrpPaise > 0) (line.mrpPaise - landed) / line.mrpPaise * 100.0 else 0.0
    return LineCost(
        taxablePaise = Math.round(taxable),
        gstPaise = Math.round(gst),
        lineTotalPaise = Math.round(lineTotal),
        landedCostPaise = Math.round(landed),
        marginPercent = margin,
    )
}
