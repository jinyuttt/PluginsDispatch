package Balancing;

class Ip_active_weight {
    String ip;
    //活跃数
    int active;
    //权重
    int weight;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Ip_active_weight{" +
                "ip='" + ip + '\'' +
                ", active=" + active +
                ", weight=" + weight +
                '}';
    }
}
