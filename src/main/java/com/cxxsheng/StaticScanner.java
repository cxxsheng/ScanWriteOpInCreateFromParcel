package com.cxxsheng;

import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.options.Options;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static com.cxxsheng.taint.Engine.LoadClass;


public class StaticScanner {




    public static void main(String[] args) throws IOException {
        // 创建一个文件输出流，并与一个实际的文件关联
        FileOutputStream fos = new FileOutputStream("output.txt");
        // 创建一个新的打印流，用于写入文件
        PrintStream ps = new PrintStream(fos);

        // 将System.out重定向到文件
        System.setOut(ps);

        LoadClass(args[0]);
        ps.close();
        fos.close();
    }
}