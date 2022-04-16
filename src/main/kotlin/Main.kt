import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.BrmItemData
import kotlinx.coroutines.Dispatchers
import notify.NotifyManager
import kotlin.system.exitProcess


@Composable
fun brmItem(item: BrmItemData) {
    if (item.antennas.size != 0) {
        Card(
            modifier = Modifier.padding(8.dp),
            elevation = 10.dp,
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = item.idd.toString(),
                    modifier = Modifier.padding(bottom = 2.dp),
                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
                )
                item.rssiList?.forEach { rssi ->
                    Text(
                        text = "RSSI: ${rssi.rssi}",
                        modifier = Modifier.padding(bottom = 2.dp),
                        style = MaterialTheme.typography.body2
                    )
                }
                item.antennas.forEach { antenna ->
                    Text(
                        text = "Antennas: ${antenna.id}",
                        modifier = Modifier.padding(bottom = 2.dp),
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}


@Composable
fun configParamChanger(configName: String, configValue: MutableState<String>) {
    Row {
        TextField(
            label = { Text(configName) },
            value = configValue.value,
            onValueChange = { configValue.value = it },
//            modifier = Modifier.height(28.dp).background(Color.Gray)
        )
    }
}


data class AntennaConfig(
    val outputPower: MutableState<String>,
    val RSSIFilter: MutableState<String>
)

data class ReaderConfig(
    val antennas: List<AntennaConfig>
)

@Composable
fun settingsColumn(nm: NotifyManager) {
    Column(modifier = Modifier.width(200.dp).padding(16.dp), horizontalAlignment = Alignment.Start) {
        val rC = ReaderConfig(
            listOf(
                AntennaConfig(remember { mutableStateOf("") }, remember { mutableStateOf("") }),
                AntennaConfig(remember { mutableStateOf("") }, remember { mutableStateOf("") })
            )
        )
        Text("Antenna 1:")
        configParamChanger("OutputPower (W)", rC.antennas[0].outputPower)
        configParamChanger("RSSIFilter", rC.antennas[0].RSSIFilter)
        Text("Antenna 2:")
        configParamChanger("OutputPower (W)", rC.antennas[1].outputPower)
        configParamChanger("RSSIFilter", rC.antennas[1].RSSIFilter)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { println("stub!") }) { Text("Apply") }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun App(text: MutableState<String>, debugText: MutableState<String>, nm: NotifyManager) {
    DesktopMaterialTheme {
        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column {
                var buttonText by remember { mutableStateOf("") }
                Text("Listening:")
                Button(
                    onClick = {
                        buttonText = if (buttonText == "Stop") {
                            nm.stop()
                            "Start"
                        } else {
                            nm.start()
                            "Stop"
                        }
                    }) {
                    Text(buttonText)
                }

                Text(
                    text = debugText.value,
                    modifier = Modifier.padding(8.dp).width(256.dp),
                    color = MaterialTheme.colors.secondary
                )
            }
            Surface(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                color = Color.Gray
            ) {
                LazyVerticalGrid(
                    cells = GridCells.Adaptive(minSize = 256.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    items(nm.itemList) {
                        brmItem(it)
                    }
                }
            }
            settingsColumn(nm)
        }
    }
}

fun main() = application {
    val text = remember { mutableStateOf("") }
    val debugText = remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope { Dispatchers.IO }
    val antennaTimeoutMs = remember { mutableStateOf(3000L) }

    val nm = NotifyManager(debugText, coroutineScope, antennaTimeoutMs)

//    val backJob = coroutineScope.launch { basicTCPConnect(text) }
    nm.start()

    fun onClose() {
        exitApplication()
        nm.stop()
        exitProcess(0)
    }
    Window(onCloseRequest = ::onClose) {
        App(text, debugText, nm)
    }
}

