package com.chaoliu.ipc.binderpool.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import com.chaoliu.ipc.aidl.IBinderPool;
import com.chaoliu.ipc.aidl.IBookManager;
import com.chaoliu.ipc.aidl.IComputer;
import com.chaoliu.ipc.aidl.ISecurityCenter;
import com.chaoliu.ipc.aidl.IWakeApp;
import com.chaoliu.ipc.binderpool.stub.BookManagerImpl;
import com.chaoliu.ipc.binderpool.stub.ComputerImpl;
import com.chaoliu.ipc.binderpool.stub.SecurityCenterImpl;
import com.chaoliu.ipc.binderpool.stub.WakeUpImpl;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * binder池 共享service
 * @author chentong
 * @date 2020/6/27
 *
 */
public class BinderPoolService extends Service {

    private static final String TAG = "BinderPoolService";

    //线程安全的
    private ConcurrentHashMap<String,IBinder> binderMap = new ConcurrentHashMap<String,IBinder>(  );

    private Binder  mBinderPool = new IBinderPool.Stub(){

        @Override
        public IBinder queryBinder(String binder) throws RemoteException {
            return binderMap.get( binder );
        }

    };

    public BinderPoolService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerBinder();

        //初始化
        for (IBinder binder : binderMap.values()){
            IBinderInit init = (IBinderInit)binder;
            init.onCreate();
        }
    }

    //注册方法 binder方法
    private void registerBinder(){
        binderMap.put( IComputer.class.getSimpleName(),new ComputerImpl() );
        binderMap.put( ISecurityCenter.class.getSimpleName(),new SecurityCenterImpl() );
        binderMap.put( IBookManager.class.getSimpleName(),new BookManagerImpl() );
        binderMap.put( IWakeApp.class.getSimpleName(),new WakeUpImpl() );

    }

    @Override
    public IBinder onBind(Intent intent) {

        int check = checkCallingOrSelfPermission( "com.chaoliu.ipc.permission.ACCESS_BINDER_POOL_SERVICE" );
        if (check == PackageManager.PERMISSION_DENIED){
            return null;
        }

        return mBinderPool;
    }

    @Override
    public void onDestroy() {

        //清理
        for (IBinder binder : binderMap.values()){
            IBinderInit init = (IBinderInit)binder;
            init.onDestroy();
        }
        binderMap.clear();

        super.onDestroy();
    }
}