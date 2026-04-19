package com.rogers.eventapp.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.rogers.eventapp.presentation.ui.theme.AppDimens
import com.rogers.eventapp.utils.DistanceUtils

@Composable
fun DistanceBadge(
    distanceKm: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppDimens.radiusXs),
        color = Color.LightGray
    ) {
        Text(
            text = DistanceUtils.formatDistance(distanceKm),
            style = MaterialTheme.typography.labelMedium,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                horizontal = AppDimens.spaceMd,
                vertical = AppDimens.spaceXs
            )
        )
    }
}