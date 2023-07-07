package workplugins;

/**
 * 输入数据插件，启动后只会向调度传输数据，不会处理数据
 */
public interface IInputPlugin extends IPlugin {
    void start();
    void stop();

}
