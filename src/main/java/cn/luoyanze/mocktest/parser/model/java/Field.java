package cn.luoyanze.mocktest.parser.model.java;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/12 9:46 PM
 */


public class Field {

    private String name;

    private String classname;

    public Field(String name, String classname) {
        this.name = name;
        this.classname = classname;
    }

    public String getName() {
        return name;
    }

    public String getClassname() {
        return classname;
    }
}
