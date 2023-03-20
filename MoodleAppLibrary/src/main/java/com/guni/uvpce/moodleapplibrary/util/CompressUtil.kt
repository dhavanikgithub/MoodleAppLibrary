package com.guni.uvpce.moodleapplibrary.util
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.*
import java.util.zip.Deflater.BEST_COMPRESSION


///**
//     * Compress a string using GZIP.
//     *
//     * @return an UTF-8 encoded byte array.
//     */
//    fun String.compress(): ByteArray {
//        val bos = ByteArrayOutputStream()
//        GZIPOutputStream(bos).bufferedWriter(Charsets.UTF_8).use { it.write(this) }
//        return bos.toByteArray()
//    }
//
//    /**
//     * Decompress a byte array using GZIP.
//     *
//     * @return an UTF-8 encoded string.
//     */
//    fun ByteArray.uncompress(): String {
//        val bais = ByteArrayInputStream(this)
//        lateinit var string: String
//        GZIPInputStream(bais).bufferedReader(Charsets.UTF_8).use { return it.readText() }
//    }
fun decompressString(compressed: String): String {
    val decoded = Base64.getDecoder().decode(compressed)
    val inflater = Inflater()
    inflater.setInput(decoded)

    val result = ByteArray(decoded.size * 10)
    val length = inflater.inflate(result)
    inflater.end()

    return String(result, 0, length, StandardCharsets.UTF_8)
}

fun compressString(input: String): String {
    val baos = ByteArrayOutputStream()
    val deflater = Deflater(BEST_COMPRESSION)
     //deflater.level = Deflater.BEST_COMPRESSION
    val dos = DeflaterOutputStream(baos, deflater)
    dos.write(input.toByteArray())
    dos.close()
    return Base64.getEncoder().encodeToString(baos.toByteArray())
}