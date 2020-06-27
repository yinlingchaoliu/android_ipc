package com.chaoliu.ipc.binderpool.stub;

import android.os.RemoteException;

import com.chaoliu.ipc.aidl.IComputer;
import com.chaoliu.ipc.binderpool.service.IBinderInit;

public class ComputerImpl extends IComputer.Stub implements IBinderInit {

    @Override
    public int add(int a, int b) throws RemoteException {
        return a + b;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }
}
