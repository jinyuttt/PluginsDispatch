package HandlerImpl;

import App.EnginCore;
import PluginEntity.MsgData;
import bus.DataBus;
import bus.MsgBus;
import com.google.gson.Gson;
import workplugins.IBusHandler;

/**
 * 回传数据
 */
public class BusHandler implements IBusHandler {
    Gson gson=new Gson();
    @Override
    public void addBusData(MsgData data) {
        DataBus.getInstance().addData(data);
    }

    @Override
    public void reply(Object obj) {

        //组件通知任务完成
       var json= gson.toJson(obj);
       var opt=gson.fromJson(json,TaskOpt.class);
       if(opt.Opt.equals("complete"))
       {
           EnginCore.getInstance().complateTask(opt.taskid);
       }
        var reply=gson.fromJson(json, MsgReply.class);
       if(!reply.topic.isEmpty())
       {
           if(reply.msg.isEmpty()) {
               reply.msg=gson.toJson(reply.data);
           }

               MsgBus.send(reply.topic, reply.msg);

       }
        //todo

    }


}
