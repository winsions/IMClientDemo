package com.company.serverApp;

/**
 * Created by winsion on 2017/5/8.
 */
import com.company.*;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.company.Main.*;


public class ServerResponseTask implements Runnable {

    private ReciveTask reciveTask;
    private SendTask sendTask;
    private Socket socket;
    private ResponseCallback tBack;

    private volatile ConcurrentLinkedQueue<BasicProtocol> dataQueue = new ConcurrentLinkedQueue<>();
    private static ConcurrentHashMap<String, Socket> onLineClient = new ConcurrentHashMap<>();

    private String userIP;

    public String getUserIP() {
        return userIP;
    }

    public ServerResponseTask(Socket socket, ResponseCallback tBack) {
        this.socket = socket;
        this.tBack = tBack;
        this.userIP = socket.getInetAddress().getHostAddress();
        System.out.println("用户IP地址：" + userIP);
    }

    @Override
    public void run() {
        try {
            //开启接收线程
            reciveTask = new ReciveTask();
            reciveTask.inputStream = new DataInputStream(socket.getInputStream());
            reciveTask.start();

            //开启发送线程
            sendTask = new SendTask();
            sendTask.outputStream = new DataOutputStream(socket.getOutputStream());
            sendTask.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (reciveTask != null) {
            reciveTask.isCancle = true;
            reciveTask.interrupt();
            if (reciveTask.inputStream != null) {
                SocketUtil.closeInputStream(reciveTask.inputStream);
                reciveTask.inputStream = null;
            }
            reciveTask = null;
        }

        if (sendTask != null) {
            sendTask.isCancle = true;
            sendTask.interrupt();
            if (sendTask.outputStream != null) {
                synchronized (sendTask.outputStream) {//防止写数据时停止，写完再停
                    sendTask.outputStream = null;
                }
            }
            sendTask = null;
        }
    }

    public void addMessage(BasicProtocol data) {
        if (!isConnected()) {
            return;
        }

        dataQueue.offer(data);
        toNotifyAll(dataQueue);//有新增待发送数据，则唤醒发送线程
    }

    public Socket getConnectdClient(String clientID) {
        return onLineClient.get(clientID);
    }

    /**
     * 打印已经链接的客户端
     */
        public static void printAllClient() {
        if (onLineClient == null) {
            return;
        }
        Iterator<String> inter = onLineClient.keySet().iterator();
        while (inter.hasNext()) {
            System.out.println("client:" + inter.next());
        }
    }

    public void toWaitAll(Object o) {
        synchronized (o) {
            try {
                o.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void toNotifyAll(Object obj) {
        synchronized (obj) {
            obj.notifyAll();
        }
    }

    private boolean isConnected() {
        if (socket.isClosed() || !socket.isConnected()) {
            onLineClient.remove(userIP);
            ServerResponseTask.this.stop();
            System.out.println("socket closed...");
            return false;
        }
        return true;
    }

    public class ReciveTask extends Thread {

        private DataInputStream inputStream;
        private boolean isCancle;

        @Override
        public void run() {
            while (!isCancle) {
                if (!isConnected()) {
                    isCancle = true;
                    break;
                }


                readFromStreamMain(inputStream);

//                BasicProtocol clientData = SocketUtil.readFromStream(inputStream);
//                if (clientData != null) {
//                    if (clientData.getProtocolType() == 0) {
//                        System.out.println("dtype: " + ((DataProtocol) clientData).getDtype() + ", pattion: " + ((DataProtocol) clientData).getPattion() + ", msgId: " + ((DataProtocol) clientData).getMsgId() + ", data: " + ((DataProtocol) clientData).getData());
//
//                        DataAckProtocol dataAck = new DataAckProtocol();
//                        dataAck.setUnused("收到消息：" + ((DataProtocol) clientData).getData());
//                        dataQueue.offer(dataAck);
//                        toNotifyAll(dataQueue); //唤醒发送线程
//
//                        tBack.targetIsOnline(userIP);
//                    } else if (clientData.getProtocolType() == 2) {
//                        System.out.println("pingId: " + ((PingProtocol) clientData).getPingId());
//
//                        PingAckProtocol pingAck = new PingAckProtocol();
//                        pingAck.setUnused("收到心跳");
//                        dataQueue.offer(pingAck);
//                        toNotifyAll(dataQueue); //唤醒发送线程
//
//                        tBack.targetIsOnline(userIP);
//                    }
//                } else {
//                    System.out.println("client is offline...");
//                    break;
//                }
            }

            SocketUtil.closeInputStream(inputStream);
        }
    }

    public class SendTask extends Thread {

        private DataOutputStream outputStream;
        private boolean isCancle;

        @Override
        public void run() {
            while (!isCancle) {
                if (!isConnected()) {
                    isCancle = true;
                    break;
                }

                BasicProtocol procotol = dataQueue.poll();
                if (procotol == null) {
                    toWaitAll(dataQueue);
                } else if (outputStream != null) {
                    synchronized (outputStream) {
                        //原来的
//                        SocketUtil.write2Stream(procotol, outputStream);
                        //我的解析
                        SocketUtil.write2StreamMy(procotol, outputStream);
                    }
                }
            }

            SocketUtil.closeOutputStream(outputStream);
        }
    }


    /**
     * 读数据
     *
     * @param inputStream
     * @return
     * @throws
     */
    public void readFromStreamMain(InputStream inputStream) {
        BufferedInputStream bis;
        int commandId = 0;
        //header中保存的是整个数据的长度值，4个字节表示。在下述write2Stream方法中，会先写入header
        int lengheader = 4;
        byte[] header = new byte[lengheader];

        try {
            bis = new BufferedInputStream(inputStream);
            int temp;
            int len = 0;
            while (len < header.length) {
                temp = bis.read(header, len, header.length - len);
                if (temp > 0) {
                    len += temp;
                } else if (temp == -1) {
                    bis.close();
                }
            }

            len = 0;

            int length = bytesToInt(header, 0);//数据的长度值
            byte[] content = new byte[length - 4];
            while (len < length - 4) {
                System.out.println(len);
                temp = bis.read(content, len, length - len - 4);

                System.out.println(temp);
                if (temp > 0) {
                    len += temp;
                }else if (temp == -1){
                    break;
                }

            }

            byte bufcommandId[] = subBytes(content, 0, 4);
            commandId = bytesToInt(bufcommandId, 0);
            content = subBytes(content, 4, length - 8);
            if (commandId == 2) {
                StringBuilder sB = new StringBuilder();
                sB.append(new String(content, 0, length - 8));
                System.out.println(sB.toString());

            } else if (commandId == 1) {
                SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd-HH:mm:ss");
//                byte2image(content, "/Users/winsion/Desktop/未命名文件夹/fjjfj.png");
                String  str = "/home/fsdf.JPG";
//                byte2image(content,str);
                InputStream sbs = new ByteArrayInputStream(content);


                savePicture(sbs);



                System.out.println(content.length);

            }

            DataAckProtocol dataAck = new DataAckProtocol();
            dataAck.setVersion(commandId);
//            dataAck.setUnused("收到消息：" + ((DataProtocol) clientData).getData());
            dataQueue.offer(dataAck);
            toNotifyAll(dataQueue); //唤醒发送线程

//            //1.总的字节长度
//            int totalSize = 4 + 4 + 4;
//            //2.响应指令类型
//
//
//            byte[] resssssss = intToBytes(totalSize);
//
//            byte resule[] = byteMerger(byteMerger(intToBytes(totalSize), intToBytes(commandId)), intToBytes(1));
//
//            //3.上传的结果 //1:上传成功 0://上传失败
//
//            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
//            try {
//                bufferedOutputStream.write(intToBytes(totalSize));
//                bufferedOutputStream.write(intToBytes(commandId));
//                bufferedOutputStream.write(intToBytes(1));
//                bufferedOutputStream.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    private void savePicture(InputStream inputStream) {
//        //上传文件功能
//
////        //判断文件是否为空
////        if (uploadFile.isEmpty()){
////
////        }
//
//        // 上传文件以日期为单位分开存放，可以提高图片的查询速度
//
//        String filePath = "/" + new SimpleDateFormat("yyyy").format(new Date()) + "/"
//                + new SimpleDateFormat("MM").format(new Date()) + "/"
//                + new SimpleDateFormat("dd").format(new Date());
//
//        // 取原始文件名
//        String originalFilename = uploadFile.getOriginalFilename();
//
//        //新文件名
//        String newFileName = imageNameCreate.getImageName()+".png";
//        //上传FTP
        boolean result = false;
        try {
            result = FtpUnit.upLoadFile("119.23.248.224",21,"winsionsFtp","Wzc19920506",
                    inputStream,"/home/winsionsFtp","/IM","picture.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}