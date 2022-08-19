package cn.luoyanze.mocktest.parser.model;

import cn.luoyanze.mocktest.parser.model.java.Field;
import cn.luoyanze.mocktest.parser.model.java.Parameter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/12 9:41 PM
 */


public class SimpleJavaSource {

    private String packageName;

    private String classname;

    private final Map<String, String> importMaps = new HashMap<>();

    private final List<Field> fields = new ArrayList<>();

    private final Set<String> staticRef = new HashSet<>();

    private List<Parameter> constructorParams = new ArrayList<>();

    private boolean hasEmptyConstructor;

    public Util UTIL;

    public SimpleJavaSource() {
        this.UTIL = new Util(this);
    }

    public class Util {

        private SimpleJavaSource source;

        public Util(SimpleJavaSource source) {
            this.source = source;
        }

        public boolean hasLogger() {
            return source.getFields().stream().map(Field::getClassname).anyMatch("Logger"::equals);
        }

        public boolean isUseSlf4j() {
            return Optional.ofNullable(source.getImportMaps().get("Logger")).filter("org.slf4j"::equals).isPresent();
        }

        public List<Field> getMockFields() {
            return source.getFields().stream()
                    .filter(it -> {
                        if (it.getClassname().equals("Logger")) {
                            return !isUseSlf4j();
                        }
                        return Optional.ofNullable(importMaps.get(it.getClassname())).map(im -> im.contains("ctrip")).isPresent();
                    }).collect(Collectors.toList());
        }

        public Set<String> getMockStatics() {
            return source.getStaticRef().stream()
                    .filter(it ->
                            Optional.ofNullable(getImportMaps().get(it)).filter(space -> space.contains("ctrip")).isPresent()
                    ).collect(Collectors.toSet());
        }

        public Set<String> getPrepareForTest() {
            Set<String> mockInFields =
                    this.getMockFields().stream().map(Field::getClassname).collect(Collectors.toSet());

            Set<String> mockForStatic = this.getMockStatics();

            return Stream.concat(mockForStatic.stream(), mockInFields.stream()).collect(Collectors.toSet());
        }

        public List<String> getConstructorParamsForTest() {
            if (isHasEmptyConstructor()) {
                return Collections.emptyList();
            }
            List<Field> fields = new ArrayList<>(source.getFields());

            List<String> fieldNames = source.getFields().stream().map(Field::getName).collect(Collectors.toList());

            return getConstructorParams().stream()
                    .map(parameter -> {
                        Field matched = fields.stream().filter(it -> it.getName().equals(parameter.getVariablename()))
                                .findFirst()
                                .orElse(null);

                        if (matched != null) {
                            fields.remove(matched);
                            return matched.getName();
                        }
                        return null;
                    }).collect(Collectors.toList());
        }

        public List<Field> getFieldsNotInConstructor() {
            List<String> constructorParams = this.getConstructorParamsForTest();
            return source.getFields().stream()
                    .filter(it -> !constructorParams.contains(it.getName()))
                    .filter(it -> it.getName().contains("ctrip"))
                    .collect(Collectors.toList());
        }

        public Set<String> getImports() {
            Set<String> mocks = this.getMockFields().stream().map(Field::getClassname).collect(Collectors.toSet());
            Set<String> mockStatics = this.getMockStatics();
            return source.getImportMaps().entrySet().stream()
                    .filter(it ->
                            getClassname().equals(it.getKey()) || mocks.contains(it.getKey()) || mockStatics.contains(it.getKey())
                    )
                    .map(it -> it.getValue() + "." + it.getKey())
                    .collect(Collectors.toSet());
        }
    }



    public void addStaticRef(String name) {
        this.staticRef.add(name);
    }

    public void addField(Field field) {
        this.fields.add(field);
    }

    public void addImports(String name, String packagename) {
        this.importMaps.put(name, packagename);
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public void setConstructorParams(List<Parameter> constructorParams) {
        this.constructorParams = constructorParams;
    }

    public void setHasEmptyConstructor(boolean hasEmptyConstructor) {
        this.hasEmptyConstructor = hasEmptyConstructor;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassname() {
        return classname;
    }

    public Map<String, String> getImportMaps() {
        return importMaps;
    }

    public List<Field> getFields() {
        return fields;
    }

    public Set<String> getStaticRef() {
        return staticRef;
    }

    public List<Parameter> getConstructorParams() {
        return constructorParams;
    }

    public boolean isHasEmptyConstructor() {
        return hasEmptyConstructor;
    }
}










