package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MovieHubViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    viewModel: MovieHubViewModel,
    category: String,
    genreId: Int,
    genreName: String,
    onBackClick: () -> Unit,
    onMediaClick: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listItems by viewModel.listItems.collectAsState()
    val page by viewModel.listPage.collectAsState()
    val totalPages by viewModel.listTotalPages.collectAsState()
    val isLoadingList by viewModel.isLoadingList.collectAsState()

    // Load list items when category changes
    LaunchedEffect(category, genreId) {
        viewModel.loadCategoryList(category, genreId, page = 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = genreName.ifBlank { "MovieHub Catalog" },
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
        bottomBar = {
            // --- PAGINATION CONTROLS ---
            if (totalPages > 1 && !isLoadingList) {
                Surface(
                    color = NearBlack,
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                if (page > 1) {
                                    viewModel.loadCategoryList(category, genreId, page - 1)
                                }
                            },
                            enabled = page > 1,
                            colors = ButtonDefaults.textButtonColors(contentColor = AmberGold)
                        ) {
                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous")
                            Text("Prev")
                        }

                        Text(
                            text = "Page $page of $totalPages",
                            color = TextGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        TextButton(
                            onClick = {
                                if (page < totalPages) {
                                    viewModel.loadCategoryList(category, genreId, page + 1)
                                }
                            },
                            enabled = page < totalPages,
                            colors = ButtonDefaults.textButtonColors(contentColor = AmberGold)
                        ) {
                            Text("Next")
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next")
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NearBlack)
                .padding(innerPadding)
        ) {
            if (isLoadingList) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(6) {
                        ShimmerBox(modifier = Modifier.fillMaxWidth().aspectRatio(0.67f))
                    }
                }
            } else if (listItems.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No content available",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listItems) { media ->
                        MediaGridCard(
                            media = media,
                            onClick = { onMediaClick(media.media_type, media.id) }
                        )
                    }
                }
            }
        }
    }
}
