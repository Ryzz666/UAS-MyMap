package com.rury.mymap.ui

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.shadow
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import com.rury.mymap.data.api.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var originText by remember { mutableStateOf("-7.9731565,112.609915") }
    var destinationText by remember { mutableStateOf("-7.9826092,112.6282364") }
    
    var originLatLng by remember { mutableStateOf<LatLng?>(LatLng(-7.9731565, 112.609915)) }
    var destinationLatLng by remember { mutableStateOf<LatLng?>(LatLng(-7.9826092, 112.6282364)) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    
    var distance by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var transportMode by remember { mutableStateOf("Mobil") }
    var selectedMode by remember { mutableStateOf("driving") } 
    var isSearchVisible by remember { mutableStateOf(true) }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(originLatLng!!, 14f)
    }

    val apiKey = "AIzaSyAMxzfxQjAg9Jr-WE5EtBpAE7xCXwz2B1Q"

    fun fetchRoute() {
        scope.launch {
            try {
                val originParts = originText.split(",")
                val destParts = destinationText.split(",")
                
                if (originParts.size == 2 && destParts.size == 2) {
                    val oLatLng = LatLng(originParts[0].trim().toDouble(), originParts[1].trim().toDouble())
                    val dLatLng = LatLng(destParts[0].trim().toDouble(), destParts[1].trim().toDouble())
                    
                    originLatLng = oLatLng
                    destinationLatLng = dLatLng
                    
                    val response = RetrofitClient.getDirectionsApiService(context).getDirections(
                        origin = "${oLatLng.latitude},${oLatLng.longitude}",
                        destination = "${dLatLng.latitude},${dLatLng.longitude}",
                        mode = selectedMode,
                        apiKey = apiKey
                    )
                    if (response.routes.isNotEmpty()) {
                        val route = response.routes[0]
                        val encodedPolyline = route.overviewPolyline.points
                        routePoints = PolyUtil.decode(encodedPolyline)
                        
                        if (route.legs.isNotEmpty()) {
                            distance = route.legs[0].distance.text
                            duration = route.legs[0].duration.text
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MapScreen", "Error fetching directions", e)
            }
        }
    }

    fun resetFields() {
        originText = ""
        destinationText = ""
        routePoints = emptyList()
        distance = ""
        duration = ""
        transportMode = "Mobil"
        selectedMode = "driving"

        originLatLng = LatLng(-7.9731565, 112.609915)
        destinationLatLng = LatLng(-7.9826092, 112.6282364)
    }

    LaunchedEffect(selectedMode) {
        if (originText.isNotEmpty() && destinationText.isNotEmpty()) {
            fetchRoute()
        }
    }

    LaunchedEffect(Unit) {
        fetchRoute()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                originLatLng?.let {
                    Marker(
                        state = remember(it) { MarkerState(position = it) },
                        title = "Asal",
                        icon = BitmapDescriptorFactory.defaultMarker(150f) // Emerald Hue
                    )
                }
                
                destinationLatLng?.let {
                    Marker(
                        state = remember(it) { MarkerState(position = it) },
                        title = "Tujuan",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }

                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = MaterialTheme.colorScheme.primary,
                        width = 12f
                    )
                }
            }

            // Toggle Search Button
            FloatingActionButton(
                onClick = { isSearchVisible = !isSearchVisible },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp),
                shape = RoundedCornerShape(12.dp),
                containerColor = if (isSearchVisible) MaterialTheme.colorScheme.surface.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary,
                contentColor = if (isSearchVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = if (isSearchVisible) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.Search,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Floating Search Card at Top
            AnimatedVisibility(
                visible = isSearchVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.85f)
            ) {
                Card(
                    modifier = Modifier.shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        TextField(
                            value = originText,
                            onValueChange = { originText = it },
                            placeholder = { Text("Lokasi Awal") },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Place,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        TextField(
                            value = destinationText,
                            onValueChange = { destinationText = it },
                            placeholder = { Text("Lokasi Tujuan") },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TransportChip(
                                selected = selectedMode == "driving",
                                onClick = { 
                                    selectedMode = "driving"
                                    transportMode = "Mobil"
                                },
                                icon = Icons.Rounded.DirectionsCar,
                                label = "Mobil"
                            )
                            TransportChip(
                                selected = selectedMode == "two_wheeler",
                                onClick = { 
                                    selectedMode = "two_wheeler"
                                    transportMode = "Motor"
                                },
                                icon = Icons.Rounded.TwoWheeler,
                                label = "Motor"
                            )
                        }
                        
                        Button(
                            onClick = { 
                                fetchRoute()
                                isSearchVisible = false
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cari", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Route Info Bar (below search card)
            AnimatedVisibility(
                visible = (distance.isNotEmpty() || duration.isNotEmpty()),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = if (isSearchVisible) 220.dp else 80.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        InfoItem(label = "Waktu", value = duration)
                        VerticalDivider(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f))
                        InfoItem(label = "Jarak", value = distance)
                        VerticalDivider(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f))
                        InfoItem(label = "Mode", value = transportMode)
                    }
                }
            }
        }
    }
}

@Composable
fun TransportChip(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = LocalContentColor.current.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}
