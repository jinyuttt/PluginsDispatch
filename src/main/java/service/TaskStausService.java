package service;

import bsh.EvalError;
import bsh.Interpreter;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import workplugins.ITask;
import workplugins.PluginAnnotation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

@PluginAnnotation(name = "comstatus")
public class TaskStausService implements ITask {

    Map<String,Boolean>map=new HashMap<>();//任务状态
    Map<String,Object>mapObj=new HashMap<>();//任务结构
    Map<String,String> mapArgs=new HashMap<>();//任务参数
    ConcurrentLinkedQueue<Object> queue=new ConcurrentLinkedQueue<>();

    volatile  boolean isinit=false;//初始化
    Gson gson=new Gson();
    Interpreter interpreter = new Interpreter();
    private void start()
    {
        if(isinit)
        {
            return;
        }
        isinit=true;
        Thread ss=new Thread(new Runnable() {
            @Override
            public void run() {
             while (true)
             {
                 var rsp=  queue.poll();
                 if(rsp==null)
                 {
                     try {
                         Thread.sleep(2000);
                     } catch (InterruptedException e) {
                         throw new RuntimeException(e);
                     }
                     if(map.isEmpty())//没有任务
                    {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    continue;
                 }
                 var json=gson.toJson(rsp);
                 var id= mapArgs.get("resultid");
                 String taskid= JsonPath.read("$"+json,id);
                 var t= mapObj.get(taskid);
                 var tjson=gson.toJson(t);
                 var c=  mapArgs.get("condtion");
                String[] artt=null;
                    if(c.contains("=")) {
                        artt = c.split("=");
                    }
                 try {
                     interpreter.set(artt[0],  JsonPath.read("$."+tjson,artt[0]));
                     interpreter.set(artt[1],  JsonPath.read("$."+json,artt[1]));
                     boolean ret= (boolean) interpreter.eval(c);
                     if(ret)
                     {
                         map.put(taskid,true);
                     }
                 } catch (EvalError e) {
                     throw new RuntimeException(e);
                 }
             }
            }
        });
        ss.start();
    }

    @Override
    public void initArg(Map<String, String> map) {
         mapArgs=map;
         start();
    }

    @Override
    public void init(Object obj) {

        var json=gson.toJson(obj);
        var id= mapArgs.get("taskid");
        String taskid= JsonPath.read("$"+json,id);
        map.put(taskid,false);
        mapObj.put(taskid,obj);

    }

    @Override
    public boolean isComplete(String taskid) {
        return map.getOrDefault(taskid,false);
    }

    @Override
    public void adddata(Object obj) {
        //防止阻塞调度
        queue.offer(obj);
    }
    @Override
    public void clear(String taskid) {
        map.remove(taskid);
        mapObj.remove(taskid);
    }
}
