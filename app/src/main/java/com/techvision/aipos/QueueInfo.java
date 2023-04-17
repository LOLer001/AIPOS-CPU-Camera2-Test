package com.techvision.aipos;

public class QueueInfo {
    public byte[] data;
    public long systemTime;
    public QueueInfo(byte[] data,long systemTime){
        this.data=data;
        this.systemTime=systemTime;
    }
}
