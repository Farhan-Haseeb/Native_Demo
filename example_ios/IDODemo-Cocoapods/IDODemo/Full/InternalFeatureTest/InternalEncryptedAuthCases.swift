//
//  InternalEncryptedAuthCases.swift
//  IDODemo (native Full temp — temp sync to native Full)
//
//  Encrypted auth: setEncryptedAuthData before connecting + mark as bound locally (integration test case).
//

import Foundation
import protocol_channel

enum InternalEncryptedAuthCases {

    /// C library JSON (primary key auth_data); can be replaced during integration testing.
    static let sampleJson = """
    {"auth_data":[80,61,187,119,250,50,234,10,21,16,32,9],"encrypted_version":0,"auth_length":12}
    """

    static func parseSampleJson(_ json: String = sampleJson) -> IDOEncryptedAuthData? {
        return IDOEncryptedAuthData(jsonString: json)
    }
}
