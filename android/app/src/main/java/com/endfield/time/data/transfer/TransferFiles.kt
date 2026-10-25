package com.endfield.time.data.transfer

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * 通过 SAF（Storage Access Framework）读写导出文件。
 * 不需要任何存储权限，用户自行选择保存位置。
 */
object TransferFiles {

    /** 单文件上限：32 MB（远超正常数据量，用于防御异常文件） */
    private const val MAX_BYTES = 32 * 1024 * 1024

    /** 文件名：优先从 ContentResolver 查询，回退到 Uri 路径 */
    fun displayName(resolver: ContentResolver, uri: Uri): String? {
        runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) return cursor.getString(index)
                }
            }
        }
        return uri.lastPathSegment
    }

    fun isGzipName(name: String?): Boolean =
        name?.lowercase()?.endsWith(".gz") == true

    /** 读取文本；按文件头自动识别 gzip（不依赖扩展名） */
    fun readText(resolver: ContentResolver, uri: Uri): String {
        val stream = resolver.openInputStream(uri)
            ?: throw ExportFormatException("无法打开所选文件")
        stream.use { input ->
            val buffered = readAllBytes(input)
            val isGzip = buffered.size >= 2 &&
                (buffered[0].toInt() and 0xFF) == 0x1F &&
                (buffered[1].toInt() and 0xFF) == 0x8B
            val payload = if (isGzip) {
                GZIPInputStream(buffered.inputStream()).use { it.readBytes() }
            } else {
                buffered
            }
            return String(payload, Charsets.UTF_8)
        }
    }

    /** 写入文本，返回实际写入字节数；gzip=true 时压缩写出 */
    fun writeText(resolver: ContentResolver, uri: Uri, text: String, gzip: Boolean): Long {
        val bytes = text.toByteArray(Charsets.UTF_8)
        val output = resolver.openOutputStream(uri, "wt")
            ?: throw ExportFormatException("无法写入所选位置")
        return output.use { out ->
            if (gzip) {
                val counter = CountingOutputStream(out)
                GZIPOutputStream(counter).use { it.write(bytes) }
                counter.count
            } else {
                out.write(bytes)
                out.flush()
                bytes.size.toLong()
            }
        }
    }

    fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    }

    private fun readAllBytes(input: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream()
        val chunk = ByteArray(16 * 1024)
        var total = 0
        while (true) {
            val read = input.read(chunk)
            if (read <= 0) break
            total += read
            if (total > MAX_BYTES) throw ExportFormatException("文件超过 32 MB，无法导入")
            buffer.write(chunk, 0, read)
        }
        return buffer.toByteArray()
    }

    private class CountingOutputStream(private val delegate: java.io.OutputStream) :
        java.io.OutputStream() {
        var count: Long = 0L
            private set

        override fun write(b: Int) {
            delegate.write(b)
            count++
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            delegate.write(b, off, len)
            count += len
        }

        override fun flush() = delegate.flush()
        override fun close() = delegate.close()
    }
}
