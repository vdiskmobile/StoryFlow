package com.youqude.storyflow.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class StreamTools 
{
    /**
     * 从输入流里面得到返回为二进制的数据
     * @param inStream 输入流
     * @return byte[] 二进制数据
     * @throws Exception
     */
    public static byte[] readInputStream(InputStream inStream,String md5) throws Exception
    {
        BufferedInputStream bis = new BufferedInputStream(inStream);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[32*2014];
        int len = 0;
        while( (len=bis.read(buffer,0,buffer.length)) != -1 )
        {
            outStream.write(buffer, 0, len);
            outStream.flush();
        }
        
        byte[] bytes = outStream.toByteArray();
        
        bis.close();
        inStream.close();
        outStream.close();
        return bytes;
    }
}