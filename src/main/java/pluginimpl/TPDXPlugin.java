package pluginimpl;

import PluginEntity.MsgData;
import workplugins.IBusHander;
import workplugins.IProcessPlugin;
import workplugins.PluginAnnotation;

@PluginAnnotation(name = "TPDX")
public class TPDXPlugin implements IProcessPlugin {


    @Override
    public void init(String arg) {

    }

    @Override
    public void addData(MsgData obj) {
        System.out.println("TPDX_addData");
    }

    @Override
    public void responce(byte[] data) {

    }

    @Override
    public void stop() {
        System.out.println("TPDX_stop");
    }


}
