package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.viewmodel.MovieHubViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: MovieHubViewModel,
    onMediaClick: (String, Int) -> Unit,
    onActorClick: (Int) -> Unit,
    onSeeAllClick: (String, Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoadingHome by viewModel.isLoadingHome.collectAsState()
    val featuredMovie by viewModel.featuredMovie.collectAsState()
    val trendingM by viewModel.trendingMovies.collectAsState()
    val trendingT by viewModel.trendingTv.collectAsState()
    val popularM by viewModel.popularMovies.collectAsState()
    val topRatedM by viewModel.topRatedMovies.collectAsState()
    val nowPlayingM by viewModel.nowPlayingMovies.collectAsState()
    val upcomingM by viewModel.upcomingMovies.collectAsState()
    val popularStars by viewModel.popularActors.collectAsState()
    val genresList by viewModel.genres.collectAsState()

    val selectedHomeTab by viewModel.selectedHomeTab.collectAsState()
    val subTabContent by viewModel.subTabContent.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()

    if (isLoadingHome) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(NearBlack)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(260.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) { ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 30.dp)) }
            }
            repeat(2) {
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(150.dp))
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(NearBlack),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // --- HEADER FEATURING HERO BANNER ---
            featuredMovie?.let { featured ->
                item {
                    HeroBanner(
                        media = featured,
                        isInWatchlist = watchlist.any { it.id == featured.id },
                        onPlayClick = { onMediaClick("movie", featured.id) },
                        onWatchlistClick = { viewModel.localStorageManager.toggleWatchlist(featured) }
                    )
                }
            }

            // --- SUB-TABS SECTION ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HomeSubTabs(
                    selectedTab = selectedHomeTab,
                    onTabSelected = { viewModel.updateHomeSubTab(it) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(subTabContent) { media ->
                        MediaRowCard(media = media, onClick = { onMediaClick(media.media_type, media.id) })
                    }
                }
            }

            // --- BROWSE BY GENRE ---
            item {
                Spacer(modifier = Modifier.height(24.dp))
                RowHeader(title = "Browse by Genre", hasSeeAll = false)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(genresList) { genre ->
                        FilterChip(
                            selected = false,
                            onClick = { onSeeAllClick("by-genre", genre.id, genre.name) },
                            label = { Text(genre.name, color = TextWhite) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceDark,
                                labelColor = TextWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                selected = false,
                                enabled = true,
                                borderColor = SurfaceVariantDark
                            )
                        )
                    }
                }
            }

            // --- TRENDING MOVIES ROW ---
            item {
                Spacer(modifier = Modifier.height(24.dp))
                RowHeader(
                    title = "Trending Movies",
                    onSeeAll = { onSeeAllClick("trending_movies", -1, "Trending Movies") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trendingM) { media ->
                        MediaRowCard(media = media, onClick = { onMediaClick("movie", media.id) })
                    }
                }
            }

            // --- TRENDING TV SHOWS ROW ---
            item {
                Spacer(modifier = Modifier.height(24.dp))
                RowHeader(
                    title = "Trending TV",
                    onSeeAll = { onSeeAllClick("trending_tv", -1, "Trending TV") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trendingT) { media ->
                        MediaRowCard(media = media, onClick = { onMediaClick("tv", media.id) })
                    }
                }
            }

            // --- POPULAR MOVIES ROW ---
            item {
                Spacer(modifier = Modifier.height(24.dp))
                RowHeader(
                    title = "Popular Movies",
                    onSeeAll = { onSeeAllClick("popular", -1, "Popular Movies") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(popularM) { media ->
                        MediaRowCard(media = media, onClick = { onMediaClick("movie", media.id) })
                    }
                }
            }

            // --- TOP RATED MOVIES ROW ---
            item {
                Spacer(modifier = Modifier.height(24.dp))
                RowHeader(
                    title = "Top Rated Movies",
                    onSeeAll = { onSeeAllClick("top_rated", -1, "Top Rated") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(topRatedM) { media ->
                        MediaRowCard(media = media, onClick = { onMediaClick("movie", media.id) })
                    }
                }
            }

            // --- NOW PLAYING MOVIES ROW ---
            item {
                Spacer(modifier = Modifier.height(24.dp))
                RowHeader(
                    title = "Now Playing in Theaters",
                    onSeeAll = { onSeeAllClick("now_playing", -1, "Now Playing") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(nowPlayingM) { media ->
                        MediaRowCard(media = media, onClick = { onMediaClick("movie", media.id) })
                    }
                }
            }

            // --- UPCOMING MOVIES ROW ---
            item {
                Spacer(modifier = Modifier.height(24.dp))
                RowHeader(
                    title = "Upcoming Releases",
                    onSeeAll = { onSeeAllClick("upcoming", -1, "Coming Soon") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(upcomingM) { media ->
                        MediaRowCard(media = media, onClick = { onMediaClick("movie", media.id) })
                    }
                }
            }

            // --- POPULAR STARS (ACTORS) ---
            item {
                Spacer(modifier = Modifier.height(24.dp))
                RowHeader(
                    title = "Popular Celebs",
                    hasSeeAll = false
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(popularStars) { celeb ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(80.dp)
                                .clickable { onActorClick(celeb.id) }
                        ) {
                            val profileUrl = getProfileUrl(celeb.profile_path)
                            if (profileUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(profileUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = celeb.name,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(SurfaceDark, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = celeb.name.take(2).uppercase(),
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = celeb.name,
                                color = TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// --- ROW HEADER ---
@Composable
fun RowHeader(
    title: String,
    hasSeeAll: Boolean = true,
    onSeeAll: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (hasSeeAll) {
            Text(
                text = "See all",
                color = AmberGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSeeAll() }
            )
        }
    }
}

// --- HERO BANNER ---
@Composable
fun HeroBanner(
    media: CompactMedia,
    isInWatchlist: Boolean,
    onPlayClick: () -> Unit,
    onWatchlistClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
    ) {
        // Backdrop cover
        val backdropUrl = getBackdropUrl(media.poster_path)
            ?: getPosterUrl(media.poster_path) // Fallback to poster path
        if (backdropUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(backdropUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = media.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceDark)
            )
        }

        // Overlay gradients for dark immersive feeling
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NearBlack.copy(alpha = 0.5f),
                            NearBlack
                        )
                    )
                )
        )

        // Contents
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AmberGold)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FEATURED",
                        color = NearBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (media.vote_average > 0) {
                    RatingCircle(rating = media.vote_average, size = 30.dp, strokeWidth = 2.dp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = media.title,
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = NearBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Play",
                        color = NearBlack,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onWatchlistClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextWhite
                    ),
                    border = BorderStroke(1.dp, TextWhite.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(
                        imageVector = if (isInWatchlist) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = "Watchlist",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isInWatchlist) "In Watchlist" else "Watchlist",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- HOME SUB-TABS COMPOSABLE ---
@Composable
fun HomeSubTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf("Trending", "Recommended", "New", "Top Rated", "Most Viewed", "Coming Soon")
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tab ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) AmberGold else SurfaceDark)
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = tab,
                    color = if (isSelected) NearBlack else TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
