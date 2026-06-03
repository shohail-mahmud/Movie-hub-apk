package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CompactMedia
import com.example.viewmodel.MovieHubViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistHistoryScreen(
    viewModel: MovieHubViewModel,
    onMediaClick: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val watchlist by viewModel.watchlist.collectAsState()
    val history by viewModel.history.collectAsState()

    var activeTab by remember { mutableStateOf("Watchlist") } // "Watchlist", "History"
    val activeList = if (activeTab == "Watchlist") watchlist else history

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NearBlack)
    ) {
        // --- SCREEN TABS NAVIGATION BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("Watchlist", "History").forEach { tab ->
                val isSelected = activeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) AmberGold else SurfaceDark)
                        .testTag("tab_button_$tab")
                        .clickable { activeTab = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
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

        // --- CLEAR ALL LINE CONTROL ---
        if (activeList.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved ${activeList.size} items",
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                TextButton(
                    onClick = {
                        if (activeTab == "Watchlist") {
                            viewModel.localStorageManager.clearWatchlist()
                        } else {
                            viewModel.localStorageManager.clearHistory()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = RatingRed),
                    modifier = Modifier.testTag("clear_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Queue",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- CORE GRID LIST DISPLAY ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (activeList.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = if (activeTab == "Watchlist") Icons.Default.Favorite else Icons.Default.History,
                        contentDescription = "Empty State",
                        tint = SurfaceVariantDark,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (activeTab == "Watchlist") "Your watchlist is empty!" else "No watched titles in your history yet.",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (activeTab == "Watchlist") "Explore trending titles and add them to your watchlist to start tracking." else "Stream movies relative to backup servers, and they'll show up right here cataloged.",
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activeList, key = { it.id }) { item ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Render standard grid card
                            MediaGridCard(
                                media = item,
                                onClick = { onMediaClick(item.media_type, item.id) }
                            )

                            // Overlay single item delete icon
                            IconButton(
                                onClick = {
                                    if (activeTab == "Watchlist") {
                                        viewModel.localStorageManager.removeFromWatchlist(item.id)
                                    } else {
                                        viewModel.localStorageManager.removeFromHistory(item.id)
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .background(Color.Black.copy(alpha = 0.72f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove From Cache",
                                    tint = RatingRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
