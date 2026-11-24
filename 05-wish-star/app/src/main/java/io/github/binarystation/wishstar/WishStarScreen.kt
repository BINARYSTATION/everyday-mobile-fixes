package io.github.binarystation.wishstar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Test tags, shared with the instrumented tests. */
object WishTags {
    const val BUTTON = "wish-button"
    const val LABEL = "wish-label"
}

/** Share of the screen height that the drawn sky takes. */
private const val SKY_HEIGHT = 0.66f

/**
 * PolygonShape.star puts its first tip at 3 o'clock. Turning it 126 degrees clockwise
 * (54 + one 72 degree step) while it morphs makes it land with a tip at 12 o'clock.
 */
private const val STAR_TURN_DEGREES = 126f

/**
 * One screen: a night sky with a progressive blur and a circle that morphs into a star.
 *
 * @param onComposed runs after every composition of this screen. Tests use it to count
 *   recompositions; the app leaves it empty.
 */
@Composable
fun WishStarScreen(
    modifier: Modifier = Modifier,
    onComposed: () -> Unit = {},
) {
    SideEffect { onComposed() }
    var wished by rememberSaveable { mutableStateOf(false) }
    // The State object is passed on, and .value is only read inside draw lambdas.
    val wish = animateFloatAsState(
        targetValue = if (wished) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "wish",
    )
    val star = remember { starShape { wish.value } }

    Box(modifier.fillMaxSize().background(WishColors.Night)) {
        NightSky(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(SKY_HEIGHT)
                .skyBlur { wish.value },
        )
        Column(
            Modifier
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Text(
                text = stringResource(R.string.title),
                color = WishColors.Cream,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.subtitle),
                color = WishColors.Muted,
                fontSize = 17.sp,
            )
        }
        WishButton(
            shape = star,
            progress = { wish.value },
            onClick = { wished = !wished },
            modifier = Modifier.align(BiasAlignment(0f, 0.18f)),
        )
        Crossfade(
            targetState = wished,
            modifier = Modifier
                .align(BiasAlignment(0f, 0.64f))
                .testTag(WishTags.LABEL),
            label = "label",
        ) { sent ->
            Text(
                text = stringResource(if (sent) R.string.sent else R.string.hint),
                color = if (sent) WishColors.Gold else WishColors.Muted,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** The tappable shape plus a soft gold glow that fades in with the star. */
@Composable
private fun WishButton(
    shape: Shape,
    progress: () -> Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionLabel = stringResource(R.string.action_wish)
    Box(modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { alpha = progress().coerceIn(0f, 1f) }
                .blur(36.dp, BlurredEdgeTreatment.Unbounded)
                .drawBehind {
                    drawCircle(WishColors.Gold.copy(alpha = 0.6f), radius = 72.dp.toPx())
                },
        )
        Box(
            Modifier
                .size(140.dp)
                .graphicsLayer { rotationZ = progress() * STAR_TURN_DEGREES }
                .clip(shape)
                .drawBehind {
                    val t = progress().coerceIn(0f, 1f)
                    drawRect(lerp(WishColors.Cream, WishColors.Gold, t))
                }
                .clickable(onClickLabel = actionLabel, onClick = onClick)
                .testTag(WishTags.BUTTON),
        )
    }
}
