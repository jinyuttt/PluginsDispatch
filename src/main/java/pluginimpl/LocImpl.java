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
    public void responce(byte[] data) {

    }

    @Override
    public void stop() {
        System.out.println("Loc_stop");
    }

    @Override
    public void finsh() {

    }


}
