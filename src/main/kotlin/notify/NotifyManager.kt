package notify

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import brmItemToDataItem
import data.Antenna
import data.BrmItemData
import de.feig.fedm.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class NotifyManager(
    var debugText: MutableState<String>,
    val coroutineScope: CoroutineScope,
    var antennaTimeoutMs: MutableState<Long>
) : IConnectListener, IReaderListener {

    private lateinit var reader: ReaderModule
    private val port = 20002 // Set Port-Number
    private val keepAlive = false // Set Keep-Alive on/off
    private var offTrigger = true

    var itemList = mutableStateListOf<BrmItemData>(
        BrmItemData(idd = "E1040102401230021304012314125123").apply { antennas.add(Antenna(123)) }
    )

    fun start() {
        offTrigger = true
        coroutineScope.launch { run() }
    }

    private suspend fun run() {
        var state: Int
        try {
            reader = ReaderModule(RequestMode.UniDirectional)
            state = reader.async().startNotification(this)
            debugText.value += "startNotification: " + ErrorCode.toString(state) + "\n"
            state = reader.startListenerThread(ListenerParam.createTcpListenerParam(port, keepAlive), this)
            debugText.value += "startListenerThread: " + ErrorCode.toString(state) + "\n"
            debugText.value += "Press any key to close" + "\n"
            while (offTrigger) {
                delay(0)
                //itemList.add(BrmItemData(idd = random().toString()))
            }
            state = reader.stopListenerThread()
            debugText.value += "stopListenerThread: " + ErrorCode.toString(state) + "\n"
            state = reader.async().stopNotification()
            debugText.value += "stopNotification: " + ErrorCode.toString(state) + "\n"
        } finally {
            reader.close()
        }
    }

    fun stop() {
        offTrigger = false
    }

    override fun onNewRequest() {
        debugText.value += "IReaderListener: NewRequest" + "\n"
        val eventType: EventType = reader.async().popEvent()
        if (eventType === EventType.BrmEvent) {
            while (reader.brm().queueItemCount() > 0) {
                val brmItem = reader.brm().popItem() ?: break

                val brmItemData = brmItemToDataItem(brmItem)
                val antennaId = if (brmItem.antennas().isValid) {
                    brmItem.antennas().antennas()
                } else null

                if (antennaId != null) {
                    brmItemData.antennas.add(Antenna(antennaId))
                }

                itemList.add(brmItemData)
            }
        }
    }

    override fun onConnect(peerInfo: PeerInfo) {
        debugText.value += "IConnectListener: Reader connected (" + peerInfo.ipAddress() + ":" + port + ")" + "\n"
    }

    override fun onDisconnect() {
        debugText.value += "IConnectListener: Reader disconnected" + "\n"
    }
}