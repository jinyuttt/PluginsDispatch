package workplugins;

import PluginEntity.MsgData;
import HandlerImpl.BusHandler;

/**
 * 组件基本功能
 */
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
     * 传入返回数据，服务回传的数据必须经过组件再次处理
     * @param data
     */
    void response(byte[]data);






    /**
     * 停止
     * @param taskid 任务id
     */
    void stop(String taskid);

  default IBusHandler getHander()
  {
            return  new BusHandler();
  }
}
