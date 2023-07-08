package workplugins;

import java.util.Map;

/**
 * 任务完成
 */
public interface ITask {


    /**
     * 传入参数
     * @param map
     */
    void initArg(Map<String,String> map);

    /**
     * 任务数据
     * @param obj
     */
    void init(Object obj);

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

    /**
     * 已经完成
     * @param taskid
     */
    void clear(String taskid);
}
