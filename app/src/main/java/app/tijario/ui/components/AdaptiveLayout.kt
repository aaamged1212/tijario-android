package app.tijario.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AdaptiveWidthClass {
    ExtraCompact,
    Compact,
    Standard,
    Wide,
}

data class AdaptiveLayoutInfo(
    val widthClass: AdaptiveWidthClass,
    val pageHorizontalPadding: Dp,
    val cardPadding: Dp,
    val sectionSpacing: Dp,
    val cardSpacing: Dp,
    val titleFontSize: TextUnit,
    val bodyFontSize: TextUnit,
    val iconSize: Dp,
) {
    val isExtraCompact: Boolean get() = widthClass == AdaptiveWidthClass.ExtraCompact
    val isCompact: Boolean get() = widthClass <= AdaptiveWidthClass.Compact
    val shouldStackActions: Boolean get() = isCompact
    val showInactiveBottomLabels: Boolean get() = !isExtraCompact
}

fun adaptiveLayoutInfo(width: Dp): AdaptiveLayoutInfo = when {
    width < 300.dp -> AdaptiveLayoutInfo(
        AdaptiveWidthClass.ExtraCompact,
        10.dp,
        12.dp,
        12.dp,
        8.dp,
        18.sp,
        13.sp,
        24.dp,
    )
    width < 360.dp -> AdaptiveLayoutInfo(
        AdaptiveWidthClass.Compact,
        12.dp,
        14.dp,
        14.dp,
        10.dp,
        19.sp,
        14.sp,
        26.dp,
    )
    width < 480.dp -> AdaptiveLayoutInfo(
        AdaptiveWidthClass.Standard,
        20.dp,
        16.dp,
        20.dp,
        12.dp,
        22.sp,
        14.sp,
        28.dp,
    )
    else -> AdaptiveLayoutInfo(
        AdaptiveWidthClass.Wide,
        24.dp,
        20.dp,
        24.dp,
        16.dp,
        24.sp,
        15.sp,
        30.dp,
    )
}

val LocalAdaptiveLayoutInfo = staticCompositionLocalOf { adaptiveLayoutInfo(360.dp) }

@Composable
fun ProvideAdaptiveLayout(content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalAdaptiveLayoutInfo provides adaptiveLayoutInfo(maxWidth)) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
