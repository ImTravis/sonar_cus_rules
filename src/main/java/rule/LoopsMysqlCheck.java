package rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Rule;
import org.sonar.java.ast.parser.BlockStatementListTreeImpl;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.*;

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
    private StatementTree allTree;

    @Override
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;
        scan(context.getTree());
    }


    @Override
    public void visitForStatement(ForStatementTree tree) {
        allTree = tree;
        LOGGER.info("sql check start");
        if (tree.statement() instanceof BlockTree) {
            BlockTree blockTree = (BlockTree) tree.statement();
            detailMethodBlock(blockTree);
        }
        super.visitForStatement(tree);
        LOGGER.info("sql check over");
    }

    //处理方法块语句ExpressionStatementTree
    public void detailExpressionStatementTree(ExpressionStatementTree expressionStatementTree) {
        LOGGER.info("ExpressionStatementTree 语句处理-46");
        if (expressionStatementTree.expression() instanceof AssignmentExpressionTree) {
            AssignmentExpressionTree assignmentExpressionTree = (AssignmentExpressionTree) expressionStatementTree.expression();
            if (assignmentExpressionTree.expression() instanceof MethodInvocationTree) {
                LOGGER.info(" MethodInvocationTree 语句处理-51");
                MethodInvocationTree methodInvocationTree = (MethodInvocationTree) assignmentExpressionTree.expression();
                if (methodInvocationTree.methodSelect() instanceof MemberSelectExpressionTree) {
                    MemberSelectExpressionTree memberSelectExpressionTree = (MemberSelectExpressionTree) methodInvocationTree.methodSelect();

//                    MethodsHelper.methodName(methodInvocationTree);
                    String methodName = memberSelectExpressionTree.identifier().name();
                    String classN = memberSelectExpressionTree.expression().toString();
                    LOGGER.info("Method_Invocation-59:" + classN + "." + methodName);

                    if (methodName.indexOf("dbQuery") != -1) {
                        context.reportIssue(this, allTree, "Cant Run mysql in for or forEach");
                    }

                }


            }
        } else if (expressionStatementTree.expression() instanceof MethodInvocationTree) {
            MethodInvocationTree methodInvocationTree = (MethodInvocationTree) expressionStatementTree.expression();
            LOGGER.info(" MethodInvocationTree 语句处理 -71");
            if (methodInvocationTree.methodSelect() instanceof IdentifierTree) {
                IdentifierTree identifierTree = (IdentifierTree) methodInvocationTree.methodSelect();

                String classN = identifierTree.symbol().owner().name();
                String methodName = identifierTree.symbol().name();
                LOGGER.info("Method_Invocation-77:" + classN + "." + methodName);

                if (methodName.indexOf("dbQuery") != -1) {
                    context.reportIssue(this, allTree, "Cant Run mysql in for or forEach");
                }

            }

        }
    }

    //处理方法块
    public void detailMethodBlock(BlockTree block) {
        LOGGER.info("处理方法块，获取它的系数 - 116" + (block.body() instanceof BlockStatementListTreeImpl) + "***" + block.body().getClass());

//        if (block.body() instanceof BlockStatementListTreeImpl) {
        if (1 == 1) {
            LOGGER.info("获取方法块中对应的语句对象 例如子类对象有：定义参数对象，方法调用对象，for{ }循环对象，if{ }对象...等  - 119");
            List<StatementTree> statementTrees = block.body();
            for (StatementTree statement : statementTrees) {
                //定义一个对象 String  name = "aaa";
                if (statement instanceof VariableTree) {
                    LOGGER.info("处理VariableTree - 99");
                }
                //语句操作，name ="bb"; 同 name = HiCHildFunction.dbQuery();
                else if (statement instanceof ExpressionStatementTree) {
                    LOGGER.info("处理ExpressionStatementTree  - 103");
                    ExpressionStatementTree expressionStatementTree = (ExpressionStatementTree) statement;
                    detailExpressionStatementTree(expressionStatementTree);
                }
                //添加了一个 IF 操作
                else if (statement instanceof IfStatementTree) {
                    LOGGER.info("deal in IF-109 ");
                    IfStatementTree ifStatementTree = (IfStatementTree) statement;
                    //如果if里面还有方法块
                    if (ifStatementTree.thenStatement() instanceof BlockTree) {
                        BlockTree blockTree = (BlockTree) ifStatementTree.thenStatement();
                        detailMethodBlock(blockTree);
                    }
                }
                //添加了一个 FOR 操作
                else if (statement instanceof ForStatementTree) {
                    LOGGER.info("deal in FOR-119 ");
                    //如果For里面还有方法块
                    ForStatementTree forStatementTree = (ForStatementTree) statement;
                    if (forStatementTree.statement() instanceof BlockTree) {
                        BlockTree blockTree = (BlockTree) forStatementTree.statement();
                        detailMethodBlock(blockTree);
                    }
                }


            }

        }

    }

}
