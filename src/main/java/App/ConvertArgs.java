package App;

import PluginEntity.MsgData;
import Task.TaskEntity;
import Task.TaskModel;
import bsh.EvalError;
import bsh.Interpreter;
import cache.CacheUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;
import engin.PluginNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import workplugins.PluginAnnotation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * 获取参数 转换
 */
public class ConvertArgs {
    static  Gson gson = new Gson();
   static Log logger= LogFactory.getLog(ConvertArgs.class);

    /**
     * 转换组件需要的数据
     * @param node  组件
     * @param msgData 来源数据
     * @return
     * @throws ClassNotFoundException
     */
    public static MsgData   convertInput(PluginNode node,MsgData msgData) throws ClassNotFoundException {
        PluginAnnotation msg = node.plugin.getClass().getAnnotation(PluginAnnotation.class);
        String pkg=  msg.input();//如果为空已经是前面业务处理，这里判空仅仅是代码的。
        if(pkg.isEmpty())
        {
            return  null;
        }
        Class<?> cls= Class.forName(pkg);//定义的类型和组件加载一起的。
        return (MsgData) ConvertArgs.convertMsg(node,msgData,cls);
    }



    /**
     * 转换
     * @param node 需要传入数据的节点
     * @param msgData 上级数据
     * @param cls 需要的数据类型
     * @return
     */
    private static Object convertMsg(PluginNode node, MsgData msgData,Class<?> cls) {

       Object objv= null;


         var vcur= gson.toJson(msgData);//转成json
         objv=gson.fromJson(vcur,cls);//通过上级数据json创建输入的对象
        JsonElement element =gson.toJsonTree(objv,cls);//将对象转成json处理，
     //   var jsonObject = element.getAsJsonObject();//转成json对象
        var jsonDoc=  JsonPath.parse(gson.toJson(objv));
        //以上相当于同字段复制。
        //根据赋值
        var map = node.map;
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        //遍历键值对集合,根据映射关系转换数据
        for (Map.Entry<String, String> me : entrySet) {
            String msg = me.getValue();
            String[] vlab = msg.split(".");
            if (vlab[0].startsWith("$")) {
                String flage = vlab[0].substring(1);
                var k = String.valueOf(msgData.msgno);
                MsgData cur = (MsgData) CacheUtil.getInstance().get(flage, k);
                var jsn = gson.toJson(cur);
                String  data= msg.replaceFirst(vlab[0], "");//去除flage
                Object v = JsonPath.read(jsn, "$" + data);
               // JsonElement jsonElement = gson.toJsonTree(v);
                jsonDoc.set("$."+me.getKey(),v);
               // jsonObject.add(me.getKey(), jsonElement);//将转换的值加入

            }
            else if (vlab[0].startsWith("[") && vlab[vlab.length-1].startsWith("]"))
            {
                String c = msg.substring(1, msg.length() - 1);

                if (c.toLowerCase().equals("devid")) {
                   // jsonObject.addProperty(me.getKey(), node.devid);
                    jsonDoc.set("$."+me.getKey(),node.devid);
                }
            }
            else if(vlab[0].startsWith("#"))
            {
                //全局
                String c = msg.substring(1);//去除#
                String taskid= msgData.taskid;
                TaskModel taskModel=TaskEntity.mapTask.get(taskid);
                if(taskModel!=null)
                {
                    var jsn = gson.toJson(taskModel);
                    Object v = JsonPath.read(jsn, "$." + c);
                    jsonDoc.set("$."+me.getKey(),v);
                 //   JsonElement jsonElement = gson.toJsonTree(v);
                  //  jsonObject.add(me.getKey(),jsonElement);
                }
            }
        }
        //jsonDoc.json();
       return gson.fromJson(jsonDoc.jsonString(),cls);
    }


    /**
     * 获取条件结果
     * @param node
     * @param msgData
     * @return
     * @throws EvalError
     */
    public static boolean  convertCondition(PluginNode node,MsgData msgData) throws EvalError {

        String condition=node.condition;
        if(condition==null||condition.isEmpty())
        {
            return  true;
        }
        String[] vlab=condition.split(",");//条件已逗号分割，第一个是表达式，后面是表达式变量
        Map<String,Object> map=new HashMap<>();
        if(vlab.length>1) {
            for (int i = 1; i < vlab.length; i++) {
                if (vlab[i].startsWith("$")) {//$标识上级节点的参数
                    String[] att = vlab[i].split(".");//取出变量中的数据
                    String flage = att[0].substring(1);//去掉$,找到名称，上级节点的数据第一个为flage名称，后面才是数据
                    var k = String.valueOf(msgData.msgno);
                    MsgData cur = (MsgData) CacheUtil.getInstance().get(flage, k);//缓存数据查找
                    if(cur==null)
                    {
                        //数据已经作废,理论上必须取到，取不到则异常了
                        logger.error("缓存中取不到数据，标识："+flage);
                        return  false;
                    }
                    var jsn = gson.toJson(cur);//取出数据
                    String c = vlab[i].replaceAll(att[0], "");//去除flage,只有数据
                    Object v = JsonPath.read(jsn, "$" + c);//解析出值
                    map.put(vlab[i], v);//添加表达式参数
                } else if (vlab[i].startsWith("[") && vlab[i].startsWith("]")) {//如果遇到本节点参数，目前只有devid
                    String c = vlab[i].substring(1, vlab[i].length() - 1);//去除[]
                    vlab[0]=vlab[0].replaceFirst(vlab[i],c);//将条件表达式去除[]
                    if (c.toLowerCase().equals("devid")) {
                        map.put(c, node.devid);
                    }

                } else if (vlab[i].startsWith("#")) {
                    //全局
                    String c = vlab[i].substring(1, vlab[i].length() );//去除#
                    vlab[0]=vlab[0].replaceFirst(vlab[i],c);//去除#
                    String taskid= msgData.taskid;
                    TaskModel taskModel=TaskEntity.mapTask.get(taskid);
                    if(taskModel!=null)
                    {
                        var jsn = gson.toJson(taskModel);
                        Object v = JsonPath.read(jsn, "$." + c);
                        map.put(c,v);
                    }
                    else
                    {
                        logger.error("没有找到任务参数");
                        return  false;
                    }

                }
                else
                {
                    //说明是上一级节点的数据。，直接取出数据
                    var jsn = gson.toJson(msgData);
                    Object v = JsonPath.read(jsn, "$"+vlab[i]);
                    map.put(vlab[i], v);
                }
            }
        }
        return   getCondtion(vlab[0], map);
    }

    /**
     * 获取条件结果
     * @param condtion
     * @param args
     * @return
     * @throws EvalError
     */
    private static boolean getCondtion(String condtion,Map<String,Object> args) throws EvalError {
        Interpreter interpreter = new Interpreter();
        for(Map.Entry<String,Object> entry : args.entrySet()) {
            interpreter.set(entry.getKey(), entry.getValue());
        }
        Object ret= interpreter.eval(condtion);
        return (boolean) ret;
    }
}

