package bus;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * zmq 网络传输
 */
public class MsgBus {

    /**
     *订阅
     */
    private ZMQ.Socket socket=null;

    /**
     * 发布
     */
    private  ZMQ.Socket snd=null;

    /**
     * 发布
     */
    private static ZMQ.Socket snds=null;

    /**
     * 发布地址
     */
    public static  String localaddress;
    private  static ZContext context = new ZContext(3);

    /**
     * 接收的数据
     */
    private BlockingQueue<MsgData> queue=new ArrayBlockingQueue<>(1000);

    public MsgBus()
    {
        snd = context.createSocket(SocketType.PUB);
    }

    /**
     * 初始化订阅地址
     * @param addrs
     */
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

    /**
     * 获取接收的数据
     * @return
     */
    public  MsgData getData()
    {
        try {
            return   queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 订阅主题
     * @param topic
     */
    public void  subscribe(String topic)
    {
          socket.subscribe(topic);
    }

    /**
     * 开启订阅接收
     */
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

    /**
     * 发布数据
     * @param topic
     * @param data
     */
    public void  sendMsg(String topic,byte[]data)
    {
        snd.sendMore(topic);
        snd.send(data);

    }

    /**
     * 发布数据
     * @param topic
     * @param data
     */
    public void  sendMsg(String topic,String data)
    {
        snd.sendMore(topic);
        snd.send(data);
    }

    /**
     * 发布数据
     * @param topic
     * @param data
     */
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
