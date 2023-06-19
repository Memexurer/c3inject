package ghp.transformer;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PlatformVariables {
    private static final Map<String, Map<String, Long>> platforms = new HashMap<>();

    private static Map<String, Long> parsePlatform(String propertiesString) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(propertiesString));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, Long> longMap = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            longMap.put((String) entry.getKey(), Long.parseLong((String) entry.getValue()));
        }

        return longMap;
    }


    public static Map<String, Long> jre1_8_0_181_64bit() {
        return parsePlatform("""
                methods=384
                heapField=1430308456
                methodSize=88
                methodsData=8
                klassOffset=1430296760
                virtualSpace=24
                oopSize=8    
                                """);
    }

    public static Map<String, Long> jvm1_8_0_362_64bit() {
        return parsePlatform("""
methods=384
methodSize=88
methodsData=8
heapField=1832463008
klassOffset=1832451104
entry=1832236688
virtualSpace=16
oopSize=8
                                """);
    }

    public static Map<String, Long> jvm1_8_0_361_32bit() {
        return parsePlatform("""
                methods=232
                heapField=1735137232
                methodSize=52
                methodsData=4
                klassOffset=1735129476
                virtualSpace=8
                oopSize=4
                """);
    }

    static {
        platforms.put("b4efb5f6", jvm1_8_0_362_64bit());
        platforms.put("b57717e6", jvm1_8_0_361_32bit());
    }
}
