package App;

import Task.TaskEntity;
import Task.TaskModel;
import bsh.EvalError;
import bsh.Interpreter;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.util.Map;

public class Application {
    public  static void  main(String[] args) throws  EvalError {
        // Util.getCurrenPlugin();
        TaskEntity.map.put("1",new TaskModel());
        EnginCore enginCore=new EnginCore();
        enginCore.start();
//        Gson gson=new Gson();
//        var p=new Person();
//        p.sex="nam";
//        p.user=new User();
//        p.user.age=10;
//        p.user.name="jin";
//
//      var jsn=  gson.toJson(p);
//
//      Object ss=  JsonPath.read(jsn, "$.user.name");

//        String condtion="$pp.devID==devid";
//        Interpreter interpreter = new Interpreter();
//        interpreter.set("$pp.devID", "5");
//        interpreter.set("devid", "5");
//
//        Object ret= interpreter.eval(condtion);
//        System.out.println(ret);
    }
}
