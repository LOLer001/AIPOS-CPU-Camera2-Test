package com.techvision.aipos;

import java.util.List;

public class HwAIposNative {
    static {
        System.loadLibrary("native-lib");
    }
    public static native int CategoryAI_SetTimeSwitch(String Jjsonstring,String Jswipath);
    public static native String CategoryAI_GetSn(String Jsn);
    public static native String CategoryAI_sendServer(String Jcode);
    public static native int CategoryAI_Sendlog();
    public static native int CategoryAI_Init(String Jparampath,String Jbinpath,String Jdbpath,String Jlogpath,String Jpicresultpath,int Janglein,String Jkeypath,int[] Jis_active,String Jswipath);
    public static native int CategoryAI_Exit();
    public static native PerformResult CategoryAI_Perform(byte[] Jbuffer, int JoriginW, int JoriginH);
    public static native int CategoryAI_Feedback(String Jname);
    public static native int CategoryAI_DBAddFeatureByName(byte[] Jbuffer, int JoriginW, int JoriginH,String Jname);
    public static native int CategoryAI_DBRemoveFeatureByName(String Jname);
    public static native int CategoryAI_DBReload(String Jdbpath);
    public static native NameListResult CategoryAI_DBClassNameList();
    public static native int CategoryAI_DBClassFeatureCnt(String Jname);
    public static native int CategoryAI_DBSetThreshold(float Jval);
    public static native float CategoryAI_DBGetThreshold();
    public static native String CategoryAI_GetVersion();
    public static native int CategoryAI_GetDBLimit();

}
