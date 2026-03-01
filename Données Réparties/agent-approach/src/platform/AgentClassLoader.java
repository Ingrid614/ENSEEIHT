// platform/AgentClassLoader.java
package platform;

import java.util.Map;

public class AgentClassLoader extends ClassLoader {

    private final Map<String, byte[]> classes;

    public AgentClassLoader(Map<String, byte[]> classes) {
        super(ClassLoader.getSystemClassLoader());
        this.classes = classes;
    }

    @Override
    protected Class<?> findClass(String name)
            throws ClassNotFoundException {

        byte[] code = classes.get(name);
        if (code == null)
            throw new ClassNotFoundException(name);

        return defineClass(name, code, 0, code.length);
    }
}
