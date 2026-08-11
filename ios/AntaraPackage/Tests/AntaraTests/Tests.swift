import XCTest
@testable import AntaraCore
@testable import AntaraCrypto
@testable import AntaraDatabase
@testable import AntaraNetwork

final class AntaraTests: XCTestCase {
    func testExample() throws {
        XCTAssertEqual(AntaraCore.version, "1.0.0")
    }
}
