package com.cxxsheng;

import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.options.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static String[] hitStrings = {
            "writeArray",
            "writeBinderArray",
            "writeBinderList",
            "writeBlob",
            "writeBlob",
            "writeBoolean",
            "writeBooleanArray",
            "writeBundle",
            "writeByte",
            "writeByteArray",
            "writeByteArray",
            "writeCharArray",
            "writeDouble",
            "writeDoubleArray",
            "writeException",
            "writeFileDescriptor",
            "writeFixedArray",
            "writeFloat",
            "writeFloatArray",
            "writeInt",
            "writeIntArray",
            "writeInterfaceArray",
            "writeInterfaceList",
            "writeInterfaceToken",
            "writeList",
            "writeLong",
            "writeLongArray",
            "writeMap",
            "writeNoException",
            "writeParcelable",
            "writeParcelableArray",
            "writeParcelableCreator",
            "writeParcelableList",
            "writePersistableBundle",
            "writeSerializable",
            "writeSize",
            "writeSizeF",
            "writeSparseArray",
            "writeSparseBooleanArray",
            "writeString",
            "writeStringArray",
            "writeStringList",
            "writeStrongBinder",
            "writeStrongInterface",
            "writeTypedArray",
            "writeTypedArrayMap",
            "writeTypedList",
            "writeTypedList",
            "writeTypedObject",
            "writeTypedSparseArray",
            "writeValue",
    };



    private static void setExclude(){
        List<String> excudeList = new ArrayList();
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_exclude(excudeList);
    }

    private static List<String> someSelfFunction = new ArrayList<>();


    private static void callSootMethod(SootMethod method, List<String> calledMethodSig){
        if (calledMethodSig.contains(method.toString())){
//                        System.out.println("skipped called method " + method);
            return;
        }
        else
            calledMethodSig.add(method.toString());

        if (!method.isConcrete())
            return;
        if (someSelfFunction.contains(method.toString())){
            return;
        }

        Body body = method.retrieveActiveBody();
        if (body.toString().contains("write")){
            for (String hitString : hitStrings){
                if (body.toString().contains(hitString)){
                    System.out.println("FBI WANTING!!!!");
                    System.out.println(body);

                    break;
                }
            }
//                        System.out.println(body);
        }
        method.getParameterTypes();
        for (Unit unit : body.getUnits()) {
            if (unit instanceof JAssignStmt){
                Value left = ((JAssignStmt) unit).getLeftOp();
                Value right = ((JAssignStmt) unit).getRightOp();
                if (right instanceof InvokeExpr){
                    SootMethod sonMethod = ((InvokeExpr) right).getMethod();
                    for (Type type : sonMethod.getParameterTypes()){
                        String typeString = type.toString();
                        if (typeString.equals("android.os.Parcel")){
                            callSootMethod(sonMethod,calledMethodSig);
                            break;
                        }
                    }
                }
            } else if (unit instanceof JInvokeStmt){
                SootMethod sm = ((JInvokeStmt) unit).getInvokeExpr().getMethod();
                for (Type type : sm.getParameterTypes()){
                    String typeString = type.toString();
                    if (typeString.equals("android.os.Parcel")){
                        callSootMethod(sm, calledMethodSig);
                        break;
                    }
                }
            }

        }
    }
    public static List LoadClass(String p){
        List<Body> rets = new ArrayList<>();
        Options.v().set_src_prec(Options.src_prec_apk_c_j);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_dir(Arrays.asList(p));
        Options.v().set_process_multiple_dex(true);
//                Options.v().set_wrong_staticness(Options.wrong_staticness_ignore);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_whole_program(true);
        setExclude();
        Scene.v().loadNecessaryClasses(); // may take dozens of seconds
        System.out.println("Class sizing... " + Scene.v().getApplicationClasses().size());
        for (SootClass clazz : Scene.v().getApplicationClasses()) {
            if (clazz.isInterface())
                continue;
            for (SootClass interfaze : clazz.getInterfaces())
            {
                if ( "android.os.Parcelable$Creator".equals(interfaze.toString())){
//                                       System.out.println(clazz);
                    // String methodSig = String.format("<%s: java.lang.Object createFromParcel(android.os.Parcel)>", clazz.getName());
                    String methodSig = "java.lang.Object createFromParcel(android.os.Parcel)";
                    SootMethod method = clazz.getMethod(methodSig);
                    System.out.println("calling " + methodSig + " in "+ clazz);
                    callSootMethod(method,new ArrayList<>());
                    break;
                }

            }
//                        for (SootMethod sootMethod : clazz.getMethods()){
//                                for (Type type : sootMethod.getParameterTypes()){
//                                        String typeString = type.toString();
//                                        if (typeString.equals("android.os.Parcel")){
//                                                Body body = sootMethod.retrieveActiveBody();
//                                                if (body.toString().contains("write")){
//                                                        for (String hitString : hitStrings){
//                                                                if (body.toString().contains(hitString)){
//                                                                        System.out.println("FBI WARNING!");
//                                                                        System.out.println(body.toString());
//                                                                }
//                                                        }
//                                                }
//
//                                                break;
//                                        }
//                                }
//                        }
        }
        return rets;
    }


    public static void main(String[] args) {
//                someSelfFunction.add("<android.os.health.HealthStats: android.util.ArrayMap createHealthStatsMap(android.os.Parcel)>");
//                someSelfFunction.add("<android.content.Intent: void <init>(android.os.Parcel)>");
//                someSelfFunction.add("<android.widget.RemoteViews: void <init>(android.os.Parcel,android.widget.RemoteViews$HierarchyRootData,android.content.pm.ApplicationInfo,int)>");
        LoadClass(args[0]);

    }
}