package com.example.splitbill.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.splitbill.data.model.Expense
import com.example.splitbill.data.model.Member
import com.example.splitbill.domain.Money
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {
    fun exportToPdf(context: Context, groupName: String, members: List<Member>, balances: Map<String, Long>, expenses: List<Expense>, getMemberName: (String) -> String): Uri? {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 dimensions
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint()
            val titlePaint = Paint().apply {
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.BLACK
            }
            val headerPaint = Paint().apply {
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.BLACK
            }
            val textPaint = Paint().apply {
                textSize = 14f
                color = Color.BLACK
            }

            var yPos = 50f
            val xMargin = 50f

            // Title
            canvas.drawText("SplitBill Report: $groupName", xMargin, yPos, titlePaint)
            yPos += 40f

            // Members & Balances
            canvas.drawText("Balances", xMargin, yPos, headerPaint)
            yPos += 25f

            for (member in members) {
                val bal = balances[member.id] ?: 0L
                val formatted = Money.formatPaise(if (bal < 0L) -bal else bal)
                val balText = if (bal > 0) "+$formatted" else if (bal < 0) "-$formatted" else "₹0.00"
                canvas.drawText("${member.name}: $balText", xMargin + 10f, yPos, textPaint)
                yPos += 20f
            }
            yPos += 20f

            // Expenses
            canvas.drawText("Expenses", xMargin, yPos, headerPaint)
            yPos += 25f

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            for (expense in expenses) {
                // If we reach the bottom, create a new page
                if (yPos > 800f) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPos = 50f
                }

                val dateStr = dateFormat.format(Date(expense.timestampMillis))
                val payerName = getMemberName(expense.paidBy)
                val amountStr = Money.formatPaise(expense.amountPaise)
                
                val text = "$dateStr - ${expense.description} ($payerName) - $amountStr"
                canvas.drawText(text, xMargin + 10f, yPos, textPaint)
                yPos += 20f
            }

            document.finishPage(page)

            val fileName = "Report_${groupName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            document.writeTo(FileOutputStream(file))
            document.close()

            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
