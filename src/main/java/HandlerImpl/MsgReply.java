package HandlerImpl;

import PluginEntity.MsgData;
import zmq.socket.pubsub.Pub;

public class MsgReply {
    public  String topic="";

    public  String msg="";

    public MsgData data;
}
