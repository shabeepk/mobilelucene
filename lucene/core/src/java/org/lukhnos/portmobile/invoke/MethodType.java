package org.lukhnos.portmobile.invoke;

public class MethodType {
    Class<?> clazz;

    MethodType (Class<?> clazz) {
        this.clazz = clazz;
    }

    public static MethodType methodType(Class<?> clazz) {
        return new MethodType(clazz);
    }

    public Class<?> getType() {
        return clazz;
    }
}
