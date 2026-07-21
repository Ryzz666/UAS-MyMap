package com.naufal.mymap.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import com.naufal.mymap.data.api.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 140.dp,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (distance.isNotEmpty() || duration.isNotEmpty()) {
                            Text(
                                text = "$duration ($distance)",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Mode: $transportMode",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        } else {
                            Text(
                                text = "Cari Rute Perjalanan",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    Button(
                        onClick = { 
                            fetchRoute()
                            scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                        },
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cari")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = originText,
                        onValueChange = { originText = it },
                        label = { Text("Lokasi Awal") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        leadingIcon = { Icon(Icons.Rounded.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = destinationText,
                        onValueChange = { destinationText = it },
                        label = { Text("Lokasi Tujuan") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        leadingIcon = { Icon(Icons.Rounded.Place, contentDescription = null, tint = Color.Red) }
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text("Pilih Transportasi:", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = selectedMode == "driving",
                            onClick = { 
                                selectedMode = "driving"
                                transportMode = "Mobil"
                            },
                            label = { Text("Mobil") },
                            shape = RoundedCornerShape(20.dp),
                            leadingIcon = { Icon(Icons.Rounded.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        FilterChip(
                            selected = selectedMode == "two_wheeler",
                            onClick = { 
                                selectedMode = "two_wheeler"
                                transportMode = "Motor"
                            },
                            label = { Text("Motor") },
                            shape = RoundedCornerShape(20.dp),
                            leadingIcon = { Icon(Icons.Rounded.TwoWheeler, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = { 
                            resetFields()
                            scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Reset")
                    }
                    
                    if (distance.isNotEmpty() || duration.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(text = "Jarak: $distance", style = MaterialTheme.typography.bodyLarge)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.AccessTime, contentDescription = null, tint = Color.Red)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(text = "Waktu: $duration", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = modifier
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
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
                
                destinationLatLng?.let {
                    Marker(
                        state = remember(it) { MarkerState(position = it) },
                        title = "Tujuan",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    )
                }

                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = MaterialTheme.colorScheme.primary,
                        width = 15f
                    )
                }
            }
        }
    }
}
