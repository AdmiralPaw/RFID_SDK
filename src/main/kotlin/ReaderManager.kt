import androidx.compose.runtime.MutableState
import de.feig.fedm.Connector
import de.feig.fedm.ErrorCode
import de.feig.fedm.ReaderModule
import de.feig.fedm.RequestMode
import de.feig.fedm.exception.FedmRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import notify.NotifyManager

class ReaderManager(
    private val debugText: MutableState<String>,
    private val coroutineScope: CoroutineScope,
    private val antennaTimeoutMs: MutableState<Long>
) {
    val reader = ReaderModule(RequestMode.UniDirectional)
    val ipAddr = "192.168.10.10"
    val port = 10001
    val notifyManager = NotifyManager(debugText, coroutineScope, antennaTimeoutMs)

    fun start() = coroutineScope.launch {
        notifyManager.start()
        connect(ipAddr, port)
    }

    fun stop() = coroutineScope.launch {
        notifyManager.stop()
        disconnect()
    }


    suspend fun connect(ipAddr: String, port: Int) {
        try {
            val connector = Connector.createTcpConnector(ipAddr, port)
            println("Start connection with Reader: " + connector.tcpIpAddress())
            reader.connect(connector)

            if (reader.lastError() != ErrorCode.Ok) {
                println("Error while Connecting: " + reader.lastError())
                println(reader.lastErrorText())
                return
            }

            println("Reader " + reader.info().readerTypeToString() + " connected.\n")
        } catch (e: FedmRuntimeException) {
            println(e.message)
        }
    }

    suspend fun disconnect() {
        try {
            reader.disconnect()

            if (reader.lastError() == ErrorCode.Ok) {
                println("\n" + reader.info().readerTypeToString() + " disconnected.")
            }
        } catch (e: FedmRuntimeException) {
            println(e.message)
        }
    }
}