package cn.luoyanze.mocktest.parser.model;

import cn.luoyanze.mocktest.parser.model.java.ClassMap;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/7 5:40 PM
 */


public class TestSourceMap {

    private ClassMap source;

    private ClassMap test;

    private boolean isRunWithAnnotated;

    public ClassMap getSource() {
        return source;
    }

    public void setSource(ClassMap source) {
        this.source = source;
    }

    public ClassMap getTest() {
        return test;
    }

    public void setTest(ClassMap test) {
        this.test = test;
    }

    public boolean isRunWithAnnotated() {
        return isRunWithAnnotated;
    }

    public void setRunWithAnnotated(boolean runWithAnnotated) {
        isRunWithAnnotated = runWithAnnotated;
    }
}
