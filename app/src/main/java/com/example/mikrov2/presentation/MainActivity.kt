/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.mikrov2.presentation

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tiles.TileService
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.example.mikrov2.complication.TestService
import com.example.mikrov2.tile.MainTileService
import androidx.wear.tiles.TileUpdateRequester


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            HydrationTrackerScreen()
        }
    }
}

@Composable
fun HydrationTrackerScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("water_prefs", Context.MODE_PRIVATE) }

    val countKey = "glasses_drank"
    val maxKey = "max_glasses"

    var glassCount by rememberSaveable {
        mutableIntStateOf(prefs.getInt(countKey, 0))
    }

    var maxGlasses by rememberSaveable {
        mutableIntStateOf(prefs.getInt(maxKey, 8))
    }
    // This function increases or decreases how many glasses you have drank
    fun updateCount(newCount: Int) {
        glassCount = newCount
        prefs.edit() { putInt(countKey, newCount) }
        requestComplicationUpdate(context)
        requestTileUpdate(context)
    }
    // This function increases or decreases how many glasses you should drink
    fun updateMaxGlasses(newMax: Int) {
        maxGlasses = newMax
        prefs.edit() { putInt(maxKey, newMax) }
        if (glassCount > newMax) updateCount(newMax)
        requestComplicationUpdate(context)
        requestTileUpdate(context)
    }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Glasses Drank: $glassCount",
                style = MaterialTheme.typography.title3,
                modifier = Modifier.padding(8.dp)
            )
        }
// Here if '+' is clicked number of glasses you drank will increase and if '-' is clicked it will decrease
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { if (glassCount > 0) updateCount(glassCount - 1) },
                    enabled = glassCount > 0
                ) { Text("-") }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { if (glassCount < maxGlasses) updateCount(glassCount + 1) },
                    enabled = glassCount < maxGlasses
                ) { Text("+") }
            }
        }

        item {
            Text(
                text = "Goal: $maxGlasses glasses",
                style = MaterialTheme.typography.title3,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
// Here if '+' is clicked number of glasses you should drink will increase and if '-' is clicked it will decrease
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { if (maxGlasses > 1) updateMaxGlasses(maxGlasses - 1) },
                    enabled = maxGlasses > 1
                ) { Text("-") }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { updateMaxGlasses(maxGlasses + 1) }
                ) { Text("+") }
            }
        }
    }


}
private fun requestComplicationUpdate(context: Context) {
    val component = ComponentName(context, TestService::class.java)
    ComplicationDataSourceUpdateRequester
        .create(context, component)
        .requestUpdateAll()
}
private fun requestTileUpdate(context: Context) {
    TileService.getUpdater(context)
        .requestUpdate(MainTileService::class.java)
}






