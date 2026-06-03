package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.viewmodel.MovieHubViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActorScreen(
    viewModel: MovieHubViewModel,
    actorId: Int,
    onBackClick: () -> Unit,
    onMediaClick: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoadingActor by viewModel.isLoadingActor.collectAsState()
    val actorDetail by viewModel.actorDetail.collectAsState()
    val actorFilmography by viewModel.actorFilmography.collectAsState()

    // Fetch Actor Details on load
    LaunchedEffect(actorId) {
        viewModel.loadActorDetails(actorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = actorDetail?.name ?: "Actor Profile",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
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
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NearBlack)
                .padding(innerPadding)
        ) {
            if (isLoadingActor) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ShimmerBox(modifier = Modifier.size(width = 110.dp, height = 165.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ShimmerBox(modifier = Modifier.size(width = 140.dp, height = 24.dp))
                            ShimmerBox(modifier = Modifier.size(width = 100.dp, height = 14.dp))
                        }
                    }
                    ShimmerBox(modifier = Modifier.fillMaxWidth().height(150.dp))
                }
            } else if (actorDetail == null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("Error loading profile", color = TextGray)
                }
            } else {
                val detail = actorDetail!!
                val birthday = detail.birthday ?: "Unknown"
                val birthplace = detail.place_of_birth ?: "Unknown"
                val biography = detail.biography

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Bio Header Info Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDark)
                        ) {
                            val profileUrl = getProfileUrl(detail.profile_path)
                            if (profileUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(profileUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = detail.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                NoImagePlaceholder(title = detail.name, modifier = Modifier.fillMaxSize())
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = detail.name,
                                color = TextWhite,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text("Born:", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = birthday,
                                color = TextGray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            )

                            Text("Place of Birth:", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = birthplace,
                                color = TextGray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Biography description
                    if (!biography.isNullOrBlank()) {
                        Column {
                            Text(
                                text = "Biography",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = biography,
                                color = TextGray,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    // Filmography Movies & Series List
                    if (actorFilmography.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 1000.dp) // Bound the grid height
                        ) {
                            Text(
                                text = "Known For",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // We can use custom grid or just a vertical list, let's render a Grid with 2 columns
                            // Since verticalScroll cannot contain nested scrollable grids of infinite size, we will chunk them or just use a horizontal scroll of cards, or a custom flow layout for cards!
                            // A Horizontal Scroll lazy row of movies is extremely elegant and doesn't conflict with vertical layout scroll offsets!
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(actorFilmography) { media ->
                                    MediaRowCard(
                                        media = media,
                                        onClick = { onMediaClick(media.media_type, media.id) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
