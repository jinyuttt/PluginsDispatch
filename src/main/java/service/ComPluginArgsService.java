package service;

import PluginEntity.MsgData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import workplugins.IBusHandler;
import workplugins.IOutPutPlugin;
import workplugins.IProcessPlugin;
import workplugins.PluginAnnotation;
import java.util.HashMap;
import java.util.Map;

/**
 * 只是参数组合没有业务
 */
@PluginAnnotation(name = "com",input = "ComMSg")
public class ComPluginArgsService implements IProcessPlugin, IOutPutPlugin {

    IBusHandler handler=null;
    String topic;
    Map<String,String> map=new HashMap<>();
    @Override
    public void init(String arg) {
     String[] args=arg.split(",");
        for (String str:args
             ) {
            String[] tmp=str.split("=");
            map.put(tmp[0],tmp[1]);
        }
        topic=map.get("pubtopic");
    }

    @Override
    public void addData(MsgData obj) {
        Gson gson=new Gson();
        JsonObject object=new JsonObject();
        object.addProperty("topic",topic);
        object.addProperty("msg",gson.toJson(obj));
        if (handler==null)
        {
            handler=getHander();
        }
     handler.reply(object);
    }

    @Override
    public void response(byte[] data) {

    }

    @Override
    public void stop(String taskid) {

    }

    @Override
    public void stop() {

    }
}
