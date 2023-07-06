package bus;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MsgBus {

    private ZMQ.Socket socket=null;
    private  ZMQ.Socket snd=null;

    private static ZMQ.Socket snds=null;
    public static  String localaddress;
    private  static ZContext context = new ZContext(3);
    private BlockingQueue<MsgData> queue=new ArrayBlockingQueue<>(1000);

    public MsgBus()
    {
        snd = context.createSocket(SocketType.PUB);
    }
    public void ini(String[] addrs)
    {

         socket = context.createSocket(SocketType.SUB);

        for (String addr:addrs
             ) {
          //  socket.connect("tcp://localhost:5555");
            if(!addr.startsWith("tcp"))
            {
                addr="tcp://"+addr;
            }
            socket.connect(addr);
        }
    }
    public  MsgData getData()
    {
        try {
            return   queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void  subscribe(String topic)
    {
          socket.subscribe(topic);
    }
    public void  start()
    {
        while (true)
        {
           String topic= socket.recvStr();
           byte[] msg= socket.recv();
           var data=new MsgData();
           data.data=msg;
           data.topic=topic;
           queue.offer(data);
        }
    }

    public void  sendMsg(String topic,byte[]data)
    {
        snd.sendMore(topic);
        snd.send(data);

    }
    public void  sendMsg(String topic,String data)
    {
        snd.sendMore(topic);
        snd.send(data);
    }

    public static void  send(String topic,String data)
    {
        if(snds==null)
        {
            snds=context.createSocket(SocketType.PUB);
           if(!localaddress.startsWith("tcp"))
           {
               localaddress="tcp://"+localaddress;
           }
            snds.bind(localaddress);
        }
        snds.sendMore(topic);
        snds.send(data);
    }
}
