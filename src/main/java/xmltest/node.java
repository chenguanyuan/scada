package xmltest;

/**
 * Created by chengy on 2018/9/19.
 */
public class node {
    private int level;
    private String nodeID;
    private String[] updevice;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public String[] getUpdevice() {
        return updevice;
    }

    public void setUpdevice(String[] updevice) {
        this.updevice = updevice;
    }
}
