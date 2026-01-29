//package br.com.bipay.webapi.login
//
//import com.google.zxing.BarcodeFormat
//import com.google.zxing.client.j2se.MatrixToImageWriter
//import com.google.zxing.common.BitMatrix
//import com.google.zxing.qrcode.QRCodeWriter
//import org.springframework.stereotype.Service
//import java.io.ByteArrayOutputStream
//import java.time.Instant
//import java.util.*
//import java.util.concurrent.ConcurrentHashMap
//
//@Service
//class QrService {
//    private val storage = ConcurrentHashMap<String, Pair<String, Long>>() // token -> (username, expiryEpochMs)
//    private val ttlMs = 2 * 60 * 1000L // 2 minutos
//
//    fun createQrFor(username: String): String {
//        val token = UUID.randomUUID().toString()
//        storage[token] = Pair(username, Instant.now().toEpochMilli() + ttlMs)
//        val content = token // QR codifica apenas o token (pode ser uma URL)
//        val writer = QRCodeWriter()
//        val matrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300)
//        val baos = ByteArrayOutputStream()
//        MatrixToImageWriter.writeToStream(matrix, "PNG", baos)
//        return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray())
//    }
//
//    fun consumeToken(token: String): String? {
//        val entry = storage.remove(token) ?: return null
//        val (username, expiry) = entry
//        return if (Instant.now().toEpochMilli() <= expiry) username else null
//    }
//}