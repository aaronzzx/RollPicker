package com.aaron.rollpicker

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.packInts
import androidx.compose.ui.util.unpackInt1
import androidx.compose.ui.util.unpackInt2
import com.aaron.rollpicker.RollPickerDefaults.LineSpacingMultiplier
import com.aaron.rollpicker.RollPickerDefaults.Loop
import com.aaron.rollpicker.RollPickerDefaults.MaxFlingVelocity
import com.aaron.rollpicker.RollPickerDefaults.Style
import com.aaron.rollpicker.RollPickerDefaults.VisibleCount
import com.aaron.rollpicker.RollPickerDefaults.getDefaultSize
import com.aaron.rollpicker.RollPickerDefaults.paintOnPick
import com.aaron.rollpicker.RollPickerStyle.Flat
import com.aaron.rollpicker.RollPickerStyle.Wheel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sin

/**
 * 垂直滚动的选择器
 *
 * @param count 个数
 * @param onPick 选中回调
 * @param state 选择器状态
 * @param style 选择器风格
 * @param visibleCount 可见数量
 * @param lineSpacingMultiplier 行间距
 * @param absMaxVelocity 最大加速度
 * @param loop 循环滚动
 * @param onItemClick 点击 item 回调
 * @param onDrawWithCache Modifier.drawWithCache 回调，提供 [RollPickerDrawScope]
 * @param onFling 惯性滑动回调
 * @param itemContent item 内容回调
 */
@Composable
fun VerticalRollPicker(
    count: Int,
    onPick: (index: RollPickerIndex) -> Unit,
    modifier: Modifier = Modifier,
    state: RollPickerState = rememberRollPickerState(),
    style: RollPickerStyle = Style,
    visibleCount: Int = VisibleCount,
    lineSpacingMultiplier: Float = LineSpacingMultiplier,
    absMaxVelocity: Float = MaxFlingVelocity,
    loop: Boolean = Loop,
    onItemClick: ((index: RollPickerIndex) -> Unit)? = null,
    onDrawWithCache: (RollPickerDrawScope.() -> DrawResult)? = paintOnPick(),
    onFling: (suspend RollPickerScope.(available: Velocity) -> Velocity)? = null,
    itemContent: @Composable RollPickerScope.(index: RollPickerIndex) -> Unit
) {
    BoxWithConstraints(modifier = modifier.size(getDefaultSize(style, visibleCount))) {
        if (maxWidth <= 0.dp || maxHeight <= 0.dp) return@BoxWithConstraints

        RollPicker(
            count = count,
            onPick = onPick,
            orientation = Orientation.Vertical,
            diameter = maxHeight,
            state = state,
            style = style,
            visibleCount = visibleCount,
            lineSpacingMultiplier = lineSpacingMultiplier,
            absMaxVelocity = absMaxVelocity,
            loop = loop,
            onItemClick = onItemClick,
            onDrawWithCache = onDrawWithCache,
            onFling = onFling,
            itemContent = itemContent
        )
    }
}

/**
 * 水平滚动的选择器
 *
 * @param count 个数
 * @param onPick 选中回调
 * @param state 选择器状态
 * @param style 选择器风格
 * @param visibleCount 可见数量
 * @param lineSpacingMultiplier 行间距
 * @param absMaxVelocity 最大加速度
 * @param loop 循环滚动
 * @param onItemClick 点击 item 回调
 * @param onDrawWithCache Modifier.drawWithCache 回调，提供 [RollPickerDrawScope]
 * @param onFling 惯性滑动回调
 * @param itemContent item 内容回调
 */
@Composable
fun HorizontalRollPicker(
    count: Int,
    onPick: (index: RollPickerIndex) -> Unit,
    modifier: Modifier = Modifier,
    state: RollPickerState = rememberRollPickerState(),
    style: RollPickerStyle = Style,
    visibleCount: Int = VisibleCount,
    lineSpacingMultiplier: Float = LineSpacingMultiplier,
    absMaxVelocity: Float = MaxFlingVelocity,
    loop: Boolean = Loop,
    onItemClick: ((index: RollPickerIndex) -> Unit)? = null,
    onDrawWithCache: (RollPickerDrawScope.() -> DrawResult)? = paintOnPick(),
    onFling: (suspend RollPickerScope.(available: Velocity) -> Velocity)? = null,
    itemContent: @Composable RollPickerScope.(index: RollPickerIndex) -> Unit
) {
    BoxWithConstraints(modifier = modifier.size(getDefaultSize(style, visibleCount))) {
        if (maxWidth <= 0.dp || maxHeight <= 0.dp) return@BoxWithConstraints

        RollPicker(
            count = count,
            onPick = onPick,
            orientation = Orientation.Horizontal,
            diameter = maxWidth,
            state = state,
            style = style,
            visibleCount = visibleCount,
            lineSpacingMultiplier = lineSpacingMultiplier,
            absMaxVelocity = absMaxVelocity,
            loop = loop,
            onItemClick = onItemClick,
            onDrawWithCache = onDrawWithCache,
            onFling = onFling,
            itemContent = itemContent
        )
    }
}

@Composable
private fun RollPicker(
    count: Int,
    onPick: (index: RollPickerIndex) -> Unit,
    orientation: Orientation,
    diameter: Dp,
    state: RollPickerState,
    style: RollPickerStyle,
    visibleCount: Int,
    lineSpacingMultiplier: Float,
    absMaxVelocity: Float,
    loop: Boolean,
    onItemClick: ((index: RollPickerIndex) -> Unit)?,
    onDrawWithCache: (RollPickerDrawScope.() -> DrawResult)?,
    onFling: (suspend RollPickerScope.(available: Velocity) -> Velocity)?,
    itemContent: @Composable RollPickerScope.(index: RollPickerIndex) -> Unit
) {
    if (count <= 0) return

    check(visibleCount > 0 && visibleCount and 1 != 0) {
        "visibleCount must be greater than 0 and must be odd number"
    }

    val actualVisibleCount = when (style) {
        is Wheel -> visibleCount + 2
        is Flat -> if (style.halfExposed) visibleCount + 1 else visibleCount
    }
    val itemSizeAnchor = when (style) {
        is Wheel -> diameter / (actualVisibleCount - 1)
        is Flat -> diameter / actualVisibleCount
    }
    val itemSize = when (style) {
        is Wheel -> itemSizeAnchor * 1.5f / lineSpacingMultiplier
        is Flat -> itemSizeAnchor / lineSpacingMultiplier
    }
    val padding = (diameter - itemSizeAnchor) / 2

    val density = LocalDensity.current
    val rollPickerScope = rememberUpdatedRollPickerScope(
        state = state,
        orientation = orientation,
        count = count,
        visibleCount = visibleCount,
        diameter = diameter,
        itemSizeAnchor = itemSizeAnchor,
        itemSize = itemSize,
        itemFontSize = density.run { itemSize.toSp() * 0.73f }
    )

    val virtualCount = if (loop) Int.MAX_VALUE else count
    val startIndexOffset = if (loop) virtualCount / 2 else 0
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = startIndexOffset + state.initialIndex
    )
    state.also {
        it.listState = listState
        it.loop = loop
        it.startIndexOffset = startIndexOffset
        it.actualCount = count
    }

    val curOnPick by rememberUpdatedState(newValue = onPick)
    LaunchedEffect(key1 = state) {
        snapshotFlow { state.isScrollInProgress }
            .drop(1)
            .filter { !it && state.currentIndexOffsetFraction == 0f }
            .map { state.currentIndex }
            .distinctUntilChanged()
            .collect { index ->
                curOnPick(index)
            }
    }

    val coroutineScope = rememberCoroutineScope()
    val internalOnItemClick: (Int) -> Unit = { index ->
        val rollPickerIndex = RollPickerIndex(state.mapIndex(index), index)
        onItemClick?.invoke(rollPickerIndex)
            ?: coroutineScope.launch {
                state.animateScrollToIndex(rollPickerIndex)
            }
    }

    val curOnFling by rememberUpdatedState(newValue = onFling)
    val curOrientation by rememberUpdatedState(newValue = orientation)
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val nestedScrollConnection = remember(rollPickerScope, state, coroutineScope) {
        val velocityRange = -absMaxVelocity..absMaxVelocity
        object : NestedScrollConnection {
            override suspend fun onPreFling(available: Velocity): Velocity {
                val internalOnFling = curOnFling
                if (internalOnFling != null) {
                    return rollPickerScope.internalOnFling(available)
                }

                var lastValue = 0f
                AnimationState(
                    initialValue = 0f,
                    initialVelocity = when (curOrientation) {
                        Orientation.Vertical -> available.y.coerceIn(velocityRange)
                        else -> available.x.coerceIn(velocityRange)
                    }
                ).animateDecay(animationSpec = decayAnimationSpec) {
                    coroutineScope.launch {
                        val delta = value - lastValue
                        state.scrollBy(-delta)
                        lastValue = value
                    }
                }
                // snap
                state.animateScrollToIndex(index = state.currentIndex)
                return available
            }
        }
    }

    val pickerBox: @Composable (index: Int) -> Unit = { index ->
        val rollPickerIndex = RollPickerIndex(state.mapIndex(index), index)
        // 第一个 Box 固定位置，确保 snap 正常
        Box(
            modifier = Modifier
                .run {
                    if (orientation == Orientation.Vertical) {
                        this
                            .fillMaxWidth()
                            .height(itemSizeAnchor)
                    } else {
                        this
                            .fillMaxHeight()
                            .width(itemSizeAnchor)
                    }
                }
                .run {
                    if (style !is Wheel) this else {
                        wheelTransformation(
                            state = state,
                            index = rollPickerIndex,
                            orientation = orientation,
                            diameter = diameter,
                            visibleCount = visibleCount,
                            itemSizeAnchor = itemSizeAnchor
                        )
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    internalOnItemClick(index)
                },
            contentAlignment = Alignment.Center
        ) {
            // 第二个 Box 实现 lineSpacingMultiplier 效果
            Box(
                modifier = Modifier
                    .run {
                        if (orientation == Orientation.Vertical) {
                            this
                                .fillMaxWidth()
                                .requiredHeight(itemSize)
                        } else {
                            this
                                .fillMaxHeight()
                                .requiredWidth(itemSize)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                rollPickerScope.itemContent(rollPickerIndex)
            }
        }
    }

    val rollPickerDrawScope = rollPickerScope.rememberRollPickerDrawScope()
    val drawWithCacheModifier = if (onDrawWithCache == null) Modifier else {
        Modifier
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithCache {
                rollPickerDrawScope.cacheDrawScope = this
                rollPickerDrawScope.onDrawWithCache()
            }
    }
    if (orientation == Orientation.Vertical) {
        LazyColumn(
            modifier = Modifier
                .then(drawWithCacheModifier)
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            state = listState,
            contentPadding = PaddingValues(vertical = padding)
        ) {
            items(virtualCount) { index ->
                pickerBox(index)
            }
        }
    } else {
        LazyRow(
            modifier = Modifier
                .then(drawWithCacheModifier)
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            state = listState,
            contentPadding = PaddingValues(horizontal = padding)
        ) {
            items(virtualCount) { index ->
                pickerBox(index)
            }
        }
    }
}

private fun Modifier.wheelTransformation(
    state: RollPickerState,
    index: RollPickerIndex,
    orientation: Orientation,
    diameter: Dp,
    visibleCount: Int,
    itemSizeAnchor: Dp
) = graphicsLayer {
    val offsetFraction = state.calculateOffsetFraction(index)
    val actualVisibleCount = visibleCount + 2
    val actualSideVisibleCount = (actualVisibleCount - 1) / 2
    val degree = offsetFraction * 90f / actualSideVisibleCount
    val safeDegree = degree.coerceIn(-90f, 90f)

    val scaleFactor = 1f - (safeDegree.absoluteValue / 90f)

    val radius = diameter.toPx() / 2f
    val radian = Math
        .toRadians(degree.toDouble())
        .toFloat()
    val arcLength = sin(radian) * radius

    // 所有子项偏移到原点（选中区域）
    val initialTrans = itemSizeAnchor.toPx() * offsetFraction
    val transOffset = initialTrans - arcLength

    if (orientation == Orientation.Vertical) {
        scaleY = scaleFactor
        translationY = transOffset
    } else {
        scaleX = scaleFactor
        translationX = transOffset
    }
}

/**
 * 自适应子项空间，自动缩放字体大小
 *
 * @param fontSizeRange 字体大小范围
 * @param fitCenter 是否忽略基线位置偏移到中间
 */
@Composable
fun RollPickerScope.RollPickerText(
    text: String,
    modifier: Modifier = Modifier,
    fontSizeRange: FontSizeRange = FontSizeRange(
        min = 8.sp,
        max = run {
            val itemFontSize = itemFontSize
            if (itemFontSize < 8.sp) 8.sp else itemFontSize
        }
    ),
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    fitCenter: Boolean = true,
    style: TextStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
) {
    AutoResizeText(
        text = text,
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(),
        fontSizeRange = fontSizeRange,
        maxLines = maxLines,
        overflow = overflow,
        softWrap = softWrap,
        fitCenter = fitCenter,
        style = style
    )
}

/**
 * 选择器作用域，提供参数
 */
@Stable
interface RollPickerScope {

    /**
     * 滚轮方向
     */
    val orientation: Orientation

    /**
     * 数据容量
     */
    val count: Int

    /**
     * 当前真实索引
     */
    val currentIndex: RollPickerIndex

    /**
     * 当前索引偏移值
     */
    val currentIndexOffsetFraction: Float

    /**
     * 可见子项数量
     */
    val visibleCount: Int

    /**
     * 直径，[VerticalRollPicker] 为高度，[HorizontalRollPicker] 为宽度
     */
    val diameter: Dp

    /**
     * 锚点子项大小，用来固定子项位置，例如滚轮的形变基于这个大小
     */
    val itemSizeAnchor: Dp

    /**
     * 子项大小
     */
    val itemSize: Dp

    /**
     * 子项字体大小
     */
    val itemFontSize: TextUnit

    /**
     * 计算给定索引偏移值
     */
    fun calculateOffsetFraction(index: RollPickerIndex): Float
}

private class RollPickerScopeImpl(
    state: RollPickerState
) : RollPickerScope {

    var state: RollPickerState by mutableStateOf(state)

    override var orientation: Orientation by mutableStateOf(Orientation.Vertical)

    override var count: Int by mutableStateOf(0)

    override val currentIndex: RollPickerIndex
        get() = state.currentIndex

    override val currentIndexOffsetFraction: Float
        get() = state.currentIndexOffsetFraction

    override var visibleCount: Int by mutableStateOf(0)

    override var diameter: Dp by mutableStateOf(0.dp)

    override var itemSizeAnchor: Dp by mutableStateOf(0.dp)

    override var itemSize: Dp by mutableStateOf(0.dp)

    override var itemFontSize: TextUnit by mutableStateOf(0.sp)

    override fun calculateOffsetFraction(index: RollPickerIndex): Float {
        return state.calculateOffsetFraction(index)
    }
}

@Composable
private fun rememberUpdatedRollPickerScope(
    state: RollPickerState,
    orientation: Orientation,
    count: Int,
    visibleCount: Int,
    diameter: Dp,
    itemSizeAnchor: Dp,
    itemSize: Dp,
    itemFontSize: TextUnit
): RollPickerScope = remember {
    RollPickerScopeImpl(state)
}.also {
    it.state = state
    it.orientation = orientation
    it.count = count
    it.visibleCount = visibleCount
    it.diameter = diameter
    it.itemSizeAnchor = itemSizeAnchor
    it.itemSize = itemSize
    it.itemFontSize = itemFontSize
}

/**
 * 绘制作用域，提供参数
 */
@Stable
interface RollPickerDrawScope : RollPickerScope, Density {

    val size: Size

    val layoutDirection: LayoutDirection

    fun onDrawBehind(block: DrawScope.() -> Unit): DrawResult

    fun onDrawWithContent(block: ContentDrawScope.() -> Unit): DrawResult

    override val density: Float

    override val fontScale: Float
}

private class RollPickerDrawScopeImpl(
    rollPickerScope: RollPickerScope,
    density: Density
) : RollPickerDrawScope,
    RollPickerScope by rollPickerScope,
    Density by density {

    var cacheDrawScope: CacheDrawScope? by mutableStateOf(null)

    override val size: Size
        get() = safeCall { size }

    override val layoutDirection: LayoutDirection
        get() = safeCall { layoutDirection }

    override val density: Float
        get() = safeCall { density }

    override val fontScale: Float
        get() = safeCall { fontScale }

    override fun onDrawBehind(block: DrawScope.() -> Unit): DrawResult {
        return safeCall { onDrawBehind(block) }
    }

    override fun onDrawWithContent(block: ContentDrawScope.() -> Unit): DrawResult {
        return safeCall { onDrawWithContent(block) }
    }

    private inline fun <T> safeCall(block: CacheDrawScope.() -> T): T {
        val cacheDrawScope = cacheDrawScope ?: error("RollPickerDrawScope is missing CacheDrawScope")
        return cacheDrawScope.block()
    }
}

@Composable
private fun RollPickerScope.rememberRollPickerDrawScope(): RollPickerDrawScopeImpl {
    val density = LocalDensity.current
    return remember(this, density) {
        RollPickerDrawScopeImpl(this, density)
    }
}

/**
 * 选择器状态容器
 */
@Composable
fun rememberRollPickerState(initialIndex: Int = 0): RollPickerState {
    return remember {
        RollPickerState(initialIndex)
    }
}

@Stable
class RollPickerState(val initialIndex: Int = 0) {

    internal var listState: LazyListState? by mutableStateOf(null)

    internal var loop: Boolean by mutableStateOf(false)

    internal var actualCount: Int by mutableStateOf(0)

    internal var startIndexOffset: Int by mutableStateOf(0)

    /**
     * 当前索引
     */
    val currentIndex: RollPickerIndex by derivedStateOf {
        val foundIndex = listState
            ?.layoutInfo
            ?.visibleItemsInfo
            ?.fastFirstOrNull {
                it.offset.absoluteValue <= it.size / 2f
            }
            ?.index
            ?: -1
        RollPickerIndex(mapIndex(foundIndex), foundIndex)
    }

    /**
     * 当前索引偏移值
     */
    val currentIndexOffsetFraction: Float by derivedStateOf {
        val curItem = listState
            ?.layoutInfo
            ?.visibleItemsInfo
            ?.fastFirstOrNull {
                it.index == currentIndex.actualValue
            }
            ?: return@derivedStateOf 0f
        -curItem.offset / curItem.size.toFloat()
    }

    /**
     * 是否在滚动中
     */
    val isScrollInProgress: Boolean by derivedStateOf {
        listState?.isScrollInProgress ?: false
    }

    /**
     * 能否向前滚动，结束方向
     */
    val canScrollForward: Boolean
        get() = listState?.canScrollForward ?: false

    /**
     * 能否向后滚动，开始方向
     */
    val canScrollBackward: Boolean
        get() = listState?.canScrollBackward ?: false

    /**
     * 计算给定索引偏移值
     */
    fun calculateOffsetFraction(index: RollPickerIndex): Float {
        return (currentIndex.actualValue - index.actualValue) + currentIndexOffsetFraction
    }

    /**
     * 按像素滚动
     */
    suspend fun scrollBy(value: Float) {
        listState?.scrollBy(value)
    }

    /**
     * 立即滚动到给定索引
     */
    suspend fun scrollToIndex(index: RollPickerIndex) {
        animateScrollToIndex(
            index = index,
            animationSpec = snap()
        )
    }

    /**
     * 动画形式滚动到给定索引
     */
    suspend fun animateScrollToIndex(
        index: RollPickerIndex,
        animationSpec: AnimationSpec<Float> = spring(
            stiffness = Spring.StiffnessMediumLow
        )
    ) {
        val listState = listState ?: return
        val size = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: return
        val fraction = calculateOffsetFraction(index)
        val scrollOffset = size * fraction
        listState.animateScrollBy(
            value = -scrollOffset,
            animationSpec = animationSpec
        )
    }

    /**
     * 转换索引，在开启循环时真实索引过大，需要进行转换才能对应数据
     */
    internal fun mapIndex(index: Int): Int {
        if (!loop) return index
        return floorMod(index - startIndexOffset, actualCount)
    }

    private fun floorMod(value: Int, other: Int): Int = when (other) {
        0 -> value
        else -> value - value.floorDiv(other) * other
    }
}

/**
 * 选择器风格
 */
@Stable
sealed class RollPickerStyle {

    /**
     * 滚轮
     */
    class Wheel : RollPickerStyle() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    /**
     * 扁平
     *
     * @param halfExposed 是否露出一半消失的 item
     */
    data class Flat(val halfExposed: Boolean = false) : RollPickerStyle()
}

fun RollPickerIndex(value: Int) = RollPickerIndex(value, -1)

private fun RollPickerIndex(index: Int, actualIndex: Int) = RollPickerIndex(packInts(index, actualIndex))

@JvmInline
value class RollPickerIndex internal constructor(private val packedValue: Long) {

    val value: Int get() = unpackInt1(packedValue)

    /**
     * 如果有真实索引存在则用真实索引，否则用 [value]
     */
    internal val actualValue: Int
        get() {
            val index = unpackInt2(packedValue)
            return if (index == -1) value else index
        }

    operator fun plus(other: RollPickerIndex): RollPickerIndex {
        val offset = abs(value - other.value)
        return this + offset
    }

    operator fun plus(offset: Int): RollPickerIndex {
        return RollPickerIndex(
            index = value + offset,
            actualIndex = actualValue + offset
        )
    }

    operator fun minus(other: RollPickerIndex): RollPickerIndex {
        val offset = abs(value - other.value)
        return this - offset
    }

    operator fun minus(offset: Int): RollPickerIndex {
        return RollPickerIndex(
            index = value - offset,
            actualIndex = actualValue - offset
        )
    }
}

object RollPickerDefaults {

    const val VisibleCount = 5
    const val LineSpacingMultiplier = 1f
    const val Loop = false
    const val MaxFlingVelocity = 3000f
    val Style = Wheel()

    private val ItemSize = 25.dp

    /**
     * 选中区域着色、形变
     */
    @Composable
    fun paintOnPick(
        color: Color = MaterialTheme.colorScheme.primary,
        scaleX: Float = 1.0f,
        scaleY: Float = 1.0f
    ): RollPickerDrawScope.() -> DrawResult {
        val block: RollPickerDrawScope.() -> DrawResult = {
            paintOnPick(color, scaleX, scaleY)
        }
        return block
    }

    private fun RollPickerDrawScope.paintOnPick(
        color: Color,
        scaleX: Float,
        scaleY: Float
    ): DrawResult {
        val path = Path().apply {
            val itemSizePx = itemSize.toPx()
            if (orientation == Orientation.Vertical) {
                val top = (size.height - itemSizePx) / 2f
                addRect(
                    Rect(
                        left = 0f,
                        top = top,
                        right = size.width,
                        bottom = top + itemSizePx
                    )
                )
            } else {
                val left = (size.width - itemSizePx) / 2f
                addRect(
                    Rect(
                        left = left,
                        top = 0f,
                        right = left + itemSizePx,
                        bottom = size.height
                    )
                )
            }
        }
        return onDrawWithContent {
            clipPath(path, clipOp = ClipOp.Difference) {
                this@onDrawWithContent.drawContent()
            }
            clipPath(path) {
                scale(scaleX = scaleX, scaleY = scaleY) {
                    this@onDrawWithContent.drawContent()
                    if (color.isSpecified) {
                        drawRect(
                            color = color,
                            blendMode = BlendMode.SrcIn
                        )
                    }
                }
            }
        }
    }

    internal fun getDefaultSize(style: RollPickerStyle, visibleCount: Int): Dp {
        val defaultItemSize = ItemSize
        if (style is Wheel) {
            return defaultItemSize * (visibleCount + 1)
        } else if (style is Flat && style.halfExposed) {
            return defaultItemSize * (visibleCount + 1)
        }
        return defaultItemSize * visibleCount
    }
}