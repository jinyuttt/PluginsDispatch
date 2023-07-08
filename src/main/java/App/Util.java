package App;

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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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

   static Log logger= LogFactory.getLog(EnginCore.class);
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
    public static ArrayList<String> readFiles1(String path, ArrayList<String> fileNameList) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    readFiles1(files[i].getPath(), fileNameList);
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
     * @throws NoSuchMethodException
     * @throws MalformedURLException
     * @throws ClassNotFoundException
     */
    private static  List<IPlugin> getCurrentPlugin(ArrayList<String> fileNameList) throws NoSuchMethodException, MalformedURLException, ClassNotFoundException {
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
            readFiles1(f.getPath(), lstcls);
            lst = getCurrentPlugin(lstcls);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return lst;
    }


    /**
     * xml节点转换PluginNode
     * @param element
     * @return
     */
    private static  PluginNode getNode(Element element)
    {
        int num=0;
        //处理当前节点
        PluginNode pluginNode=new PluginNode();
       var attr= element.attribute("name");
       if(attr!=null)
       {
           pluginNode.name= attr.getText();
       }
       attr=element.attribute("flage");
        if(attr!=null)
        {
            pluginNode.flage=  attr.getText();
        }
        attr=element.attribute("arg");
        if(attr!=null)
        {
            pluginNode.arg=  attr.getText();
        }
        attr=element.attribute("condtion");
        if(attr!=null)
        {
            pluginNode.condition= attr.getText();
        }
        attr=element.attribute("instance");
        if(attr!=null)
        {
           String count= attr.getText();
           num=Integer.valueOf(count);
        }
        attr=element.attribute("subtopic");
        if(attr!=null)
        {
            String topic= attr.getText();
            pluginNode.subTopic=new ArrayList<>();
            String[] strs=topic.split(",");
            pluginNode.subTopic.addAll(Arrays.asList(strs));
            for (String tmp:strs
                 ) {
              var lst=  PluginEngine.topic.getOrDefault(topic,null);
              if(lst==null)
              {
                  lst=new ArrayList<>();
                  PluginEngine.topic.put(tmp,lst);
              }
              lst.add(pluginNode);

            }
        }
        var child=element.element("Args");
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
                pluginNode.map=map;
            }
        }
        if(num>1)
        {
            //处理本节点多实例
            var childs=element.element("Nodes");
            if(childs!=null)
            {
                pluginNode.pluginList=new ArrayList<>();
                List<Element> lst=  childs.elements("Node");
                for (Element nodetmp:lst
                     ) {
                     PluginNode tmp=new PluginNode();
                     tmp.name=pluginNode.name;
                    attr=nodetmp.attribute("flage");
                    if(attr!=null)
                    {
                        tmp.flage=  attr.getText();
                    }
                    attr=nodetmp.attribute("arg");
                    if(attr!=null)
                    {
                        tmp.arg=  attr.getText();
                    }
                    attr=nodetmp.attribute("condtion");
                    if(attr!=null)
                    {
                        tmp.condition= attr.getText();
                    }
                    attr=nodetmp.attribute("devid");
                    if(attr!=null)
                    {
                        tmp.devid= attr.getText();
                    }
                    attr=element.attribute("subtopic");
                    if(attr!=null)
                    {
                        String topic= attr.getText();
                        tmp.subTopic=new ArrayList<>();
                        String[] strs=topic.split(",");
                        pluginNode.subTopic.addAll(Arrays.asList(strs));
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
          // pluginNode.nexNode= getNode(element.element("Plugin"));
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
                pluginNode.nextNode.add(getNode(ele));
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
                            pluginNode.nextNode.add(getNode(childo));
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
    public  static List<LinkNode> getNode(String path)
    {
        String file = path;
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(file);
            //获取XML文档的根节点，即hr标签
            Element root = document.getRootElement();
            //elements方法用于获取指定的标签集合
            List<Element> employees =  root.elements("Link");
            List<LinkNode> lst=new ArrayList<>();
            for(Element employee : employees){
                LinkNode node=new LinkNode();
              Attribute att= employee.attribute("name");
              if(att!=null)
              {
                  node.name=att.getText();
              }
                List<Element>  elements= employee.elements("Plugin");
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
                        node.root=getNode(e);
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



}

