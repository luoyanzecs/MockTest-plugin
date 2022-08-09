package cn.luoyanze.mocktest.parser.model.java;


/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/7/27 6:15 PM
 */

public class ClassMap {

    private String classname;

    private String packageName;


    private boolean isCtrip;

    private boolean isBasic;


    public ClassMap(String classname, String packageName) {
        this.classname = classname;
        this.packageName = packageName.trim();
        this.isCtrip = this.packageName.startsWith("com.ctrip");
    }

    @Override
    public String toString() {
        return packageName + "." + classname;
    }


    public boolean isBasic() {
        return isBasic;
    }

    public void setBasic(boolean basic) {
        isBasic = basic;
    }


    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isCtrip() {
        return isCtrip;
    }

    public void setCtrip(boolean ctrip) {
        isCtrip = ctrip;
    }
}
