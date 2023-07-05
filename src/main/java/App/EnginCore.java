package App;

import engin.LinkNode;
import engin.PluginEngine;
import engin.PluginNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import workplugins.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EnginCore {
    Log logger= LogFactory.getLog(EnginCore.class);
    ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
    public void start()
    {
       String path= this.getClass().getClassLoader().getResource("").getPath();
        //读取xml
       path= path+"Plugin.xml";
       var lst= Util.getNode(path);
       var lstP=Util.getCurrenPlugin();
        PluginEngine.lst=lst;
        for (LinkNode link:lst
             ) {
            if(link.iniPlugin!=null)
            {
                for (PluginNode node:link.iniPlugin
                     ) {
                    var tmpss=   lstP.stream().filter(p->{
                        PluginAnnotation name =  p.getClass().getAnnotation(PluginAnnotation.class);
                        if(IInitPlugin.class.isInstance(p))
                        {
                            if(name.name().toLowerCase().equals(node.name.toLowerCase()))
                            {
                                return  true;
                            }
                        }
                     return false;
                 });
                 if(tmpss==null)
                 {
                     continue;
                 }
                  var tmp=tmpss.findFirst();
                    try {
                        node.plugin = tmp.get().getClass().getConstructor().newInstance();
                        IInitPlugin cur= (IInitPlugin) node.plugin;

                        newCachedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                cur.init();
                                cur.start();
                            }
                        });


                    }catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                if(link.root!=null)
                {
                     PluginNode node=link.root;
                  do {

                      PluginNode finalNode = node;
                      var tmp = lstP.stream().filter(p -> {
                          PluginAnnotation name = p.getClass().getAnnotation(PluginAnnotation.class);
                          String cur= finalNode.name;
                          if (name.name().toLowerCase().equals(cur.toLowerCase())) {
                              return true;
                          }

                          return false;
                      }).findFirst();
                      try {
                          node.plugin = tmp.get().getClass().getConstructor().newInstance();
                          if(node.pluginList!=null)
                          {
                              for (PluginNode tmpnode: node.pluginList
                                   ) {
                                  tmpnode.plugin=tmp.get().getClass().getConstructor().newInstance();
                                  if(IInputPlugin.class.isInstance(tmpnode.plugin))
                                  {
                                       IInputPlugin inputPlugin= (IInputPlugin) tmpnode.plugin;
                                       newCachedThreadPool.execute(new Runnable() {
                                           @Override
                                           public void run() {
                                               inputPlugin.start();

                                           }
                                       });


                                  }
                                  if(IProcessPlugin.class.isInstance(tmpnode.plugin))
                                  {
                                      IProcessPlugin processPlugin= (IProcessPlugin) tmpnode.plugin;

                                  }
                              }
                          }
                          else
                          {

                              if(IInputPlugin.class.isInstance(node.plugin))
                              {
                                  IInputPlugin inputPlugin= (IInputPlugin) node.plugin;
                                  inputPlugin.start();

                              }

                          }
                      }
                      catch (Exception ex)
                      {
                          ex.printStackTrace();
                      }
                      node=node.nexNode;
                  }
                  while (node!=null);
                }
            }
        }

        System.out.println("");
    }
}
