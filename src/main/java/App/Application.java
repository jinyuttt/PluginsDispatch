package App;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;

public class Application {
    public  static void  main(String[] args) throws IOException, NoSuchMethodException, ClassNotFoundException {
        // Util.getCurrenPlugin();
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
    }
}
