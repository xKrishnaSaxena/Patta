package com.patta.pharmacy.util

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.patta.pharmacy.data.repo.CompletedBill
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Renders a [CompletedBill] to a small PDF and opens the Android share sheet
 * (WhatsApp shows up there). Shop details are placeholders until a Store profile
 * screen lands — swap SHOP_* below for real values.
 */
object BillPdf {

    private const val SHOP_NAME = "Sharma Medical Store"
    private const val SHOP_LICENSE = "DL: 20B/21B-4567"
    private const val SHOP_GSTIN = "GSTIN: 22ABCDE1234F1Z5"

    fun generateAndShare(context: Context, bill: CompletedBill) {
        val doc = PdfDocument()
        val pageWidth = 320
        val lineH = 18
        val topPad = 24
        val bodyLines = bill.lines.size
        val pageHeight = topPad + (10 + bodyLines + 6) * lineH + 40
        val page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
        val c = page.canvas

        val title = Paint().apply { textSize = 15f; isFakeBoldText = true }
        val small = Paint().apply { textSize = 9f; color = 0xFF555555.toInt() }
        val normal = Paint().apply { textSize = 11f }
        val bold = Paint().apply { textSize = 12f; isFakeBoldText = true }
        val right = Paint().apply { textSize = 11f; textAlign = Paint.Align.RIGHT }
        val rightBold = Paint().apply { textSize = 13f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT }

        var y = topPad
        val left = 14f
        val rightX = (pageWidth - 14).toFloat()

        c.drawText(SHOP_NAME, left, y.toFloat(), title); y += lineH
        c.drawText(SHOP_LICENSE, left, y.toFloat(), small); y += 12
        c.drawText(SHOP_GSTIN, left, y.toFloat(), small); y += lineH

        val dateStr = SimpleDateFormat("dd/MM/yy  hh:mm a", Locale("en", "IN")).format(Date(bill.dateTimeMillis))
        c.drawText("Bill: ${bill.billNo}", left, y.toFloat(), normal)
        c.drawText(dateStr, rightX, y.toFloat(), right); y += lineH
        c.drawLine(left, y.toFloat(), rightX, y.toFloat(), small); y += lineH

        bill.lines.forEach { line ->
            c.drawText(line.name.take(24), left, y.toFloat(), normal)
            c.drawText(Money.format(line.lineTotalPaise), rightX, y.toFloat(), right)
            y += 13
            c.drawText("${line.qty} ${line.unitLabel} x ${Money.format(line.ratePaise)}", left + 6, y.toFloat(), small)
            y += lineH
        }

        c.drawLine(left, y.toFloat(), rightX, y.toFloat(), small); y += lineH
        c.drawText("Subtotal", left, y.toFloat(), normal)
        c.drawText(Money.format(bill.totals.subtotalPaise), rightX, y.toFloat(), right); y += lineH
        c.drawText("GST (included)", left, y.toFloat(), normal)
        c.drawText(Money.format(bill.totals.gstPaise), rightX, y.toFloat(), right); y += lineH
        c.drawText("TOTAL", left, y.toFloat(), bold)
        c.drawText(Money.format(bill.totals.totalPaise), rightX, y.toFloat(), rightBold); y += lineH
        c.drawText("Payment: ${bill.paymentMode.uppercase()}", left, y.toFloat(), small); y += lineH + 6
        c.drawText("Dhanyavaad! Get well soon.", left, y.toFloat(), small)

        doc.finishPage(page)

        val dir = File(context.cacheDir, "bills").apply { mkdirs() }
        val file = File(dir, "${bill.billNo}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Aapka bill — ${bill.billNo}, ${Money.format(bill.totals.totalPaise)}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Bill bhejo").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
