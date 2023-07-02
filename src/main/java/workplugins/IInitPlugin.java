package workplugins;

public interface IInitPlugin extends IPlugin {
    void  init();
    void  start();

    void  stop();
}
