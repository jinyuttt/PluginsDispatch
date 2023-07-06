package workplugins;

import PluginEntity.MsgData;
import HanderImpl.BusHander;


public interface IPlugin {

    /**
     * 初始化参数
     * @param arg
     */
    void  init(String arg);

    /**
     * 传入处理参数
     * @param obj
     */
    void  addData(MsgData obj);

    /**
     * 传入返回数据
     * @param data
     */
    void  responce(byte[]data);
    void  stop();

  default   IBusHander  getHander()
  {
            return  new BusHander();
  }
}
