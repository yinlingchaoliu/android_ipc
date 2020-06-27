// IWakeApp.aidl
package com.chaoliu.ipc.aidl;

// Declare any non-default types here with import statements

interface IWakeApp {
    //初始化app状态
    int init();
    int wakeup();
    int talk();
}