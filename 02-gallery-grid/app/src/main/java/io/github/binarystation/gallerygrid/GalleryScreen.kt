package io.github.binarystation.gallerygrid

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private const val COLUMNS = 3
private val GridGap = 8.dp

/** The memory bar is full at 600 MB, a little more than 12 photos at 48 MB each. */
private const val BAR_SCALE_BYTES = 600_000_000L

@Composable
fun GalleryScreen(viewModel: GalleryViewModel = viewModel()) {
    val sampled = viewModel.mode == DecodeMode.SAMPLED
    val accent = if (sampled) GalleryColors.Leaf else GalleryColors.Coral
    // Surface also sets the default text color for everything inside it.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(horizontal = 16.dp),
        ) {
            Header(onReload = viewModel::reload)
            ModeCard(sampled = sampled, accent = accent, onChange = viewModel::switchMode)
            MemoryCard(
                totalBytes = viewModel.totalBytes,
                decoded = viewModel.bitmaps.size,
                accent = accent,
            )
            if (viewModel.photoFiles.isEmpty()) {
                DrawingPhotos(viewModel.drawnCount)
            } else {
                PhotoGrid(viewModel, accent)
            }
        }
    }
}

@Composable
private fun Header(onReload: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onReload) {
            Text(stringResource(R.string.action_reload))
        }
    }
}

/** Shows which mode is on and lets you flip it. */
@Composable
private fun ModeCard(sampled: Boolean, accent: Color, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(if (sampled) R.string.mode_after else R.string.mode_before),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            Text(
                text = stringResource(
                    if (sampled) R.string.mode_after_detail else R.string.mode_before_detail,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
            )
        }
        Switch(
            checked = sampled,
            onCheckedChange = onChange,
            modifier = Modifier.testTag("mode-switch"),
        )
    }
}

/** The number this demo is about: the sum of allocationByteCount over every decoded bitmap. */
@Composable
private fun MemoryCard(totalBytes: Long, decoded: Int, accent: Color) {
    val fill by animateFloatAsState(
        targetValue = (totalBytes.toFloat() / BAR_SCALE_BYTES).coerceIn(0f, 1f),
        label = "memory-bar",
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.memory_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatBytes(totalBytes),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = accent,
                    modifier = Modifier.testTag("total"),
                )
            }
            Text(
                text = stringResource(R.string.decoded_count, decoded, TestPhotos.COUNT),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(GalleryColors.Background),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fill)
                    .background(accent),
            )
        }
        Text(
            text = stringResource(R.string.memory_scale),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun DrawingPhotos(drawn: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
    ) {
        Text(stringResource(R.string.drawing_photos, drawn, TestPhotos.COUNT))
        LinearProgressIndicator(
            progress = { drawn / TestPhotos.COUNT.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
    }
}

@Composable
private fun PhotoGrid(viewModel: GalleryViewModel, accent: Color) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // The slot size in pixels. This is also what the AFTER decode asks for.
        val cellPx = with(LocalDensity.current) {
            ((maxWidth - GridGap * (COLUMNS - 1)) / COLUMNS).roundToPx()
        }
        LaunchedEffect(viewModel.photoFiles, viewModel.mode, viewModel.round, cellPx) {
            viewModel.load(cellPx)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(COLUMNS),
            horizontalArrangement = Arrangement.spacedBy(GridGap),
            verticalArrangement = Arrangement.spacedBy(GridGap),
        ) {
            items(count = viewModel.photoFiles.size, key = { it }) { index ->
                PhotoTile(
                    index = index,
                    bitmap = viewModel.bitmaps[index],
                    failed = index in viewModel.failed,
                    accent = accent,
                )
            }
        }
    }
}

@Composable
private fun PhotoTile(index: Int, bitmap: Bitmap?, failed: Boolean, accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .testTag("tile-$index"),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            val image = remember(bitmap) { bitmap.asImageBitmap() }
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Text(
                text = formatBytes(bitmap.allocationByteCount.toLong()),
                color = accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .background(Color(0xCC0A0C10), RoundedCornerShape(8.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
                    .testTag("bytes-$index"),
            )
        } else if (failed) {
            Text(
                text = "Out of memory",
                color = GalleryColors.Coral,
                fontSize = 12.sp,
            )
        }
    }
}
