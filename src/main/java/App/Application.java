package App;

import Task.TaskEntity;
import Task.TaskModel;
import bsh.EvalError;
import bsh.Interpreter;
import bus.MsgBus;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class Application {
    public  static void  main(String[] args) throws FileNotFoundException {

       var map= YamlUtil.getApplication();
         Map<String,String> maptask= (Map<String, String>) map.get("tasktopic");
        String str=  maptask.get("name");
        TaskEntity.tasktopic=str;

        Map<String,String> sub= (Map<String, String>) map.get("topic-name");
      str=  sub.get("sub");
      String[] subTopics=str.split(",");
        Map<String,String> address= (Map<String, String>) map.get("topic-address");
       str= address.get("sub");
       String[] subaddress=str.split(",");
       ServiceImpl service=new ServiceImpl();
       service.ini(subaddress,subTopics);
       service.start();
       str= address.get("pub");
        MsgBus.localaddress=str;
        TaskEntity.map.put("1",new TaskModel());
        EnginCore enginCore=new EnginCore();
        enginCore.start();

    }
}
