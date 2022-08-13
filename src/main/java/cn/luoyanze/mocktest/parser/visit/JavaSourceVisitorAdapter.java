package cn.luoyanze.mocktest.parser.visit;

import cn.luoyanze.mocktest.parser.model.SimpleJavaSource;
import cn.luoyanze.mocktest.parser.model.java.Field;
import cn.luoyanze.mocktest.parser.model.java.Parameter;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/12 9:40 PM
 */


public class JavaSourceVisitorAdapter extends VoidVisitorAdapter<SimpleJavaSource> {


    @Override
    public void visit(final CompilationUnit n, final SimpleJavaSource source) {
        String name = n.getPrimaryTypeName().orElse("");
        String packageName= n.getPackageDeclaration().map(NodeWithName::getNameAsString).orElse("");
        source.setClassname(name);
        source.setPackageName(packageName);

        // import插入
        n.getImports().stream()
                .map(ImportDeclaration::getName)
                .forEach(it -> source.addImports(it.getIdentifier(), it.getQualifier().map(Node::toString).orElse("")));

        // 处理主类
        n.getTypes().stream().filter(it -> it.getNameAsString().contains(name)).forEach(it -> it.accept(this, source));
    }

    @Override
    public void visit(final ClassOrInterfaceDeclaration n, final SimpleJavaSource source) {

        n.getMembers().forEach(p -> p.accept(this, source));
    }

    @Override
    public void visit(final FieldDeclaration n, final SimpleJavaSource source) {
        n.getVariables().forEach(variableDeclarator -> {
            source.addField(new Field(variableDeclarator.getNameAsString(), variableDeclarator.getTypeAsString()));
        });
        super.visit(n, source);
    }

    @Override
    public void visit(final ConstructorDeclaration n, final SimpleJavaSource source) {
        List<Parameter> parameters =
                n.getParameters().stream()
                        .map(it -> new Parameter(it.getTypeAsString(), it.getNameAsString()))
                        .collect(Collectors.toList());
        if (parameters.size() == 0) {
            source.setHasEmptyConstructor(true);
        }
        source.setConstructorParams(parameters);
        n.getBody().accept(this, source);
    }

    @Override
    public void visit(MethodDeclaration n, final SimpleJavaSource source) {
        super.visit(n, source);
    }

    @Override
    public void visit(final MethodCallExpr n, final SimpleJavaSource source) {
        n.getScope().ifPresent(it -> {
            if (it.isNameExpr()) {
                // 插入静态引用类
                NameExpr nameExpr = it.asNameExpr();
                String nameAsString = nameExpr.getNameAsString();
                if (source.getImportMaps().containsKey(nameAsString)) {
                    System.out.println(nameAsString);
                    source.addStaticRef(nameAsString);
                }
            }
        });
        super.visit(n, source);
    }


}
