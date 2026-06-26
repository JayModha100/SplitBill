package com.example.splitbill.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.splitbill.data.model.Expense
import com.example.splitbill.domain.Money
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    fun exportToCsv(context: Context, groupName: String, expenses: List<Expense>, getMemberName: (String) -> String): Uri? {
        try {
            val fileName = "Expenses_${groupName.replace(" ", "_")}_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)

            val writer = file.bufferedWriter()
            writer.write("Date,Description,Category,Payer Name,Amount,Split Type\n")
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            for (expense in expenses) {
                val dateStr = dateFormat.format(Date(expense.timestampMillis))
                val description = expense.description.replace(",", " ") // simple escape
                val category = expense.category.replace(",", " ")
                val payerName = getMemberName(expense.paidBy).replace(",", " ")
                val amountStr = Money.toRupees(expense.amountPaise).toString()
                val splitType = expense.splitType.label
                
                writer.write("$dateStr,$description,$category,$payerName,$amountStr,$splitType\n")
            }
            writer.close()

            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
