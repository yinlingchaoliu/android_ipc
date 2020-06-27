package com.chaoliu.ipc.binderpool.stub;

import android.os.RemoteException;
import android.util.Log;

import com.chaoliu.ipc.aidl.IWakeApp;
import com.chaoliu.ipc.binderpool.service.IBinderInit;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 播放语音或唤醒app
 */
public class WakeUpImpl extends IWakeApp.Stub implements IBinderInit {

    AtomicBoolean status = new AtomicBoolean( false );

    @Override
    public int init() throws RemoteException {
        status.set( false );
        return 0;
    }

    @Override
    public int wakeup() throws RemoteException {
        Log.e( "WakeUpImpl","wakeup============" );
        return 0;
    }

    @Override
    public int talk() throws RemoteException {
        status.set( true );
        Log.e( "WakeUpImpl","talk=========" );
        return 0;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }
}
