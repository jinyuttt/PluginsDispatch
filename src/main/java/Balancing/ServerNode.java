package Balancing;

 public  class ServerNode {
    String ip;
    int weight;//初始配置好的权重
    int currentWeight;//当前的权重,初始为 0

    public ServerNode(String ip, int weight) {
        this.ip = ip;
        this.weight = weight;
    }

    public void decrementTotal(int totalWeight) {
        currentWeight = currentWeight - totalWeight;
    }

    public void incrementWeight() {
        currentWeight = currentWeight + weight;
    }

    public void setCurrentWeight(int currentWeight) {
        this.currentWeight = currentWeight;
    }

     @Override
     public String toString() {
         return ip;
     }
 }
