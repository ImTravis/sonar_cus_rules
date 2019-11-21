package rule;

public class NestedLoopsForSqlCheck {

    public void checkSql1() {

            String a = "a";
            dbQuery();

    }

    //假设这个方法是调用数据库
    public void dbQuery() {
        System.out.println("c测试");
    }

    public void checkSql2() {
        for (int k = 0; k < 10; k++) {
            String a = "b";
            if (a.equals("b")) {
                for (int a = 0; a < 10; a++) {
                    checkSql1();
                }
            }
        }
    }

}
