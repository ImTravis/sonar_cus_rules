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

//    @Override
//    public void visitClass(ClassTree tree) {
//        allTree = tree;
//        String className = tree.simpleName().name();
//        dealClass(tree);
//        super.visitClass(tree);
//    }

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
//    @Override
//    public void visitForEachStatement(ForEachStatement tree) {
//        allTree = tree;
//        LOGGER.info("visit ForEachStatement");
//        super.visitForEachStatement(tree);
//    }

    /**
     * 处理类中定义的常量
     *
     * @param tree
     */
    public void detailVariableTree(VariableTree tree) {
        //如果是定义的常量
        LOGGER.info("<<******>>" + tree.kind().name());
        String variableName = tree.simpleName().name();
        String variableIdentify = tree.type().toString();
        LOGGER.info("常量定义【" + variableIdentify + " " + variableName + "】");
    }

    //处理方法块语句ExpressionStatementTree
    public void detailExpressionStatementTree(ExpressionStatementTree expressionStatementTree) {
        LOGGER.info("ExpressionStatementTree 语句处理-73");
        if (expressionStatementTree.expression() instanceof AssignmentExpressionTree) {
            AssignmentExpressionTree assignmentExpressionTree = (AssignmentExpressionTree) expressionStatementTree.expression();
            if (assignmentExpressionTree.expression() instanceof MethodInvocationTree) {
                LOGGER.info(" MethodInvocationTree 语句处理-77");
                MethodInvocationTree methodInvocationTree = (MethodInvocationTree) assignmentExpressionTree.expression();
                if (methodInvocationTree.methodSelect() instanceof MemberSelectExpressionTree) {
                    MemberSelectExpressionTree memberSelectExpressionTree = (MemberSelectExpressionTree) methodInvocationTree.methodSelect();

//                    MethodsHelper.methodName(methodInvocationTree);
                    String methodName = memberSelectExpressionTree.identifier().name();
                    String classN = memberSelectExpressionTree.expression().toString();
                    LOGGER.info("Method_Invocation-85:" + classN + "." + methodName);

                    if (methodName.indexOf("dbQuery") != -1) {
                        context.reportIssue(this, allTree, "The Name Of Abstract Class should use Abstract or Base first");
                    }

                }


            }
        } else if (expressionStatementTree.expression() instanceof MethodInvocationTree) {
            MethodInvocationTree methodInvocationTree = (MethodInvocationTree) expressionStatementTree.expression();
            LOGGER.info(" MethodInvocationTree 语句处理 -97");
            if (methodInvocationTree.methodSelect() instanceof IdentifierTree) {
                IdentifierTree identifierTree = (IdentifierTree) methodInvocationTree.methodSelect();

                String classN = identifierTree.symbol().owner().name();
                String methodName = identifierTree.symbol().name();
                LOGGER.info("Method_Invocation-103:" + classN + "." + methodName);

                if (methodName.indexOf("dbQuery") != -1) {
                    context.reportIssue(this, allTree, "Cant Run mysql in for or forEach");
                }

            }

        }
    }

    //处理方法块
    public void detailMethodBlock(BlockTree block) {
        LOGGER.info("处理方法块，获取它的系数 - 116"+(block.body() instanceof BlockStatementListTreeImpl)+"***"+block.body().getClass());


//        if (block.body() instanceof BlockStatementListTreeImpl) {
        if(1==1){
            LOGGER.info("获取方法块中对应的语句对象 例如子类对象有：定义参数对象，方法调用对象，for{ }循环对象，if{ }对象...等  - 119");
            List<StatementTree> statementTrees = block.body();
            for (StatementTree statement : statementTrees) {
                //定义一个对象 String  name = "aaa";
                if (statement instanceof VariableTree) {
                    LOGGER.info("处理VariableTree - 124");
                }
                //语句操作，name ="bb"; 同 name = HiCHildFunction.dbQuery();
                else if (statement instanceof ExpressionStatementTree) {
                    LOGGER.info("处理ExpressionStatementTree  - 128");
                    ExpressionStatementTree expressionStatementTree = (ExpressionStatementTree) statement;
                    detailExpressionStatementTree(expressionStatementTree);
                }
                //添加了一个 IF 操作
                else if (statement instanceof IfStatementTree) {
                    LOGGER.info("deal in IF-134 ");
                    IfStatementTree ifStatementTree = (IfStatementTree) statement;
                    //如果if里面还有方法块
                    if (ifStatementTree.thenStatement() instanceof BlockTree) {
                        BlockTree blockTree = (BlockTree) ifStatementTree.thenStatement();
                        detailMethodBlock(blockTree);
                    }
                }
                //添加了一个 FOR 操作
                else if (statement instanceof ForStatementTree) {
                    LOGGER.info("deal in FOR-144 ");
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

    //处理 method方法
    public void detailMethodTree(MethodTree methodTree) {
        LOGGER.info("处理方法，获取名称，类型，等-162");
        //获取方法中每一行语句
        if (methodTree.block() instanceof BlockTree) {
            //获取方法块
            BlockTree block = (BlockTree) methodTree.block();
            detailMethodBlock(block);
            LOGGER.info("***************");
        }

        //over


    }

    //处理class类
    public void dealClass(ClassTree tree) {
        String className = tree.simpleName().name();
        if (tree instanceof ClassTree) {
            ClassTree classTree = (ClassTree) tree;
            List<Tree> members = classTree.members();
            for (Tree mem : members) {
                LOGGER.info("操作每一个成员");
                if (mem instanceof VariableTree) {
                    //处理类中的参数定义tree
                    detailVariableTree((VariableTree) mem);
                } else if (mem instanceof MethodTree) {
                    //处理类中的方法tree
                    detailMethodTree((MethodTree) mem);
                }
            }
        } else {
            LOGGER.info("非class文件不处理");
        }
    }
}
