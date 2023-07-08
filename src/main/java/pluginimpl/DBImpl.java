package pluginimpl;

import PluginEntity.MsgData;
import workplugins.IInitPlugin;
import workplugins.PluginAnnotation;

@PluginAnnotation(name = "DB")
public class DBImpl implements IInitPlugin {
    @Override
    public void init() {

    }

    @Override
    public void start() {
        System.out.println("DB_start");
    }

    @Override
    public void init(String arg) {

    }

    @Override
    public void addData(MsgData obj) {
        System.out.println("DB_stop");
    }

    @Override
    public void response(byte[] data) {

    }

    @Override
    public void stop(String taskid) {

    }

    @Override
    public void stop() {
       System.out.println("DB_stop");
    }


}
