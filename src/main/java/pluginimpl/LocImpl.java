package pluginimpl;

import PluginEntity.MsgData;
import workplugins.IOutPutPlugin;
import workplugins.PluginAnnotation;

@PluginAnnotation(name = "Loc")
public class LocImpl  implements IOutPutPlugin {


    @Override
    public void init(String arg) {

    }

    @Override
    public void addData(MsgData obj) {
        System.out.println("Loc_addData");
    }

    @Override
    public void response(byte[] data) {

    }

    @Override
    public void stop(String taskid) {

    }

    @Override
    public void stop() {
        System.out.println("Loc_stop");
    }




}
