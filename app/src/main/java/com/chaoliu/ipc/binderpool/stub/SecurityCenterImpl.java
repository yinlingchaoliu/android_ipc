package com.chaoliu.ipc.binderpool.stub;

import android.os.RemoteException;

import com.chaoliu.ipc.aidl.ISecurityCenter;
import com.chaoliu.ipc.binderpool.service.IBinderInit;

/**
 *
 * 内部要写成线程安全的
 *
 */
public class SecurityCenterImpl extends ISecurityCenter.Stub implements IBinderInit {
    @Override
    public String encrypt(String content) throws RemoteException {
        return "encrypt " + content;
    }

    @Override
    public String decrypt(String passwd) throws RemoteException {
        return "decrypt "+passwd;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }
}
