package helfy;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PopulatePlatformVariables {
    public static Object yes;
    public static void main(String[] args) throws Exception {
        String jdkHash = Integer.toHexString((System.getProperty("java.vm.version") + System.getProperty("java.vm.vendor") + System.getProperty("sun.arch.data.model")).hashCode());

        JVM jvm = new JVM();

        Map<String, Object> variables = new HashMap<>();


        //todo: problem - library handle sie zmienia z jakiegos powodu :(
        // a jak handle sie zmieni, to caly injector przestaje dzialac i huj

        variables.put("entry", jvm.getSymbol("gHotSpotVMStructs"));
        variables.put("klassOffset", jvm.type("java_lang_Class").global("_klass_offset"));
        variables.put("methodSize", jvm.type("Method").size);
        variables.put("methods", jvm.type("InstanceKlass").offset("_methods"));
        variables.put("oopSize", jvm.intConstant("oopSize"));
        variables.put("heapField", jvm.type("CodeCache").global("_heap"));
        variables.put("methodsData", jvm.type("Array<Method*>").offset("_data"));
        variables.put("virtualSpace", jvm.type("VirtualSpace").field("_low").offset);
        // variables.put("memory", jvm.type("CodeHeap").field("_memory").offset); is 0

        Properties properties = new Properties();
        for(Map.Entry<String, Object> entry: variables.entrySet()) {
            properties.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        properties.store(byteArrayOutputStream, "yeah");

        System.out.println(jdkHash + "\n" + new String(byteArrayOutputStream.toByteArray()));
    }
}
