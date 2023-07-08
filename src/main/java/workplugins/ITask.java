package workplugins;

/**
 * 任务完成
 */
public interface ITask {

    /**
     * 判断任务是否完成
     * @return
     */
    boolean isComplete(String taskid);

    /**
     * 传入结果数据
     * @param obj
     */
    void  adddata(Object obj);
}
