package com.chaoliu.ipc;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.chaoliu.ipc.aidl.IComputer;
import com.chaoliu.ipc.aidl.ISecurityCenter;
import com.chaoliu.ipc.binderpool.client.BinderPool;
import com.chaoliu.ipc.wake.TalkOrWakeApp;

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
        Log.e(TAG,"visit onCreate");

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

        testTalkOrWake();
    }

    private void testTalkOrWake() {

        new Thread( new Runnable() {
            @Override
            public void run() {
                TalkOrWakeApp  talk = new  TalkOrWakeApp(MainActivity.this);
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

    private void testBinderPool(){

        BinderPool binderPool = BinderPool.getInstance( this );

        Log.e(TAG,"visit 创建");

        IBinder securityBinder = binderPool.queryBinder( ISecurityCenter.class );
        ISecurityCenter mSecurityCenter = ISecurityCenter.Stub.asInterface( securityBinder );

        Log.e(TAG,"visit ISecurityCenter");

        String msg = "hello android";

        try {
            String de = mSecurityCenter.decrypt( msg );
            String en = mSecurityCenter.encrypt( msg );
            Log.e(TAG,"visit " + de);
            Log.e(TAG,"visit " + en);
        } catch (Exception e) {
            e.printStackTrace();
        }

        IBinder computerBinder = binderPool.queryBinder( IComputer.class );
        IComputer mComputer = IComputer.Stub.asInterface( computerBinder );

        try {
            int value = mComputer.add( 4,7 );
            Log.e( TAG,"value " +value );
        } catch (Exception e) {
            Log.e( TAG,e.getMessage() );
        }
    }

}