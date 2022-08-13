package cn.luoyanze.mocktest.parser;

import cn.luoyanze.mocktest.parser.model.SimpleJavaSource;
import cn.luoyanze.mocktest.parser.model.TestSourceMap;
import cn.luoyanze.mocktest.parser.visit.JavaSourceVisitorAdapter;
import cn.luoyanze.mocktest.parser.visit.JavaTestWithSourceVisitorAdapter;
import cn.luoyanze.mocktest.service.TemplateService;
import cn.luoyanze.mocktest.parser.visit.PreviousTestVisitAdapter;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import freemarker.template.TemplateException;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/5 8:38 PM
 */


public class JavaParserTest {

    private static class MethodVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(final ClassOrInterfaceDeclaration n, final  Object arg) {
            JavaParser javaParser = new JavaParser();

            //ArrayInitializerExpr arrayInitializerExpr = new ArrayInitializerExpr();
            //NodeList<Expression> expressions = new NodeList<>();
            //expressions.add(new ClassExpr(javaParser.parseClassOrInterfaceType("ABC").getResult().get()));
            //expressions.add(new ClassExpr(javaParser.parseClassOrInterfaceType("DDD").getResult().get()));
            //arrayInitializerExpr.setValues(expressions);
            //n.addSingleMemberAnnotation("PrepareForTest", arrayInitializerExpr);
            //NodeList<MemberValuePair> expressions1 = new NodeList<>();
            //expressions1.add(new MemberValuePair("", new ClassExpr(javaParser.parseClassOrInterfaceType("DDD").getResult().get())));

            //n.addAnnotation(new NormalAnnotationExpr(new Name("MockPolicy"), expressions1));

            //n.getAnnotations().stream()
            //        .filter(it -> it.getNameAsString().equals("PrepareForTest"))
            //        .findFirst().ifPresent(Node::remove);

            System.out.println("------------");
        }

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            n.getBody().ifPresent(it -> {
                it.addStatement(0,
                        new ExpressionStmt(new VariableDeclarationExpr(
                                new JavaParser().parseClassOrInterfaceType("String").getResult().get(),
                                "abc = 123"
                        ))
                );
            });
            // here you can access the attributes of the method.
            // this method will be called for all methods in this
            // CompilationUnit, including inner class methods
            //System.out.println(n.getName());
            //System.out.println(n.getAnnotations());
            //System.out.println(n.getModifiers());
            ////System.out.println(n.getBody());
            //n.addAnnotation("cn.luo.Moo");
            //BlockStmt blockStmt = n.getBody().orElse(null);
            //blockStmt.addStatement("System.out.println();");
            //System.out.println(blockStmt);
            System.out.println("----------");
        }

        @Override
        public void visit(final FieldDeclaration n, Object arg) {
            //System.out.println(n.getName());
            //n.addAnnotation("Mock");
            System.out.println("field");
        }

        @Override
        public void visit(final PackageDeclaration n, Object arg) {
            //System.out.println(n.getName());
            //System.out.println(n.getName());
            //System.out.println("package");
        }

        @Override
        public void visit(final ImportDeclaration n, Object arg) {
            //System.out.println(n.getName());
            //System.out.println(n.getName());
            //System.out.println("----------------");
            //System.out.println("import");
        }

        //@Override
        //public void visit(final CompilationUnit n, final Object arg) {
        //    //System.out.println("---------");
        //    //
        //    //n.getTypes().forEach(p -> p.accept(this, arg));
        //    //n.getComment().ifPresent(l -> l.accept(this, arg));
        //    System.out.println("parse");
        //
        //}

        @Override
        public void visit(final Name n, final Object arg) {
            System.out.println(n);
        }

        @Override
        public void visit(final ConstructorDeclaration n, final Object arg) {
            //System.out.println("------");
        }

    }

    @Test
    public void javaParser() throws Exception{
        CompilationUnit parse = StaticJavaParser.parse(Paths.get("NearbyRestaurantRepositoryImplTest.java"));
        new MethodVisitor().visit(parse, null);
        System.out.println(parse);

    }


    @Test
    public void testParser() throws Exception{
        SimpleJavaSource javaFileV3 = new SimpleJavaSource();

        CompilationUnit parse = StaticJavaParser.parse(Paths.get("NearbyRestaurantRepositoryImpl.java"));
        new JavaSourceVisitorAdapter().visit(parse, javaFileV3);

        CompilationUnit parse1 = StaticJavaParser.parse(Paths.get("NearbyRestaurantRepositoryImplTest.java"));
        new JavaTestWithSourceVisitorAdapter().visit(parse1, javaFileV3);
        System.out.println(parse1);
    }

    @Test
    public void testReg() {
        String str = "testObj = new NearbyRestaurantRepositoryImpl();";

        System.out.println(Pattern.compile("new\\s+NearbyRestaurantRepositoryImpl\\s*?\\(").matcher(str).find());
    }

    @Test
    public void testPrepare() throws IOException {
        TestSourceMap testSourceMap = new TestSourceMap();
        CompilationUnit parse1 = StaticJavaParser.parse(Paths.get("NearbyRestaurantRepositoryImplTest.java"));
        new PreviousTestVisitAdapter().visit(parse1, testSourceMap);
        System.out.println("-----------");
    }

    @Test
    public void test_create_file() throws IOException {
        Path path = Paths.get("/Users/luoyanze", "abc.txt");
        //Files.createDirectories(path);

        System.out.println(Files.exists(Paths.get("/Users/a")));
    }

    @Test
    public void testJavaSOurceVisitorAdapter() throws IOException, TemplateException {

        SimpleJavaSource simpleJavaSource = new SimpleJavaSource();
        CompilationUnit parse = StaticJavaParser.parse(Paths.get("NearbyRestaurantRepositoryImpl.java"));
        new JavaSourceVisitorAdapter().visit(parse, simpleJavaSource);
        String myTest = TemplateService.generateTemplate(simpleJavaSource, "MyTest");
        System.out.println(myTest);
        System.out.println("-------");
    }


    @Test public void testparse() {
        JavaParser javaParser = new JavaParser();
        ParseResult<Statement> statementParseResult = javaParser.parseStatement("MemberModifier.    field(A.class, \"abc\").set(abc, 123);");
        System.out.println(statementParseResult);
    }
}








