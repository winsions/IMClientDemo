package com.company;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.company.Main.byteMerger;
import static com.company.Main.intToBytes;

/**
 * Created by winsion on 2017/5/8.
 */
public class SocketUtil {
    private static Map<Integer, String> msgImp = new HashMap<>();

    static {
        msgImp.put(DataProtocol.PROTOCOL_TYPE, "com.shandiangou.sdgprotocol.lib.protocol.DataProtocol");       //0
        msgImp.put(DataAckProtocol.PROTOCOL_TYPE, "com.shandiangou.sdgprotocol.lib.protocol.DataAckProtocol"); //1
        msgImp.put(PingProtocol.PROTOCOL_TYPE, "com.shandiangou.sdgprotocol.lib.protocol.PingProtocol");       //2
        msgImp.put(PingAckProtocol.PROTOCOL_TYPE, "com.shandiangou.sdgprotocol.lib.protocol.PingAckProtocol"); //3
    }

    /**
     * 解析数据内容
     *
     * @param data
     * @return
     */
    public static BasicProtocol parseContentMsg(byte[] data) {
        int protocolType = BasicProtocol.parseType(data);
        String className = msgImp.get(protocolType);
        BasicProtocol basicProtocol;
        try {
            basicProtocol = (BasicProtocol) Class.forName(className).newInstance();
            basicProtocol.parseContentData(data);
        } catch (Exception e) {
            basicProtocol = null;
            e.printStackTrace();
        }
        return basicProtocol;
    }

    /**
     * 读数据
     *
     * @param inputStream
     * @return
     * @throws
     */
    public static BasicProtocol readFromStream(InputStream inputStream) {
        BasicProtocol protocol;
        BufferedInputStream bis;

        //header中保存的是整个数据的长度值，4个字节表示。在下述write2Stream方法中，会先写入header
        int lengheader = BasicProtocol.LENGTH_LEN;
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
                    return null;
                }
            }

            len = 0;

            int length = bytesToInt(header,0);//数据的长度值
            byte[] content = new byte[length-BasicProtocol.LENGTH_LEN];
            while (len < length-4) {
                System.out.println(len);
                temp = bis.read(content, len, length-len-4);

                if (temp > 0) {
                    len += temp;
                }
            }

            protocol = parseContentMsg(content);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return protocol;
    }



    /**
     * 我的写数据写数据
     *
     * @param protocol
     * @param outputStream
     */
    public static void write2StreamMy(BasicProtocol protocol, OutputStream outputStream) {
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);


                    //1.总的字节长度
            int totalSize = 4 + 4 + 4;
            //2.响应指令类型


            //3.上传的结果 //1:上传成功 0://上传失败


            try {
                bufferedOutputStream.write(intToBytes(totalSize));
                bufferedOutputStream.write(intToBytes(protocol.getVersion()));
                bufferedOutputStream.write(intToBytes(1));
                bufferedOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }


    }


    /**
     * 写数据
     *
     * @param protocol
     * @param outputStream
     */
    public static void write2Stream(BasicProtocol protocol, OutputStream outputStream) {
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        byte[] buffData = protocol.genContentData();
        byte[] header = int2ByteArrays(buffData.length);
        try {
            bufferedOutputStream.write(header);
            bufferedOutputStream.write(buffData);
            bufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭输入流
     *
     * @param is
     */
    public static void closeInputStream(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭输出流
     *
     * @param os
     */
    public static void closeOutputStream(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] int2ByteArrays(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    public static int bytesToInt(byte[] ary, int offset) {
        int value;
        value = (int) ((ary[offset]&0xFF)
                | ((ary[offset+1]<<8) & 0xFF00)
                | ((ary[offset+2]<<16)& 0xFF0000)
                | ((ary[offset+3]<<24) & 0xFF000000));
        return value;
    }

    public static int byteArrayToInt(byte[] b) {
        int intValue = 0;
        for (int i = 0; i < b.length; i++) {
            intValue += (b[i] & 0xFF) << (8 * (3 - i)); //int占4个字节（0，1，2，3）
        }
        return intValue;
    }

    public static int byteArrayToInt(byte[] b, int byteOffset, int byteCount) {
        int intValue = 0;
        for (int i = byteOffset; i < (byteOffset + byteCount); i++) {
            intValue += (b[i] & 0xFF) << (8 * (3 - (i - byteOffset)));
        }
        return intValue;
    }

    public static int bytes2Int(byte[] b, int byteOffset) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        byteBuffer.put(b, byteOffset, 4); //占4个字节
        byteBuffer.flip();
        return byteBuffer.getInt();
    }
}
