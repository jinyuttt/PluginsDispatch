package workplugins;

import PluginEntity.MsgData;

/**
 * 组件回传数据接口
 */
public interface IBusHandler {

    /**
     * 传入处理的数据
     * @param data
     */
    void  addBusData(MsgData data);

    /**
     * 需要传回框架处理的其它数据
     * @param obj
     */
    void  reply(Object obj);


}
