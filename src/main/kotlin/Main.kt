import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.Antenna
import data.BrmItemData
import kotlinx.coroutines.Dispatchers
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
                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1

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
        )
    }
}


@Composable
fun settingsColumn(rM: ReaderManager) {
    Column(modifier = Modifier.width(200.dp).padding(16.dp), horizontalAlignment = Alignment.Start) {
        Text("Antenna 1:")
        configParamChanger("OutputPower (W)", rM.rC.antennas[0].outputPower)
        configParamChanger("RSSIFilter", rM.rC.antennas[0].RSSIFilter)
        Text("Antenna 2:")
        configParamChanger("OutputPower (W)", rM.rC.antennas[1].outputPower)
        configParamChanger("RSSIFilter", rM.rC.antennas[1].RSSIFilter)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { rM.applyAntennasConfig() }) { Text("Apply") }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun App(text: MutableState<String>, debugText: MutableState<String>, rM: ReaderManager) {
    DesktopMaterialTheme {
        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(modifier = Modifier.width(180.dp).padding(top = 16.dp, end = 16.dp)) {
                var nMbutton by remember { mutableStateOf("Stop") }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Listening")
                    Canvas(modifier = Modifier.size(12.dp), onDraw = {
                        if (nMbutton == "Stop") {
                            drawCircle(color = Color(0, 168, 107))
                        } else {
                            drawCircle(color = Color(168, 0, 50))
                        }
                    })
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                    onClick = {
                        nMbutton = if (nMbutton == "Stop") {
                            rM.notifyManager.stop()
                            "Start"
                        } else {
                            rM.notifyManager.start()
                            "Stop"
                        }
                    }) {
                    Text(nMbutton)
                }
                Spacer(modifier = Modifier.height(16.dp))

                var connectionState by remember { mutableStateOf("Connect") }
                var enabled by remember { mutableStateOf(true) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TCP Connection")
                    Canvas(modifier = Modifier.size(12.dp), onDraw = {
                        if (rM.connectionState.value == 1) {
                            drawCircle(color = Color(0, 168, 107))
                        } else {
                            drawCircle(color = Color(168, 0, 50))
                        }
                    })
                }

                var readerIp by remember { mutableStateOf("192.168.10.10") }
                TextField(
                    label = { Text("Reader IP") },
                    shape = RoundedCornerShape(topEnd = 20.dp),
                    value = readerIp,
                    onValueChange = { readerIp = it },
                )

                var readerPort by remember { mutableStateOf("10001") }
                TextField(
                    shape = RectangleShape,
                    label = { Text("Reader Port") },
                    value = readerPort,
                    onValueChange = { readerPort = it },
                )
//                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomEnd = 20.dp),
                    enabled = enabled,
                    onClick = {
                        connectionState = if (rM.connectionState.value == -1) {
                            rM.connect(readerIp, readerPort.toInt())
                            enabled = false
                            rM.connectionState.value = 0
                            "Connection..."
                        } else {
                            rM.disconnect()
                            "Disconnect"
                        }.toString()
                    }) {
                    if (rM.connectionState.value != 0) {
                        enabled = true
                    }
                    Text(connectionState)
                }
            }
            Surface(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                color = Color.Gray
            ) {
                LazyVerticalGrid(
                    cells = GridCells.Adaptive(minSize = 256.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    items(rM.notifyManager.itemList) {
                        brmItem(it)
                    }
                }
            }
            settingsColumn(rM)
        }
    }
}

fun main() = application {
    val text = remember { mutableStateOf("") }
    val debugText = remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope { Dispatchers.IO }
    val antennaTimeoutMs = remember { mutableStateOf(3000L) }

    val connectionState = remember { mutableStateOf(-1) }
//    val itemList = remember {
//        mutableStateOf<List<BrmItemData>>(
//            listOf(
//                BrmItemData(idd = "E1040102401230021304012314125123").apply { antennas.add(Antenna(123)) },
//                BrmItemData(idd = "E10402314125123").apply { antennas.add(Antenna(123)) },
//                BrmItemData(idd = "E10401012314125123").apply { antennas.add(Antenna(123)) },
//                BrmItemData(idd = "E104010240104012314125123").apply { antennas.add(Antenna(123)) },
//                BrmItemData(idd = "E1040102401230021304012314125123").apply { antennas.add(Antenna(123)) },
//                BrmItemData(idd = "E10401024004012314125123").apply { antennas.add(Antenna(123)) },
//                BrmItemData(idd = "E1040102401230021304012314125123").apply { antennas.add(Antenna(123)) },
//                BrmItemData(idd = "E10414125123").apply { antennas.add(Antenna(123)) },
//                BrmItemData(idd = "E104010240123002125123").apply { antennas.add(Antenna(123)) },
//            )
//        )
//    }
    val rC = ReaderConfigure(
        listOf(
            AntennaConfig(remember { mutableStateOf("") }, remember { mutableStateOf("") }),
            AntennaConfig(remember { mutableStateOf("") }, remember { mutableStateOf("") })
        )
    )

    val rM = ReaderManager(debugText, coroutineScope, antennaTimeoutMs, connectionState, rC)

    fun onClose() {
        exitApplication()
        rM.stop()
        exitProcess(0)
    }
    Window(onCloseRequest = ::onClose, title = "Reader Tool") {
        App(text, debugText, rM)
    }

    rM.start()
}

