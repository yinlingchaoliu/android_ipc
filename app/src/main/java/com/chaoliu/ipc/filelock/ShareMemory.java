package com.chaoliu.ipc.filelock;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * 采用内存映射 + 文件锁
 * 多进程之前通信
 * @author chentong
 */
public class ShareMemory {

    //定义一个随机存取文件对象
    private RandomAccessFile mRAFile = null;
    //定义文件大小
    private static final int FILE_SIZE = 10 * 1024;
    //内存映射
    private MappedByteBuffer mapBuf = null;
    //定义相应的文件通道
    private FileChannel fc = null;

    /**
     * @param filePath 共享文件全路径
     */
    public ShareMemory(String filePath) {
        try {
            mRAFile = new RandomAccessFile( filePath, "rw" );
            fc = mRAFile.getChannel();
            mapBuf = fc.map( FileChannel.MapMode.READ_WRITE, 0, FILE_SIZE );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param ps   锁定区域开始的位置；必须为非负数
     * @param len  锁定区域的大小；必须为非负数
     * @param buff 写入的数据
     * @return 0 写入失败 len 写入成功
     */
    public synchronized int write(int ps, int len, byte[] buff) {
        if (ps >= FILE_SIZE || ps + len >= FILE_SIZE || ps < 0 || len <= 0) {
            return 0;
        }

        //定义文件区域锁定的标记。
        FileLock flock = null;
        try {
            //获取此通道的文件给定区域上的锁定。
            flock = fc.lock( ps, len, false );
            if (flock != null) {

                mapBuf.position( ps );
                ByteBuffer bf1 = ByteBuffer.wrap( buff );
                mapBuf.put( bf1 );
                //释放此锁定。
                flock.release();

                return len;
            }
        } catch (Exception e) {
            if (flock != null) {
                try {
                    flock.release();
                } catch (IOException e1) {
                }
            }
            return 0;
        }

        return 0;
    }


    /**
     * @param ps   锁定区域开始的位置；必须为非负数
     * @param len  锁定区域的大小；必须为非负数
     * @param buff 要取的数据
     * @return 0 写入失败 len 写入成功
     */
    public synchronized int read(int ps, int len, byte[] buff) {
        if (ps >= FILE_SIZE || ps < 0 || len <= 0) {
            return -1;
        }

        //定义文件区域锁定的标记。
        FileLock fl = null;
        try {
            //文件区域锁定
            fl = fc.lock( ps, len, false );
            if (fl != null) {
                //System.out.println( "ps="+ps );
                mapBuf.position( ps );
                if (mapBuf.remaining() < len) {
                    len = mapBuf.remaining();
                }

                if (len > 0) {
                    mapBuf.get( buff, 0, len );
                }

                fl.release();

                return len;
            }
        } catch (Exception e) {
            if (fl != null) {
                try {
                    fl.release();
                } catch (Exception e1) {
                }

            }
            return 0;
        }

        return 0;
    }


    public synchronized void  close(){

        if (fc!=null){
            try {
                fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mapBuf = null;

        if (mRAFile!=null){
            try {
                mRAFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }

}
