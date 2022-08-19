package cn.luoyanze.mocktest.parser.visit;

import cn.luoyanze.mocktest.parser.model.TestSourceMap;
import cn.luoyanze.mocktest.parser.model.java.ClassMap;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/7 5:39 PM
 *
 * 获取映射关系
 */

public class PreviousTestVisitAdapter extends VoidVisitorAdapter<TestSourceMap> {
    private Set<String> maybeSource = new HashSet<>();

    @Override
    public void visit(final CompilationUnit n, final TestSourceMap map) {

        ClassMap test = new ClassMap(n.getPrimaryTypeName().orElse("null"), n.getPackageDeclaration().map(NodeWithName::getNameAsString).orElse("null"));
        map.setTest(test);

        n.getTypes().forEach(p -> {
            if (p.getNameAsString().contains(map.getTest().getClassname())) {
                p.accept(this, map);
            }
        });

        Map<String, String> maybeSourceImport = n.getImports().stream()
                .map(ImportDeclaration::getName)
                .collect(Collectors.toMap(
                        Name::getIdentifier,
                        it -> it.getQualifier().map(Node::toString).orElse("")
                ));

        maybeSource.stream()
                .filter(it -> !Strings.isNullOrEmpty(maybeSourceImport.get(it)))
                .findFirst()
                .ifPresentOrElse(
                        it -> map.setSource(new ClassMap(it, maybeSourceImport.get(it))),
                        () -> maybeSource.stream()
                                .findFirst()
                                .ifPresent(it -> map.setSource(new ClassMap(it, test.getPackageName())))
                );

    }


    @Override
    public void visit(final ClassOrInterfaceDeclaration n, final TestSourceMap map) {
        boolean isAnnotatedByPowerMockRunner =
                n.getAnnotations().stream()
                        .filter(Expression::isSingleMemberAnnotationExpr)
                        .filter(it -> it.getNameAsString().equals("RunWith"))
                        .map(Expression::asSingleMemberAnnotationExpr)
                        .map(SingleMemberAnnotationExpr::getMemberValue)
                        .filter(Expression::isClassExpr)
                        .map(Expression::asClassExpr)
                        .map(ClassExpr::getType)
                        .map(Node::toString)
                        .anyMatch("PowerMockRunner"::equals);


        map.setRunWithAnnotated(isAnnotatedByPowerMockRunner);

        List<String> fieldTypes =
                n.getMembers().stream()
                        .filter(BodyDeclaration::isFieldDeclaration)
                        .map(BodyDeclaration::asFieldDeclaration)
                        .collect(Collectors.toList()).stream().map(it -> it.getVariables().stream().findFirst().orElse(null))
                        .filter(Objects::nonNull)
                        .map(NodeWithType::getTypeAsString).collect(Collectors.toList());

        this.maybeSource =
                fieldTypes.stream().filter(it -> map.getTest().getClassname().toLowerCase().contains(it.toLowerCase())).collect(Collectors.toSet());

    }
}








