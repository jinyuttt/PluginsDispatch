package Balancing;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 负载均衡算法
 * 一般用轮询（Round）、加权轮询（weightRound）、hash一致（consistencyHashLoadBlance）
 */
public class LoadBlanceInstance {
     Map<String, Integer> serverWeightMap = new HashMap<>();
    static  int visualNum=1000;//虚拟个数

    public  int clientnum=0;

    public List<String> lstClient=new ArrayList<>();
    Random  random=new Random();
    int inddexclient=0;

    /**
     * 初始化服务器地址和加权值
     * @param map
     */
    public   void init(Map<String, Integer> map)
    {
        serverWeightMap.clear();
        serverWeightMap.putAll(map);
        treeMap.clear();
        createVisul();
    }

    /**
     * 获取地址
     * @return
     */
    private  List<String> MapCasttoList() {
        //这里重新一个map,作为缓冲, 目的是避免服务器上下线带来的问题
        Map<String, Integer> serverMap = getServerMap();
        Set<String> strings = serverMap.keySet();
        List<String> list = new ArrayList<>();
        list.addAll(strings);
        return list;
    }

    private  Map<String, Integer> getServerMap() {
        Map<String, Integer> serverMap = new HashMap<>();
        serverMap.putAll(serverWeightMap);
        return serverMap;
    }

     AtomicInteger index = new AtomicInteger();

    //1: 简单的轮询算法:

    /**
     * 轮询算法
     * @return
     */
    public  String Round() {
        List<String> ipList = MapCasttoList();
        if (index.get() >= ipList.size()) {
            index.set(0);
        }
        return ipList.get(index.getAndIncrement() % ipList.size());
    }


    //2: 随机算法

    /**
     * 随机算法
     * @return
     */
    public  String RandomLoadBlance() {
        List<String> ipList = MapCasttoList();
        int index = new Random().nextInt(ipList.size());
        return ipList.get(index);
    }


    //3: ip 的hash法: 将ip hash,

    /**
     * p 的hash法: 将ip hash,
     * @return
     */
    public  String IpHashLoadBlance() {
        List<String> ipList = MapCasttoList();
        //获取ipList, 然后计算HashCode, 之后和 size计算出对应的index
        List<Long> ipHashList = ipList.stream().map(ip -> myHashCode(ip)).collect(Collectors.toList());
        int i = new Random().nextInt(ipList.size());
        Long index = ipHashList.get(i) % ipList.size();
        return ipList.get(index.intValue());
    }

    //因为 hashCode方法会出现负数,所以这里使用自写的hashCode方法
    private  long myHashCode(String str) {
        long h = 0;
        if (h == 0) {
            int off = 0;
            char val[] = str.toCharArray();
            long len = str.length();
            for (long i = 0; i < len; i++) {
                h = 31 * h + val[off++];
            }
        }
        return h;
    }

    /**
     * 4: 一致性hash 负载轮询算法
     *
     *
     */
     TreeMap<Long, String> treeMap = new TreeMap<>();


    //str 这里的str 是指使用某个请求的标志(请求名,或者别的),来hash,最终命中hash环对应的ip
    private   void  createVisul()
    {
        List<String> serverIpList = MapCasttoList();
        for (String ip : serverIpList) {
            for (int i = 0; i < visualNum; i++) {
                //这里的hash算法,是对 2 ^ 32 次方 取模
                long ipHashCode = circleHash(ip + i);
                treeMap.put(ipHashCode, ip);
            }
        }
    }
    /**
     * hash一致
     * @param str  一个标识
     * @return
     */
    public  String consistencyHashLoadBlance(String str) {
        long hashCode = circleHash(str);
        //找到比这个 hashCode 值大的所有子树
        SortedMap<Long, String> tailSubMap = treeMap.tailMap(hashCode);
        if (tailSubMap == null) {
            //返回hash环的第一个节点 对应的ip
            return treeMap.get(treeMap.firstKey());
        }
        long key=0;
        //否则返回 hash环中,这个 子树的第一个节点对应的ip
        try
        {
            key=tailSubMap.firstKey();
        }
        catch (Exception ex)
        {
            key=treeMap.firstKey();
        }
        return treeMap.get(key);
    }

    /**
     * hash一致
     * @return
     */
    public  String consistencyHashLoadBlance() {
        if(lstClient.isEmpty())
        {
            if(clientnum<2)
            {
                clientnum=serverWeightMap.size()*visualNum;
            }
            for (int i=0;i<clientnum;i++)
            {
                lstClient.add(String.valueOf(new Random().nextInt()));
            }
        }
        int index=inddexclient%clientnum;
        inddexclient++;
        String str=lstClient.get(index);

       return  consistencyHashLoadBlance(str);
    }


    // 32位的 Fowler-Noll-Vo 哈希算法 改用murmurHash
    //
    private static int FNVHash(String key) {
        final int p = 16777619;
        Long hash = 2166136261L;
        for (int idx = 0, num = key.length(); idx < num; ++idx) {
            hash = (hash ^ key.charAt(idx)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash.intValue();
    }

    /**
     * murmurHash 算法
     * @param key
     * @return
     */
    private static Long murmurHash(String key) {
        HashFunction function = Hashing.murmur3_128();
        HashCode hascode = function.hashString(key, Charset.forName("utf-8"));
        return hascode.asLong();
    }

    private  static  int circleHash(String key)
    {
        return  FNVHash(key);
    }

    /**
     * 5: 基于权重的轮询算法: 平滑加权轮询算法, Nginx的默认轮询算法就是 加权轮询算法
     *  https://my.oschina.net/u/1267069/blog/4437331
     * <p>
     * 思路: 比如 A : 5 , B : 3 , C : 2   (服务器 A,B,C 对应权重分别是 5,3,2)
     * ip: A,B,C
     * weight: 5,3,2 (计算得到 totalWeight = 10)
     * currentWeight: 0,0,0 (当前ip的初始权重都为0)
     * <p>
     * 请求次数: |  currentWeight = currentWeight + weight  |  最大权重为  |  返回的ip为 |  最大的权重 - totalWeight,其余不变
     *      1   |           5,3,2    (0,0,0 + 5,3,2)       |     5      |      A     |      -5,3,2
     *      2   |           0,6,4    (-5,3,2 + 5,3,2)      |     6      |      B     |       0,-4,4
     *      3   |           5,-1,6    (0,-4,4 + 5,3,2)     |     6      |     C      |       5,-1,-4
     *      4   |          10,2,-2    (5,-1,-4 + 5,3,2)    |     10     |     A      |       0,2,-2
     *      5   |           5,5,0                          |     5      |     A      |       -5,5,0
     *      6   |           0,8,2                          |     8      |     B      |       0,-2,2
     *      7   |           5,1,4                          |     5      |     A      |       -5,1,4
     *      8   |           0,4,6                          |     6      |     C      |       0,4,-4
     *      9   |           5,7,-2                         |     7      |     B      |       5,-3,-2
     *      10  |           10,0,0                         |     10     |     A      |        0,0,0
     * <p>
     * 至此结束: 可以看到负载轮询的策略是: A,B,C,A,A,B,A,C,B,A,
     *
     *   循环获取到权重最大的节点,和总权重, 之后将这个权重最大节点的权重 - 总权重, 最后返回这个权重最大节点对应的ip
     */

    /**
     * 基于权重的轮询算法
     * @param serverNodeList 服务IP
     * @return
     */
    private  String weightRound(List<ServerNode> serverNodeList) {
        ServerNode selectedServerNode = null;
        int maxWeight = 0;
        int totalWeight = 0;
        for (ServerNode serverNode : serverNodeList) {
            serverNode.incrementWeight();//获取节点的当前权重
            if (serverNode.currentWeight > maxWeight) {
                //节点的当前权重 大于最大权重, 就将该节点当做是选中的节点
                maxWeight = serverNode.currentWeight;
                selectedServerNode = serverNode;
            }
            //计算总的权重
            totalWeight += serverNode.weight;
        }
        // 当循环结束的时候, selectedServerNode就是权重最大的节点,对应的权重为maxWeight,  总的权重为totalWeight
        if (selectedServerNode != null) {
            //将权重最大的这个节点,的权重值减去 总权重
            selectedServerNode.decrementTotal(totalWeight);
            //并返回这个权重对应的 ip
            return selectedServerNode.ip;
        }
        return "";
    }

    private static List<ServerNode> getServerNodeList(Map<String, Integer> serverMap) {
        List<ServerNode> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : serverMap.entrySet()) {
            ServerNode serverNode = new ServerNode(entry.getKey(), entry.getValue());
            list.add(serverNode);
        }
        return list;
    }

    /**
     *  基于权重的轮询算法
     * @return
     */
    public   String weightRound()
    {
        var lst=getServerNodeList(getServerMap());
        return weightRound(lst);

    }


    /**
     * 6: 加权随机
     *  思路:  1: 一个ip的权重为5, 就将这个ip存到list中五次, 然后随机,  简单,但是有缺点 list会很大,执行效率低
     *
     *  思路:  2:  转换思路: 将每一个ip的权重转化为 X轴上的数字, 随机的数字,落在X轴的那个位置, 就返回这个位置对应的 ip
     *        比如: 192.168.1.100 , 3
     *             192.168.1.102 , 6
     *             192.168.1.105 , 4
     *        转换为X轴 为: 0--3-----9---13   可以看到这里需要获取所有权重之和
     *        产生的随机数为: 12 ,
     *        12 和 第一个ip的权重3 比较, 12 >= 3
     *        然后 12 - 3 = 9, 9 和第二个ip的权重6比较, 9 >= 6
     *        然后 9 - 6 =3 , 3 和第三个ip的权重4比较, 3 < 4 跳出---->说明随机的ip 是 192.168.1.105
     *
     * 这里实现思路2
     */
    public  String weightRandom(){
        //1: 获取所有ip权重之和
        List<Integer> weightList = new ArrayList<>(getServerMap().values());
        int totalWeight = weightList.stream().mapToInt(e->e.intValue()).sum();
        //2: 产生一个随机数
        int index = new Random().nextInt(totalWeight);
        for (String ip : getServerMap().keySet()) {
            //2.1 获取这个ip对应的权重
            Integer weight = getServerMap().get(ip);

            if(index < weight){
                return ip;
            }
            index = index - weight;
        }
        return "";
    }


    /**
     * 6 : 最小活跃数算法 (这里基于 dubbo的最小活跃数算法)
     * 什么是最小活跃数算法:
     * 一个服务器有一个活跃数active, 当处理一个请求时,active+1, 请求处理完active-1, 并发情况下,这个active的值越小,说明这台服务器性能高, 那么这台服务器就应该多处理些请求
     * <p>
     * 有三种情况
     * 1: 只有一个ip 活跃数最低,那么就返回这个ip
     * 2: 有多个ip 活跃数最低,且权重不同,返回权重最大的那个ip(权重最大的ip 有可能是多个, 这里返回第一个)
     * 3: 有多个ip 活跃数最低,且权重相同, 随机返回
     * <p>
     * 也有特殊情况,加入第一台服务器A,2000年买的,处理请求最大数为100, 第二台机器B,2010年买的处理请求最大数为500, 第三台机器C,2020年买的处理最大请求数为2000,对应的活跃数分别为:A-90, B-400,C-800,如果按照最小活跃数应该A机器
     * 处理请求会更多,但实际上C机器还能够处理1200个请求,所以最小活跃数算法,适用于各个机器性能相近, 处理请求的时间长短,取决于某个请求的计算复杂度
     * <p>
     * 实现思路:
     * 1: 循环list, 如果
     */
    private  String leastActiveLoadBalance(List<Ip_active_weight> invokers) {
        int length = invokers.size();
        // 所有invoker活跃数中 最小的活跃数, 初始为-1
        int leastActive = -1;

        // 所有invoker活跃数中,活跃数最小,且相同的个数, 默认0个
        int leastCount = 0;

        // leastIndexes 这个数组存的是最小活跃数对应的下标,  leastIndexes的大小不一定为1, 因为有可能有多个ip对应的活跃数最小,且相同
        int[] leastIndexes = new int[length];

        // 用来存 每个ip对应的权重
        int[] weights = new int[length];

        // 所有 ip的 权重之和, 初始为0
        int totalWeight = 0;
        // 这是一个标准
        int firstWeight = 0;

        // 这是一个标志, 默认true 每一个最小活跃数相同的ip 权重都相同
        boolean sameWeight = true;
        for (int i = 0; i < length; i++) {
            Ip_active_weight invoker = invokers.get(i);
            //获取对应的活跃数
            int active = invoker.getActive();
            //获取对应的权重
            int afterWarmup = invoker.getWeight();
            // 先将权重保存起来
            weights[i] = afterWarmup;
            if (leastActive == -1 || active < leastActive) {
                //更新 最小活跃数
                leastActive = active;
                // 最小的活跃数的个数 加1
                leastCount = 1;
                //将 当前invoker就是最小活跃数,将他对应的 下标存入 leastIndexes数组中
                leastIndexes[0] = i;
                //叠加总权重
                totalWeight = afterWarmup;
                //设置第一个权重,用来作比较,
                firstWeight = afterWarmup;
                sameWeight = true;
            } else if (active == leastActive) {
                leastIndexes[leastCount++] = i;
                totalWeight += afterWarmup;
                if (sameWeight && afterWarmup != firstWeight) {
                    //权重不同,将标志位 设置为false, 根据权重大小返回,说明有多个ip,最小活跃数相同,权重不同,置为false, 按照权重大小返回
                    sameWeight = false;
                }
            }
        }
        //跳出循环时候, 已经找到了 最小活跃数,的集合
        if (leastCount == 1) {
            //活跃数最小的只有一个,直接返回
            return invokers.get(leastIndexes[0]).getIp();
        }

        // sameWeight为false ,说明有多个ip 最小活跃数相同, 权重不同,
        if (!sameWeight && totalWeight > 0) {
            //这里用到的思想是:  X轴, 随机出来一个权重a,落到哪个区域内(a-权重 < 0, 就返回这个权重对应的 ip),
            int offsetWeight = new Random().nextInt(totalWeight);
            for (int i = 0; i < leastCount; i++) {
                int leastIndex = leastIndexes[i];
                offsetWeight -= weights[leastIndex];
                if (offsetWeight < 0) {
                    return invokers.get(leastIndex).getIp();
                }
            }
        }
        //所有的最小活跃数相同,且权重相同,随机返回
        return invokers.get(leastIndexes[new Random().nextInt(leastCount)]).getIp();
    }

    private  List<Ip_active_weight> buildIpActiveWeightList() {
        Map<String, Integer> serverMap = getServerMap();
        List<Ip_active_weight> list = new ArrayList<>(serverMap.keySet().size());
        getServerMap().forEach((key, value) -> {
            Ip_active_weight ip_active_weight = new Ip_active_weight();
            ip_active_weight.setIp(key);
            //这里随机权重(1-10)
            ip_active_weight.setActive(new Random().nextInt(3) + 1);
            ip_active_weight.setWeight(value);
            list.add(ip_active_weight);
        });
        return list;
    }

    /**
     * 最小活跃数算法
     * @return
     */
    public   String leastActiveLoadBalance()
    {
        return   leastActiveLoadBalance(buildIpActiveWeightList());
    }




}
