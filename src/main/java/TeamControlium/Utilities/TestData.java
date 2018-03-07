package TeamControlium.Utilities;

import java.util.HashMap;

public final class TestData {
    private static HashMap<String, HashMap<String, ? super Object>> testData = new HashMap<>();


    public static void Clear() {
        Logger.WriteLine(Logger.LogLevels.FrameworkDebug,"Clearing Test data Repository");
        testData.clear();
    }

    public static boolean HasCategory(String key) {
        return testData.containsKey(key);
    }

    public static <T> T getItem(Class<T> type, String category, String name) throws Exception {

        if (!type.isPrimitive()) {
            if (testData.containsKey(category)) {
                HashMap<String, ? super Object> cat = testData.get(category);
                if (cat.containsKey(name)) {
                    Object o = testData.get(category).get(name);
                    if (o.getClass() == type) {
                        try {
                            return type.cast(testData.get(category).get(name));
                        } catch (Exception ex) {
                            Logger.WriteLine(Logger.LogLevels.Error, "Error getting [" + category + "." + name + "] from test data repository: " + ex.getMessage());
                            throw ex;
                        }
                    } else {
                        Logger.WriteLine(Logger.LogLevels.Error, "Error getting [" + category + "." + name + "]. Type is " + o.getClass().getTypeName() + " but wanted " + type.getTypeName());
                        throw new Exception("Error getting [" + category + "." + name + "]. Type is " + o.getClass().getTypeName() + " but wanted " + type.getTypeName());
                    }
                } else {
                    Logger.WriteLine(Logger.LogLevels.Error, "Error getting [" + category + "." + name + "]. [" + name + "] does not exist in [" + category + "]!");
                    throw new Exception("Error getting [" + category + "." + name + "]. [" + name + "] does not exist in Category [" + category + "]!");
                }
            } else {
                Logger.WriteLine(Logger.LogLevels.Error, "Error getting [" + category + "." + name + "]. [" + category + "] does not exist!");
                throw new Exception("Error getting [" + category + "." + name + "]. Category [" + category + "] does not exist!");
            }
        }
        else {
            Logger.WriteLine(Logger.LogLevels.Error, "Error getting [" + category + "." + name + "]. Cannot store primative types but expected type [" +type.getTypeName()+"]!");
            throw new Exception("Error getting [" + category + "." + name + "]. Cannot store primative types but expected type [" +type.getTypeName()+"]!");
        }
    }

    public static <T> void setItem(String category, String name, T value) {

        if (!testData.containsKey(category)) {
            HashMap<String,? super Object> x = new HashMap<>();
            testData.put(category,x);
        }
        HashMap<String,? super Object> cat = testData.get(category);
        cat.put(name,value);
    }


}
