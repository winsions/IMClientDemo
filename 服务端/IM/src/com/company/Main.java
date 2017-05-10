package com.company;

import javax.imageio.stream.FileImageOutputStream;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Main {

    public static void main(String[] args) throws IOException {


        ServerSocket server = new ServerSocket(9091);

        while (true) {
            Socket socket = server.accept();
            invoke(socket);
        }
    }

    private static void invoke(final Socket client) throws IOException {
        new Thread(new Runnable() {
            public void run() {


                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = client.getInputStream();
                    outputStream = client.getOutputStream();
                    readFromStream(inputStream,outputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                finally {
//                    try {
//                        inputStream.close();
//                    } catch (Exception e) {}
//                    try {
//                        outputStream.close();
//                    } catch (Exception e) {}
//                    try {
//                        client.close();
//                    } catch (Exception e) {}
//                }


//                BufferedReader in = null;
//                PrintWriter out = null;
//                InputStream inputStream = null;
//                try {
//                    in = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
////                    out = new PrintWriter(client.getOutputStream());
//
//                    inputStream = client.getInputStream();
//
//                    byte buf[] = new byte[10240];
//                    int len = 0;
//                    int totalLen = 0;
//                    int oldlLen = 0;
//                    int commandId = 0;
//                    byte bufimage[] = null;
//                    String str = null;
//                    while ((len = (inputStream.read(buf))) > 0) {
//
//                        if (totalLen == 0) {
////
//                            byte buff[] = subBytes(buf, 0, 4);
//                            oldlLen = bytesToInt(buff, 0);
////                            System.out.println("oldlLen" + oldlLen);
//
//                            byte bufcommandId[] = subBytes(buf, 4, 4);
//                            commandId = bytesToInt(bufcommandId, 0);
//                            bufimage = subBytes(buf, 8, len - 8);
//                            System.out.println(bufimage.length);
//                        } else {
//                            System.out.println(buf.length);
//                            System.out.println(bufimage.length);
//
//                            bufimage = byteMerger(bufimage, buf);
//                            System.out.println(bufimage.length);
//                        }
//
//                        totalLen = len + totalLen;
//                        System.out.println("len" + len);
//                        if (totalLen == oldlLen) {
//                            totalLen = 0;
//                            if (commandId == 2) {
//                                StringBuilder sB = new StringBuilder();
//                                sB.append(new String(bufimage, 0, oldlLen - 8));
//                                System.out.println(sB.toString());
//
//                                str = "文字:" + sB.toString();
//                            } else if (commandId == 1) {
//                                SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd-HH:mm:ss");
//                                byte2image(bufimage, "/Users/winsion/Desktop/未命名文件夹/fjjfj.png");
////                                str = "/home/fsdf.JPG";
////                                byte2image(bufimage,str);
////                                System.out.println(bufimage.length);
//                                str = "图片:" + str;
//
//                            }
//                            //1.总的字节长度
//                            int totalSize = 4 + 4 + 4;
//                            //2.响应指令类型
//
//
//                            byte[] resssssss = intToBytes(totalSize);
//
//                            byte resule[] = byteMerger(byteMerger(intToBytes(totalSize), intToBytes(commandId)), intToBytes(1));
//
//                            //3.上传的结果 //1:上传成功 0://上传失败
//                            System.out.println(str);
//
//
//                            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(client.getOutputStream());
//                            try {
//                                bufferedOutputStream.write(intToBytes(totalSize));
//                                bufferedOutputStream.write(intToBytes(commandId));
//                                bufferedOutputStream.write(intToBytes(1));
//                                bufferedOutputStream.flush();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                    }
//
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                } finally {
//                    try {
//                        in.close();
//                    } catch (Exception e) {
//                    }
//                    try {
//                        out.close();
//                    } catch (Exception e) {
//                    }
//                    try {
//                        client.close();
//                    } catch (Exception e) {
//                    }
//                }


            }
        }).start();
    }


    public static int bytesToInt(byte[] ary, int offset) {
        int value;
        value = (int) ((ary[offset] & 0xFF)
                | ((ary[offset + 1] << 8) & 0xFF00)
                | ((ary[offset + 2] << 16) & 0xFF0000)
                | ((ary[offset + 3] << 24) & 0xFF000000));
        return value;
    }


    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。
     *
     * @param value 要转换的int值
     * @return byte数组
     */
    public static byte[] intToBytes(int value) {
        byte[] byte_src = new byte[4];
        byte_src[3] = (byte) ((value & 0xFF000000) >> 24);
        byte_src[2] = (byte) ((value & 0x00FF0000) >> 16);
        byte_src[1] = (byte) ((value & 0x0000FF00) >> 8);
        byte_src[0] = (byte) ((value & 0x000000FF));
        return byte_src;
    }


    //java 合并两个byte数组
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }

    //参数是图片的路径
    //byte数组到图片到硬盘上
    public static void byte2image(byte[] data, String path) {
        if (data.length < 3 || path.equals("")) return;//判断输入的byte是否为空
        try {
            FileImageOutputStream imageOutput = new FileImageOutputStream(new File(path));//打开输入流
            imageOutput.write(data, 0, data.length);//将byte写入硬盘
            imageOutput.close();
            System.out.println("Make Picture success,Please find image in " + path);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace();
        }
    }


    /**
     * 读数据
     *
     * @param inputStream
     * @return
     * @throws
     */
    public static void readFromStream(InputStream inputStream, OutputStream outputStream) {
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

                if (temp > 0) {
                    len += temp;
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
                                byte2image(content,str);
                                System.out.println(content.length);

            }

            //1.总的字节长度
            int totalSize = 4 + 4 + 4;
            //2.响应指令类型


            byte[] resssssss = intToBytes(totalSize);

            byte resule[] = byteMerger(byteMerger(intToBytes(totalSize), intToBytes(commandId)), intToBytes(1));

            //3.上传的结果 //1:上传成功 0://上传失败

            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            try {
                bufferedOutputStream.write(intToBytes(totalSize));
                bufferedOutputStream.write(intToBytes(commandId));
                bufferedOutputStream.write(intToBytes(1));
                bufferedOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
