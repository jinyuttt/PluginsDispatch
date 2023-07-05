package Task;

import Task.TaskModel;

import java.util.concurrent.ConcurrentHashMap;

public class TaskEntity {

    /**
     * taskid,
     */
    public static ConcurrentHashMap<String, TaskModel> map=new ConcurrentHashMap<String, TaskModel>();

    public  static  String taskid="";

    public  static long instance=0;
}
