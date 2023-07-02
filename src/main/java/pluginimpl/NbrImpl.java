package pluginimpl;

import PluginEntity.MsgData;
import workplugins.IBusHander;
import workplugins.IProcessPlugin;
import workplugins.PluginAnnotation;

@PluginAnnotation(name = "NBr")
public class NbrImpl implements IProcessPlugin {


    @Override
    public void addData(MsgData obj) {

    }

    @Override
    public void stop() {

    }


}
