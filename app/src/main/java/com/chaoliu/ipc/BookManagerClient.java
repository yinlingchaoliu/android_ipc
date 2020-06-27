package com.chaoliu.ipc;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.chaoliu.ipc.aidl.Book;
import com.chaoliu.ipc.aidl.IBookManager;
import com.chaoliu.ipc.aidl.IOnNewBookArrivedListener;
import com.chaoliu.ipc.service.BookManagerService;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 *
 * client
 * @author chentong
 *
 *
 */
public class BookManagerClient {

    private static final String TAG ="BookManagerClient";

    private Activity activity;

    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;

    private IBookManager mRemoteBookManager;

    private Handler mHandler ;

    BookManagerClient(Activity activity){
        this.activity = activity;
        mHandler = new AppHandler( activity );
    }

    //绑定server
    public void bindService(){
        Intent intent = new Intent( activity, BookManagerService.class );
        activity.bindService( intent, mConnection, Context.BIND_AUTO_CREATE );
    }

    //解绑
    public void unbindService(){
        if (mRemoteBookManager!=null&& mRemoteBookManager.asBinder().isBinderAlive()){
            try {
                mRemoteBookManager.unregisterListener( mOnNewBookArrivedListener );
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        activity.unbindService( mConnection );
        mHandler.removeCallbacksAndMessages( null);
    }

    private static class AppHandler extends Handler {
        //弱引用，在垃圾回收时，被回收
        WeakReference<Activity> mActivity;

        AppHandler(Activity activity){
            this.mActivity=new WeakReference<>(activity);
        }

        public void handleMessage(Message msg){
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

    //binder 意外死亡代理 服务断掉service重启 在binder线程池中，不能方位UI方法
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.e(TAG, "mDeathRecipient-->binderDied-->");
            if (mRemoteBookManager == null) {
                return;
            }
            mRemoteBookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mRemoteBookManager = null;
            //Binder死亡，重新绑定服务
            Log.e(TAG, "mDeathRecipient-->bindService");
            Intent intent = new Intent(activity, BookManagerService.class);
            activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    };

    // onServiceConnected onServiceDisconnected 运行在UI线程 放在子线程不要有耗时操作
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            IBookManager bookManger = IBookManager.Stub.asInterface( iBinder );

            try {
                mRemoteBookManager = bookManger;

                //解决binder意外死亡
                iBinder.linkToDeath(mDeathRecipient,0);

                List<Book> list = bookManger.getBookList();
                Log.i( TAG, "query list " + list.getClass().getCanonicalName() );
                Log.i( TAG, "query list string " + list.toString() );

                bookManger.addBook( new Book( 3, "易筋经" ) );
                Log.i( TAG, "query list string " + bookManger.getBookList().toString() );

                //注册监听
                mRemoteBookManager.registerListener( mOnNewBookArrivedListener );
            } catch (Exception e) {

            }

        }

        //重连服务 在UI线程中
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteBookManager = null;
        }
    };

}
