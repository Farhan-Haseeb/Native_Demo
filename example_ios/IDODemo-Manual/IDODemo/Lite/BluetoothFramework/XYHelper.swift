//
//  XYHelper.swift
//  XYCBCentralManager
//
//  Created by macvivi on 2021/1/4.
//

import UIKit

class XYHelper: NSObject {
    //MARK: Get a view controller from the storyboard
    //storyboardStr: storyboard name; nil means Main
    //viewController: name of the view controller
    static func getViewController(storyboardStr: String?, viewController: String) -> UIViewController {
        var storyboard = UIStoryboard()
        if let storyboardStr = storyboardStr {
            storyboard =  UIStoryboard.init(name: storyboardStr, bundle: nil)
        }else {
            storyboard =  UIStoryboard.init(name: "Main", bundle: nil)
        }
        let vc = storyboard.instantiateViewController(withIdentifier: viewController)
        return vc
    }
}

extension UIView {
    //MARK: UIView's width,height,x,y
    var width: CGFloat {
        self.frame.size.width
    }
    var height: CGFloat {
        self.frame.size.height
    }
    var x: CGFloat {
        self.frame.origin.x
    }
    var y: CGFloat {
        self.frame.origin.y
    }
}

//MARK: Print with time, class and line number
//The last two parameters are always self and #line
//printXY("Connected", obj: self, line: #line)
func printXY(_ any:Any,obj:Any,line:Int) {
    let date = Date()
     let timeFormatter = DateFormatter()
     //Date display format, adjust as needed
    let strNowTime = getCurrentTimeWithDateFormatString("HH:mm:ss.SSS")
     print("\(strNowTime) \(type(of: obj)) \(line) \(any)")
}

//MARK: Get the current time and date
func getCurrentTimeWithDateFormatString(_ dateFormat : String) -> String{
    let date = Date()
     let timeFormatter = DateFormatter()
     //Date display format, adjust as needed
     timeFormatter.dateFormat = dateFormat
     let strNowTime = timeFormatter.string(from: date) as String
    return strNowTime
}

extension Data {
//MARK: Data to hex string
    /// Create hexadecimal string representation of `Data` object.
    ///
    /// - returns: `String` representation of this `Data` object.

    func hexadecimal() -> String {
        return map { String(format: "%02x", $0) }
            .joined(separator: "")
    }
}

extension String {
//MARK:Hex string to Data
    /// Create `Data` from hexadecimal string representation
    ///
    /// This takes a hexadecimal representation and creates a `Data` object. Note, if the string has any spaces or non-hex characters (e.g. starts with '<' and with a '>'), those are ignored and only hex characters are processed.
    ///
    /// - returns: Data represented by this hexadecimal string.

    func hexadecimal() -> Data? {
        var data = Data(capacity: self.count / 2)

        let regex = try! NSRegularExpression(pattern: "[0-9a-f]{1,2}", options: .caseInsensitive)
        regex.enumerateMatches(in: self, range: NSMakeRange(0, utf16.count)) { match, flags, stop in
            let byteString = (self as NSString).substring(with: match!.range)
            var num = UInt8(byteString, radix: 16)!
            data.append(&num, count: 1)
        }

        guard data.count > 0 else { return nil }

        return data
    }

}
