package com.techvision.aipos;

public class NameListResult {
    public String[] nameList;
    public int[] cntList;

    public NameListResult(String[] confidence,int[] category){
        this.nameList=confidence;
        this.cntList=category;
    }
}
