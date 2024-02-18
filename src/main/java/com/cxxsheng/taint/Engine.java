package com.cxxsheng.taint;

import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.options.Options;

import java.util.*;

import static com.cxxsheng.taint.GlobalRules.*;

public class Engine {

    public static Map<String,String> taintPoint = new HashMap<>();


    public static HashSet<String> sources = new HashSet<>();


    private static void callSootMethod(SootMethod method, List<String> calledMethodSig, int depth){
        String stars = String.join("", Collections.nCopies(depth, "_"));
        String logPrefix = "|" + stars;

        if (needSkip(method))
            return;

        if (calledMethodSig.contains(method.toString())){
            System.out.println(logPrefix + "skipped called method " + method);
            return;
        }
        else
            calledMethodSig.add(method.toString());


        System.out.println(logPrefix + "calling " + method);

        // no body or do not need to enter deep in java.lang* class method
        if (!method.isConcrete() ||  needIgnoreBody(method))
            return;


        Body body = method.retrieveActiveBody();


        method.getParameterTypes();
        for (Unit unit : body.getUnits()) {
            if (unit instanceof JAssignStmt){
                Value left = ((JAssignStmt) unit).getLeftOp();
                Value right = ((JAssignStmt) unit).getRightOp();
                if (right instanceof InvokeExpr){
                    SootMethod sonMethod = ((InvokeExpr) right).getMethod();
                    callSootMethod(sonMethod,calledMethodSig, ++depth);

                }
            } else if (unit instanceof JInvokeStmt){
                SootMethod sm = ((JInvokeStmt) unit).getInvokeExpr().getMethod();
                callSootMethod(sm, calledMethodSig, ++depth);
            }

        }
    }


    public static List LoadClass(String p){
        List<Body> rets = new ArrayList<>();
        Options.v().set_src_prec(Options.src_prec_apk_c_j);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_dir(Arrays.asList(p));
        Options.v().set_process_multiple_dex(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_whole_program(true);
        setExclude();
        Scene.v().loadNecessaryClasses(); // may take dozens of seconds
        System.out.println("Class sizing... " + Scene.v().getApplicationClasses().size());
        for (SootClass clazz : Scene.v().getApplicationClasses()) {
            if (clazz.isInterface())
                continue;
            List<SootMethod> sos = clazz.getMethods();
            for (SootClass interfaze : clazz.getInterfaces())
            {
                if ( "android.os.Parcelable$Creator".equals(interfaze.toString())){
                    String methodSig = "java.lang.Object createFromParcel(android.os.Parcel)";
                    SootMethod method = clazz.getMethod(methodSig);
                    System.out.println("calling " + methodSig + " in "+ clazz);
                    callSootMethod(method, new ArrayList<>(),0);
                }
            }
        }
        return rets;
    }
}
