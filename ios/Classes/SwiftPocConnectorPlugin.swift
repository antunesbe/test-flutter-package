import Flutter
import UIKit

public class SwiftPocConnectorPlugin: NSObject, FlutterPlugin {
    var session: URLSession?;
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "poc_connector", binaryMessenger: registrar.messenger())
    let instance = SwiftPocConnectorPlugin()
    instance.session = URLSession(configuration: .default, delegate: instance, delegateQueue: nil)
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {

    switch call.method {
      case "connectorGet":
        guard let args = call.arguments else {
            return
        }
        if let myArgs = args as? [String: Any],
           let url = myArgs["url"] as? String {
            self.get(url: url, completionHandler: { [weak self] (heroes) in
                result(heroes)

                // Reload the table view using the main dispatch queue
//                DispatchQueue.main.async {
//                  tableView.reloadData()
//                }
              })
        }
        break
      case "listDocuments":
        result([])
        return
      case "1":
        print("1")
      case "2":
        print("2")
      default:
        //result(FlutterMethodNotImplemented)
        return
    }
  }
    
    func get(url: String, completionHandler: @escaping (Any) -> Void) {
        
        let url = URL(string: url)!

        let task = self.session?.dataTask(with: url, completionHandler: { (data, response, error) in
          if let error = error {
            print("Error with fetching films: \(error)")
            return
          }
          
          guard let httpResponse = response as? HTTPURLResponse,
                (200...299).contains(httpResponse.statusCode) else {
            print("Error with the response, unexpected status code: \(response)")
            return
          }
            do {
//                let jsonResponse = try JSONSerialization.jsonObject(with: data!, options: [])
                let stringResponse = String(decoding: data!, as: UTF8.self)
//                if let dictionary = jsonResponse as? [String: Any] {
                    completionHandler(stringResponse);
                    // if let content = dictionary["content"] as? [Any] {
                    //     completionHandler(content)
                    // }
//                }
            
            }catch let parsingError {
                print("Error parsing")
                return
            }
            })
        task!.resume()
      }
}

struct Heroes: Codable {
    var content: Array<Hero>
}

struct Hero: Codable {
    var id: Int
    var name: String
}

extension SwiftPocConnectorPlugin : URLSessionDelegate {
    public func urlSession(
        _ session: URLSession,
        didReceive challenge: URLAuthenticationChallenge,
        completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void) {
           //Trust the certificate even if not valid
           let urlCredential = URLCredential(trust: challenge.protectionSpace.serverTrust!)

           completionHandler(.useCredential, urlCredential)
    }
}
