package com.chaoliu.ipc;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.chaoliu.ipc.aidl.IComputer;
import com.chaoliu.ipc.aidl.ISecurityCenter;
import com.chaoliu.ipc.binderpool.client.BinderPool;
import com.chaoliu.ipc.filelock.ShareMemory;
import com.chaoliu.ipc.wake.TalkOrWakeApp;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

// @author chentong
//todo 客户端实现
public class MainActivity extends Activity {

    private static final String TAG = "BookManagerClient";

    private BookManagerClient client;
    private BookManagerClientFromPool bookManagerClientFromPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Log.e( TAG, "visit onCreate" );

        //todo 1
//        client = new BookManagerClient( this );
//        client.bindService();

        //todo 2 简单实用
//        new Thread( new Runnable() {
//            @Override
//            public void run() {
//                testBinderPool();
//            }
//        } ).start();

//        //todo 3  支持重连
//        bookManagerClientFromPool = new BookManagerClientFromPool( this );
//        bookManagerClientFromPool.bindService();

//        testTalkOrWake();

//        Log.e( TAG,"ssss" );
//
//        NIOFileLock fileLock = new NIOFileLock( this,"app.txt" );
//        try {
//            fileLock.write( "xxxxxxxxxxx" );
//            String str = fileLock.read();
//            Log.e( TAG,new String( str ) );
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        new Thread( new Runnable() {
            @Override
            public void run() {
                testShareMonmeny();
            }
        } ).start();



    }

    public static char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes).flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }


    public void testShareMonmeny(){
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "niofile" + File.separator + "app.sm";

        ShareMemory sm = new ShareMemory( filepath );


        String str = "xxxxxxx000000";
        try {

            byte[] buf = new byte[20];
            Arrays.fill( buf, (byte)0 );

            byte[] bytes = str.getBytes();

            int size = bytes.length;

            Log.e( TAG, "buf1 size  " + size );


            for (int i = 0; i < size; i++) {
                buf[i] = bytes[i];
            }

            Log.e( TAG, "buf  " + new String( buf ) );
            sm.write( 0, size, buf );

            byte[] buf1 = new byte[20];
            sm.read( 0, size, buf1 );


            String str12 = new String( buf1 );
            Log.e( TAG, "buf1  " + new String( buf1 )  );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void testTalkOrWake() {

        new Thread( new Runnable() {
            @Override
            public void run() {
                TalkOrWakeApp talk = new TalkOrWakeApp( MainActivity.this );
                talk.talkOrWake();
            }
        } ).start();

    }

    @Override
    protected void onDestroy() {
//        client.unbindService();
        bookManagerClientFromPool.bindService();
        super.onDestroy();
    }

    private void testBinderPool() {

        BinderPool binderPool = BinderPool.getInstance( this );

        Log.e( TAG, "visit 创建" );

        IBinder securityBinder = binderPool.queryBinder( ISecurityCenter.class );
        ISecurityCenter mSecurityCenter = ISecurityCenter.Stub.asInterface( securityBinder );

        Log.e( TAG, "visit ISecurityCenter" );

        String msg = "hello android";

        try {
            String de = mSecurityCenter.decrypt( msg );
            String en = mSecurityCenter.encrypt( msg );
            Log.e( TAG, "visit " + de );
            Log.e( TAG, "visit " + en );
        } catch (Exception e) {
            e.printStackTrace();
        }

        IBinder computerBinder = binderPool.queryBinder( IComputer.class );
        IComputer mComputer = IComputer.Stub.asInterface( computerBinder );

        try {
            int value = mComputer.add( 4, 7 );
            Log.e( TAG, "value " + value );
        } catch (Exception e) {
            Log.e( TAG, e.getMessage() );
        }
    }

}