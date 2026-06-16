package ui.game

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import com.caverock.androidsvg.SVG
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getDrawableResourceBytes
import org.jetbrains.compose.resources.rememberResourceEnvironment
import androidx.compose.ui.graphics.asImageBitmap

@OptIn(ExperimentalResourceApi::class)
@Composable
fun rememberSvgPainter(resource: DrawableResource, sizePx: Int = 128): Painter {
    val environment = rememberResourceEnvironment()
    val context = LocalContext.current

    val painter by produceState<Painter?>(null, resource) {
        val bytes = getDrawableResourceBytes(environment, resource)
        val svg = SVG.getFromString(String(bytes))
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        svg.renderToCanvas(canvas)
        value = BitmapPainter(bitmap.asImageBitmap())
    }

    return painter ?: ColorPainter(
        Color.Transparent
    )
}