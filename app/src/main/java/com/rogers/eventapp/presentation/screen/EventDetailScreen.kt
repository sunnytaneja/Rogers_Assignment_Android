package com.rogers.eventapp.presentation.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.rogers.eventapp.domain.model.Event
import com.rogers.eventapp.presentation.LocalImageLoader
import com.rogers.eventapp.presentation.ui.theme.AppDimens
import com.rogers.eventapp.presentation.viewmodel.EventDetailViewModel
import com.rogers.eventapp.utils.DateUtils
import com.rogers.eventapp.utils.DeepLinkUtils
import com.rogers.eventapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(AppDimens.space3xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(AppDimens.space64),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(AppDimens.spaceXl))
                        Text(uiState.error!!, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(AppDimens.spaceXl))
                        Button(onClick = viewModel::retry) { Text(stringResource(R.string.retry)) }
                    }
                }

                uiState.event != null -> {
                    EventDetailContent(
                        event = uiState.event!!,
                        distanceText = uiState.distanceText,
                        isBookmarkLoading = uiState.isBookmarkLoading,
                        onNavigateBack = onNavigateBack,
                        onToggleBookmark = viewModel::onToggleBookmark,
                        onNavigateToMaps = {
                            DeepLinkUtils.openMapsForNavigation(
                                context = context,
                                latitude = uiState.event!!.latitude,
                                longitude = uiState.event!!.longitude,
                                label = uiState.event!!.title
                            )
                        },
                        onShare = {
                            DeepLinkUtils.shareEvent(context, uiState.event!!.title, eventId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EventDetailContent(
    event: Event,
    distanceText: String?,
    isBookmarkLoading: Boolean,
    onNavigateBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    onNavigateToMaps: () -> Unit,
    onShare: () -> Unit
) {
    val scrollState = rememberScrollState()
    val imageLoader = LocalImageLoader.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(modifier = Modifier.height(AppDimens.imageLg)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.imageUrl)
                    .crossfade(true)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.spaceXl)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleActionButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.spaceMd)) {
                    CircleActionButton(onClick = onShare) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = stringResource(R.string.share),
                            tint = Color.White
                        )
                    }
                    CircleActionButton(onClick = onToggleBookmark, enabled = !isBookmarkLoading) {
                        if (isBookmarkLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(AppDimens.sizeMd),
                                color = Color.White,
                                strokeWidth = AppDimens.spaceXs
                            )
                        } else {
                            Icon(
                                imageVector = if (event.isBookmarked) Icons.Default.Bookmark
                                else Icons.Outlined.BookmarkBorder,
                                contentDescription = stringResource(R.string.bookmark),
                                tint = if (event.isBookmarked) Color.Green
                                else Color.White
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.spaceXxl),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spaceXl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(AppDimens.spaceMd))
            }

            HorizontalDivider()

            InfoRow(
                icon = Icons.Default.CalendarMonth,
                label = stringResource(R.string.label_date),
                value = DateUtils.formatDisplayDate(event.time)
            )
            InfoRow(
                icon = Icons.Default.Schedule,
                label = stringResource(R.string.label_time),
                value = DateUtils.formatDisplayTime(event.time)
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(AppDimens.sizeMd),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(AppDimens.spaceLg))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.label_location),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    distanceText?.let {
                        Spacer(Modifier.height(AppDimens.spaceSm))
                        Text(
                            text = stringResource(R.string.format_distance_away, it),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Blue
                        )
                    }
                }
            }

            Button(
                onClick = onNavigateToMaps,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppDimens.radiusMd)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null)
                Spacer(Modifier.width(AppDimens.spaceMd))
                Text(stringResource(R.string.get_directions))
            }

            HorizontalDivider()

            Spacer(Modifier.height(AppDimens.spaceXl))
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(AppDimens.sizeMd),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(AppDimens.spaceLg))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CircleActionButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.size(AppDimens.sizeXl),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.35f),
        onClick = onClick,
        enabled = enabled
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}