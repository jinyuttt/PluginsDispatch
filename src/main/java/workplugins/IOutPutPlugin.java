package workplugins;

/**
 * 输出数据组件，传入数据处理后不会再回传调度数据
 */
public interface IOutPutPlugin extends IPlugin{

    void stop();

    void finsh();

}
