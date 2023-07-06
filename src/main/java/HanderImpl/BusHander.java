package HanderImpl;

import PluginEntity.MsgData;
import bus.DataBus;
import bus.MsgBus;
import com.google.gson.Gson;
import workplugins.IBusHander;

/**
 * 回传数据
 */
public class BusHander implements IBusHander {
    Gson gson=new Gson();
    @Override
    public void addBusData(MsgData data) {
        DataBus.getInstance().addData(data);
    }

    public <T> void sendMsg(String topic,T msg){


        var data=gson.toJson(msg);
        MsgBus.send(topic,data);
    }
}
