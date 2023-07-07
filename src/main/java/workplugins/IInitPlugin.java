package workplugins;

/**
 * 初始化数据组件，启动不会返回数据
 */
public interface IInitPlugin extends IPlugin {
    void  init();
    void  start();

    void  stop();
}
