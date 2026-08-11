package org.circle13.antara.core.network

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

interface DiscoveryService {
    fun startAdvertising(token: ByteArray, capabilities: Int)
    fun stopAdvertising()
    fun startScanning(): Flow<DiscoveredPeerEvent>
    fun stopScanning()
}

data class DiscoveredPeerEvent(
    val peerAddressHash: ByteArray,
    val signalStrengthRssi: Int,
    val deviceCapabilities: Int,
    val connectionParameters: Map<String, Any>
)

class DiscoveryServiceImpl(
    private val bluetoothAdapter: BluetoothAdapter?
) : DiscoveryService {

    private val serviceUuid = UUID.fromString("0000fd5a-0000-1000-8000-00805f9b34fb")
    private var advertiseCallback: AdvertiseCallback? = null
    private var scanCallback: ScanCallback? = null

    override fun startAdvertising(token: ByteArray, capabilities: Int) {
        try {
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
            val advertiser = bluetoothAdapter.bluetoothLeAdvertiser ?: return
            
            stopAdvertising() // Cancel existing advertisements first

            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(true)
                .build()

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceData(ParcelUuid(serviceUuid), token)
                .build()

            advertiseCallback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                    super.onStartSuccess(settingsInEffect)
                }
                override fun onStartFailure(errorCode: Int) {
                    super.onStartFailure(errorCode)
                }
            }

            advertiser.startAdvertising(settings, data, advertiseCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stopAdvertising() {
        try {
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
            val advertiser = bluetoothAdapter.bluetoothLeAdvertiser ?: return
            advertiseCallback?.let {
                advertiser.stopAdvertising(it)
                advertiseCallback = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun startScanning(): Flow<DiscoveredPeerEvent> = callbackFlow {
        try {
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                close()
                return@callbackFlow
            }
            val scanner = bluetoothAdapter.bluetoothLeScanner
            if (scanner == null) {
                close()
                return@callbackFlow
            }

            stopScanning()

            scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    result?.let {
                        val record = it.scanRecord
                        val serviceData = record?.getServiceData(ParcelUuid(serviceUuid))
                        if (serviceData != null) {
                            trySend(
                                DiscoveredPeerEvent(
                                    peerAddressHash = serviceData,
                                    signalStrengthRssi = it.rssi,
                                    deviceCapabilities = 0,
                                    connectionParameters = mapOf("device" to it.device)
                                )
                            )
                        }
                    }
                }
            }

            val filter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(serviceUuid))
                .build()

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()

            scanner.startScan(listOf(filter), settings, scanCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        awaitClose {
            stopScanning()
        }
    }

    override fun stopScanning() {
        try {
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
            val scanner = bluetoothAdapter.bluetoothLeScanner ?: return
            scanCallback?.let {
                scanner.stopScan(it)
                scanCallback = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
