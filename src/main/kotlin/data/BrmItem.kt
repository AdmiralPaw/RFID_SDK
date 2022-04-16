package data

import kotlinx.coroutines.Job

data class BrmDate(
    val day: Int,
    val month: Int,
    val year: Int,
)

data class BrmTime(
    val hour: Int,
    val minute: Int,
    val second: Int,
    val milliSecond: Int,
)

data class BrmData(
    val data: String,
    val blockCount: Long,
    val blockSize: Long,
)

data class BrmInput(
    val input: Long,
    val state: Int,
)

data class RSSI(
    val rssi: Int? = null,
    val phaseAngel: Float? = null,
)

data class DeviceInfo(
    val id: String,
    val type: Type,
) {
    enum class Type {
        Device,
        Scanner,
    }
}

data class Antenna(
    val id: Long?,
    var timeout: Job? = null
)

data class BrmItemData(
    val date: BrmDate? = null,
    val time: BrmTime? = null,
    var idd: String? = null,
    var rssiList: List<RSSI>? = null,
    val data: BrmData? = null,
    val antennas: MutableList<Antenna> = mutableListOf(),
    val mac: String? = null,
    val input: BrmInput? = null,
    val deviceInfo: DeviceInfo? = null,
)