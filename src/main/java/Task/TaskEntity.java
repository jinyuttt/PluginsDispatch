package Task;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务消息
 */
public class TaskEntity {

    /**
     * taskid,
     */
    public static ConcurrentHashMap<String, TaskModel> mapTask =new ConcurrentHashMap<String, TaskModel>();


    /**
     * taskid,
     */
    public static ConcurrentHashMap<String,TaskStatus> mapStatus=new ConcurrentHashMap<String, TaskStatus>();

    /**
     * 用户接收任务的主题
     */
    public  static  String tasktopic;

    /**
     * 任务根标识
     */
    public  static  final String rootFlage="taskmodel";
}
