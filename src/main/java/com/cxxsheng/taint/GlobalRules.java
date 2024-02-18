package com.cxxsheng.taint;

import soot.SootMethod;
import soot.options.Options;

import java.util.ArrayList;
import java.util.List;

public class GlobalRules {



    private static final String[] skipClassList = {
            "android.os.Parcel",
    };

    private static final String[] needIgnoreMethodBody = {
            "java.lang.*"
    };

    public static boolean needSkip(SootMethod method) {
        String clazzName = method.getDeclaringClass().getName();
        for (String skipClass : skipClassList){
            if (skipClass.startsWith("*")){
                if (clazzName.endsWith(skipClass.substring(1)))
                    return true;
            }else if (skipClass.endsWith("*")){
                if (clazzName.startsWith(skipClass.substring(0, skipClass.length() - 1)))
                    return true;
            }else {
                if (clazzName.equals(skipClass))
                    return true;
            }
        }
        return false;
    }

    public static boolean needIgnoreBody(SootMethod method){
        String clazzName = method.getDeclaringClass().getName();
        for (String ignoreBody : needIgnoreMethodBody){
            if (ignoreBody.startsWith("*")){
                if (clazzName.endsWith(ignoreBody.substring(1)))
                    return true;
            }else if (ignoreBody.endsWith("*")){
                if (clazzName.startsWith(ignoreBody.substring(0, ignoreBody.length() - 1)))
                    return true;
            }else {
                if (clazzName.equals(ignoreBody))
                    return true;
            }
        }
        return false;
    }


    public static void setExclude(){
        List<String> excudeList = new ArrayList();
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_exclude(excudeList);
    }

}
