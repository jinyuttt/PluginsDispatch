package workplugins;

public enum Policy {

    /**
     * 随机负载均衡
     */
    Robin,
    /**
     * 顺序轮训
     */
    Order,

    /**
     * 条件
     */
    Condition,

    /**
     * 全部调度
     */
    All
}
