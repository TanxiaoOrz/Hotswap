package hotswap;

import java.io.*;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;


/**
 * @Author: 张骏山
 * @Date: 2024/4/15 13:34
 * @PackageName: hotswap
 * @ClassName: hotswap.Agent
 * @Description: 热更新代理类
 * @Version: 1.0
 **/
public class Agent {

    private static byte[] getBytes(String filePath) throws InvocationTargetException, IllegalAccessException {

        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            StringWriter error = new StringWriter();
            e.printStackTrace(new PrintWriter(error));
        }
        return buffer;
    }

    public static Class<?> getClazz(String className, Instrumentation instrumentation) {
        return Arrays.stream(
                instrumentation.getAllLoadedClasses()
                ).filter(aClass -> aClass.getName().equals(className))
                .findFirst().orElse(null);
    }


    public static void agentmain(String arg, Instrumentation instrumentation) {
        System.out.println("agentStart");
        try {
            String[] split = arg.split(";");
            String classPath = split[0];
            String filePath = split[1];
            boolean isVirtual = split[2].equals("0");
//            console("arg = " + arg,instrumentation);
//            console("isVirtual = " + isVirtual,instrumentation);
//            console("filePath = " + filePath,instrumentation);
//            console("classPath = " + classPath,instrumentation);
//            System.out.println("agentStart");

            Class<?> console = getClazz("hotswap.Console",instrumentation);
            Method log = console.getMethod("log", String.class);
            log.invoke(null,"start");
            log.invoke(null,"arg:"+arg);
            log.invoke(null,"classPath:"+classPath);
            log.invoke(null,"filePath:"+filePath);
            byte[] bytes = getBytes(filePath);
//            log.invoke(null,new String(bytes));
//            System.out.println("bytes = " + bytes);
            Class<?> toSwap = getClazz(classPath,instrumentation);
//            System.out.println("toSwap = " + toSwap);
            instrumentation.redefineClasses();
            ClassDefinition classDefinition = new ClassDefinition(toSwap, bytes);
            log.invoke(null,"start");
            instrumentation.redefineClasses(classDefinition);
            log.invoke(null,"start");
            log.invoke(null,(String.valueOf(isVirtual)));
            if (isVirtual) {
                Class<?> clazz = getClazz(classPath,instrumentation);
                Object versionControl = clazz.getConstructor().newInstance();
                Method getVersion = clazz.getDeclaredMethod("getVersion");
                String version = (String) getVersion.invoke(versionControl);
//                System.out.println("version = " + version);
                log.invoke(null, "version = " + version);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void console(String str, Instrumentation instrumentation) throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
//        Class<?> clazz = getClazz("hotswap.Console",instrumentation);
//        Method log = clazz.getDeclaredMethod("log", String.class);
//        log.invoke(null,str);
//        System.out.println(str);
//    }

}