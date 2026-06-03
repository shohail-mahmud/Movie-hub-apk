package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.CompactMedia
import com.example.ui.theme.*

// --- LOGO ---
@Composable
fun MovieHubLogo(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = "Movie",
            color = TextWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(AmberGold)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "Hub",
                color = NearBlack,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- RATING CIRCLE ---
@Composable
fun RatingCircle(
    rating: Double, // 0.0 to 10.0
    size: Dp = 40.dp,
    strokeWidth: Dp = 3.dp,
    modifier: Modifier = Modifier
) {
    val ratingPercent = (rating * 10).toInt() // e.g. 7.5 -> 75%
    val color = when {
        ratingPercent >= 70 -> RatingGreen
        ratingPercent >= 50 -> RatingAmber
        else -> RatingRed
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .background(Color.Black.copy(alpha = 0.82f), CircleShape)
            .padding(strokeWidth)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw background track
            drawCircle(
                color = color.copy(alpha = 0.2f),
                style = Stroke(width = strokeWidth.toPx())
            )
            // Draw active arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = (ratingPercent * 3.6f),
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx())
            )
        }
        Text(
            text = "$ratingPercent%",
            color = TextWhite,
            fontSize = (size.value * 0.32f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// --- SHIMMER BOX SKELETON ---
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val shimmerColors = listOf(
        SurfaceDark,
        SurfaceVariantDark,
        SurfaceDark
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

// --- NO IMAGE PLACEHOLDER ---
@Composable
fun NoImagePlaceholder(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(SurfaceVariantDark, SurfaceDark)
                )
            )
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ImageNotSupported,
                contentDescription = "No Image Available",
                tint = TextGray,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = TextGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- IMAGES BASE UTILS ---
fun getPosterUrl(path: String?): String? = path?.let { "https://image.tmdb.org/t/p/w500$it" }
fun getBackdropUrl(path: String?): String? = path?.let { "https://image.tmdb.org/t/p/w1280$it" }
fun getProfileUrl(path: String?): String? = path?.let { "https://image.tmdb.org/t/p/w185$it" }

// --- MEDIA ROW CARD ---
@Composable
fun MediaRowCard(
    media: CompactMedia,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "click_scale"
    )

    Card(
        modifier = modifier
            .width(130.dp)
            .height(240.dp)
            .scale(scale)
            .testTag("media_card_${media.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                val posterUrl = getPosterUrl(media.poster_path)
                if (posterUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(posterUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = media.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    NoImagePlaceholder(title = media.title, modifier = Modifier.fillMaxSize())
                }

                if (media.vote_average > 0) {
                    RatingCircle(
                        rating = media.vote_average,
                        size = 34.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = media.title,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val year = media.release_date?.take(4) ?: "N/A"
            val typeText = if (media.media_type == "tv") "TV Show" else "Movie"
            Text(
                text = "$year • $typeText",
                color = TextGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// --- MEDIA GRID CARD ---
@Composable
fun MediaGridCard(
    media: CompactMedia,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "grid_click_scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .testTag("media_card_grid_${media.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.67f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                val posterUrl = getPosterUrl(media.poster_path)
                if (posterUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(posterUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = media.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    NoImagePlaceholder(title = media.title, modifier = Modifier.fillMaxSize())
                }

                if (media.vote_average > 0) {
                    RatingCircle(
                        rating = media.vote_average,
                        size = 34.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = media.title,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val year = media.release_date?.take(4) ?: "N/A"
                val typeText = if (media.media_type == "tv") "TV Show" else if (media.media_type == "person") "Actor" else "Movie"
                Text(
                    text = "$typeText • $year",
                    color = TextGray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --- PULSING GREEN ACTIVE SERVER DOT ---
@Composable
fun PulseLiveDot(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val sizeScale by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCirc),
            repeatMode = RepeatMode.Reverse
        ),
        label = "size_pulse"
    )
    val alphaScale by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCirc),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(sizeScale * 1.5f)
                .background(Color(0xFF22C55E).copy(alpha = 0.3f * alphaScale), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(sizeScale)
                .background(Color(0xFF22C55E), CircleShape)
        )
    }
}
