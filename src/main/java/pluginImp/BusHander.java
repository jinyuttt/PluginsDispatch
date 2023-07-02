package pluginImp;

import PluginEntity.MsgData;
import bus.DataBus;
import workplugins.IBusHander;

/**
 * 回传数据
 */
public class BusHander implements IBusHander {
    @Override
    public void addBusData(MsgData data) {
        DataBus.getInstance().addData(data);
    }
}
