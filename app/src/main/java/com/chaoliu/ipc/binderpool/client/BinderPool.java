package com.chaoliu.ipc.binderpool.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import com.chaoliu.ipc.aidl.IBinderPool;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * binder池
 * @author chentong
 * @date 2020-6-27
 */
public class BinderPool {

    private static final String TAG = "BinderPool";
    private Context mContext;
    private IBinderPool mBinderPool;

    private CountDownLatch latch;

    private static volatile BinderPool sInstance = null;

    private ConcurrentHashMap<Class,Connection> connectionMap = new ConcurrentHashMap<>(  );

    private Handler mHandler = new Handler( Looper.getMainLooper() );

    private BinderPool() {
    }

    public static BinderPool getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BinderPool.class) {
                if (sInstance == null) {
                    sInstance = new BinderPool( context );
                }
            }
        }
        return sInstance;
    }

    private BinderPool(Context context) {
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    public synchronized void registerConnection(Class clazz, Connection connection){
        connectionMap.put( clazz,connection );
    }

    private  void connectBinderPoolService() {

        latch = new CountDownLatch( 1 );

        bindService();

        //用来保障 connection初始化完毕
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void bindService(){
        //通过action访问 binderPoolService  可以跨app调用
        Intent intent = new Intent(  );
        intent.setAction( "com.chaoliu.ipc.binderpool.service.BinderPoolService" );
        intent.setPackage( "com.chaoliu.ipc" );

        //避免直接引用
//        Intent intent = new Intent( mContext, BinderPoolService.class );
        mContext.bindService( intent, mBinderPoolConnection, Context.BIND_AUTO_CREATE );
    }


    //binder池查询
    public IBinder queryBinder(Class clazz) {
        IBinder binder = null;
        try {
            if (mBinderPool != null) {
                binder = mBinderPool.queryBinder( clazz.getSimpleName() );
            }
        } catch (RemoteException e) {
        }
        return binder;
    }

    public  interface Connection{
        void onServiceConnected();
    }

    //创建serverConnection
    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            mBinderPool = IBinderPool.Stub.asInterface( iBinder );
            try {
                mBinderPool.asBinder().linkToDeath( mDeathRecipient, 0 );

                mHandler.post( new Runnable() {
                    @Override
                    public void run() {
                        for (Connection connection :connectionMap.values()){
                            connection.onServiceConnected();
                        }
                    }
                } );

            } catch (RemoteException e) {
                e.printStackTrace();
            }

            latch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    //死亡代理
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (mBinderPool!=null){
                mBinderPool.asBinder().unlinkToDeath( mDeathRecipient, 0 );
                mBinderPool = null;
            }
            connectBinderPoolService();
        }
    };

}
