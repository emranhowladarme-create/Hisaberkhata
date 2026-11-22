package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter

data class ParsedHisabQr(
    val businessName: String = "",
    val phone: String = "",
    val amount: Double = 0.0,
    val customerName: String = "",
    val rawPayload: String = "",
)

object QrHelper {
    fun buildPaymentPayload(
        businessName: String,
        phone: String,
        amount: Double,
        customerName: String = "",
    ): String {
        val cleanBiz = businessName.replace("|", " ").trim()
        val cleanPhone = phone.replace("|", "").trim()
        val cleanCust = customerName.replace("|", " ").trim()
        return "HISAB_PAY|biz=$cleanBiz|phone=$cleanPhone|amt=${amount.toLong()}|cust=$cleanCust"
    }

    fun parsePayload(raw: String): ParsedHisabQr {
        if (!raw.startsWith("HISAB_PAY|")) {
            return ParsedHisabQr(rawPayload = raw)
        }
        val parts = raw.split("|").drop(1)
        var biz = ""
        var phone = ""
        var amt = 0.0
        var cust = ""
        for (p in parts) {
            val idx = p.indexOf('=')
            if (idx <= 0) continue
            val k = p.substring(0, idx)
            val v = p.substring(idx + 1)
            when (k) {
                "biz" -> biz = v
                "phone" -> phone = v
                "amt" -> amt = v.toDoubleOrNull() ?: 0.0
                "cust" -> cust = v
            }
        }
        return ParsedHisabQr(
            businessName = biz,
            phone = phone,
            amount = amt,
            customerName = cust,
            rawPayload = raw,
        )
    }

    fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1,
            )
            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            for (x in 0 until sizePx) {
                for (y in 0 until sizePx) {
                    bmp.setPixel(x, y, if (matrix[x, y]) Color.parseColor("#1A1C1E") else Color.WHITE)
                }
            }
            bmp
        } catch (_: Exception) {
            null
        }
    }

    fun decodeFromBitmap(bitmap: Bitmap): String? {
        return try {
            val w = bitmap.width
            val h = bitmap.height
            val pixels = IntArray(w * h)
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
            val source = RGBLuminanceSource(w, h, pixels)
            val binary = BinaryBitmap(HybridBinarizer(source))
            MultiFormatReader().decode(binary).text
        } catch (_: Exception) {
            null
        }
    }
}
