package App;

import PluginEntity.MsgData;
import bsh.EvalError;
import bsh.Interpreter;
import cache.CacheUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;
import engin.PluginNode;
import workplugins.PluginAnnotation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 获取参数
 */
public class ConvertArgs {
    static    Gson gson = new Gson();

    /**
     * 转换组件需要的数据
     * @param node  组件
     * @param msgData 来源数据
     * @return
     * @throws ClassNotFoundException
     */
    public static MsgData   convertInput(PluginNode node,MsgData msgData) throws ClassNotFoundException {

        PluginAnnotation msg = node.plugin.getClass().getAnnotation(PluginAnnotation.class);
        String pkg=  msg.input();
        Class<?> cls= Class.forName(pkg);
        return (MsgData) ConvertArgs.convertMsg(node,msgData,cls);

    }

    /**
     * 转换
     * @param node
     * @param msgData
     * @param cls 需要的数据类型
     * @return
     */
    private static Object convertMsg(PluginNode node, MsgData msgData,Class<?> cls) {

       Object objv= null;
        try {
            objv=  cls.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonElement element =  gson.toJsonTree(objv,cls);
        var obj = element.getAsJsonObject();
        var vjson = gson.toJson(msgData);
        var vcur = gson.fromJson(vjson, JsonObject.class);
        //首先将相同的字段赋值
        var fileds = obj.entrySet();
        for (Map.Entry<String, JsonElement> enty : fileds
        ) {
            var vlaue = vcur.get(enty.getKey());
            obj.add(enty.getKey(), vlaue);
        }
        //根据赋值
        var map = node.map;
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        //遍历键值对集合
        for (Map.Entry<String, String> me : entrySet) {
            String msg = me.getValue();
            String[] vlab = msg.split(".");
            for (int i = 0; i < vlab.length; i++) {
                if (vlab[i].startsWith("$")) {
                    String flage = vlab[0].substring(1);
                    var k = String.valueOf(msgData.msgno);
                    MsgData cur = (MsgData) CacheUtil.getInstance().get(flage, k);
                    var jsn = gson.toJson(cur);
                    String c = msg.replaceAll(vlab[0], "");
                    Object v = JsonPath.read(jsn, "$" + c);
                    String jsonElement = gson.toJson(v);
                    JsonElement jsonElement1 = gson.fromJson(jsonElement, JsonElement.class);
                    obj.add(me.getKey(), jsonElement1);
                } else if (vlab[i].startsWith("[") && vlab[i].startsWith("]")) {
                    String c = vlab[i].substring(1, vlab[i].length() - 1);
                    if (c.toLowerCase().equals("devid")) {
                        obj.addProperty(me.getKey(), node.devid);
                    }
                } else if (vlab[i].startsWith("#")) {
                    //全局
                }


            }
        }
       return gson.fromJson(obj,cls);
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
        String[] vlab=condition.split(",");
        Map<String,Object> map=new HashMap<>();
        if(vlab.length>1) {
            for (int i = 1; i < vlab.length; i++) {
                if (vlab[i].startsWith("$")) {
                    String[] att = condition.split(".");
                    String flage = att[0].substring(1);
                    var k = String.valueOf(msgData.msgno);
                    MsgData cur = (MsgData) CacheUtil.getInstance().get(flage, k);
                    Gson gson = new Gson();
                    var jsn = gson.toJson(cur);
                    String c = condition.replaceAll(att[0], "");
                    Object v = JsonPath.read(jsn, "$" + c);
                    map.put(vlab[i], v);
                } else if (vlab[i].startsWith("[") && vlab[i].startsWith("]")) {
                    String c = vlab[i].substring(1, vlab[i].length() - 1);
                    if (c.toLowerCase().equals("devid")) {
                        map.put(vlab[i], node.devid);

                    }
                } else if (vlab[i].startsWith("#")) {
                    //全局
                }
                else
                {

                    Gson gson = new Gson();
                    var jsn = gson.toJson(msgData);
                    Object v = JsonPath.read(jsn, "$"+vlab[i]);
                    map.put(vlab[i], v);
                }
            }
        }
        return   getCondtion(condition,map);
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

