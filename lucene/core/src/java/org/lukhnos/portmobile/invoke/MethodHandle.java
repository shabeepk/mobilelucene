package org.lukhnos.portmobile.invoke;

public abstract class MethodHandle {
    public abstract Object invokeExact();
    public abstract MethodHandle asType(MethodType newType);

    static public class NoArgsConstructor extends MethodHandle {
        Class<?> clazz;

        NoArgsConstructor(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object invokeExact() {
            try {
                return clazz.newInstance();
            } catch (InstantiationException|IllegalAccessException e) {
                // Note: May not be the wisest choice.
                return null;
            }
        }

        @Override
        public MethodHandle asType(MethodType newType) {
            return this;
        }
    }
}
