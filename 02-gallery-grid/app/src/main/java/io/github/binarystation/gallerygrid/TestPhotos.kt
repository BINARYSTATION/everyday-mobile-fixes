package io.github.binarystation.gallerygrid

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import java.io.File
import kotlin.math.sin
import kotlin.random.Random

/**
 * Our own test photos: 12 landscapes drawn with Canvas at 4000 x 3000 (12 MP) and saved as JPEG.
 * Nobody's pictures are used. They are drawn once, on first launch, into the app's private folder.
 */
object TestPhotos {
    const val COUNT = 12
    const val WIDTH = 4000
    const val HEIGHT = 3000

    private class Scene(
        val skyTop: Int,
        val skyBottom: Int,
        val sun: Int,
        val far: Int,
        val near: Int,
        val sunX: Float,
        val sunY: Float,
    )

    private val scenes = listOf(
        Scene(0xFF2B3A67.toInt(), 0xFFFF9A62.toInt(), 0xFFFFE08A.toInt(), 0xFF7B5E8F.toInt(), 0xFF2E2A4A.toInt(), 0.70f, 0.46f), // sunrise
        Scene(0xFFF7B267.toInt(), 0xFFFFE5B4.toInt(), 0xFFFFFAE0.toInt(), 0xFFD98B4A.toInt(), 0xFF9C5B2E.toInt(), 0.28f, 0.30f), // dunes
        Scene(0xFF4A90D9.toInt(), 0xFFCFE8FF.toInt(), 0xFFFFF3C4.toInt(), 0xFF8FA9C4.toInt(), 0xFF3E5C7A.toInt(), 0.78f, 0.22f), // alps
        Scene(0xFF00A8A8.toInt(), 0xFFB8F2E6.toInt(), 0xFFFFD166.toInt(), 0xFF2A9D8F.toInt(), 0xFF1B5E5A.toInt(), 0.22f, 0.26f), // lagoon
        Scene(0xFF3D2C8D.toInt(), 0xFFF08CAE.toInt(), 0xFFFFD6A5.toInt(), 0xFF6C3A7C.toInt(), 0xFF2C1E4A.toInt(), 0.50f, 0.50f), // dusk
        Scene(0xFF05122B.toInt(), 0xFF2BD99F.toInt(), 0xFFEAF2FF.toInt(), 0xFF1B3A4B.toInt(), 0xFF0B1F2A.toInt(), 0.80f, 0.18f), // aurora
        Scene(0xFF7FD1FF.toInt(), 0xFFE8F8C8.toInt(), 0xFFFFF176.toInt(), 0xFF8BC34A.toInt(), 0xFF3E8E41.toInt(), 0.30f, 0.24f), // meadow
        Scene(0xFFFF7E5F.toInt(), 0xFFFEB47B.toInt(), 0xFFFFF0B3.toInt(), 0xFFC0522C.toInt(), 0xFF7A2E1C.toInt(), 0.66f, 0.36f), // canyon
        Scene(0xFF2C3E50.toInt(), 0xFFFD746C.toInt(), 0xFFFFE9A8.toInt(), 0xFF34495E.toInt(), 0xFF1B2631.toInt(), 0.40f, 0.48f), // harbor
        Scene(0xFF89CFF0.toInt(), 0xFFF0FBFF.toInt(), 0xFFFFFFFF.toInt(), 0xFFB5D8EC.toInt(), 0xFF6FA8C9.toInt(), 0.18f, 0.20f), // glacier
        Scene(0xFF0B0B2B.toInt(), 0xFF3A2E6B.toInt(), 0xFFFFF4C2.toInt(), 0xFF28235A.toInt(), 0xFF110F30.toInt(), 0.62f, 0.22f), // midnight
        Scene(0xFFE9A23B.toInt(), 0xFFFBE3A1.toInt(), 0xFFFFF7D6.toInt(), 0xFFC8553D.toInt(), 0xFF7F2E1F.toInt(), 0.74f, 0.30f), // autumn
    )

    init {
        check(scenes.size == COUNT)
    }

    fun file(dir: File, index: Int): File = File(dir, "photo-%02d.jpg".format(index + 1))

    /** Draws the photos that are missing and returns all of them in order. [onDrawn] gets the number of photos ready so far. */
    fun ensure(dir: File, onDrawn: (Int) -> Unit = {}): List<File> {
        dir.mkdirs()
        return List(COUNT) { index ->
            val target = file(dir, index)
            if (!target.exists()) {
                val temp = File(dir, target.name + ".tmp")
                // One 48 MB canvas at a time, released right after the JPEG is written.
                val bitmap = draw(index)
                temp.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
                bitmap.recycle()
                check(temp.renameTo(target)) { "Could not save ${target.name}" }
            }
            onDrawn(index + 1)
            target
        }
    }

    private fun draw(index: Int): Bitmap {
        val scene = scenes[index]
        val random = Random(index + 1)
        val w = WIDTH.toFloat()
        val h = HEIGHT.toFloat()
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.shader = LinearGradient(0f, 0f, 0f, h * 0.75f, scene.skyTop, scene.skyBottom, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint)

        val sunX = scene.sunX * w
        val sunY = scene.sunY * h
        paint.shader = RadialGradient(sunX, sunY, 900f, withAlpha(scene.sun, 140), withAlpha(scene.sun, 0), Shader.TileMode.CLAMP)
        canvas.drawCircle(sunX, sunY, 900f, paint)
        paint.shader = null
        paint.color = scene.sun
        canvas.drawCircle(sunX, sunY, 300f, paint)

        // Three ridges, far to near, each a few sine waves with a random phase.
        val ridges = listOf(
            Triple(0.60f, scene.far, 0.35f),
            Triple(0.72f, blend(scene.far, scene.near), 0.5f),
            Triple(0.84f, scene.near, 0.65f),
        )
        ridges.forEach { (base, color, roughness) ->
            val phaseA = random.nextFloat() * 6.28f
            val phaseB = random.nextFloat() * 6.28f
            val path = Path().apply {
                moveTo(0f, h)
                var x = 0f
                while (x <= w) {
                    val wave = sin(x / 520f + phaseA) * 150f + sin(x / 170f + phaseB) * 70f * roughness
                    lineTo(x, base * h + wave + random.nextFloat() * 24f * roughness)
                    x += 40f
                }
                lineTo(w, h)
                close()
            }
            paint.color = color
            canvas.drawPath(path, paint)
        }

        // A little grain, so the JPEG is not a perfectly smooth gradient.
        repeat(1500) {
            paint.color = withAlpha(if (random.nextBoolean()) Color.WHITE else Color.BLACK, 10 + random.nextInt(14))
            canvas.drawCircle(random.nextFloat() * w, random.nextFloat() * h, 4f + random.nextFloat() * 12f, paint)
        }
        return bitmap
    }

    private fun withAlpha(color: Int, alpha: Int): Int = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

    private fun blend(a: Int, b: Int): Int = Color.rgb(
        (Color.red(a) + Color.red(b)) / 2,
        (Color.green(a) + Color.green(b)) / 2,
        (Color.blue(a) + Color.blue(b)) / 2,
    )
}
