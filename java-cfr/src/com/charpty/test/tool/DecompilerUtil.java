package com.charpty.test.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.getopt.GetOptParser;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.DumperFactoryImpl;

/**
 * @author CaiBo
 * @version $Id$
 */
public class DecompilerUtil {

    /**
     * 反编译一个目录下所有的jar文件和class文件，并把结果保存到指定的目录下
     *
     * @param classDir 待编译的文件的目录地址
     * @param outputDir 输出目录地址
     */
    public static void decompiler(String classDir, String outputDir) {
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = ".";
        }
        String[] args = new String[3];
        GetOptParser optParser = new GetOptParser();
        Options options = null;

        List<File> files = new ArrayList<>(128);
        getVaildFiles(classDir, files);
        System.out.println("-------------------------------list files-------------------------------");
        for (File f : files) {
            System.out.println(f.getName() + " : " + f.getAbsolutePath());
        }
        System.out.println("------------------------------end of list-------------------------------");
        System.out.println("total count: " + files.size());

        args[1] = "--outputdir";
        args[2] = outputDir;
        for (int i = 0, size = files.size(); i < size; ++i) {
            File file = files.get(i);
            System.out.println("**************************************************************");
            System.out.println("decompile file: " + file.getName() + ";process: " + i + "/" + size);
            System.out.println("**************************************************************");
            args[0] = file.getAbsolutePath();
            try {
                options = optParser.parse(args, OptionsImpl.getFactory());
            } catch (Exception e) {
                optParser.showHelp(OptionsImpl.getFactory(), e);
                System.exit(1);
            }
            ClassFileSourceImpl classFileSource = new ClassFileSourceImpl(options);
            DCCommonState dcCommonState = new DCCommonState(options, classFileSource);
            String path = options.getOption(OptionsImpl.FILENAME);
            String type = options.getOption(OptionsImpl.ANALYSE_AS);
            if (type == null) {
                type = dcCommonState.detectClsJar(path);
            }
            DumperFactoryImpl dumperFactory = new DumperFactoryImpl();
            if (type.equals("jar")) {
                Decompiler.doJar(dcCommonState, path, dumperFactory);
            } else {
                Decompiler.doClass(dcCommonState, path, dumperFactory);
            }
        }
    }

    private static void getVaildFiles(String baseDir, List<File> result) {
        if (baseDir == null || baseDir.isEmpty()) {
            throw new UtilException("baseDir can not be null");
        }
        File tmp = new File(baseDir);
        if (!tmp.exists()) {
            throw new UtilException("no such file or directory: " + baseDir);
        }
        if (!tmp.isDirectory()) {
            if (baseDir.endsWith(".jar") || baseDir.endsWith(".class")) {
                result.add(tmp);
            } else {
                throw new UtilException("single file must be *.jar or *.class: " + baseDir);
            }
            return;
        }

        File[] files = tmp.listFiles();
        for (File f : files) {
            String path = f.getPath();
            if (f.isDirectory()) {
                getVaildFiles(path, result);
            }
            if (path.endsWith(".jar") || path.endsWith(".class")) {
                result.add(f);
            }
        }
    }
}
