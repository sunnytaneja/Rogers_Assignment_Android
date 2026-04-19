package com.rogers.eventapp.presentation.screen

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalWifiConnectedNoInternet4
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.rogers.eventapp.presentation.components.EventCard
import com.rogers.eventapp.presentation.ui.theme.AppDimens
import com.rogers.eventapp.presentation.viewmodel.EventListViewModel
import com.rogers.eventapp.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EventListScreen(
    onEventClick: (String) -> Unit,
    viewModel: EventListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    ) { permissionsResult ->
        val granted = permissionsResult.values.any { it }
        if (granted) viewModel.onLocationPermissionGranted()
        else viewModel.onLocationPermissionDenied()
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionState.allPermissionsGranted) {
            locationPermissionState.launchMultiplePermissionRequest()
        } else {
            viewModel.onLocationPermissionGranted()
        }
    }

    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) {
            pullRefreshState.animateToHidden()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    uiState.locationError?.let { error ->
        AlertDialog(
            onDismissRequest = {
                viewModel.clearLocationError()
            },
            title = {
                Text(stringResource(R.string.dialog_location_title))
            },
            text = {
                Text(error)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearLocationError()
                    }
                ) {
                    Text(stringResource(R.string.dialog_location_dismiss))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.screen_title_discover),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Icon(
                        imageVector = if (uiState.forceRefresh) Icons.Default.NetworkWifi else Icons.Default.SignalWifiConnectedNoInternet4,
                        contentDescription = if (uiState.forceRefresh) stringResource(R.string.cd_force_refresh_on) else stringResource(
                            R.string.cd_force_refresh_off
                        ),
                        tint = if (uiState.forceRefresh)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(AppDimens.spaceSm))
                    Switch(
                        checked = uiState.forceRefresh,
                        onCheckedChange = { enabled ->
                            viewModel.setForceRefresh(enabled)
                        }
                    )

                    Spacer(modifier = Modifier.width(AppDimens.spaceLg))
                    if (uiState.hasLocationPermission) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = stringResource(R.string.cd_location_active),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = AppDimens.spaceLg)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = {
                viewModel.refresh()
            },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChanged = viewModel::onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimens.spaceXl, vertical = AppDimens.spaceMd)
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        uiState.error != null && uiState.events.isEmpty() -> {
                            ErrorContent(
                                message = uiState.error!!,
                                onRetry = viewModel::refresh
                            )
                        }

                        uiState.filteredEvents.isEmpty() && !uiState.isLoading -> {
                            EmptyContent(
                                hasSearch = uiState.searchQuery.isNotBlank()
                            )
                        }

                        else -> {
                            LazyColumn(
                                contentPadding = PaddingValues(
                                    horizontal = AppDimens.spaceXl,
                                    vertical = AppDimens.spaceMd
                                ),
                                verticalArrangement = Arrangement.spacedBy(AppDimens.spaceLg)
                            ) {
                                items(
                                    items = uiState.filteredEvents,
                                    key = { it.id }
                                ) { event ->
                                    EventCard(
                                        event = event,
                                        onEventClick = { onEventClick(event.id) },
                                        onBookmarkClick = { viewModel.onToggleBookmark(event) }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(AppDimens.spaceMd))
                                }
                            }
                        }
                    }

                    if (uiState.isLoading) {
                        LoadingContent()
                    }
                    if (uiState.error != null && uiState.events.isNotEmpty()) {
                        LaunchedEffect(uiState.error) {
                            snackbarHostState.showSnackbar(uiState.error!!)
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.cd_search))
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = stringResource(R.string.cd_clear_search)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(AppDimens.radiusMd)
    )
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = Color.LightGray)
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimens.space3xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.WifiOff,
            contentDescription = null,
            modifier = Modifier.size(AppDimens.size2xl),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(AppDimens.spaceXl))
        Text(
            text = stringResource(R.string.error_title_load_events),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(AppDimens.spaceMd))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(AppDimens.space2xl))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(AppDimens.spaceMd))
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun EmptyContent(hasSearch: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimens.space3xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(AppDimens.size2xl),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(AppDimens.spaceXl))
        Text(
            text = if (hasSearch) stringResource(R.string.empty_no_results) else stringResource(R.string.empty_no_events),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        if (hasSearch) {
            Spacer(modifier = Modifier.height(AppDimens.spaceMd))
            Text(
                text = stringResource(R.string.empty_search_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}