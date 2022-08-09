package cn.luoyanze.mocktest.parser.visit;

import cn.luoyanze.mocktest.parser.model.JavaFileV3;
import cn.luoyanze.mocktest.parser.model.java.ClassMap;
import cn.luoyanze.mocktest.parser.model.java.FieldV3;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
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
 * @Date 2022/8/6 11:42 AM
 */


public class TestVisitorAdapter extends VoidVisitorAdapter<JavaFileV3> {

    private final JavaParser javaParser = new JavaParser();


    @Override
    public void visit(final CompilationUnit n, final JavaFileV3 file) {

        n.getTypes().forEach(p -> {
            if (p.getNameAsString().contains(file.getName())) {
                p.accept(this, file);
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
        file.getUseImports().stream().map(ClassMap::toString).forEach(n::addImport);
        List<ImportDeclaration> allImports = n.getImports().stream().sorted(Comparator.comparing(NodeWithName::getNameAsString)).collect(Collectors.toList());

        n.setImports(new NodeList<>(allImports));
    }


    @Override
    public void visit(final ClassOrInterfaceDeclaration n, final JavaFileV3 file) {

        Set<String> annotations = n.getAnnotations().stream().map(NodeWithName::getNameAsString).collect(Collectors.toSet());
        if (!annotations.contains("RunWith")) {
            return;
        }
        // 注解处理
        processAnnotaions(n, file);

        // 字段处理
        processFields(n, file);

        // setup方法处理
        processSetupMethod(n, file);

        sortFieldsAndMethods(n);

    }

    private static void sortFieldsAndMethods(ClassOrInterfaceDeclaration n) {
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

    private void processSetupMethod(final ClassOrInterfaceDeclaration n, JavaFileV3 file) {
        List<MethodDeclaration> setupMethods = n.getMembers().stream()
                .filter(BodyDeclaration::isMethodDeclaration)
                .map(BodyDeclaration::asMethodDeclaration)
                .filter(it -> it.getAnnotations().stream() .map(NodeWithName::getNameAsString).anyMatch("Before"::equals))
                .collect(Collectors.toList());

        List<MethodDeclaration> beforeClassMethods = n.getMembers().stream()
                .filter(BodyDeclaration::isMethodDeclaration)
                .map(BodyDeclaration::asMethodDeclaration)
                .filter(it -> it.getAnnotations().stream() .map(NodeWithName::getNameAsString).anyMatch("BeforeClass"::equals))
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

        // 添加mockStatic(), 去除重复的mock()
        Set<String> needMocks = file.getFields().stream().map(FieldV3::getClassname).collect(Collectors.toSet());
        Set<String> existMockStatics = new HashSet<>();
        List<Statement> needRemoveStatements = setupBody.getStatements().stream()
                .filter(Statement::isExpressionStmt)
                .filter(statement -> {
                    Expression expression = statement.asExpressionStmt().getExpression();
                    if (expression.isMethodCallExpr()) {
                        MethodCallExpr methodCallExpr = expression.asMethodCallExpr();
                        if ("mockStatic".equals(methodCallExpr.getNameAsString())) {
                            String typeAsString = methodCallExpr.getArguments().get(0).asClassExpr().getTypeAsString();
                            if ("LoggerFactory".equals(typeAsString)) {
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
                            return methodCallExpr.getScope().toString().contains("LoggerFactory.getLogger");
                        }
                    }
                    if (expression.isAssignExpr()) {
                        return Pattern.compile("new\\s+" + file.getName() + "\\s*?\\(").matcher(expression.toString()).find();
                    }
                    return false;
                }).collect(Collectors.toList());

        needRemoveStatements.forEach(setupBody::remove);

        Sets.union(file.getCtripStaticMocks(), existMockStatics)
                .forEach(mockStaticClassname -> {
                    ExpressionStmt expressionStmt = new ExpressionStmt(
                            new MethodCallExpr(
                                    new NameExpr("PowerMockito"),
                                    "mockStatic",
                                    new NodeList<>(new ClassExpr(javaParser.parseClassOrInterfaceType(mockStaticClassname).getResult().get()))
                            )
                    );
                    setupBody.addStatement(0, expressionStmt);
                });
    }

    private void processFields(final ClassOrInterfaceDeclaration n, JavaFileV3 file) {

        List<FieldDeclaration> fields = n.getMembers().stream()
                .filter(BodyDeclaration::isFieldDeclaration)
                .map(BodyDeclaration::asFieldDeclaration)
                .collect(Collectors.toList());

        fields.forEach(it ->  it.accept(this, file));

        // fields 补偿
        Set<String> needMockFields = file.getFields().stream().map(FieldV3::getClassname).collect(Collectors.toSet());

        Set<String> alreadyMockedFields = fields.stream()
                .map(FieldDeclaration::getVariables)
                .map(it -> it.stream().findFirst().map(NodeWithType::getTypeAsString).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        needMockFields.removeAll(alreadyMockedFields);

        file.getFields().stream()
                .filter(it -> needMockFields.contains(it.getClassname()))
                .forEach(field -> {
                    FieldDeclaration fieldDeclaration = n.addPrivateField(field.getClassname(), field.getName());
                    fieldDeclaration.addMarkerAnnotation("Mock");
                });

    }

    @Override
    public void visit(final FieldDeclaration n, JavaFileV3 file) {
        n.getVariables().stream().findFirst().ifPresent(variableDeclarator -> {
            if (variableDeclarator.getTypeAsString().equals(file.getName())) {
                // add @InjectMocks
                variableDeclarator.removeInitializer();
                boolean isModifyByInjectMocks = n.getAnnotations().stream().map(NodeWithName::getNameAsString).anyMatch("InjectMocks"::equals);
                if (!isModifyByInjectMocks) {
                    n.addMarkerAnnotation("InjectMocks");
                }
            }

            Set<String> needMockFields = file.getFields().stream().map(FieldV3::getClassname).collect(Collectors.toSet());
            if (needMockFields.contains(variableDeclarator.getTypeAsString())) {
                // add @Mock
                variableDeclarator.removeInitializer();
                boolean isModifyByInjectMocks = n.getAnnotations().stream().map(NodeWithName::getNameAsString).anyMatch("Mock"::equals);
                if (!isModifyByInjectMocks) {
                    n.addMarkerAnnotation("Mock");
                }
            }
        });
    }

    private void processAnnotaions(ClassOrInterfaceDeclaration n, JavaFileV3 file) {

        // slf4j proxy
        if (file.getLogger() != null) {
            n.getAnnotations().stream().filter(it -> "MockPolicy".equals(it.getNameAsString())).findFirst().ifPresent(it -> n.getAnnotations().remove(it));
            javaParser.parseClassOrInterfaceType("Slf4jMockPolicy").getResult().ifPresent(
                    classOrInterfaceType -> {
                        ArrayInitializerExpr arrayInitializerExpr = new ArrayInitializerExpr(new NodeList<>(new ClassExpr(classOrInterfaceType)));
                        n.addSingleMemberAnnotation("MockPolicy", arrayInitializerExpr);
                    }
            );
        }
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
        Set<String> prepareForTestItems = file.getUseImports().stream().map(ClassMap::getClassname).collect(Collectors.toSet());

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
        Set<String> suppressStaticInitializationItems = file.getUseImports().stream().map(ClassMap::toString).collect(Collectors.toSet());
        ArrayInitializerExpr arrayInitializerExprPrepareForTestSuppressStatic = new ArrayInitializerExpr(
                new NodeList<>(Sets
                                .intersection(
                                        file.getImports().stream().map(ClassMap::toString).collect(Collectors.toSet()),
                                        Sets.union(currentSuppressStaticInitializationItems, suppressStaticInitializationItems)
                                )
                                .stream().sorted().map(StringLiteralExpr::new).collect(Collectors.toSet())
                )
        );
        n.addSingleMemberAnnotation("SuppressStaticInitializationFor", arrayInitializerExprPrepareForTestSuppressStatic);
    }

}
