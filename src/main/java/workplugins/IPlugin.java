package workplugins;

import PluginEntity.MsgData;
import pluginImp.BusHander;


public interface IPlugin {


    void  addData(MsgData obj);
    void  stop();

  default   IBusHander  getHander()
  {
            return  new BusHander();
  }
}
