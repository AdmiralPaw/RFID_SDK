import data.*
import de.feig.fedm.BrmItem
import de.feig.fedm.utility.HexConvert

/**
 * @param brmItem
 * @return Content of the BRM-Item as string
 */
fun brmItemToDataItem(brmItem: BrmItem): BrmItemData {
    val brmDate = if (brmItem.dateTime().isValidDate)
        BrmDate(
            day = brmItem.dateTime().day(),
            month = brmItem.dateTime().month(),
            year = brmItem.dateTime().year(),
        )
    else null

    val brmTime = if (brmItem.dateTime().isValidTime)
        BrmTime(
            hour = brmItem.dateTime().hour(),
            minute = brmItem.dateTime().minute(),
            second = brmItem.dateTime().second(),
            milliSecond = brmItem.dateTime().milliSecond(),
        )
    else null

    val rssiList = mutableListOf<RSSI>()
    val idd = if (brmItem.tag().isValid) {
        val list = brmItem.tag().rssiValues()
        for (rssiItem in list) {
            rssiList.add(
                RSSI(
                    rssi = if (rssiItem.isValid) rssiItem.rssi() else null,
                    phaseAngel = rssiItem.phaseAngle()
                )
            )
        }
        brmItem.tag().iddToHexString()
    } else null

    val brmData = if (brmItem.dataBlocks().isValid) {
        BrmData(
            data = HexConvert.toHexString(brmItem.dataBlocks().blocks(), " "),
            blockCount = brmItem.dataBlocks().blockCount(),
            blockSize = brmItem.dataBlocks().blockSize(),
        )
    } else null

    val mac = if (brmItem.mac().isValid) {
        HexConvert.toHexString(brmItem.mac().macAddress(), ":")
    } else null

    val brmInput = if (brmItem.input().isValid) {
        BrmInput(
            input = brmItem.input().input(),
            state = brmItem.input().state(),
        )
    } else null

    val deviceInfo = if (brmItem.scannerId().isValid) {
        DeviceInfo(
            id = brmItem.scannerId().scannerId(),
            type = if (brmItem.scannerId().type() == BrmItem.SectorScannerId.TypeDeviceId) {
                DeviceInfo.Type.Device
            } else {
                DeviceInfo.Type.Scanner
            }
        )
    } else null

    return BrmItemData(
        date = brmDate,
        time = brmTime,
        idd = idd,
        rssiList = rssiList,
        data = brmData,
        mac = mac,
        input = brmInput,
        deviceInfo = deviceInfo,
    )
}