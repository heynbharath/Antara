// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "AntaraPackage",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(
            name: "AntaraPackage",
            targets: ["AntaraCore", "AntaraCrypto", "AntaraDatabase", "AntaraNetwork", "AntaraUI"]
        ),
    ],
    dependencies: [
        // Swift Protobuf dependency
        .package(url: "https://github.com/apple/swift-protobuf.git", from: "1.25.2"),
        // GRDB SQLite wrapper (supports SQLCipher)
        .package(url: "https://github.com/groue/GRDB.swift.git", from: "6.24.2")
    ],
    targets: [
        .target(
            name: "AntaraCore",
            dependencies: [
                .product(name: "SwiftProtobuf", package: "swift-protobuf")
            ],
            exclude: ["antara.proto"]
        ),
        .target(
            name: "AntaraCrypto",
            dependencies: [
                "AntaraCore"
            ]
        ),
        .target(
            name: "AntaraDatabase",
            dependencies: [
                "AntaraCore",
                .product(name: "GRDB", package: "GRDB.swift")
            ]
        ),
        .target(
            name: "AntaraNetwork",
            dependencies: [
                "AntaraCore"
            ]
        ),
        .target(
            name: "AntaraUI",
            dependencies: [
                "AntaraCore",
                "AntaraNetwork"
            ]
        ),
        .testTarget(
            name: "AntaraTests",
            dependencies: ["AntaraCore", "AntaraCrypto", "AntaraDatabase", "AntaraNetwork"]
        ),
    ]
)
