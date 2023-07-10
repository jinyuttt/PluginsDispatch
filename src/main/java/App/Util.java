package App;

import com.google.gson.Gson;
import engin.LinkNode;
import engin.PluginEngine;
import engin.PluginNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import workplugins.IPlugin;
import workplugins.ITask;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 工具类 读取配置扫描组件
 */
public class Util {

   static Log logger= LogFactory.getLog(Util.class);

    /**
     * 任务插件
     */
   static List<ITask> lstTask=new ArrayList<>();

    /**
     * 读取Jar
     * @param path 路径
     * @return
     * @throws IOException
     */
    public static List<IPlugin> getPlugin(String path) throws IOException {
        List<IPlugin> lst = new ArrayList<>();
        JarFile jarFile = new JarFile(path);
        Enumeration<JarEntry> e = jarFile.entries();
        JarEntry entry;
        while (e.hasMoreElements()) {
            entry = (JarEntry) e.nextElement();
            //
            if (entry.getName().indexOf("META-INF") < 0 && entry.getName().indexOf(".class") >= 0) {
                String classFullName = entry.getName();
                //去掉后缀.class
                String className = classFullName.substring(0, classFullName.length() - 6).replace("/", ".");
                System.out.println(className);

                Class<?> c = null;
                try {
                    try {
                        // 用className这个类来装载c,但还没有实例化
                        c = Class.forName(className);
                        if (!c.isInterface()) {
                            try {

                                if (!c.getConstructor().trySetAccessible()) {
                                    continue;
                                }
                            }
                            catch (Exception ex)
                            {
                                continue;
                            }
                            var obj=c.getConstructor().newInstance();
                            if (IPlugin.class.isInstance(obj)) {

                                lst.add((IPlugin) obj);
                            }
                            if (ITask.class.isInstance(obj)) {

                               lstTask.add((ITask) obj);
                            }
                        }
                    } catch (ClassNotFoundException e1) {
                        e1.printStackTrace();
                        logger.error(e1);

                    }
                } catch (Exception e1) {

                }

            }
        }
        return lst;
    }

    /**
     * 获取文件
     * @param path
     * @param fileNameList
     * @return
     */
    public static ArrayList<String> readFiles(String path, ArrayList<String> fileNameList) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    readFiles(files[i].getPath(), fileNameList);
                } else {
                    String path1 = files[i].getPath();
                    fileNameList.add(path1);
                }
            }
        } else {
            String path1 = file.getPath();
            String fileName = path1.substring(path1.lastIndexOf("\\") + 1);
            fileNameList.add(fileName);
        }
        return fileNameList;
    }

    /**
     *
     * @param fileNameList
     * @return
     * @throws MalformedURLException
     * @throws ClassNotFoundException
     */
    private static  List<IPlugin> getCurrentPlugin(ArrayList<String> fileNameList) throws MalformedURLException, ClassNotFoundException {
        List<IPlugin> lst = new ArrayList<>();
        for (var file:fileNameList
             ) {
            var classFullName = file;
            String className = classFullName.substring(0, classFullName.length() - 6).replace("/", ".");
            System.out.println(className);
            //
             if(!classFullName.endsWith(".class"))
             {
                 continue;
             }
                File file1 = new File(file);
                var clazzPath = file1.getParentFile();
                URL url=clazzPath.toURI().toURL();
           // 然后加载类：
            ClassLoader loader = new URLClassLoader(new URL[]{url});
            var clsname=className.substring(clazzPath.getParentFile().getAbsolutePath().length()+1);
             clsname=clsname.replace("\\",".");
           // 然后加载类：
            Class<?> cls=loader.loadClass(clsname);

                Class<?> c = null;

                try {
                    // 用className这个类来装载c,但还没有实例化
                 //  c = Class.forName(clsname);
                    if (!cls.isInterface()) {
                       try {
                           if (!cls.getConstructor().trySetAccessible()) {
                               continue;
                           }
                       }
                       catch (Exception ex)
                       {
                           continue;
                       }
                      var obj=  cls.getConstructor().newInstance();
                        if (IPlugin.class.isInstance(obj)) {

                            lst.add((IPlugin) obj);
                        }
                        if((ITask.class.isInstance(obj)))
                        {
                            lstTask.add((ITask) obj);
                        }
                    }
                } catch (Exception e1) {
                   logger.error(e1);
                }


        }
            return lst;
    }

    /**
     * 获取当前包的组件
     * @return
     */
    public static List<IPlugin> getCurrenPlugin() {
        List<IPlugin> lst = new ArrayList<>();
        try {
            ArrayList<String> lstcls = new ArrayList<>();
            var pkgs = Util.class.getModule().getPackages();
            final File f = new File(Util.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            readFiles(f.getPath(), lstcls);
            lst = getCurrentPlugin(lstcls);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return lst;
    }


    /**
     * 拷贝数据
     * @param obj
     * @return
     * @param <T>
     */
    private static <T> T CopyObj(T obj)
    {
        Gson gson=new Gson();
       var json= gson.toJson(obj);
       T o= (T) gson.fromJson(json,obj.getClass());
       return o;
    }
    /**
     * xml节点转换PluginNode
     * @param element
     * @return
     */
    private static  PluginNode getLinkNode(Element element)
    {
        int num=0;
        //处理当前节点
        PluginNode pluginNode=new PluginNode();
       var attr= element.attribute("name");//读取节点组件名称
       if(attr!=null)
       {
           pluginNode.name= attr.getText();
       }
       attr=element.attribute("flage");//读取节点组件唯一标识
        if(attr!=null)
        {
            pluginNode.flage=  attr.getText();
        }
        attr=element.attribute("arg");//读取组件的参数
        if(attr!=null)
        {
            pluginNode.arg=  attr.getText();
        }
        attr=element.attribute("condtion");//读取节点组件的条件
        if(attr!=null)
        {
            pluginNode.condition= attr.getText();
        }
        attr=element.attribute("instance");//读取节点组件实例个数
        if(attr!=null)
        {
           String count= attr.getText();
           num=Integer.valueOf(count);
        }
        attr=element.attribute("displayname");//读取节点组件唯一标识
        if(attr!=null)
        {
            pluginNode.display=  attr.getText();
        }
        attr=element.attribute("subtopic");//读取节点的订阅主题
        if(attr!=null)
        {
            String topic= attr.getText();
            pluginNode.subTopic=new ArrayList<>();
            String[] strs=topic.split(",");//逗号分割
            pluginNode.subTopic.addAll(Arrays.asList(strs));
            for (String tmp:strs
                 ) {
              var lst=  PluginEngine.topic.get(topic);
              if(lst==null)
              {
                  lst=new ArrayList<>();
                  PluginEngine.topic.put(tmp,lst);
              }
              lst.add(pluginNode);
            }
        }
        attr=element.attribute("convert");//读取节点的转换文件
        if(attr!=null)
        {
            String file= attr.getText();
            try {
                pluginNode.map=readIni(file);
            } catch (IOException e) {
                logger.error(e);
            }
        }
        var child=element.element("Args");//读取的参数转换关系
        if(child!=null)
        {
            Element args= child;
            if(args!=null)
            {
                Map<String,String> map=new HashMap<>();
                List<Element> arglst=args.elements();
                for (Element e:arglst
                ) {
                    map.put(e.getName(),e.getText());
                }
                if(pluginNode.map==null) {
                    pluginNode.map = map;
                }
                else
                {
                    pluginNode.map.putAll(map);
                }
            }
        }

        if(num>1)
        {
            //处理本节点多实例
            var childs=element.element("Nodes");//读取配置
            if(childs!=null)
            {
                pluginNode.pluginList=new ArrayList<>();
                List<Element> lst=  childs.elements("Node");//每个实例
                for (Element nodetmp:lst
                     ) {
                     PluginNode tmp=CopyObj(pluginNode);

                    attr=nodetmp.attribute("flage");//每个实例唯一
                    if(attr!=null)
                    {
                        tmp.flage=  attr.getText();
                    }
                    attr=nodetmp.attribute("arg");//实例参数
                    if(attr!=null)
                    {
                        tmp.arg=  attr.getText();
                    }
                    attr=nodetmp.attribute("condtion");//实例条件
                    if(attr!=null)
                    {
                        tmp.condition= attr.getText();
                    }
                    attr=nodetmp.attribute("devid");//实例对应的设备id
                    if(attr!=null)
                    {
                        tmp.devid= attr.getText();
                    }
                    attr=nodetmp.attribute("weight");//实例对应的设备id
                    if(attr!=null)
                    {
                        tmp.devid= attr.getText();
                    }
                    attr=element.attribute("subtopic");//实例对应的订阅
                    if(attr!=null)
                    {
                        String topic= attr.getText();
                        tmp.subTopic=new ArrayList<>();
                        String[] strs=topic.split(",");
                        pluginNode.subTopic.addAll(Arrays.asList(strs));

                        //同时放入
                        for (String tmptopic:strs
                        ) {
                            var lstNode=  PluginEngine.topic.getOrDefault(tmptopic,null);
                            if(lstNode==null)
                            {
                                lstNode=new ArrayList<>();
                                PluginEngine.topic.put(tmptopic,lstNode);
                            }
                            lstNode.add(tmp);
                        }
                    }
                    pluginNode.pluginList.add(tmp);
                }

            }
        }
        if(element.elements("Plugin").size()>0)
        {

            //继续处理子节点
           List<Element> list=  element.elements("Plugin");
           pluginNode.nextNode=new ArrayList<>();
            for (Element ele:list
                 ) {

               var eattr=  ele.attribute("ischild");
               if(eattr!=null)
               {
                   if(eattr.getText().trim().toLowerCase().equals("true"))
                   {
                       //只是跳转子节点，不是本节点的子节点
                       continue;
                   }
               }
                pluginNode.nextNode.add(getLinkNode(ele));
            }
        }
        else
        {
            //跳转子节点
            attr=element.attribute("child");
            if(attr!=null)
            {
                pluginNode.nextNode=new ArrayList<>();
                String[] chlds=attr.getText().split(",");
                for (String flagearr:chlds
                     ) {
                      String str="//Plugin[@flage='"+flagearr+"']";
                    var nodes=  element.selectNodes(str);
                    if(nodes!=null) {
                        Element childo = (Element) nodes.get(0);
                        if (childo != null) {
                            pluginNode.nextNode.add(getLinkNode(childo));
                        }
                    }
                }


            }
        }
        return  pluginNode;
    }

    /**
     * 获取链路
     * @param path xml路径
     * @return
     */
    public  static List<LinkNode> getLinkNode(String path)
    {
        String file = path;
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(file);
            //获取XML文档的根节点，即hr标签
            Element root = document.getRootElement();
            //elements方法用于获取指定的标签集合
            List<Element> link =  root.elements("Link");
            List<LinkNode> lst=new ArrayList<>();
            for(Element linknode : link){
                LinkNode node=new LinkNode();
              Attribute att= linknode.attribute("name");
              if(att!=null)
              {
                  node.name=att.getText();
              }
              att= linknode.attribute("taskcomplete");
                if(att!=null)
                {
                    node.taskComplete=att.getText();
                }
                List<Element>  elements= linknode.elements("Plugin");
                node.iniPlugin=new ArrayList<>();
                for (Element  e:elements
                     ) {
                    PluginNode tmp=new PluginNode();
                    tmp.name=  e.attribute("name").getText();
                    if(e.elements("Plugin").size()==0)
                    {
                        node.iniPlugin.add(tmp);
                    }
                    else
                    {
                        node.root= getLinkNode(e);
                    }
                }
                lst.add(node);
            }
            return lst;
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
     return null;
    }


    /**
     * 读取映射关系
     * @param path
     * @return
     * @throws IOException
     */
    public static Map<String,String> readIni(String path) throws IOException {
        Properties properties = new Properties();
        FileReader fileReader = new FileReader(path);

        properties.load(fileReader);
        Map<String,String> map=new HashMap<>();
        for (Map.Entry p:properties.entrySet()
             ) {
            map.put(p.getKey().toString(),p.getValue().toString());
        }
        return  map;
    }

}

