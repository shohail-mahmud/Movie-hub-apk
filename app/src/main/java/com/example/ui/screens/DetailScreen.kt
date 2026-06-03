package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.CompactMedia
import com.example.data.MovieDetail
import com.example.data.TvDetail
import com.example.viewmodel.MovieHubViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: MovieHubViewModel,
    mediaType: String, // "movie" or "tv"
    id: Int,
    onBackClick: () -> Unit,
    onActorClick: (Int) -> Unit,
    onMediaClick: (String, Int) -> Unit,
    onPlayClick: (String, Int, Int, Int) -> Unit, // mediaType, id, season, episode
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLoadingDetail by viewModel.isLoadingDetail.collectAsState()
    val movieDetail by viewModel.movieDetail.collectAsState()
    val tvDetail by viewModel.tvDetail.collectAsState()
    val currentCast by viewModel.currentCast.collectAsState()
    val recommendations by viewModel.currentRecommendations.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()

    val isTv = mediaType == "tv"
    val isSaved = watchlist.any { it.id == id }

    // Season & Episode tracking for TV
    var selectedSeason by remember { mutableStateOf(1) }
    var selectedEpisode by remember { mutableStateOf(1) }

    // Fetch details on load
    LaunchedEffect(id, mediaType) {
        viewModel.loadMediaDetails(id, isTv)
        selectedSeason = 1
        selectedEpisode = 1
    }

    // Reset episode when season changes based on season episode count
    val maxEpisodes = remember(tvDetail, selectedSeason) {
        val seasonInfo = tvDetail?.seasons?.find { it.season_number == selectedSeason }
        seasonInfo?.episode_count ?: 12
    }
    LaunchedEffect(maxEpisodes) {
        if (selectedEpisode > maxEpisodes) {
            selectedEpisode = 1
        }
    }

    if (isLoadingDetail) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(NearBlack)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(260.dp))
            ShimmerBox(modifier = Modifier.width(200.dp).height(24.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(100.dp))
            repeat(2) {
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp))
            }
        }
    } else {
        val title = if (isTv) tvDetail?.name ?: "TV Show Detail" else movieDetail?.title ?: "Movie Detail"
        val overview = if (isTv) tvDetail?.overview else movieDetail?.overview
        val voteAverage = if (isTv) tvDetail?.vote_average ?: 0.0 else movieDetail?.vote_average ?: 0.0
        val backdropPath = if (isTv) tvDetail?.backdrop_path else movieDetail?.backdrop_path
        val posterPath = if (isTv) tvDetail?.poster_path else movieDetail?.poster_path
        val releaseDate = if (isTv) tvDetail?.first_air_date else movieDetail?.release_date
        val genres = if (isTv) tvDetail?.genres else movieDetail?.genres
        val runtimeText = if (isTv) {
            "${tvDetail?.number_of_seasons ?: 1} Seasons"
        } else {
            val runtime = movieDetail?.runtime ?: 0
            if (runtime > 0) "${runtime / 60}h ${runtime % 60}m" else "N/A"
        }

        val videos = if (isTv) tvDetail?.videos else movieDetail?.videos
        val trailerVideo = videos?.results?.find { it.site.lowercase() == "youtube" && it.type.lowercase() == "trailer" }
            ?: videos?.results?.find { it.site.lowercase() == "youtube" }

        val mainScroll = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(NearBlack)
                .verticalScroll(mainScroll)
                .padding(bottom = 60.dp)
        ) {
            // --- BACKDROP HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                val backdropUrl = getBackdropUrl(backdropPath) ?: getPosterUrl(posterPath)
                if (backdropUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(backdropUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Backdrop",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Smooth gradients
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    NearBlack
                                )
                            )
                        )
                )

                // Back Button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
            }

            // --- TITLE & METADATA OVERLAY LAYOUT ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Foreground Poster
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(165.dp)
                        .offset(y = (-40).dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceDark)
                ) {
                    val posterUrl = getPosterUrl(posterPath)
                    if (posterUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(posterUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Poster",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        NoImagePlaceholder(title = title, modifier = Modifier.fillMaxSize())
                    }
                }

                // Meta Info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = title,
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val year = releaseDate?.take(4) ?: "N/A"
                    Text(
                        text = "$year • $runtimeText",
                        color = TextGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vote Average Circle
                    if (voteAverage > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RatingCircle(rating = voteAverage, size = 36.dp, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "User Score",
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- GENRE CHIPS ---
            genres?.let { genreList ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-20).dp)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genreList.forEach { genre ->
                        Box(
                            modifier = Modifier
                                .border(1.dp, SurfaceVariantDark, RoundedCornerShape(12.dp))
                                .background(SurfaceDark, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = genre.name,
                                color = TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // --- ACTION CONTROLS BUTTONS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-10).dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play stream button
                Button(
                    onClick = {
                        onPlayClick(mediaType, id, selectedSeason, selectedEpisode)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Watch Now",
                        tint = NearBlack,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Watch Now",
                        color = NearBlack,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Add / Remove from Watchlist
                IconButton(
                    onClick = {
                        val compact = if (isTv) tvDetail?.toCompactMedia() else movieDetail?.toCompactMedia()
                        compact?.let { viewModel.localStorageManager.toggleWatchlist(it) }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(SurfaceDark, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Watchlist Toggle",
                        tint = if (isSaved) AmberGold else TextWhite
                    )
                }

                // Native Share / Copy URL button
                IconButton(
                    onClick = {
                        val shareUrl = if (isTv) "https://www.moviehub/tv/$id" else "https://www.moviehub/movie/$id"
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Stream $title on MovieHub: $shareUrl")
                        }
                        try {
                            context.startActivity(Intent.createChooser(shareIntent, "Share $title"))
                        } catch (e: Exception) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("share_link", shareUrl)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Link copied!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(SurfaceDark, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = TextWhite
                    )
                }
            }

            // --- TV EPISODES SELECTORS (ONLY FOR TV) ---
            if (isTv && tvDetail != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(SurfaceDark, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Episode Selector",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Season Chips Scroll
                    Text("Season", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(tvDetail?.number_of_seasons ?: 1) { index ->
                            val sNum = index + 1
                            val isSelected = selectedSeason == sNum
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AmberGold else SurfaceVariantDark)
                                    .clickable { selectedSeason = sNum }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "S$sNum",
                                    color = if (isSelected) NearBlack else TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Episode Chips Scroll
                    Text("Episode", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(maxEpisodes) { index ->
                            val eNum = index + 1
                            val isSelected = selectedEpisode == eNum
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AmberGold else SurfaceVariantDark)
                                    .clickable { selectedEpisode = eNum }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "E$eNum",
                                    color = if (isSelected) NearBlack else TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // --- OVERVIEW DESCRIPTION ---
            overview?.let {
                if (it.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Storyline",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = it,
                            color = TextGray,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // --- DETAILED YOUTUBE TRAILER ---
            trailerVideo?.let { trailer ->
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Official Trailer",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.77f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                    ) {
                        // YouTube iframe web view embed
                        val embedUrl = "https://www.youtube.com/embed/${trailer.key}"
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { ctx ->
                                android.webkit.WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    webViewClient = android.webkit.WebViewClient()
                                    loadUrl(embedUrl)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // --- HORIZONTAL CAST ROW ---
            if (currentCast.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Top Billed Cast",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(currentCast.take(15)) { castItem ->
                            Column(
                                modifier = Modifier
                                    .width(80.dp)
                                    .clickable { onActorClick(castItem.id) }
                            ) {
                                val profileUrl = getProfileUrl(castItem.profile_path)
                                if (profileUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(profileUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = castItem.name,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .background(SurfaceDark, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = castItem.name.take(2).uppercase(),
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = castItem.name,
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = castItem.character ?: "",
                                    color = TextGray,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // --- RECOMMENDATIONS ROW ---
            if (recommendations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Recommended for You",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recommendations) { rMedia ->
                            MediaRowCard(
                                media = rMedia,
                                onClick = { onMediaClick(rMedia.media_type, rMedia.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
