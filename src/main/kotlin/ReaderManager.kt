import androidx.compose.runtime.MutableState
import data.BrmItemData
import de.feig.fedm.Connector
import de.feig.fedm.ErrorCode
import de.feig.fedm.ReaderModule
import de.feig.fedm.RequestMode
import de.feig.fedm.exception.FedmRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import notify.NotifyManager
import java.net.InetAddress

data class AntennaConfig(
    val outputPower: MutableState<String>,
    var RSSIFilter: MutableState<String>
)

data class ReaderConfigure(
    val antennas: List<AntennaConfig>
)

class ReaderManager(
    private val debugText: MutableState<String>,
    private val coroutineScope: CoroutineScope,
    private val antennaTimeoutMs: MutableState<Long>,
    var connectionState: MutableState<Int>,
//    var itemList: MutableState<List<BrmItemData>>,
    val rC: ReaderConfigure
) {
    val reader = ReaderModule(RequestMode.UniDirectional)
    val ipAddr = "192.168.10.10"
    val port = 10001
    val notifyManager = NotifyManager(debugText, coroutineScope, antennaTimeoutMs)
    val persistent = true

    fun start() = coroutineScope.launch(Dispatchers.IO) {
        connect(ipAddr, port)
        readAntennasConfig()
        notifyManager.start()
    }

    fun stop() = coroutineScope.launch(Dispatchers.IO) {
        notifyManager.stop()
        disconnect()
    }


    private fun readAntennasConfig() {
        if (!reader.isConnected) return
        val state = reader.config().readCompleteConfiguration(persistent);
        println("readCompleteConfiguration: " + ErrorCode.toString(state));
        try {
            var counter = 1
            rC.antennas.forEach {
                it.RSSIFilter.value = reader.config().getStringConfigPara(
                    "AirInterface.Antenna.UHF.No${counter}.RSSIFilter",
                    persistent
                )
                it.outputPower.value = reader.config().getStringConfigPara(
                    "AirInterface.Antenna.UHF.No${counter}.OutputPower",
                    persistent
                )
                counter += 1
            }
        } catch (e: FedmRuntimeException) {
            println("readAntennasConfig: " + e.message)
        }
    }

    fun applyAntennasConfig() {
        if (!reader.isConnected) return
        try {
            var counter = 1
            rC.antennas.forEach {
                reader.config().changeConfigPara(
                    "AirInterface.Antenna.UHF.No${counter}.RSSIFilter",
                    it.RSSIFilter.value,
                    persistent
                )
                reader.config().changeConfigPara(
                    "AirInterface.Antenna.UHF.No${counter}.OutputPower",
                    it.outputPower.value,
                    persistent
                )
                counter += 1
            }
            reader.config().applyConfiguration(persistent)
        } catch (e: FedmRuntimeException) {
            println("applyAntennasConfig: " + e.message)
        }
    }


    fun connect(ipAddr: String, port: Int) = coroutineScope.launch(Dispatchers.IO) {
        try {
            if (!InetAddress.getByName(ipAddr).isReachable(1000)) {
                return@launch
            }
            val connector = Connector.createTcpConnector(ipAddr, port)
            println("Start connection with Reader: " + connector.tcpIpAddress())
            reader.connect(connector)

            if (reader.lastError() != ErrorCode.Ok) {
                println("Error while Connecting: " + reader.lastError())
                println(reader.lastErrorText())
                return@launch
            }

            println("Reader " + reader.info().readerTypeToString() + " connected.\n")

            val state = reader.config().readCompleteConfiguration(persistent);
            println("readCompleteConfiguration: " + ErrorCode.toString(state));

            connectionState.value = 1

        } catch (e: FedmRuntimeException) {
            println(e.message)
            connectionState.value = -1
        }
    }

    fun disconnect() = coroutineScope.launch(Dispatchers.IO) {
        try {
            reader.disconnect()

            if (reader.lastError() == ErrorCode.Ok) {
                println("\n" + reader.info().readerTypeToString() + " disconnected.")
            }
            connectionState.value = -1
        } catch (e: FedmRuntimeException) {
            println(e.message)
            connectionState.value = -1
        }
    }
}