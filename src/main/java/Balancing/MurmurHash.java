package Balancing;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.Charset;
import java.util.*;

public class MurmurHash {
     private TreeMap<Long,ServerNode> nodes; //虚拟节点到真实节点的映射

     private TreeMap<Long,ServerNode> treeKey; //key到真实节点的映射

     private List<ServerNode> shards = new ArrayList(); //真实机器节点

    private List<String> lstClient=new ArrayList<>();
    int indexclient=0;

    int clientnum=1;

    private final int NODE_NUM = 100; //每个机器节点关联的虚拟节点个数
  public  void  initServer(List<ServerNode> shards)
  {
      this.shards =shards;
      init();
  }
    private void init() { //初始化一致性hash环

        nodes = new TreeMap();

        treeKey= new TreeMap();
        for (int i = 0; i != shards.size(); ++i) { //每个真实机器节点都需要关联虚拟节点

            final ServerNode shardInfo =shards.get(i);
            for (int n = 0; n < NODE_NUM; n++) {//一个真实机器节点关联NODE_NUM个虚拟节点

                nodes.put(hash("SHARD-" + shardInfo.ip + "-NODE-" + n), shardInfo);

            }

        }

    }

    /**
    /添加一个虚拟节点进环形结构,lg为虚拟节点的hash值
     */
    public void addS(ServerNode s) {

        System.out.println("增加主机"+s+"的变化:");for (int n = 0; n < NODE_NUM; n++) {

            addS(hash("SHARD-" + s.ip + "-NODE-" +n), s);

        }

    }

    /**
     * 添加
     * @param lg
     * @param s
     */
    private void addS(Long lg, ServerNode s){

        SortedMap<Long,ServerNode> tail =nodes.tailMap(lg);

        SortedMap<Long,ServerNode> head =nodes.headMap(lg);

        SortedMap between;
        if(head.size()==0){

            between=treeKey.tailMap(nodes.lastKey());

        }else {

            Long begin = head.lastKey();

            between = treeKey.subMap(begin, false, lg, true);

        }

        nodes.put(lg, s);
        for(Iterator<Long> it = between.keySet().iterator(); it.hasNext();) {

            Long lo = it.next();

            treeKey.put(lo, nodes.get(lg));

            System.out.println("hash(" + lo + ")改变到->" + tail.get(tail.firstKey()));

        }

    }

    /**
     * 删除真实节点是s
     * @param s
     */
    public void deleteS(ServerNode s){
        if(s==null) {
            return;

        }

        System.out.println("删除主机"+s+"的变化:");
        for(int i=0;i< nodes.size();i++)
        {
            SortedMap<Long,ServerNode>  tail = nodes.tailMap(hash("SHARD-" + s.ip + "-NODE-" + i));

            SortedMap<Long,ServerNode>  head= nodes.headMap(hash("SHARD-" + s.ip + "-NODE-" + i));

            SortedMap between;
            if (head.size() == 0) {

                between = treeKey.tailMap(nodes.lastKey());

            } else {

                Long begin = head.lastKey();

                Long end = tail.firstKey();

                between = treeKey.subMap(begin, false, end, true);//在s节点的第i个虚拟节点的所有key的集合

            }
            nodes.remove(tail.firstKey());//从nodes中删除s节点的第i个虚拟节点

            for (Iterator<Long> it = between.keySet().iterator(); it.hasNext(); ) {

                Long lo = it.next();
                if (tail.size() == 0) {//如果是尾节点，则关联到首接点上

                    treeKey.put(lo, nodes.firstEntry().getValue());

                    System.out.println("hash(" + lo + ")改变到->" + nodes.firstEntry().getValue());

                } else {

                    treeKey.put(lo, tail.get(tail.firstKey()));

                    System.out.println("hash(" + lo + ")改变到->" + tail.get(tail.firstKey()));

                }

            }
        }
    }
    public ServerNode keyToNode(String key) {

        Long hashKey = hash(key);

        SortedMap<Long,ServerNode> tail = nodes.tailMap(hashKey); //沿环的顺时针找到一个虚拟节点//如果是尾节点

        if (tail.size() == 0) {//映射节点为首节点

            treeKey.put(hashKey, nodes.firstEntry().getValue());

          //  System.out.println(key + "(hash: " + hashKey + ")连接到主机->" + nodes.firstEntry().getValue());
            return nodes.firstEntry().getValue();

        }

        treeKey.put(hashKey, tail.get(tail.firstKey()));

       // System.out.println(key + "(hash:" + hashKey + ")连接到主机->" + tail.get(tail.firstKey()));
        return  tail.get(tail.firstKey());
    }

    public  ServerNode keyToNode()
    {
        if(lstClient.isEmpty())
        {
            if(clientnum<2)
            {
                clientnum=shards.size();
            }
            for (int i=0;i<clientnum;i++)
            {
                lstClient.add(String.valueOf(new Random().nextInt()));
            }
        }

        int index=indexclient%clientnum;
        indexclient++;
        String str=lstClient.get(index);

      return   keyToNode(str);
    }
    private static Long hash(String key)
    {
        HashFunction function = Hashing.murmur3_128();

        HashCode hascode = function.hashString(key, Charset.forName("utf-8"));
       return hascode.asLong();
    }
}
