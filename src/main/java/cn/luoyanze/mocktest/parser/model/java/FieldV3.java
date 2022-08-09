package cn.luoyanze.mocktest.parser.model.java;

import java.util.List;
import java.util.Objects;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/6 4:05 PM
 */


public class FieldV3 {

    private String classname;

    private String name;

    private String initClass;

    private String initMethod;

    private List<String> initArgs;


    public String getClassname() {
        return classname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldV3 fieldV3 = (FieldV3) o;
        return name.equals(fieldV3.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInitClass() {
        return initClass;
    }

    public void setInitClass(String initClass) {
        this.initClass = initClass;
    }

    public String getInitMethod() {
        return initMethod;
    }

    public void setInitMethod(String initMethod) {
        this.initMethod = initMethod;
    }

    public List<String> getInitArgs() {
        return initArgs;
    }

    public void setInitArgs(List<String> initArgs) {
        this.initArgs = initArgs;
    }
}
