package App;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 读取yml
 */
public class YamlUtil {

    /**
     * 递归将 YML 节点值存入 MAP
     * @param stack
     * @param map
     * @param yml
     */
    private static void getVal(Stack<String> stack,Map<String,Object> map,Map<String,Object> yml){
        for (String key:map.keySet()){
            Object tmp = map.get(key);
            stack.add(key);
            if (tmp instanceof Map){
                getVal(stack,(Map<String,Object>)tmp,yml);
            }else {
                yml.put(stack.stream().collect(Collectors.joining(".")),tmp);
                stack.pop();
            }
        }
        if (!stack.isEmpty()){
            stack.pop();
        }
    }

    /**
     * 读取
     * @param path
     * @return
     * @throws FileNotFoundException
     */
    public  static  Map<String,Object> getVal(String path) throws FileNotFoundException {
        File file = new File(path);

        Map<String,Object> map = null;

        if (file.exists()){

            InputStream inputStream = new FileInputStream(file);

            Yaml yaml = new Yaml();

            map = yaml.load(inputStream);
        }
        return  map;
    }

    /**
     * 读取
     * @param path
     * @return
     * @throws FileNotFoundException
     */
    public  static  Map<String,Object> getLevelVal(String path) throws FileNotFoundException {
        File file = new File(path);
        Map<String,Object> map = null;

        if (file.exists()){

            InputStream inputStream = new FileInputStream(file);

            Yaml yaml = new Yaml();

            map = yaml.load(inputStream);
        }
        Map<String,Object> yml = new ConcurrentHashMap<>(16);

        if (map!=null&&!map.isEmpty()){
            getVal(new Stack<>(),map,yml);
        }

        yml.forEach((k,v)->{
            System.out.println(k + ":" + v);
        });
        return  yml;
    }

    /**
     * 读取Application
     * @return
     * @throws FileNotFoundException
     */
    public static  Map<String,Object> getApplication() throws FileNotFoundException {


        String path= YamlUtil.class.getClassLoader().getResource("").getPath();
        //读取xml
        path= path+"application.yml";
        return  getVal(path);
    }
}
