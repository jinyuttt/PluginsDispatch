package pluginimpl;

import PluginEntity.MsgData;
import workplugins.IProcessPlugin;
import workplugins.PluginAnnotation;

@PluginAnnotation(name = "CSGJ")
public class CsgjImpl implements IProcessPlugin {


    @Override
    public void init(String arg) {

    }

    @Override
    public void addData(MsgData obj) {
        System.out.println("CSGJ_addData");
    }

    @Override
    public void response(byte[] data) {

    }

    @Override
    public void stop(String taskid) {

    }

    @Override
    public void stop() {
        System.out.println("CSGJ_stop");
    }



}
