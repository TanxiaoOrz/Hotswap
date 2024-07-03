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

    public static Class<?> getClazz(String className, Instrumentation instrumentation) {
        return Arrays.stream(
                instrumentation.getAllLoadedClasses()
                ).filter(aClass -> aClass.getName().equals(className))
                .findFirst().orElse(null);
    }


    public static void agentmain(String arg, Instrumentation instrumentation) {
        try {
            Class<?> console = getClazz("hotswap.Console",instrumentation);
            Method log = console.getMethod("log", String.class);
            log.invoke(null,"start");

            try {
                Class<?> proxy = getClazz("hotswap.AgentProxy", instrumentation);
                Method setInstrumentation = proxy.getMethod("setInstrumentation", Instrumentation.class);
                setInstrumentation.invoke(null, instrumentation);
                log.invoke(null, "初始化完成");
            }catch (Exception e) {
                log.invoke(null,e.getMessage());
                throw e;
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