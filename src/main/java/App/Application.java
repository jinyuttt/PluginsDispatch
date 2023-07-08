package App;

import Balancing.LoadBlance;
import Task.TaskEntity;
import Task.TaskModel;
import bus.MsgBus;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class Application {
//    public  static void  main(String[] args) throws FileNotFoundException {
//
//        var map = YamlUtil.getApplication();
//        Map<String, String> maptask = (Map<String, String>) map.get("tasktopic");
//        String str = maptask.get("name");
//        TaskEntity.tasktopic = str;
//        Map<String, String> sub = (Map<String, String>) map.get("topic-name");
//        str = sub.get("sub");
//        String[] subTopics = str.split(",");
//        Map<String, String> address = (Map<String, String>) map.get("topic-address");
//        str = address.get("sub");
//        String[] subaddress = str.split(",");
//        ServiceImpl service = new ServiceImpl();
//        //所有需要订阅的地址和主题
//        service.ini(subaddress, subTopics);
//        service.start();
//        //本地地址
//        str = address.get("pub");
//        MsgBus.localaddress = str;
//        //
//        AppCst.mapTask= (Map<String, Map<String, String>>) map.get("taskcomplate");
//        TaskEntity.mapTask.put("1", new TaskModel());
//        EnginCore enginCore = new EnginCore();
//        enginCore.start();
//
//    }

    public  static void  main(String[] args)
    {
        String[]strm={"csgj1","csgj2","csgj3","csgj4","csgj5"};
        Map<String,Integer> map=new HashMap<>();
        for (int j=0;j<strm.length;j++) {
            map.put(strm[j],j+1 );

        }
        LoadBlance.init(map);
        for (int i=0;i<30;i++) {
            for (int j=0;j<strm.length;j++) {
                var str = LoadBlance.consistencyHashLoadBlance(strm[j]);
                System.out.println(str);
            }

        }
        System.out.println("-------------------------");
        for (int i=0;i<30;i++) {

            var ss=  LoadBlance.weightRandom();
            System.out.println(ss);
        }
        System.out.println("-------------------------");
        for (int i=0;i<30;i++) {
            var ss=  LoadBlance.Round();
            System.out.println(ss);
        }
    }
}
