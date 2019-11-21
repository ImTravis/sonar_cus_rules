package utils;

import jdk.nashorn.internal.ir.Block;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.java.ast.parser.ArgumentListTreeImpl;
import org.sonar.java.ast.parser.ListTreeImpl;
import org.sonar.java.ast.parser.StatementExpressionListTreeImpl;
import org.sonar.java.model.InternalSyntaxToken;
import org.sonar.java.model.declaration.ModifiersTreeImpl;
import org.sonar.java.model.expression.IdentifierTreeImpl;
import org.sonar.java.model.expression.MemberSelectExpressionTreeImpl;
import org.sonar.java.model.expression.MethodInvocationTreeImpl;
import org.sonar.java.model.statement.BlockTreeImpl;
import org.sonar.java.model.statement.ExpressionStatementTreeImpl;
import org.sonar.plugins.java.api.tree.*;

import javax.swing.plaf.nimbus.State;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @ClassName SonarJavaUtils
 * @Author xcc
 * @Date 2019/11/12 14:24
 * @Desc 自定义工具类
 **/
public class SonarJavaUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SonarJavaUtils.class);

    /**
     * 根据某一个子节点，获取顶层ClassTree
     *
     * @param tree
     * @return
     */
    public static ClassTree getClassTree(Tree tree) {
        while (!(tree.parent() instanceof ClassTree)) {
            tree = tree.parent();
        }
        tree = tree.parent();
        ClassTree classTree = (ClassTree) tree;
        return classTree;
    }

    /**
     * 获取ClassTree子类中，MethodTree子类集合
     *
     * @param tree ClassTree对象
     * @return List<MethodTree>
     */
    public static List<MethodTree> getClassMethod(ClassTree tree) {
        return getClassMethod(tree, null);
    }

    /**
     * 获取ClassTree子类中，MethodTree子类集合，并根据mName筛选
     *
     * @param tree  ClassTree对象
     * @param mName MethodName 的名字
     * @return List<MethodTree>
     */

    public static List<MethodTree> getClassMethod(ClassTree tree, String mName) {
        List<MethodTree> result = new ArrayList<>();
        String className = tree.simpleName().name();
        if (tree instanceof ClassTree) {
            ClassTree classTree = (ClassTree) tree;
            List<Tree> members = classTree.members();
            for (Tree mem : members) {
                //操作每一个成员
                if (mem instanceof VariableTree) {
                    //处理类中的参数定义tree
                } else if (mem instanceof MethodTree) {
                    String methodName = ((MethodTree) mem).symbol().name();
                    if (mName == null) {
                        result.add((MethodTree) mem);
                    } else if (methodName.equals(mName)) {
                        result.add((MethodTree) mem);
                    }
                }
            }
        } else {
            LOGGER.info("非class文件不处理");
            return null;
        }
        return result;
    }


    //获取这个树的编码代码
    public static StringBuffer getTreeStatementCode(Tree tree, StringBuffer buffer) {
        if(tree == null){
            return buffer;
        }
        Iterable<Tree> iterator = null;
        //如果该树 调用了其他方法的，需要特殊处理，
        if (tree instanceof MethodInvocationTree) {
            MethodInvocationTreeImpl treeChild = (MethodInvocationTreeImpl) tree;
            iterator = treeChild.children();
            if (iterator != null) {
                Iterator item = iterator.iterator();
                while (item.hasNext()) {
                    buffer = (getTreeStatementCode((Tree) item.next(), buffer));
                }
            }
            //如果方法里面，还嵌套了方法，则解析嵌套的方法，
            String menthodName = treeChild.methodSelect().toString();
            if(treeChild.methodSelect() instanceof MemberSelectExpressionTree){
                LOGGER.info("Java内置的函数，非自定义的方法和函数调用，不在解析");
            }
            else if(treeChild.methodSelect() instanceof IdentifierTreeImpl){
                LOGGER.info("解析自定义函数");
                //获取该方法的顶层类，并通过class和方法名 获取到方法
                List<MethodTree> methods = SonarJavaUtils.getClassMethod(getClassTree(tree), menthodName);
                LOGGER.info("解析嵌套方法：【" + menthodName + "】");
                buffer.append("\n");
                if (!methods.isEmpty()) {
                    for (MethodTree childMethod : methods) {
                        buffer = getTreeStatementCode(childMethod, buffer);
                    }
                }
                buffer.append("\n");
                LOGGER.info("自定义函数解析结束");
            }

        } else if (tree instanceof InternalSyntaxToken) {
            InternalSyntaxToken treeChild = (InternalSyntaxToken) tree;
            if (treeChild.isEOF()) {
                iterator = treeChild.children();
                if (iterator != null) {
                    Iterator item = iterator.iterator();
                    while (item.hasNext()) {
                        buffer = (getTreeStatementCode((Tree) item.next(), buffer));
                    }
                }
            } else {
                buffer.append(treeChild.text());
            }
        }

        /**
         * 继承ListTreeImpl 的tree 没有children ,例如：StatementExpressionListTreeImpl  ModifiersTreeImpl
         * 因为 StatementExpressionListTreeImpl 没有 children()方法，所有需要单独处理
         */
        else if (tree instanceof ListTreeImpl) {
            ListTreeImpl listTree = (ListTreeImpl) tree;
            iterator = listTree.children();
            if (iterator != null) {
                Iterator item = iterator.iterator();
                while (item.hasNext()) {
                    buffer = (getTreeStatementCode((Tree) item.next(), buffer));
                }
            }
        } else {
            try {
                iterator = (Iterable<Tree>) tree.getClass().getDeclaredMethod("children").invoke(tree);
                if (iterator != null) {
                    Iterator item = iterator.iterator();
                    while (item.hasNext()) {
                        buffer = (getTreeStatementCode((Tree) item.next(), buffer));
                    }
                }
            } catch (Exception e) {
                LOGGER.info("解析报错：" + e.getMessage());
            }
        }

        return buffer;

    }


}
