package com.rogers.eventapp.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.rogers.eventapp.domain.model.Event
import com.rogers.eventapp.presentation.LocalImageLoader
import com.rogers.eventapp.presentation.ui.theme.AppDimens
import com.rogers.eventapp.utils.DateUtils

@Composable
fun EventCard(
    event: Event,
    onEventClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val imageLoader = LocalImageLoader.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEventClick),
        shape = RoundedCornerShape(AppDimens.radiusLg),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.elevationSm)
    ) {
        Column {
            // Hero Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.imageSm)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(event.imageUrl)
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(
                                topStart = AppDimens.spaceXl,
                                topEnd = AppDimens.spaceXl
                            )
                        )
                )

                // Bookmark button
                BookmarkButton(
                    isBookmarked = event.isBookmarked,
                    onClick = onBookmarkClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(AppDimens.spaceMd)
                )
            }

            // Content
            Column(
                modifier = Modifier.padding(AppDimens.spaceXl),
                verticalArrangement = Arrangement.spacedBy(AppDimens.space6)
            ) {
                // Title
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.spaceSm)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimens.sizeXs),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = DateUtils.formatDisplayFull(event.time),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Location + Distance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimens.sizeXs),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )

                    event.distanceKm?.let { distance ->
                        Spacer(Modifier.width(AppDimens.spaceMd))
                        DistanceBadge(distanceKm = distance)
                    }
                }
            }
        }
    }
}