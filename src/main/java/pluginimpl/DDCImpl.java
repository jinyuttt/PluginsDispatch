package pluginimpl;

import PluginEntity.MsgData;
import workplugins.IBusHander;
import workplugins.IProcessPlugin;
import workplugins.PluginAnnotation;

@PluginAnnotation(name = "DDC")
public class DDCImpl implements IProcessPlugin {


    @Override
    public void addData(MsgData obj) {
        System.out.println("DDCp_addData");
    }

    @Override
    public void stop() {
        System.out.println("DDC_stop");
    }



}
