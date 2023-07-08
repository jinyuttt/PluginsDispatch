package bus;
import App.ConvertArgs;
import PluginEntity.MsgData;
import Task.TaskEntity;
import cache.CacheUtil;
import engin.PluginEngine;
import engin.PluginNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import workplugins.Policy;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 组件之间数据传递
 */
public class DataBus {
 Log logger= LogFactory.getLog(DataBus.class);
        private static class LazyHolder {
            private static final DataBus INSTANCE = new DataBus();
        }

    public static final DataBus getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 所有返回的数据
     */
    private  BlockingQueue<MsgData> queue=new ArrayBlockingQueue<MsgData>(1000);


    /**
     * 需要移除的数据
     */
    private BlockingQueue<Long> msgqueue=new ArrayBlockingQueue<>(10000);
        private DataBus (){
            removeMsg();
            start();
        }

    /**
     * 加入数据
     * @param obj
     */
    public void  addData(MsgData obj) {
            queue.add(obj);
            CacheUtil.getInstance().putmap(obj.flage,String.valueOf(obj.msgno),obj);
            var log=new LogProcess();
            log.taskid=String.valueOf(obj.taskid);
            log.flage=obj.flage;
            LogBus.getInstance().add(log);
        }

    /**
     * 每个节点查找数据移除
     * @param node
     * @param no
     */
    private  void remove(PluginNode node,long no)
        {
            CacheUtil.getInstance().remove(node.flage,String.valueOf(no));
            for (PluginNode tmp:node.pluginList
                 ) {
                CacheUtil.getInstance().remove(node.flage,String.valueOf(no));
                if(tmp.pluginList!=null)
                {
                    for (PluginNode child:tmp.pluginList
                         ) {
                        remove(child,no);
                    }
                }
            }
        }

    /**
     * 移除执行完成的数据
     */
    private void removeMsg()
        {
            Thread thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Long no=  msgqueue.take();
                        logger.debug("移除数据");
                        var list = PluginEngine.lst;
                        for (var link:list
                             ) {
                            remove(link.root,no);
                        }

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            });
            thread.setName("remove");
            thread.setDaemon(true);
            thread.start();
        }

    /**
     * 查找插件
     * @param child
     * @param flage
     * @return
     */
    private  PluginNode getFlageNode(PluginNode child,String flage)
        {
            if(child.flage.equals(flage))
            {
                return  child;
            }
            else
            {
                if(child.pluginList!=null)
                {
                    for (PluginNode tmp :child.pluginList
                         ) {
                       var node= getFlageNode(tmp,flage);
                       if(node!=null)
                       {
                           return  node;
                       }
                    }
                }
            }

            return  null;
        }
    /**
     * 开始接收数据
     */
    private  void  start() {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        MsgData msg = null;
                        try {
                            msg = queue.take();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if(msg==null)
                        {
                            continue;
                        }
                       var model= TaskEntity.mapTask.getOrDefault(String.valueOf(msg.taskid),null);
                       //找到对应的流程
                        if(model==null)
                        {
                            logger.error("流程对应数据");
                            continue;
                        }
                        var linkNode = PluginEngine.lst.parallelStream().filter(p->p.name==model.name).findFirst();
                        if(linkNode==null)
                        {
                            logger.error("流程对应错误，检查任务与流程的对应");
                            continue;
                        }
                        var next = linkNode.get().root;
                        PluginNode node=null;
                        if(msg.flage.equals(TaskEntity.rootFlage))
                        {
                            //单独处理接收的任务启动
                            node=new PluginNode();
                            node.pluginList=new ArrayList<>();
                            node.nextNode=new ArrayList<>();
                            node.nextNode.add(node);
                        }
                        else {
                            //找到数据节点
                            node = getFlageNode(next, msg.flage);
                            if (node == null) {
                                logger.error("插件标识与数据标识不匹配，请检查流程中插件标识与数据标识,数据局标识：" + msg.flage);
                                continue;
                            }
                        }
                        //找到下一级节点
                        var child=node.nextNode;
                        if(child!=null)
                        {
                           logger.error("插件无下级节点，不应该返回数据或配置流程错误");
                           continue;
                        }
                        int num=0;
                        for (PluginNode childnode:child
                             ) {
                            try {
                                //准备调度下一级
                                if(ConvertArgs.convertCondition(childnode,msg))
                                {
                                    MsgData rsp= null;
                                    try {
                                        rsp = ConvertArgs.convertInput(childnode,msg);
                                        if(childnode.pluginList==null||childnode.pluginList.size()==0) {
                                            childnode.plugin.addData(rsp);
                                        }
                                        else
                                        {
                                            //多实例
                                            if(node.policy== Policy.All) {
                                                for (PluginNode tmp : node.pluginList
                                                ) {
                                                    tmp.plugin.addData(rsp);
                                                }
                                            }
                                            else  if(node.policy==Policy.Order)
                                            {
                                                 int index= node.index%node.pluginList.size();
                                                  var cur= node.pluginList.get(index);
                                                  cur.plugin.addData(rsp);
                                                  node.index++;
                                            }
                                            else  if(node.policy==Policy.Robin)
                                            {
                                                //负载均衡，准备算法

                                            }
                                            else
                                            {
                                                for (PluginNode tmp : node.pluginList
                                                ) {
                                                    if(ConvertArgs.convertCondition(tmp,rsp)) {
                                                        tmp.plugin.addData(rsp);
                                                    }
                                                }
                                            }

                                        }
                                        //调度插件还有下一级
                                        if(childnode.nextNode!=null&&childnode.nextNode.size()>0)
                                        {
                                            num++;
                                        }
                                    } catch (ClassNotFoundException e) {
                                        throw new RuntimeException(e);
                                    }

                                }
                            } catch (Exception e) {

                               logger.error(e);
                            }
                        }
                        //移除数据了
                        if(num==0)
                        {
                            msgqueue.add(msg.msgno);

                        }
                    }
                }
            });
            thread.setDaemon(true);
            thread.setName("databus");
            thread.start();
        }

   public  void  clear(String taskid)
   {
       queue.removeIf(p->p.taskid.equals(taskid));
   }

}
