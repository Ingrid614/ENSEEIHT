// platform/AgentMessage.java
package platform;

import java.io.Serializable;
import java.util.Map;

public class AgentMessage implements Serializable {
    public String mainClass;
    public Map<String, byte[]> classes;
    public byte[] agentState;
}
