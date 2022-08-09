package cn.luoyanze.mocktest.parser.model;

import cn.luoyanze.mocktest.parser.model.java.ClassMap;
import cn.luoyanze.mocktest.parser.model.java.FieldV3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/6 12:37 PM
 */


public class JavaFileV3 {

    private String packageName;

    private String name;

    private final Set<ClassMap> imports = new HashSet<>();

    private final Set<String> annotations = new HashSet<>();

    private final List<FieldV3> fields = new ArrayList<>();

    private List<String> initParams = new ArrayList<>();

    private final Set<String> staticMocks = new HashSet<>();

    private FieldV3 logger;

    private boolean hasEmptyConstructor;

    private String testFilenameForGenerate;

    public String getTestFilenameForGenerate() {
        return testFilenameForGenerate;
    }

    public void setTestFilenameForGenerate(String testFilenameForGenerate) {
        this.testFilenameForGenerate = testFilenameForGenerate;
    }

    public Set<ClassMap> getImports() {
        return this.imports;
    }

    public Set<String> getStaticMocks() {
        return this.staticMocks;
    }

    public Set<String> getCtripStaticMocks() {
        return this.staticMocks.stream()
                .filter(it -> getUseImports().stream().anyMatch(ctrip -> it.equals(ctrip.getClassname())))
                .collect(Collectors.toSet());
    }

    public Set<ClassMap> getUseImports() {
        return getImports().stream()
                .filter(ClassMap::isCtrip)
                .filter(it ->
                        this.fields.stream().map(FieldV3::getClassname).collect(Collectors.toSet()).contains(it.getClassname())
                                || this.staticMocks.contains(it.getClassname())
                                || it.getClassname().equals(this.logger.getClassname())
                ).collect(Collectors.toSet());
    }

    public boolean isUseSlf4j() {
        return imports.stream().map(ClassMap::toString).anyMatch("org.slf4j.Logger"::equals);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    public boolean isHasEmptyConstructor() {
        return hasEmptyConstructor;
    }

    public void setHasEmptyConstructor(boolean hasEmptyConstructor) {
        this.hasEmptyConstructor = hasEmptyConstructor;
    }

    public List<String> getInitParams() {
        return initParams;
    }

    public void setInitParams(List<String> initParams) {
        this.initParams = initParams;
    }

    public FieldV3 getLogger() {
        return logger;
    }

    public void setLogger(FieldV3 logger) {
        this.logger = logger;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getAnnotations() {
        return annotations;
    }

    public List<FieldV3> getFields() {
        return fields;
    }
}
