package App;

import Task.TaskEntity;
import Task.TaskModel;
import bus.MsgBus;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;

import java.io.FileNotFoundException;
import java.util.Map;

public class Application {
    public  static void  main(String[] args) throws FileNotFoundException {

        String strr="{\n" +
                "    \"foo\":{\n" +
                "        \"bar\":\"value\"\n" +
                "    }\n" +
                "}";
      var doc=  JsonPath.parse(strr);
        Gson gson=new Gson();
      JsonObject obj=  gson.fromJson(strr,JsonObject.class);
     //   JsonElement jsonElement=
      obj.addProperty("foo.mm","123");

      doc.set("$.foo.bar",123);
     System.out.println( doc.jsonString());


        var map = YamlUtil.getApplication();
        Map<String, String> maptask = (Map<String, String>) map.get("tasktopic");
        String str = maptask.get("name");
        TaskEntity.tasktopic = str;
        Map<String, String> sub = (Map<String, String>) map.get("topic-name");
        str = sub.get("sub");
        String[] subTopics = str.split(",");
        Map<String, String> address = (Map<String, String>) map.get("topic-address");
        str = address.get("sub");
        String[] subaddress = str.split(",");
        ServiceImpl service = new ServiceImpl();
        //所有需要订阅的地址和主题
        service.ini(subaddress, subTopics);
        service.start();
        //本地地址
        str = address.get("pub");
        MsgBus.localaddress = str;
        //
        AppCst.mapTask= (Map<String, Map<String, String>>) map.get("taskcomplate");
        TaskEntity.mapTask.put("1", new TaskModel());
        EnginCore enginCore = new EnginCore();
        enginCore.start();

    }


}
