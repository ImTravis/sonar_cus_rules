import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import rule.LoopsMysqlCheck;

/**
 * @ClassName LoopsMysqlCheckTest
 * @Author Administrator
 * @Date 2019/11/1114:39
 * @Desc
 **/
public class LoopsMysqlCheckTest {
    @Test
    public void test() {
        try {
            JavaCheckVerifier.verify("src/test/files/NestedLoopsForSqlCheck.java", new LoopsMysqlCheck());
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
    }
}
