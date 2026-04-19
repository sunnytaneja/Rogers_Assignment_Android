package com.rogers.eventapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogers.eventapp.presentation.components.EventCard
import com.rogers.eventapp.presentation.ui.theme.AppDimens
import com.rogers.eventapp.presentation.viewmodel.BookmarksViewModel
import com.rogers.eventapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onEventClick: (String) -> Unit,
    viewModel: BookmarksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message.asString(context))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.bookmarks_tab),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.bookmarks.isEmpty() -> {
                    EmptyBookmarksContent()
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = AppDimens.spaceXl,
                            vertical = AppDimens.spaceMd
                        ),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.spaceLg)
                    ) {
                        item {
                            Text(
                                text = pluralStringResource(
                                    id = R.plurals.bookmarks_count,
                                    count = uiState.bookmarks.size,
                                    uiState.bookmarks.size
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = AppDimens.spaceSm)
                            )
                        }

                        items(
                            items = uiState.bookmarks,
                            key = { it.id }
                        ) { event ->
                            EventCard(
                                event = event.copy(isBookmarked = true),
                                onEventClick = { onEventClick(event.id) },
                                onBookmarkClick = { viewModel.onRemoveBookmark(event) }
                            )
                        }

                        item { Spacer(Modifier.height(AppDimens.spaceMd)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBookmarksContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimens.space3xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.BookmarkBorder,
            contentDescription = null,
            modifier = Modifier.size(AppDimens.size3xl),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(AppDimens.spaceXl))
        Text(
            text = stringResource(R.string.empty_bookmarks_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(AppDimens.spaceMd))
        Text(
            text = stringResource(R.string.empty_bookmarks_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}