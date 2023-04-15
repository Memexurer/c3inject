package helfy;

import java.util.HashMap;
import java.util.Map;

public class Main {
    private static Map<String, Object> variables;

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.version"));
        System.out.println("hi");
        populateVariables();
    }

    public static void populateVariables() {
        JVM jvm = new JVM();
        variables = new HashMap<>();
        variables.put("klassOffset", jvm.type("java_lang_Class").global("_klass_offset"));
        variables.put("methods", jvm.type("InstanceKlass").offset("_methods"));
        variables.put("methodsData", jvm.type("Array<Method*>").offset("_data"));
        variables.put("methodSize", jvm.type("Method").size);
        variables.put("oopSize", jvm.intConstant("oopSize"));
        variables.put("heap", jvm.type("CodeCache").global("_heap"));
        variables.put("virtualSpaceMin", jvm.type("VirtualSpace").field("_high").offset);
       // variables.put("memory", jvm.type("CodeHeap").field("_memory").offset); is 0

        System.out.println(variables);
    }
}
