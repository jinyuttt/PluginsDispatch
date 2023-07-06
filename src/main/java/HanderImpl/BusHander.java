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
    @Override
    public void addBusData(MsgData data) {
        DataBus.getInstance().addData(data);
    }

    public <T> void sendMsg(String topic,T msg){
        MsgBus msgBus=new MsgBus();
        Gson gson=new Gson();
        var data=gson.toJson(msg);
        msgBus.sendMsg(topic,data);
    }
}
