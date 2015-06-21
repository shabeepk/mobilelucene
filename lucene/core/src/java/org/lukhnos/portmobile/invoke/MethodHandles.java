package org.lukhnos.portmobile.invoke;

public class MethodHandles {
    static public class Lookup {
        public MethodHandle findConstructor(Class<?> refc, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            return new MethodHandle.NoArgsConstructor(refc);
        }
    }

    static public final Lookup publicLookupInstance = new Lookup();

    static public Lookup publicLookup() {
        return publicLookupInstance;
    }
}
