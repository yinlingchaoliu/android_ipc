// IBinderPool.aidl
package com.chaoliu.ipc.aidl;

import android.os.IBinder;

// Declare any non-default types here with import statements

interface IBinderPool {
    //默认注册
    IBinder queryBinder(String binder);
}