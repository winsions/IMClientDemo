//
//  ViewController.swift
//  IM客户端
//
//  Created by 梁奎元 on 2017/5/10.
//  Copyright © 2017年 九章软件. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    @IBOutlet weak var stateLable: UILabel!
    
   fileprivate  lazy var clientSocket : GCDAsyncSocket = {
      
        let socket = GCDAsyncSocket()
        socket?.delegate = self
        socket?.delegateQueue = DispatchQueue.main
         return socket!
    }()
    



    ///  连接服务器
    @IBAction func connectToHost(_ sender: Any) {
        

       try?  clientSocket.connect(toHost: "119.23.248.224", onPort: 9091)
    }

     /// 断开服务器
    @IBAction func closeToHost(_ sender: Any) {
        clientSocket.disconnect()
    }
    
     ///  发送文字
    @IBAction func sendText(_ sender: Any) {
        
        let text = "Hello,自定义协议-----0508"
        
        let textData = text.data(using: .utf8)
        
        guard let textData2 = textData  else {
            return
        }
        JZChatMessageTool.sendData(sendData:  textData2, commandId: 2, client: clientSocket)
    }
    
     ///  发送图片
    @IBAction func sendImag(_ sender: Any) {
        
        let image = UIImage(named:"testImage")
        
        guard let sendImage = image else {
            print("没有加载到图片")
            return
        }
        
        let imageData_ = UIImagePNGRepresentation(sendImage)
        
        guard let imageData = imageData_ else {
            return
        }
         JZChatMessageTool.sendData(sendData:  imageData, commandId: 1, client: clientSocket)
    }   
}










/// 代理
extension ViewController : GCDAsyncSocketDelegate{

    // 与服务器连接成功
    func socket(_ sock: GCDAsyncSocket!, didConnectToHost host: String!, port: UInt16) {
        
        stateLable.text = "连接中.."
        stateLable.backgroundColor = UIColor.green
          //  保证能接收到数据
        sock.readData(withTimeout: -1, tag: 0)
        
    }
    
    // 接收服务器响应的数据
    func socket(_ sock: GCDAsyncSocket!, didRead data: Data!, withTag tag: Int) {
       
        var totalSize = 0
        var commandId = 0
        var result = 0
        
        
        (data as NSData).getBytes(&totalSize, range: NSMakeRange(0, 4))
        (data as NSData).getBytes(&commandId, range: NSMakeRange(4, 4))
        (data as NSData).getBytes(&result, range: NSMakeRange(8, 4))
        
        
        var str = ""
        
        if commandId == 0x00000001 {
            str = "图片"
        }else if commandId == 0x00000002{
            str = "文字"
        }
        
        if result == 1
        {
            str.append("上传成功")
        }else
        {
           str.append("上传失败")
        }
        
        print("\(str)")
        
    
        clientSocket.readData(withTimeout: -1, tag: 0)
        
    }
    
    
    // 与服务器连接断开
    func socketDidDisconnect(_ sock: GCDAsyncSocket!, withError err: Error!) {
        stateLable.text = "断开.."
        stateLable.backgroundColor = UIColor.red
    }
    

}
