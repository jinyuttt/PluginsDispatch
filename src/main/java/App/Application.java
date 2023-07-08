package App;

import Task.TaskEntity;
import Task.TaskModel;
import bus.MsgBus;

import java.io.FileNotFoundException;
import java.util.Map;

public class Application {
    public  static void  main(String[] args) throws FileNotFoundException {

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
