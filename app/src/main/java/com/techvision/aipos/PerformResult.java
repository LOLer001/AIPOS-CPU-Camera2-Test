package com.techvision.aipos;


public class PerformResult {
    public int ret;
    public float[] confidence;
    public String[] category;

    public PerformResult(int ret,float[] confidence,String[] category){
        this.ret=ret;
        this.confidence=confidence;
        this.category=category;
    }
}
