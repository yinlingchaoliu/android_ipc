package com.chaoliu.ipc.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.chaoliu.ipc.aidl.Book;
import com.chaoliu.ipc.aidl.IBookManager;
import com.chaoliu.ipc.aidl.IOnNewBookArrivedListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 考虑线程并发安全
 * 多个客户端访问的时候
 * todo 服务端实现
 */
public class BookManagerService extends Service {

    private static final String TAG = "BookManagerService";

    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<Book>();

    //todo 处理远程监听注册
    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList = new RemoteCallbackList<>();

    private AtomicBoolean isDestory = new AtomicBoolean( false );

    private Binder mBinder = new IBookManager.Stub() {

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add( book );
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListenerList.register( listener );
            Log.i( TAG, "注册" );
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListenerList.unregister( listener );
            Log.i( TAG, "解绑" );
        }

    };

    public BookManagerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add( new Book( 1, "Android" ) );
        mBookList.add( new Book( 2, "ios" ) );
        new Thread( new ServiceWorker() ).start();
    }

    //权限校验 另一种方法 在Ontransact拦截
    @Override
    public IBinder onBind(Intent intent) {
        int check = checkCallingOrSelfPermission( "com.chaoliu.ipc.permission.ACCESS_BOOK_SERVICE" );
        Log.e( TAG, "checkCallingPermission "+ check);
        if (check == PackageManager.PERMISSION_DENIED){
            return null;
        }
        return mBinder;
    }

    @Override
    public void onDestroy() {
        isDestory.set( true );
        super.onDestroy();
    }


    //beginBroadcast   finishBroadcast 必须配对使用 即使为获得size
    private void onNewBookArrived(Book newBook) throws RemoteException {
        mBookList.add( newBook );
        //获得 list大小
        int size = mListenerList.beginBroadcast();
        for (int i= 0 ; i<size;i++){
            IOnNewBookArrivedListener listener = mListenerList.getBroadcastItem( i );
            if (listener!=null){
                try {
                    listener.onNewBookArrived( newBook );
                }catch (RemoteException e){
                }
            }
        }
        mListenerList.finishBroadcast();
    }

    private class ServiceWorker implements Runnable{

        @Override
        public void run() {

            //检查点1
            while (!isDestory.get()){

                try {
                    Thread.sleep( 5000 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //检查点2
                if (isDestory.get()) return;

                int bookId = mBookList.size()+1;
                Book newBook = new Book( bookId,"new book "+ bookId );
                if (bookId == 8){
                    android.os.Process.killProcess(android.os.Process.myPid());
                    return;
                }
                try {
                    onNewBookArrived( newBook );
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        }
    }

}