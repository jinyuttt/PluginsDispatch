package pluginimpl;

import PluginEntity.MsgData;
import workplugins.IBusHander;
import workplugins.IProcessPlugin;
import workplugins.PluginAnnotation;

@PluginAnnotation(name = "CSGJ")
public class CsgjImpl implements IProcessPlugin {


    @Override
    public void addData(MsgData obj) {
        System.out.println("CSGJ_addData");
    }

    @Override
    public void stop() {
        System.out.println("CSGJ_stop");
    }



}
