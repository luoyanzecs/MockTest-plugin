package cn.luoyanze.mocktest.parser.visit;

import cn.luoyanze.mocktest.parser.model.JavaFileV3;
import cn.luoyanze.mocktest.parser.model.java.ClassMap;
import cn.luoyanze.mocktest.parser.model.java.FieldV3;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/6 12:18 PM
 */


public class SourceVisitorAdapter extends VoidVisitorAdapter<JavaFileV3> {

    private static final String COMMENT_LINE_REG = "//[\\s\\S]*?\\n";

    private static final String COMMENT_LINES_REG = "/\\*[\\s\\S]*?\\*/";

    private static final String STATIC_CLASS_REG = "[\\s|_|!|\\||\\&]+([A-Z][A-Z|a-z|0-9|_]*?)[\\s]*?\\.[a-z|A-Z|_]*?[\\s]*?\\(";

    @Override
    public void visit(final ClassOrInterfaceDeclaration n, final JavaFileV3 file) {
        file.setName(n.getNameAsString());

        n.getExtendedTypes().forEach(p -> p.accept(this, file));
        n.getImplementedTypes().forEach(p -> p.accept(this, file));
        n.getTypeParameters().forEach(p -> p.accept(this, file));
        n.getMembers().forEach(p -> p.accept(this, file));
        n.getModifiers().forEach(p -> p.accept(this, file));
        n.getName().accept(this, file);
        n.getAnnotations().forEach(p -> p.accept(this, file));
        n.getComment().ifPresent(l -> l.accept(this, file));
    }

    @Override
    public void visit(final PackageDeclaration n, final JavaFileV3 file) {
        file.setPackageName(n.getNameAsString());
    }

    @Override
    public void visit(final ImportDeclaration n, final JavaFileV3 file) {
        ClassMap classMap = new ClassMap(n.getName().getIdentifier(), n.getName().getQualifier().map(Objects::toString).orElse(""));
        file.getImports().add(classMap);
    }


    @Override
    public void visit(final FieldDeclaration n, final JavaFileV3 file) {
        n.getVariables()
                .getFirst()
                .ifPresent(it -> {
                    FieldV3 field = new FieldV3();
                    field.setClassname(it.getTypeAsString());
                    field.setName(it.getNameAsString());

                    it.getInitializer().ifPresent(expression -> {
                        if (expression.isMethodCallExpr()) {
                            MethodCallExpr methodCallExpr = expression.asMethodCallExpr();
                            methodCallExpr.getScope().map(Expression::asNameExpr).map(NodeWithSimpleName::getNameAsString).ifPresent(field::setInitClass);
                            field.setInitMethod(methodCallExpr.getNameAsString());
                            field.setInitArgs(methodCallExpr.getArguments().stream().map(Node::toString).collect(Collectors.toList()));
                        }
                    });

                    if (field.getClassname().endsWith("Logger")) {
                        file.setLogger(field);
                    } else {
                        if (field.getInitClass() != null) {
                            file.getStaticMocks().add(field.getInitClass());
                        }
                        file.getFields().add(field);
                    }

                });
    }

    @Override
    public void visit(final ConstructorDeclaration n, final JavaFileV3 file) {
        List<String> params = n.getParameters().stream().map(Parameter::getType).map(Objects::toString).collect(Collectors.toList());

        if (params.isEmpty()) {
            file.setHasEmptyConstructor(true);
        }

        file.setInitParams(params);
    }

    @Override
    public void visit(MethodDeclaration n, final JavaFileV3 file) {

        n.getBody().map(Objects::toString).ifPresent(
                body -> {
                    String bodyWithoutComment = body.replaceAll(COMMENT_LINES_REG, "").replaceAll(COMMENT_LINE_REG, "");
                    Matcher matcher = Pattern.compile(STATIC_CLASS_REG).matcher(bodyWithoutComment);
                    if (matcher.find() && matcher.groupCount() > 0) {
                        file.getStaticMocks().add(matcher.group(1));
                    }
                }
        );
    }

    //private void handleMethod(Statement statement, JavaFileV3 file) {
    //    if (statement.isExpressionStmt()) {
    //        statement.asExpressionStmt().getExpression().asVariableDeclarationExpr()
    //                .getVariables().stream()
    //                .findFirst().ifPresent(variableDeclarator -> {
    //                    variableDeclarator.getInitializer().
    //                });
    //    } else if (statement.isIfStmt()) {
    //
    //    } else if (statement.isTryStmt()) {
    //
    //    } else if (statement.isReturnStmt()) {
    //
    //    } else if (statement.isForStmt()) {
    //
    //    } else {
    //
    //    }
    //}
}
