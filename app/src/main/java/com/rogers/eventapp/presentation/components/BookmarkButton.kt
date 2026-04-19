package com.rogers.eventapp.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.rogers.eventapp.presentation.ui.theme.AppDimens
import com.rogers.eventapp.R

@Composable
fun BookmarkButton(
    isBookmarked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(AppDimens.space36),
        shape = RoundedCornerShape(AppDimens.radiusSm),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isBookmarked) Icons.Default.Bookmark
                else Icons.Outlined.BookmarkBorder,
                contentDescription = if (isBookmarked) stringResource(R.string.cd_bookmark_remove) else stringResource(
                    R.string.cd_bookmark_add
                ),
                modifier = Modifier.size(AppDimens.sizeMd),
                tint = if (isBookmarked) Color.Green
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}