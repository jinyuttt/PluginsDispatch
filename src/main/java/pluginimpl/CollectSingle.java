package pluginimpl;

import PluginEntity.MsgData;
import workplugins.IBusHandler;
import workplugins.IInputPlugin;
import workplugins.PluginAnnotation;

@PluginAnnotation(name = "Cap")
public class CollectSingle implements IInputPlugin {
    @Override
    public void start() {
        System.out.println("Cap_start");
        IBusHandler hander=getHander();
        hander.addBusData(new MsgData() {
            @Override
            public int hashCode() {
                return super.hashCode();
            }
        });
    }

    @Override
    public void init(String arg) {

    }

    @Override
    public void addData(MsgData obj) {
        System.out.println("Cap_addData");

    }

    @Override
    public void response(byte[] data) {

    }

    @Override
    public void stop(String taskid) {

    }

    @Override
    public void stop() {
        System.out.println("Cap_stop");
    }



}
