// IBookManager.aidl
package com.chaoliu.ipc.aidl;
import com.chaoliu.ipc.aidl.Book;
import com.chaoliu.ipc.aidl.IOnNewBookArrivedListener;

interface IBookManager {
    //查询
    List<Book> getBookList();
    //添加
    void addBook(in Book book);

    //服务端回调客户端
    //注册监听
    void registerListener(IOnNewBookArrivedListener listener);
    //卸载监听
    void unregisterListener(IOnNewBookArrivedListener listener);
}
