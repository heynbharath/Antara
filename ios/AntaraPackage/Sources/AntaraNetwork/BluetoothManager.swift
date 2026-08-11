import Foundation
import CoreBluetooth
import AntaraCore

public final class BluetoothManager: NSObject, CBCentralManagerDelegate, CBPeripheralManagerDelegate {
    
    private var centralManager: CBCentralManager!
    private var peripheralManager: CBPeripheralManager!
    
    private let serviceUUID = CBUUID(string: "0000fd5a-0000-1000-8000-00805f9b34fb")
    private var discoveredPeers = [UUID: CBPeripheral]()
    
    // Callback flow for discovered peers
    public var onPeerDiscovered: ((UUID, Data) -> Void)?
    
    public override init() {
        super.init()
        self.centralManager = CBCentralManager(delegate: self, queue: nil, options: [
            CBCentralManagerOptionRestoreIdentifierKey: "AntaraCentralRestorationKey"
        ])
        self.peripheralManager = CBPeripheralManager(delegate: self, queue: nil, options: [
            CBPeripheralManagerOptionRestoreIdentifierKey: "AntaraPeripheralRestorationKey"
        ])
    }
    
    // MARK: - CBCentralManagerDelegate
    
    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn {
            // Background scan must explicitly target our Service UUID
            centralManager.scanForPeripherals(withServices: [serviceUUID], options: [
                CBCentralManagerScanOptionAllowDuplicatesKey: NSNumber(value: false)
            ])
        }
    }
    
    public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        // Extract Service Data containing discovery token
        if let serviceDataDict = advertisementData[CBAdvertisementDataServiceDataKey] as? [CBUUID: Data],
           let token = serviceDataDict[serviceUUID] {
            discoveredPeers[peripheral.identifier] = peripheral
            onPeerDiscovered?(peripheral.identifier, token)
        }
    }
    
    public func centralManager(_ central: CBCentralManager, willRestoreState dict: [String : Any]) {
        // CoreBluetooth calls this when restoring background operations
        if let restoredPeripherals = dict[CBCentralManagerRestoredStatePeripheralsKey] as? [CBPeripheral] {
            for peripheral in restoredPeripherals {
                discoveredPeers[peripheral.identifier] = peripheral
            }
        }
    }
    
    // MARK: - CBPeripheralManagerDelegate
    
    public func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        if peripheral.state == .poweredOn {
            startAdvertisingToken(Data(repeating: 0, count: 16)) // Default token
        }
    }
    
    public func startAdvertisingToken(_ token: Data) {
        guard peripheralManager.state == .poweredOn else { return }
        
        peripheralManager.stopAdvertising()
        
        // Setup service and characteristic
        let characteristic = CBMutableCharacteristic(
            type: CBUUID(string: "00002a24-0000-1000-8000-00805f9b34fb"),
            properties: [.read, .write],
            value: nil,
            permissions: [.readable, .writeable]
        )
        
        let service = CBMutableService(type: serviceUUID, primary: true)
        service.characteristics = [characteristic]
        
        peripheralManager.add(service)
        
        // Configure advertisement data dictionary
        let advertisementData: [String: Any] = [
            CBAdvertisementDataServiceUUIDsKey: [serviceUUID],
            CBAdvertisementDataServiceDataKey: [serviceUUID: token]
        ]
        
        peripheralManager.startAdvertising(advertisementData)
    }
    
    public func peripheralManager(_ peripheral: CBPeripheralManager, willRestoreState dict: [String : Any]) {
        // Restores active BLE services in the background
    }
}
