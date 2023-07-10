package Task;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务消息
 */
public class TaskEntity {

    /**
     * taskid,接收的任务消息
     */
    public static ConcurrentHashMap<String, TaskModel> mapTask =new ConcurrentHashMap<String, TaskModel>();


    /**
     * taskid,任务状态
     */
    public static ConcurrentHashMap<String,TaskStatus> mapStatus=new ConcurrentHashMap<String, TaskStatus>();

    /**
     * 接收任务的主题，配置
     */
    public  static  String tasktopic;

    /**
     * 任务根标识，接收的是任务数据开始数据，数据需要找起点
     */
    public  static  final String rootFlage="taskmodel";
}
