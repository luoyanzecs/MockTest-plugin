package cn.luoyanze.mocktest.parser.visit;

import cn.luoyanze.mocktest.parser.model.SimpleJavaSource;
import cn.luoyanze.mocktest.parser.model.java.Field;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/13 4:04 PM
 */


public class JavaTestWithSourceVisitorAdapter extends VoidVisitorAdapter<SimpleJavaSource> {

    private final JavaParser javaParser = new JavaParser();

    private String testVariable;

    @Override
    public void visit(final CompilationUnit n, final SimpleJavaSource source) {

        n.getTypes().forEach(p -> {
            if (p.getNameAsString().contains(source.getClassname())) {
                p.accept(this, source);
            }
        });

        n.addImport("org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy");
        n.addImport("org.powermock.core.classloader.annotations.MockPolicy");
        n.addImport("org.junit.Before");
        n.addImport("org.junit.Test");
        n.addImport("org.junit.runner.RunWith");
        n.addImport("org.mockito.InjectMocks");
        n.addImport("org.mockito.Mock");
        n.addImport("org.mockito.Mockito");
        n.addImport("org.powermock.api.mockito.PowerMockito");
        n.addImport("org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy");
        n.addImport("org.powermock.api.support.membermodification.MemberModifier");
        n.addImport("org.powermock.core.classloader.annotations.MockPolicy");
        n.addImport("org.powermock.core.classloader.annotations.PrepareForTest");
        n.addImport("org.powermock.core.classloader.annotations.SuppressStaticInitializationFor");
        n.addImport("org.powermock.modules.junit4.PowerMockRunner");

        source.UTIL.getImports().forEach(n::addImport);
        List<ImportDeclaration> allImports = n.getImports().stream().sorted(Comparator.comparing(NodeWithName::getNameAsString)).collect(Collectors.toList());
        n.setImports(new NodeList<>(allImports));
    }

    @Override
    public void visit(final ClassOrInterfaceDeclaration n, final SimpleJavaSource source) {

        Set<String> annotations = n.getAnnotations().stream().map(NodeWithName::getNameAsString).collect(Collectors.toSet());
        if (!annotations.contains("RunWith")) {
            return;
        }
        // 注解处理
        processAnnotations(n, source);
        // 字段处理
        processFields(n, source);
        // setup方法处理
        processSetupMethod(n, source);

        sortFieldsAndMethods(n);
    }

    private void processSetupMethod(final ClassOrInterfaceDeclaration n, final SimpleJavaSource source) {
        List<MethodDeclaration> setupMethods =
                n.getMembers().stream()
                        .filter(BodyDeclaration::isMethodDeclaration)
                        .map(BodyDeclaration::asMethodDeclaration)
                        .filter(it -> it.getAnnotations().stream().map(NodeWithName::getNameAsString).anyMatch("Before"::equals))
                        .collect(Collectors.toList());

        List<MethodDeclaration> beforeClassMethods =
                n.getMembers().stream()
                        .filter(BodyDeclaration::isMethodDeclaration)
                        .map(BodyDeclaration::asMethodDeclaration)
                        .filter(it -> it.getAnnotations().stream().map(NodeWithName::getNameAsString).anyMatch("BeforeClass"::equals))
                        .collect(Collectors.toList());

        MethodDeclaration firstSetup = setupMethods.stream().findFirst().orElse(new MethodDeclaration());
        if (firstSetup.getBody().isEmpty()) {
            firstSetup.setBody(new BlockStmt());
        }
        BlockStmt setupBody = firstSetup.getBody().get();
        // 去除多余的@Before setup
        for (int i = 1; i < setupMethods.size(); i++) {
            MethodDeclaration methodDeclaration = setupMethods.get(i);
            methodDeclaration.getBody()
                    .map(BlockStmt::getStatements)
                    .ifPresent(st -> st.forEach(statement -> setupBody.addStatement(0, statement)));
            methodDeclaration.remove();
        }

        // 禁用@BeforeClass
        for (MethodDeclaration beforeClassMethod : beforeClassMethods) {
            beforeClassMethod.getBody()
                    .map(BlockStmt::getStatements)
                    .ifPresent(st -> st.forEach(statement -> setupBody.addStatement(0, statement)));
            beforeClassMethod.remove();
        }

        // 去除重复的mock(), Membermodifier.field()
        Set<String> needMocks = source.UTIL.getMockFields().stream().map(Field::getClassname).collect(Collectors.toSet());
        Set<String> existMockStatics = new HashSet<>();
        List<Statement> needRemoveStatements = setupBody.getStatements().stream()
                .filter(Statement::isExpressionStmt)
                .filter(statement -> {
                    Expression expression = statement.asExpressionStmt().getExpression();
                    if (expression.isMethodCallExpr()) {
                        MethodCallExpr methodCallExpr = expression.asMethodCallExpr();
                        if ("mockStatic".equals(methodCallExpr.getNameAsString())) {
                            String typeAsString = methodCallExpr.getArguments().get(0).asClassExpr().getTypeAsString();
                            if ("LoggerFactory".equals(typeAsString) && source.UTIL.isUseSlf4j()) {
                                return true;
                            }
                            existMockStatics.add(typeAsString);
                            return true;
                        }
                        if ("mock".equals(methodCallExpr.getNameAsString())) {
                            String typeAsString = methodCallExpr.getArguments().get(0).asClassExpr().getTypeAsString();
                            return needMocks.contains(typeAsString);
                        }
                        if ("thenReturn".equals(methodCallExpr.getNameAsString())) {
                            return methodCallExpr.getScope().toString().contains("LoggerFactory.getLogger") && source.UTIL.isUseSlf4j();
                        }
                        if (methodCallExpr.getScope().map(Node::toString).filter(it -> it.startsWith("MemberModifier.field")).isPresent()) {
                            return true;
                        }
                    }
                    if (expression.isAssignExpr()) {
                        return Pattern.compile("new\\s+" + source.getClassname() + "\\s*?\\(").matcher(expression.toString()).find();
                    }
                    return false;
                }).collect(Collectors.toList());

        needRemoveStatements.forEach(setupBody::remove);

        // 添加mockStatic
        Sets.union(source.UTIL.getMockStatics(), existMockStatics)
                .forEach(mockStaticClassname ->
                        javaParser.parseClassOrInterfaceType(mockStaticClassname).getResult()
                                .ifPresent(classOrInterfaceType ->
                                        setupBody.addStatement(
                                                0,
                                                new ExpressionStmt(new MethodCallExpr(new NameExpr("PowerMockito"), "mockStatic", new NodeList<>(new ClassExpr(classOrInterfaceType))))
                                        )
                                )
                );

        // 添加new 方法
        List<Expression> paramExprs = source.UTIL.getConstructorParamsForTest().stream()
                .map(param -> param == null ? new NullLiteralExpr() : new NameExpr(param))
                .collect(Collectors.toList());

        javaParser.parseClassOrInterfaceType(source.getClassname()).getResult()
                .ifPresent(sourceClass-> {
                    ExpressionStmt newExpressionStmt = new ExpressionStmt(
                            new AssignExpr(new NameExpr(testVariable), new ObjectCreationExpr(null, sourceClass, new NodeList<>(paramExprs)), AssignExpr.Operator.ASSIGN)
                    );
                    setupBody.addStatement(newExpressionStmt);
                });


        // 添加membermodifier.field
        List<Field> memeberModifierFields =
                source.UTIL.getMockFields().stream()
                        .filter(it -> !source.UTIL.getConstructorParamsForTest().contains(it.getName()))
                        .collect(Collectors.toList());

        String memberModifierTemplate = "MemberModifier.field(${class_name}.class, \"${field.name}\").set(${test_variable}, ${field.name});";
        memeberModifierFields.forEach(it -> {
            String statement = memberModifierTemplate
                    .replace("${class_name}", source.getClassname())
                    .replace("${field.name}", it.getName())
                    .replace("${test_variable}", testVariable);

            javaParser.parseStatement(statement).getResult().ifPresent(setupBody::addStatement);
        });
    }

    @Override
    public void visit(FieldDeclaration n, SimpleJavaSource source) {
        n.getAnnotations().stream()
                .filter(it -> it.getNameAsString().equals("InjectMocks"))
                .findFirst()
                .ifPresent(annotationExpr -> n.getAnnotations().remove(annotationExpr));

        VariableDeclarator variable = n.getVariable(0);
        if (variable.getTypeAsString().equals(source.getClassname())) {
            testVariable = variable.getNameAsString();
        }
        super.visit(n, source);
    }

    private void processFields(final ClassOrInterfaceDeclaration n, final SimpleJavaSource source) {

        List<FieldDeclaration> fields = n.getMembers().stream()
                .filter(BodyDeclaration::isFieldDeclaration)
                .map(BodyDeclaration::asFieldDeclaration)
                .collect(Collectors.toList());

        fields.forEach(it ->  it.accept(this, source));

        // fields 补偿
        List<Field> needMockFields = source.UTIL.getMockFields();

        Set<String> alreadyMockedFields = fields.stream()
                .map(FieldDeclaration::getVariables)
                .map(it -> it.stream().findFirst().map(NodeWithType::getTypeAsString).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        needMockFields.stream()
                .filter(it -> !alreadyMockedFields.contains(it.getClassname()))
                .forEach(field -> {
                    FieldDeclaration fieldDeclaration = n.addPrivateField(field.getClassname(), field.getName());
                    fieldDeclaration.addMarkerAnnotation("Mock");
                });

    }

    private void sortFieldsAndMethods(ClassOrInterfaceDeclaration n) {
        List<BodyDeclaration<?>> members = n.getMembers().stream().sorted((o1, o2) -> {
            if (o1.isFieldDeclaration() && o2.isFieldDeclaration()) {
                return 0;
            } else if (o1.isFieldDeclaration() && o2.isMethodDeclaration()) {
                return -1;
            } else if (o1.isMethodDeclaration() && o2.isFieldDeclaration()) {
                return 1;
            } else if (o1.isMethodDeclaration() && o2.isMethodDeclaration()) {
                MethodDeclaration method1 = o1.asMethodDeclaration();
                MethodDeclaration method2 = o2.asMethodDeclaration();
                if (method1.getAnnotations().size() > 0 && method1.getAnnotations().get(0).toString().equals("@Before")) {
                    return -1;
                }
                return 1;
            } else return 0;
        }).collect(Collectors.toList());

        n.setMembers(new NodeList<>(members));
    }


    private void processAnnotations(ClassOrInterfaceDeclaration n, SimpleJavaSource source) {

        // slf4j proxy
        n.getAnnotations().stream().filter(it -> "MockPolicy".equals(it.getNameAsString())).findFirst().ifPresent(it -> n.getAnnotations().remove(it));
        javaParser.parseClassOrInterfaceType("Slf4jMockPolicy").getResult().ifPresent(
                classOrInterfaceType -> {
                    ArrayInitializerExpr arrayInitializerExpr = new ArrayInitializerExpr(new NodeList<>(new ClassExpr(classOrInterfaceType)));
                    n.addSingleMemberAnnotation("MockPolicy", arrayInitializerExpr);
                }
        );

        // 添加prepare注解
        Optional<AnnotationExpr> prepareForTestItemsExpr = n.getAnnotations().stream().filter(it -> "PrepareForTest".equals(it.getNameAsString())).findFirst();
        Set<String> currentprepareForTestItems = prepareForTestItemsExpr
                .map(Expression::asSingleMemberAnnotationExpr)
                .map(SingleMemberAnnotationExpr::getMemberValue)
                .map(Expression::asArrayInitializerExpr)
                .map(ArrayInitializerExpr::getValues)
                .orElse(new NodeList<>()).stream()
                .map(Expression::asClassExpr)
                .map(NodeWithType::getTypeAsString)
                .collect(Collectors.toSet());

        prepareForTestItemsExpr.ifPresent(it -> n.getAnnotations().remove(it));

        Set<String> prepareForTestItems = source.UTIL.getPrepareForTest();

        ArrayInitializerExpr arrayInitializerExprPrepareForTest = new ArrayInitializerExpr(
                new NodeList<>(
                        Sets.union(currentprepareForTestItems, prepareForTestItems).stream().sorted()
                                .map(javaParser::parseClassOrInterfaceType)
                                .map(ParseResult::getResult)
                                .filter(Optional::isPresent).map(Optional::get)
                                .map(ClassExpr::new).collect(Collectors.toList())
                )
        );
        n.addSingleMemberAnnotation("PrepareForTest", arrayInitializerExprPrepareForTest);

        // 添加SuppressStaticInitializationFor注解
        Optional<AnnotationExpr> suppressStaticInitializationExpr = n.getAnnotations().stream().filter(it -> "SuppressStaticInitializationFor".equals(it.getNameAsString())).findFirst();
        Set<String> currentSuppressStaticInitializationItems =
                suppressStaticInitializationExpr
                        .map(Expression::asSingleMemberAnnotationExpr)
                        .map(SingleMemberAnnotationExpr::getMemberValue)
                        .map(Expression::asArrayInitializerExpr)
                        .map(ArrayInitializerExpr::getValues)
                        .orElse(new NodeList<>()).stream()
                        .map(Expression::asStringLiteralExpr)
                        .map(LiteralStringValueExpr::getValue)
                        .collect(Collectors.toSet());



        suppressStaticInitializationExpr.ifPresent(it -> n.getAnnotations().remove(it));
        Set<String> suppressStaticInitializationItems = source.UTIL.getPrepareForTest();
        ArrayInitializerExpr arrayInitializerExprPrepareForTestSuppressStatic = new ArrayInitializerExpr(
                new NodeList<>(Sets
                        .intersection(
                                source.getImportMaps().entrySet().stream().map(it -> it.getValue() + "." + it.getKey()).collect(Collectors.toSet()),
                                Sets.union(currentSuppressStaticInitializationItems, suppressStaticInitializationItems)
                        )
                        .stream().sorted().map(StringLiteralExpr::new).collect(Collectors.toSet())
                )
        );
        n.addSingleMemberAnnotation("SuppressStaticInitializationFor", arrayInitializerExprPrepareForTestSuppressStatic);
    }
}
