//package com.rogers.eventapp.presentation.components
//
//import android.text.format.DateUtils
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.font.FontWeight
//import com.rogers.eventapp.R
//import com.rogers.eventapp.presentation.ui.theme.AppDimens
//
//@Composable
//fun RelativeTimeBadge(
//    isoTime: String,
//    modifier: Modifier = Modifier
//) {
//    val relativeTime = DateUtils.
//    getRelativeTime(isoTime)
//    val isUrgent =
//        relativeTime == stringResource(R.string.today) || relativeTime == stringResource(R.string.tomorrow)
//
//    Surface(
//        modifier = modifier,
//        shape = RoundedCornerShape(AppDimens.radiusXs),
//        color = if (isUrgent) MaterialTheme.colorScheme.errorContainer
//        else MaterialTheme.colorScheme.surfaceVariant
//    ) {
//        Text(
//            text = relativeTime,
//            style = MaterialTheme.typography.labelSmall,
//            color = if (isUrgent) MaterialTheme.colorScheme.error
//            else MaterialTheme.colorScheme.onSurfaceVariant,
//            fontWeight = FontWeight.SemiBold,
//            modifier = Modifier.padding(
//                horizontal = AppDimens.spaceMd,
//                vertical = AppDimens.spaceSm
//            )
//        )
//    }
//}