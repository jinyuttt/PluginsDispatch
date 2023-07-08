package HanderImpl;

import PluginEntity.MsgData;
import bus.DataBus;
import bus.MsgBus;
import com.google.gson.Gson;
import workplugins.IBusHanlder;

/**
 * 回传数据
 */
public class BusHander implements IBusHanlder {
    Gson gson=new Gson();
    @Override
    public void addBusData(MsgData data) {
        DataBus.getInstance().addData(data);
    }

    @Override
    public void reply(Object obj) {
        //todo
    }


}
