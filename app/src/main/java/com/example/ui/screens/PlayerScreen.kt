package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebChromeClient.CustomViewCallback
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.CompactMedia
import com.example.viewmodel.MovieHubViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return context as? Activity
}

data class StreamingServer(
    val name: String,
    val description: String,
    val movieUrl: String,
    val tvUrl: String
)

val streamingServers = listOf(
    StreamingServer(
        "VidKing (Default)",
        "Default Server",
        "https://www.vidking.net/embed/movie/{id}?color=f59e0b&autoPlay=true",
        "https://www.vidking.net/embed/tv/{id}/{s}/{e}?color=f59e0b&autoPlay=true&nextEpisode=true&episodeSelector=true"
    ),
    StreamingServer(
        "VidLink",
        "Fast Stream",
        "https://vidlink.pro/movie/{id}?primaryColor=f59e0b",
        "https://vidlink.pro/tv/{id}/{s}/{e}?primaryColor=f59e0b"
    ),
    StreamingServer(
        "VidSrc.to",
        "Super Backup",
        "https://vidsrc.to/embed/movie/{id}",
        "https://vidsrc.to/embed/tv/{id}/{s}/{e}"
    ),
    StreamingServer(
        "VidSrc.xyz",
        "Multi-Server",
        "https://vidsrc.xyz/embed/movie/{id}",
        "https://vidsrc.xyz/embed/tv/{id}/{s}/{e}"
    ),
    StreamingServer(
        "VidSrc.me",
        "Classic Server",
        "https://vidsrc.me/embed/movie?tmdb={id}",
        "https://vidsrc.me/embed/tv?tmdb={id}&season={s}&episode={e}"
    ),
    StreamingServer(
        "VidSrc.cc",
        "Alternative Backup",
        "https://vidsrc.cc/v2/embed/movie/{id}",
        "https://vidsrc.cc/v2/embed/tv/{id}/{s}/{e}"
    ),
    StreamingServer(
        "Embed.su",
        "Subtitles Backup",
        "https://embed.su/embed/movie/{id}",
        "https://embed.su/embed/tv/{id}/{s}/{e}"
    ),
    StreamingServer(
        "SmashyStream",
        "Dual Audio / Hindi",
        "https://embed.smashystream.com/play/movie/{id}",
        "https://embed.smashystream.com/play/tv/{id}/{s}/{e}"
    )
)

fun buildEmbedUrl(server: StreamingServer, id: Int, isTv: Boolean, season: Int, episode: Int): String {
    return if (isTv) {
        server.tvUrl
            .replace("{id}", id.toString())
            .replace("{s}", season.toString())
            .replace("{e}", episode.toString())
    } else {
        server.movieUrl
            .replace("{id}", id.toString())
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: MovieHubViewModel,
    mediaType: String, // "movie" or "tv"
    id: Int,
    initialSeason: Int,
    initialEpisode: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeServerIndex by viewModel.activeServerIndex.collectAsState()
    val isTv = mediaType == "tv"

    var currentSeason by remember { mutableStateOf(initialSeason) }
    var currentEpisode by remember { mutableStateOf(initialEpisode) }

    val movieDetail by viewModel.movieDetail.collectAsState()
    val tvDetail by viewModel.tvDetail.collectAsState()

    // Determine current Title & add to Watch History
    val mediaTitle = if (isTv) tvDetail?.name ?: "TV Show Streaming" else movieDetail?.title ?: "Movie Streaming"
    val maxEpisodes = remember(tvDetail, currentSeason) {
        val seasonInfo = tvDetail?.seasons?.find { it.season_number == currentSeason }
        seasonInfo?.episode_count ?: 12
    }

    // Auto-save history when title detail is available
    LaunchedEffect(movieDetail, tvDetail, currentSeason, currentEpisode) {
        if (isTv && tvDetail != null) {
            val compact = tvDetail!!.toCompactMedia().copy(
                title = "${tvDetail!!.name} - S${currentSeason}E${currentEpisode}",
                savedAt = System.currentTimeMillis()
            )
            viewModel.localStorageManager.addToHistory(compact)
        } else if (!isTv && movieDetail != null) {
            viewModel.localStorageManager.addToHistory(movieDetail!!.toCompactMedia())
        }
    }

    // Embed Url Construction
    val activeServer = streamingServers.getOrElse(activeServerIndex) { streamingServers.first() }
    val embedUrl = buildEmbedUrl(activeServer, id, isTv, currentSeason, currentEpisode)

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // Enable landscape screen rotation loaded effect
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Modern SystemBars Edge-To-Edge toggle for full-screen landscape
    LaunchedEffect(isLandscape) {
        val activity = context.findActivity()
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (isLandscape) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Scaffold(
        topBar = {
            if (!isLandscape) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isTv) "$mediaTitle (S${currentSeason}E${currentEpisode})" else mediaTitle,
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go Back",
                                tint = TextWhite
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NearBlack,
                        titleContentColor = TextWhite
                    )
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NearBlack)
                .padding(if (isLandscape) PaddingValues(0.dp) else innerPadding)
        ) {
            // --- VIDEO VIEWER VIEW ---
            Box(
                modifier = if (isLandscape) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.77f) // 16:9
                }
                .background(Color.Black)
            ) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        android.webkit.WebView(ctx).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                            }
                            webViewClient = android.webkit.WebViewClient()
                            webChromeClient = object : android.webkit.WebChromeClient() {
                                private var customView: View? = null
                                private var customViewCallback: CustomViewCallback? = null

                                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                    super.onShowCustomView(view, callback)
                                    if (customView != null) {
                                        onHideCustomView()
                                        return
                                    }
                                    customView = view
                                    customViewCallback = callback
                                    
                                    val activity = ctx.findActivity()
                                    val decorView = activity?.window?.decorView as? FrameLayout
                                    decorView?.addView(customView, FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT
                                    ))
                                }

                                override fun onHideCustomView() {
                                    super.onHideCustomView()
                                    if (customView == null) return
                                    
                                    val activity = ctx.findActivity()
                                    val decorView = activity?.window?.decorView as? FrameLayout
                                    decorView?.removeView(customView)
                                    customView = null
                                    customViewCallback?.onCustomViewHidden()
                                    customViewCallback = null
                                }
                            }
                            loadUrl(embedUrl)
                        }
                    },
                    update = { view ->
                        if (view.url != embedUrl) {
                            view.loadUrl(embedUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (!isLandscape) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- INFO BANNER TIP ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDark)
                            .border(1.dp, SurfaceVariantDark, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info Tip",
                            tint = AmberGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "If the stream is slow or not loading, switch to a backup server. TV URLs reload automatically on picker change.",
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- TV EPISODES PICKER (SWIFT EPISODE NAV WITHIN PLAYER) ---
                    if (isTv && tvDetail != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .background(SurfaceDark, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Next Episode Quick Link",
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

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
                                    val isSelected = currentSeason == sNum
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) AmberGold else SurfaceVariantDark)
                                            .clickable {
                                                currentSeason = sNum
                                                currentEpisode = 1 // Reset episode on season swap
                                            }
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
                                    val isSelected = currentEpisode == eNum
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) AmberGold else SurfaceVariantDark)
                                            .clickable { currentEpisode = eNum }
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
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // --- SERVERS SELECTOR HEADER ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Backup Stream Servers",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Scrollable chip selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            streamingServers.forEachIndexed { index, server ->
                                val isSelected = activeServerIndex == index
                                Box(
                                    modifier = Modifier
                                        .width(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) SurfaceVariantDark else SurfaceDark)
                                        .border(
                                            1.dp,
                                            if (isSelected) AmberGold else SurfaceVariantDark,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .testTag("server_chip_$index")
                                        .clickable { viewModel.selectServer(index) }
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = server.name,
                                                color = if (isSelected) AmberGold else TextWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            if (isSelected) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    PulseLiveDot()
                                                    Text(
                                                        text = "Live",
                                                        color = Color(0xFF22C55E),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = server.description,
                                            color = TextGray,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
