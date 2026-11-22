package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.Customer
import com.example.data.model.LedgerTransaction
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    fun generateAccountingPdf(
        context: Context,
        businessName: String,
        periodLabel: String,
        totalIncome: Double,
        totalExpense: Double,
        totalPaona: Double,
        totalDena: Double,
        customers: List<Customer>,
        transactions: List<LedgerTransaction>,
    ): Result<Uri> = runCatching {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 72dpi
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val headerPaint = Paint().apply {
            color = Color.parseColor("#0B8A56")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 88f, headerPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            isAntiAlias = true
        }
        canvas.drawText("Hishaber Khata - $businessName", 32f, 40f, titlePaint)
        canvas.drawText("Report Period: $periodLabel  |  Date: ${Formatters.formatShortDate(System.currentTimeMillis())}", 32f, 65f, subPaint)

        val sectionPaint = Paint().apply {
            color = Color.parseColor("#1A1C1E")
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#333333")
            textSize = 11.5f
            isAntiAlias = true
        }
        val greenPaint = Paint(bodyPaint).apply { color = Color.parseColor("#0A8754"); isFakeBoldText = true }
        val redPaint = Paint(bodyPaint).apply { color = Color.parseColor("#D92D20"); isFakeBoldText = true }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 1f
        }

        var y = 122f
        canvas.drawText("Financial Summary", 32f, y, sectionPaint)
        y += 22f
        canvas.drawText("Total Income / Received:", 32f, y, bodyPaint)
        canvas.drawText(Formatters.formatTaka(totalIncome), 240f, y, greenPaint)
        y += 18f
        canvas.drawText("Total Expense / Paid:", 32f, y, bodyPaint)
        canvas.drawText(Formatters.formatTaka(totalExpense), 240f, y, redPaint)
        y += 18f
        val profit = totalIncome - totalExpense
        canvas.drawText("Net Profit / Loss:", 32f, y, bodyPaint)
        canvas.drawText(Formatters.formatTaka(profit), 240f, y, if (profit >= 0) greenPaint else redPaint)
        y += 18f
        canvas.drawText("Total Customer Receivable (Paona):", 32f, y, bodyPaint)
        canvas.drawText(Formatters.formatTaka(totalPaona), 240f, y, greenPaint)
        y += 18f
        canvas.drawText("Total Customer Payable (Dena):", 32f, y, bodyPaint)
        canvas.drawText(Formatters.formatTaka(totalDena), 240f, y, redPaint)

        y += 20f
        canvas.drawLine(32f, y, 563f, y, linePaint)
        y += 24f

        canvas.drawText("Customer Due Balances (Top 12)", 32f, y, sectionPaint)
        y += 20f
        customers.take(12).forEach { c ->
            val label = if (c.balance >= 0) "Paona" else "Dena"
            canvas.drawText("${c.name} (${c.phone.ifBlank { "-" }})", 32f, y, bodyPaint)
            canvas.drawText(
                "$label: ${Formatters.formatTaka(kotlin.math.abs(c.balance))}",
                380f,
                y,
                if (c.balance >= 0) greenPaint else redPaint
            )
            y += 16f
        }

        y += 14f
        canvas.drawLine(32f, y, 563f, y, linePaint)
        y += 24f

        canvas.drawText("Recent Transactions (Up to 15)", 32f, y, sectionPaint)
        y += 20f
        transactions.take(15).forEach { tx ->
            if (y > 800f) return@forEach
            val who = tx.customerName.ifBlank { "Cashbook" }
            val desc = tx.description.ifBlank { tx.type }.take(28)
            canvas.drawText("${Formatters.formatShortDate(tx.dateMillis)}  $who - $desc", 32f, y, bodyPaint)
            val isPositive = tx.type == "received" || tx.type == "income"
            canvas.drawText(
                Formatters.formatTaka(tx.amount),
                440f,
                y,
                if (isPositive) greenPaint else redPaint
            )
            y += 16f
        }

        document.finishPage(page)

        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, "hisaber_khata_report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun generateCustomerLedgerPdf(
        context: Context,
        businessName: String,
        customerName: String,
        customerPhone: String,
        filterLabel: String,
        openingBalance: Double,
        cashDue: Double,
        totalDeposit: Double,
        closingBalance: Double,
        transactions: List<LedgerTransaction>,
    ): Result<Uri> = runCatching {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val headerPaint = Paint().apply {
            color = Color.parseColor("#057A4B")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 92f, headerPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 19f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = Color.WHITE
            textSize = 11.5f
            isAntiAlias = true
        }
        canvas.drawText("Hishabi Khata - $businessName", 32f, 38f, titlePaint)
        canvas.drawText("Customer: $customerName (${customerPhone.ifBlank { "-" }})  |  Filter: $filterLabel", 32f, 64f, subPaint)

        val sectionPaint = Paint().apply {
            color = Color.parseColor("#1A1C1E")
            textSize = 13.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#333333")
            textSize = 11f
            isAntiAlias = true
        }
        val greenPaint = Paint(bodyPaint).apply { color = Color.parseColor("#057A4B"); isFakeBoldText = true }
        val redPaint = Paint(bodyPaint).apply { color = Color.parseColor("#D92D20"); isFakeBoldText = true }
        val linePaint = Paint().apply { color = Color.parseColor("#D9EAE0"); strokeWidth = 1f }

        var y = 124f
        canvas.drawText("Ledger Summary", 32f, y, sectionPaint)
        y += 20f
        canvas.drawText("Opening Balance:", 32f, y, bodyPaint)
        canvas.drawText(Formatters.formatTaka(openingBalance), 220f, y, bodyPaint)
        y += 18f
        canvas.drawText("Cash Due (Dilam/Becha):", 32f, y, bodyPaint)
        canvas.drawText(Formatters.formatTaka(cashDue), 220f, y, redPaint)
        y += 18f
        canvas.drawText("Total Deposit (Pelam):", 32f, y, bodyPaint)
        canvas.drawText(Formatters.formatTaka(totalDeposit), 220f, y, greenPaint)
        y += 18f
        canvas.drawText("Closing Balance:", 32f, y, sectionPaint)
        canvas.drawText(Formatters.formatTaka(closingBalance), 220f, y, if (closingBalance >= 0) redPaint else greenPaint)

        y += 20f
        canvas.drawLine(32f, y, 563f, y, linePaint)
        y += 22f

        canvas.drawText("Date", 32f, y, sectionPaint)
        canvas.drawText("Description", 120f, y, sectionPaint)
        canvas.drawText("Dilam", 315f, y, sectionPaint)
        canvas.drawText("Pelam", 395f, y, sectionPaint)
        canvas.drawText("Balance", 480f, y, sectionPaint)
        y += 12f
        canvas.drawLine(32f, y, 563f, y, linePaint)
        y += 18f

        var running = openingBalance
        transactions.sortedBy { it.dateMillis }.take(32).forEach { tx ->
            if (y > 800f) return@forEach
            val isGave = tx.type == "gave"
            running += if (isGave) tx.amount else -tx.amount
            canvas.drawText(Formatters.formatShortDate(tx.dateMillis), 32f, y, bodyPaint)
            canvas.drawText(tx.description.ifBlank { if (isGave) "Dilam/Becha" else "Pelam" }.take(24), 120f, y, bodyPaint)
            canvas.drawText(if (isGave) Formatters.formatTaka(tx.amount) else "-", 315f, y, if (isGave) redPaint else bodyPaint)
            canvas.drawText(if (!isGave) Formatters.formatTaka(tx.amount) else "-", 395f, y, if (!isGave) greenPaint else bodyPaint)
            canvas.drawText(Formatters.formatTaka(running), 480f, y, bodyPaint)
            y += 17f
        }

        document.finishPage(page)

        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, "hishabi_khata_customer_report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()

        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
