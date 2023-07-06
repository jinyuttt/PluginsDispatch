package Task;

import Task.TaskModel;

import javax.swing.plaf.PanelUI;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class TaskEntity {

    /**
     * taskid,
     */
    public static ConcurrentHashMap<String, TaskModel> map=new ConcurrentHashMap<String, TaskModel>();

    //public  static  String taskid="";

    //public  static long instance=0;

    /**
     * 用户接收任务的主题
     */
    public  static  String tasktopic;

    /**
     * 任务根标识
     */
    public  static  final String rootFlage="taskmodel";
}
