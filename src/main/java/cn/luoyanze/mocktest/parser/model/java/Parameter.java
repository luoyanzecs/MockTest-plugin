package cn.luoyanze.mocktest.parser.model.java;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/12 10:26 PM
 */


public class Parameter {
    private final String classname;
    private final String variablename;

    public Parameter(String classname, String variablename) {
        this.classname = classname;
        this.variablename = variablename;
    }

    public String getClassname() {
        return classname;
    }

    public String getVariablename() {
        return variablename;
    }
}
