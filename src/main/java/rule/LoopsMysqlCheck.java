package rule;

import com.sun.javafx.binding.ExpressionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Rule;
import org.sonar.java.ast.parser.ArgumentListTreeImpl;
import org.sonar.java.ast.parser.BlockStatementListTreeImpl;
import org.sonar.java.checks.helpers.ExpressionsHelper;
import org.sonar.java.checks.helpers.MethodsHelper;
import org.sonar.java.model.InternalSyntaxToken;
import org.sonar.java.model.expression.IdentifierTreeImpl;
import org.sonar.java.model.expression.MemberSelectExpressionTreeImpl;
import org.sonar.java.model.expression.MethodInvocationTreeImpl;
import org.sonar.java.model.statement.ExpressionStatementTreeImpl;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.*;
import utils.SonarJavaUtils;

import java.util.Iterator;
import java.util.List;

/**
 * @ClassName LoopsMysqlCheck
 * @Author Administrator
 * @Date 2019/11/11 14:36
 * @Desc 嵌套for循环
 **/
@Rule(key = "LoopsMysqlCheck")
public class LoopsMysqlCheck extends BaseTreeVisitor implements JavaFileScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoopsMysqlCheck.class);
    private JavaFileScannerContext context;

    private String validDB = "dbQuery";

    @Override
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;
        scan(context.getTree());
    }
//    @Override
//    public void visitClass(ClassTree tree) {
//        allTree = tree;
//        super.visitClass(tree);
//    }

    @Override
    public void visitForStatement(ForStatementTree tree) {
        LOGGER.info("sql check start");
        StringBuffer expressStatement = new StringBuffer("CODE:\n");
        expressStatement = SonarJavaUtils.getTreeStatementCode(tree, expressStatement);
        LOGGER.info("for 循环  涉及到的 代码:" + expressStatement.toString());
        if (expressStatement.indexOf(validDB) != -1) {
            context.reportIssue(this, tree, "Cant Run mysql in for or forEach");
        }
        super.visitForStatement(tree);
        LOGGER.info("sql check over");
    }


}
