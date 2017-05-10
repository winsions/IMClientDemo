package com.company;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

/*
需要依赖
<dependency>
        <groupId>commons-net</groupId>
            <artifactId>commons-net</artifactId>
            <version>${commons-net.version}</version>
</dependency>

 */


/**
 * FTP工具类
 * Created by winsion on 2017/4/17.
 */
public class FtpUnit {


    /**
     * Description: 向FTP服务器上传文件
     *
     * @param hostName    FTP服务器hostname
     * @param port        FTP服务器端口
     * @param username    FTP登录账号
     * @param password    FTP登录密码
     * @param basePath    FTP服务器基础目录
     * @param filePath    FTP服务器文件存放路径。例如分日期存放：/2015/01/01。文件的路径为basePath+filePath
     * @param fileName    上传到FTP服务器上的文件名
     * @param inputStream 输入流
     * @return 成功返回true，否则返回false
     */
    public static boolean upLoadFile(String hostName, int port, String username, String password,
                                     InputStream inputStream, String basePath, String filePath, String fileName) throws IOException {
        boolean result = false;

        FTPClient ftpClient = new FTPClient();

        try {
            int reply;
            ftpClient.connect(hostName, port);
            ftpClient.login(username, password);
            reply = ftpClient.getReplyCode();
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return result;
            }
            if (!ftpClient.changeWorkingDirectory(basePath + filePath)) {
                //创建目录
                String[] dirs = filePath.split("/");
                String temppath = basePath;
                for (String dir : dirs) {
                    if (dir == null || "".equals(dir)) continue;
                    temppath += "/" + dir;
                    if (!ftpClient.changeWorkingDirectory(temppath)) {
                        if (!ftpClient.makeDirectory(temppath)) {
                            return result;
                        } else {
                            ftpClient.changeWorkingDirectory(temppath);
                        }
                    }
                }
            }
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            if (!ftpClient.storeFile(fileName, inputStream)) {
                return result;
            }
            ftpClient.logout();
            inputStream.close();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return result;

    }


    /**
     * Description: 从FTP服务器下载文件
     *
     * @param host       FTP服务器hostname
     * @param port       FTP服务器端口
     * @param username   FTP登录账号
     * @param password   FTP登录密码
     * @param remotePath FTP服务器上的相对路径
     * @param fileName   要下载的文件名
     * @param localPath  下载后保存到本地的路径
     * @return
     */
    public static boolean downloadFile(String host, int port, String username, String password, String remotePath,
                                       String fileName, String localPath) {
        boolean result = false;
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(host, port);
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return result;
            }
            ftp.setControlEncoding("UTF-8");
            ftp.enterLocalPassiveMode();
            ftp.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
            ftp.changeWorkingDirectory(remotePath);// 转移到FTP服务器目录
            FTPFile[] fs = ftp.listFiles();

            if (fs == null) return result;
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
                    File localFile = new File(localPath + "/" + ff.getName());
                    OutputStream outputStream = new FileOutputStream(localFile);
                    String ftpPath = remotePath +"/" + ff.getName();
                    boolean res = ftp.retrieveFile(ftpPath, outputStream);
                    outputStream.close();
                    if (!res) return result;
                    break;
                }
            }

            ftp.logout();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return result;
    }


    public static void main(String[] args) throws IOException {

        //上传
        FileInputStream inputStream = new FileInputStream(new File("/Users/winsion/Desktop/王子臣 .doc"));
      boolean b =  upLoadFile("192.168.239.137",21,"winsionsFtp",
                "Wzc19920506",inputStream,"/home/winsionsFtp",
                "/2015/01/21","王子臣 .doc");

        System.out.println(b);


        //下载
//        boolean x = downloadFile("192.168.239.137", 21, "winsionsFtp", "Wzc19920506", "/home/winsionsFtp", "WechatIMG4577.png", "/Users/winsion/Desktop");
//        System.out.println(x);

    }
}
