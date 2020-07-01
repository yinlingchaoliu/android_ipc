package com.chaoliu.ipc.wake;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.chaoliu.ipc.aidl.IWakeApp;
import com.chaoliu.ipc.binderpool.client.BinderPool;

import java.util.concurrent.locks.ReentrantLock;

public class TalkOrWakeApp {

    //为了简化模型  binderpool采用一个service连接池 ，其实是多个的
    BinderPool[] pools = new BinderPool[3];

    private volatile int use = -1;
    private volatile Node head;

    private ReentrantLock lock = new ReentrantLock(  );

    private Context mContext;

    public TalkOrWakeApp(Context context){
        mContext= context.getApplicationContext();
        BinderPool pool = BinderPool.getInstance( context );
        pools[0] = pool;
        pools[1] = pool;
        pools[2] = pool;
        use = -1;
        head = create();
        Log.e( "talk","TalkOrWakeApp" );
    }

    //应该直接使用
    public Node create(){

        lock.lock();

        try {
            head = new Node();
            Node tmp = head;
            for (int i = 0; i< 3;i++){
                IBinder binder = pools[i].queryBinder( IWakeApp.class );
                tmp.mRemoteWakeApp = IWakeApp.Stub.asInterface( binder );
                if (i == 2){
                    tmp.next = null;
                    break;
                }
                tmp.next = new Node();
                tmp = tmp.next;
            }
        }finally {
            lock.unlock();
        }
        return head;
    }


    public void talkOrWake(){

        use = -1;
        if (head == null) return;

        lock.lock();

        try {
            Node tmpNode = head;
            while (tmpNode!=null) {

                try {
                    if (tmpNode.mRemoteWakeApp == null) break;
                    if (use == -1){
                        use = tmpNode.mRemoteWakeApp.talk();
                    }else{
                        tmpNode.mRemoteWakeApp.wakeup();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                tmpNode = tmpNode.next;
            }
        }
        finally {
            lock.unlock();
        }


    }

    class Node{
        IWakeApp mRemoteWakeApp;
        Node next;
    }

}
