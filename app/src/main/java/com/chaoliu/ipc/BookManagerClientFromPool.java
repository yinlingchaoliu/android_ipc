package com.chaoliu.ipc;

import android.app.Activity;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.chaoliu.ipc.aidl.Book;
import com.chaoliu.ipc.aidl.IBookManager;
import com.chaoliu.ipc.aidl.IOnNewBookArrivedListener;
import com.chaoliu.ipc.binderpool.client.BinderPool;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * client
 *
 * @author chentong
 */
public class BookManagerClientFromPool {

    private static final String TAG = "BookManagerClient";

    private Activity activity;

    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;

    private volatile IBookManager mRemoteBookManager;

    private Handler mHandler;

    BookManagerClientFromPool(Activity activity) {
        this.activity = activity;
        mHandler = new AppHandler( activity );
    }

    //绑定server
    public void bindService()  {
        new Thread( new Runnable() {
            @Override
            public void run() {
                final BinderPool pool = BinderPool.getInstance( activity );

                BinderPool.Connection connection = new BinderPool.Connection(){
                    @Override
                    public void onServiceConnected() {
                        IBinder binder = pool.queryBinder( IBookManager.class );
                        mRemoteBookManager = IBookManager.Stub.asInterface( binder );
                        try {
                            mRemoteBookManager.registerListener( mOnNewBookArrivedListener );
                            List<Book> list = mRemoteBookManager.getBookList();
                            Log.i( TAG, "query list " + list.getClass().getCanonicalName() );
                            Log.i( TAG, "query list string " + list.toString() );
                            mRemoteBookManager.addBook( new Book( 3, "易筋经" ) );
                            Log.i( TAG, "query list string " + mRemoteBookManager.getBookList().toString() );
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                };

                //启动
                connection.onServiceConnected();
                //注册服务
                pool.registerConnection(IBookManager.class,  connection);

            }
        } ).start();
    }

    //解绑
    public void unbindService() {
        if (mRemoteBookManager != null && mRemoteBookManager.asBinder().isBinderAlive()) {
            try {
                mRemoteBookManager.unregisterListener( mOnNewBookArrivedListener );
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mHandler.removeCallbacksAndMessages( null );
    }

    private static class AppHandler extends Handler {
        //弱引用，在垃圾回收时，被回收
        WeakReference<Activity> mActivity;

        AppHandler(Activity activity) {
            this.mActivity = new WeakReference<>( activity );
        }

        public void handleMessage(Message msg) {
            Activity activity = mActivity.get();

            if (activity == null) return;

            switch (msg.what) {
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Log.i( TAG, "receive new book :" + msg.obj.toString() );
                    break;
                default:
                    super.handleMessage( msg );
            }
        }
    }

    //回调listener 不要有耗时交易 可以放在线程中处理
    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage( MESSAGE_NEW_BOOK_ARRIVED, newBook ).sendToTarget();
        }
    };
}
