// IOnNewBookArrivedListener.aidl
package com.chaoliu.ipc.aidl;
import com.chaoliu.ipc.aidl.Book;

interface IOnNewBookArrivedListener {
    //新书收到通知
    void onNewBookArrived(in Book newBook);
}
