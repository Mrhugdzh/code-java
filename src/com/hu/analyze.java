package com.hu;

import java.io.FileWriter;
import java.util.*;
/**
 * @Classname analyze
 * @Description 算符优先分析法 语法分析器
 * @Date 2020/05/18 23：57
 * @Created by hugang
 */
public class analyze {
    // FIRSTVT集合
    private static Map<Character, Set<Character>> firstVt = new HashMap<>();
    //LASTVT集合
    private static Map<Character, Set<Character>> lastVt = new HashMap<>();
    //输入的文法
    private static List<String> input = new ArrayList<>();
    //终结符
    private static Set<Character> End = new LinkedHashSet<>();
    //非终结符
    private static Set<Character> NoEnd = new LinkedHashSet<>();
    //算符矩阵
    private static Map<String, Character> matrix = new HashMap<>();
    //输入
    private static Scanner in = new Scanner(System.in);
    //文法的左右分割一一对应
    private static Map<Character, List<String>> produce = new HashMap<>();
    /**
     * @description ：获得firstVt集合
     *                处理的形式有：
     *                       1.P->a....
     *                       2.P->a|...
     *                       3.P->Qa...
     *                       4.P->....|Qa...
     *                       5.P->Q...
     * @author：hugang
     * @date ：2020/5/19 0:02
     * @param ：Character s  非终结符
     * @param : Set<Character> fvt first集合
     * @return
     */
    private static void getFirstVT(Character s, Set<Character> fvt) {
        String str = null;
        int i = 0;
        //获取该非终结符的产生式
        for (i = 0; i < input.size(); i++) {
            if (input.get(i).charAt(0) == s) {
                str = input.get(i);
            }
        }
        for (i = 3; i < str.length(); i++) {
            if (str.charAt(i) < 65 || str.charAt(i) > 90) {
                //形式：P->a.....，P->a|A 即以终结符开头，该终结符入Firstvt
                if ((str.charAt(i - 1) == '>' && str.charAt(i - 2) == '-') || str.charAt(i - 1) == '|') {
                    fvt.add(str.charAt(i));
                }
                //解决形式：P->Qa....，P->Q|Qa即先以非终结符开头，紧跟终结符，则终结符入Firstvt
                if ((str.charAt(i - 2) == '|' || (str.charAt(i - 2) == '>' && str.charAt(i - 3) == '-')) && str.charAt(i - 1) >= 65 && str.charAt(i - 1) <= 90) {
                    fvt.add(str.charAt(i));
                }
            }
            //若有P->Q.....，P->Q|A即以非终结符开头，该非终结符的Firstvt加入P的Firstvt   A是65，Z是95，在65-95之间的是非终结符
            if (str.charAt(i - 1) == '|' && str.charAt(i) >= 65 && str.charAt(i) <= 90) {
                if (str.charAt(i) == str.charAt(0)) {
                    continue;
                }
                //递归
                getFirstVT(str.charAt(i), fvt);
            }
        }
    }
    /**
     * @description ：获得lastVt集合
     *                处理的形式有：
     *                      1.P->.....aQ
     *                      2.P->.....aQ|....
     *                      3.P->.....Q
     *                      4.P->.....Q|....
     * @author：hugang
     * @date ：2020/5/19 8:55
     * @param ：Character s  非终结符
     * @param : Set<Character> lvt last集合
     * @return
     */
    private static void getLastVT(Character s, Set<Character> lvt) {
        String str = null;
        int i = 0;
        //获取该非终结符的产生式
        for (i = 0; i < input.size(); i++) {
            if (input.get(i).charAt(0) == s) {
                str = input.get(i);
            }
        }
        for (i = 3; i < str.length(); i++) {
            //P->....aQ，即先以非终结符结尾，前面是终结符，则终结符入Lastvt,Q处于产生式最后一位的情况
            if (str.charAt(i) < 65 || str.charAt(i) > 90) {
                //解决形如：P->.....a和P->......aQ
                //第一个条件：P->......a,倒数第一位是终结符
                //第二个条件：如果倒数第二位是终结符，则它的前一位一定是非终结符（排除:P->...ba和P->ba）
                if (i == str.length() - 1 || (i == str.length() - 2 && str.charAt(i + 1) >= 65 && str.charAt(i + 1) <= 90 && str.charAt(i) != '|' && (str.charAt(i) != '>' && str.charAt(i) != '-'))) {
                    lvt.add(str.charAt(i));
                }
                if (i < str.length() - 2) {
                    //解决形如：P->.....a|....和P->......aQ|....
                    //第一个条件是：P->.....a|....
                    //第二个条件：P->.....aQ|....即先以非终结符结尾，前面是终结符，则终结符入Lastvt
                    if (str.charAt(i + 1) == '|' || (str.charAt(i + 2) == '|' && str.charAt(i + 1) >= 65 && str.charAt(i + 1) <= 90)) {
                        lvt.add(str.charAt(i));
                    }
                }
            } else {
                //P->....Q，即以非终结符结尾，该非终结符的Lastvt入P的Lastvt
                if (i == str.length() - 1) {
                    //形如P->....Q
                    //如果是以自己结尾，不做处理
                    if (str.charAt(i) == str.charAt(0)) {
                        continue;
                    }
                    //否则的话求出该字符的lvt，加入
                    getLastVT(str.charAt(i), lvt);
                } else if (str.charAt(i + 1) == '|') {
                    //形如P->......Q|.....
                    if (str.charAt(i) == str.charAt(0)) {
                        continue;
                    }
                    getLastVT(str.charAt(i), lvt);
                }
            }
        }
    }
    /**
     * @description ：显示firstVt集合和lastVt集合
     * @author：hugang
     * @date ：2020/5/19 9:23
     * @param
     * @return
     */
    private static void DisplayFirstVT_LastVT() {
        //计算每一个非终结符的firstVT
        for (int i = 0; i < input.size(); i++) {
            Set<Character> fvt = new HashSet<>();
            getFirstVT(input.get(i).charAt(0), fvt);
            firstVt.put(input.get(i).charAt(0), fvt);
        }
        //计算每一个非终结符的lastVT
        for (int i = 0; i < input.size(); i++) {
            Set<Character> lvt = new HashSet<>();
            getLastVT(input.get(i).charAt(0), lvt);
            lastVt.put(input.get(i).charAt(0), lvt);
        }
        System.out.println("firstVt集合如下:");
        for (Map.Entry<Character, Set<Character>> entry : firstVt.entrySet()) {
            System.out.print("firstVt(" + entry.getKey() + "): {");
            int flag = 0;
            for (Character value : entry.getValue()) {
                flag++;
                System.out.print(value);
                if (flag != entry.getValue().size()) {
                    System.out.print(",");
                }
            }
            System.out.println("}");
        }
        System.out.println("lastVt集合如下:");
        for (Map.Entry<Character, Set<Character>> entry : lastVt.entrySet()) {
            System.out.print("lastVt(" + entry.getKey() + "): {");
            int flag = 0;
            for (Character value : entry.getValue()) {
                flag++;
                System.out.print(value);
                if (flag != entry.getValue().size()) {
                    System.out.print(",");
                }
            }
            System.out.println("}");
        }
    }
/**
 * @description ：获取所有终结符
 * @author：hugang
 * @date ：2020/5/19 9:25
 * @param
 * @return
 */
private static void getEnd() {
    for (int i = 0; i < input.size(); i++) {
        String temp = input.get(i);
        for (int j = 3; j < temp.length(); j++) {
            if (temp.charAt(j) < 65 || temp.charAt(j) > 90 && temp.charAt(j) != '|') {
                End.add(temp.charAt(j));
            }
        }
    }
    End.add('#');
}
/**
 * @description ：获取所有非终结符
 * @author：hugang
 * @date ：2020/5/19 9:26
 * @param
 * @return
 */
private static void getNoEnd() {
    for (int i = 0; i < input.size(); i++) {
        String temp = input.get(i);
        for (int j = 3; j < temp.length(); j++) {
            if (temp.charAt(j) >= 65 && temp.charAt(j) <= 90) {
                NoEnd.add(temp.charAt(j));
            }
        }
    }
}
/**
 * @description ：将每一行的文法分离,如E->E+T|T分离成E和E+T,T
 * @author：hugang
 * @date ：2020/5/19 9:27
 * @param
 * @return
 */
private static void getProduce() {
    for (int i = 0; i < input.size(); i++) {
        List<String> list = new ArrayList<>();
        //取出每一个产生式
        String str = input.get(i);
        StringBuffer a = new StringBuffer();
        for (int j = 3; j < str.length(); j++) {
            //从第三个开始，因为前面是E->....
            if (str.charAt(j) != '|') {
                //如果不是 | ，则前面的都是一个产生式
                a.append(str.charAt(j));
            } else {
                //如果是 | ，则前面的是一个完整的产生式。
                list.add(a.toString());
                //清空a
                a.delete(0, a.length());
            }
        }
        list.add(a.toString());
        produce.put(str.charAt(0), list);
    }
}
/**
 * @description ：错误
 * @author：hugang
 * @date ：2020/5/19 9:30
 * @param
 * @return
 */
public static void partError() {
    //错误的字符格式：((    ))   (#
    matrix.put(")(", 'b');
    matrix.put("((", 'b');
    matrix.put("(#", 'a');
}
/**
 * @description ：构造算符优先矩阵并打印
 * 用Map<String,Character>存,String中存优先变得行值和列值,Character表示String中所存行列的大小关系如"++"表示行为'+',列为'+'的时候,关系为Character中的值
 * @author：hugang
 * @date ：2020/5/19 9:31
 * @param :
 * @return
 */
private static void priorityMatrix() {
    for (int i = 0; i < input.size(); i++) {
        //获得每一个产生式
        String str = input.get(i);
        for (int j = 3; j < input.get(i).length(); j++) {
            //如果是一个终结符
            if ((str.charAt(j) < 65 || str.charAt(j) > 90) && (str.charAt(j) != '|')) {
                //此位不是最后一位，并且它的下一位是终结符，形式：P->....aa...
                if (j < str.length() - 1 && (str.charAt(j + 1) < 65 || str.charAt(j + 1) > 90)) {
                    //可以产生关系（a,A） 关系为 =
                    String temp = str.charAt(j) + "" + str.charAt(j + 1);
                    matrix.put(temp, '=');
                } else {
                    //如果不是倒数第二位，并且它的后面第二个是终结符，形式:P->....a?b..
                    if (j < str.length() - 2 && (str.charAt(j + 2) < 65 || str.charAt(j + 2) > 90) && (str.charAt(j + 2) != '|')) {
                        //可以产生关系(a,b) 关系为 =
                        matrix.put(str.charAt(j) + "" + str.charAt(j + 2), '=');
                    }
                }
                //如果此位不是最后一位，并且下一位是非终结符，形式：P->....aA...
                if (j < str.length() - 1 && str.charAt(j + 1) >= 65 && str.charAt(j + 1) <= 90) {
                    //此时a < firstVt(A)
                    Set<Character> coll = firstVt.get(str.charAt(j + 1));
                    for (Character value : coll) {
                        matrix.put(str.charAt(j) + "" + value, '<');
                    }
                }
                //如果它的前一位是非终结符，形式：P->....Aa
                if (j - 1 != 2 && str.charAt(j - 1) >= 65 && str.charAt(j - 1) <= 90) {
                    //此时a > lastVt(A)
                    Set<Character> coll = lastVt.get(str.charAt(j - 1));
                    for (Character value : coll) {
                        matrix.put(value + "" + str.charAt(j), '>');
                    }
                }
            }
        }
    }
    //# < firstVT(E)
    Set<Character> coll = firstVt.get(input.get(0).charAt(0));
    for (Character value : coll) {
        matrix.put('#' + "" + value, '<');
    }
    //# > lastVT(E)
    Set<Character> coll1 = lastVt.get(input.get(0).charAt(0));
    for (Character value : coll1) {
        matrix.put(value + "" + '#', '>');
    }
    partError();
    //空的地方都填写b
    for (Character value : End) {
        for (Character value1 : End) {
            if (matrix.get(value + "" + value1) == null) {
                matrix.put(value + "" + value1, 'b');
            }
        }
    }
    //(#,#) 填写 =
    matrix.put("##", '=');
    //获得所有非终结符
    getEnd();
    System.out.println("\n构造的算符优先关系表如下:");
    int kong = 0;
    for (Character value : End) {
        if (kong == 0) {
            System.out.print("   ");
        }
        kong++;
        System.out.print(value);
        if (kong != End.size()) {
            System.out.print("  ");
        }
    }
    System.out.println();
    for (Character value : End) {
        System.out.print(value);
        for (Character value1 : End) {
            Character ch = matrix.get(value + "" + value1);
            if (ch != null) {
                System.out.print("  " + ch);
            } else {
                System.out.print("  " + " ");
            }
        }
        System.out.println();
    }
}
/**
 * @description ：判断其是不是算符文法，如果没有连个连续非终结符号相连的就是算符优先文法
 * @author：hugang
 * @date ：2020/5/19 9:46
 * @param
 * @return
 */
private static boolean isOperator() {
    int i;
    for (i = 0; i < input.size(); i++) {
        for (int j = 0; j < input.get(i).length() - 1; j++) {
            String str = input.get(i);
            if (str.charAt(j) >= 65 && str.charAt(j) <= 90) {
                if ((str.charAt(j + 1) >= 65 && str.charAt(j + 1) <= 90)) {
                    return false;
                }
            }
        }
    }
    return true;
}
/**
 * @description ：判断其是不是终结符
 * @author：hugang
 * @date ：2020/5/19 9:46
 * @param ：ch 需要判断的字符
 * @return
 */
private static boolean isEnd(Character ch) {
    for (Character value : End) {
        if (value.equals(ch)) {
            return true;
        }
    }
    return false;
}
/**
 * @description ：判断其是不是非终结符
 * @author：hugang
 * @date ：2020/5/19 9:47
 * @param：ch 需要判断的字符
 * @return
 */
private static boolean isNoEnd(Character ch) {
    for (Character value : NoEnd) {
        if (value.equals(ch)) {
            return true;
        }
    }
    return false;
}
/**
 * @description ：根据产生式右部分返回左边
 * @author：hugang
 * @date ：2020/5/19 9:47
 * @param
 * @return
 */
private static char retLeft(String str) {
    char ch = 0;
    for (Map.Entry<Character, List<String>> map : produce.entrySet()) {
        ch = map.getKey();
        for (String value : map.getValue()) {
            if (value.length() != str.length()) {
                continue;
            }
            int i;
            for (i = 0; i < str.length(); i++) {

                if (str.charAt(i) >= 65 && str.charAt(i) <= 90) {
                    if (value.charAt(i) >= 65 && value.charAt(i) <= 90) {
                    } else {
                        break;
                    }
                } else {
                    if (value.charAt(i) != str.charAt(i)) {
                        break;
                    }
                }
            }
            if (i == str.length()) {
                return ch;
            }
        }
    }
    return 0;
}
/**
 * @description ：将字符数组转换成字符串
 * @author：hugang
 * @date ：2020/5/19 9:55
 * @param
 * @return
 */
public static String replaceToString(List<Character> list) {
    StringBuffer a = new StringBuffer();
    for (Character value : list) {
        if (value != ',' && value != '[' && value != ']') {
            a.append(value);
        }
    }
    return a.toString();
}
/**
 * @description ：算符优先分析过程，使用一个符号栈，用它寄存终结符和非终结符,k代表符号栈的深度，在正常情况下，算法工作完毕时，符号栈S应呈现:#N
 * 过程：
 * k:=1;
 * S[k]:= ‘#’;
 * REPEAT
 *      把下一个输入符号读进 a 中；
 *      IF S[k] ∈ Vt THEN j:=k ELSE j:= k-1;
 *      WHILE S[j] > a DO
 *      规约
 *            BEGIN
 *            REPEAT
 *                 Q:=S[j];
 *                 IF S[j-1]∈ VT THEN j:j-1 ELSE j:j-2
 *            UNTIL S[j] < Q;
 *            把 S[j+1]…S[k]归约为某个 N；
 *            k:=j+1;
 *            S[k]:=N;
 *            END OF WHILE;
 *       移进
 *       IF S[j] < a OR s[j] = a THEN
 *            BEGIN k:=k+1;S[k]:=N; END
 *       ELSE ERROR
 * UNTIL a = ‘#’
 * @author：hugang
 * @date ：2020/5/19 9:55
 * @param
 * @return
 */
public static void analysisProcess() {
    int status = 0;
    int count = 0;
    int k = 0;
    int j = 0;
    int step = 0;
    String gui = null;
    System.out.println("请输入要分析的句子(注意:记得以'#'结束)");
    String sentence = null;
    sentence = in.nextLine();
    //检查用户输入的句子，是否以 # 结尾，不是的话修改
    if (sentence.charAt(sentence.length() - 1) != '#') {
        sentence = sentence + "#";
    }
    List<Character> listStack = new ArrayList<>();
    System.out.printf("%-8s%-20s%-8s%-10s%-8s\n", "步骤", "栈", "a读入", "剩余串", "操作");
    listStack.add('#');
    char a = sentence.charAt(step++);
    do {
        if (status == 0) {
            if (count != 0) {
                System.out.printf("%-8s\n%-8d %-20s %-8c %-10s", "移进", count, replaceToString(listStack), a, sentence.substring(step));
            } else {
                System.out.printf("%-8d %-20s %-8c %-10s", count, replaceToString(listStack), a, sentence.substring(step));
            }
        } else {
            System.out.printf("%-8s\n%-8d %-20s %-8c %-10s", gui, count, replaceToString(listStack), a, sentence.substring(step));
        }
        char ch = listStack.get(k);
        if (isEnd(ch)) {
            j = k;
        } else if (j >= 1) {
            j = k - 1;
        }
        char temp = 0;
        if (matrix.get(listStack.get(j) + "" + a) != null) {
            //规约
            while (matrix.get(listStack.get(j) + "" + a).equals('>')) {
                if (listStack.size() == 2 && a == '#') {
                    break;
                }
                StringBuffer judge = new StringBuffer();
                do {
                    temp = listStack.get(j);
                    if (isEnd(listStack.get(j - 1))) {
                        j = j - 1;
                    } else {
                        j = j - 2;
                    }
                } while (!matrix.get(listStack.get(j) + "" + temp).equals('<'));
                for (int i = j + 1; i < listStack.size(); i++) {
                    judge.append(listStack.get(i));
                }
                int te = listStack.size();
                for (int t = j + 1; t < te; t++) {
                    listStack.remove(j + 1);
                }
                char res = retLeft(judge.toString());
                if (res != 0) {
                    count++;
                    k = j + 1;
                    listStack.add(res);
                    status = 1;
                    gui = "用" + res + "->" + judge.toString() + "规约";
                    if (status == 0) {
                        System.out.printf("%-8s\n%-8d %-20s %-8c %-10s", "移进", count, replaceToString(listStack), a, sentence.substring(step));
                    } else {
                        System.out.printf("%-8s\n%-8d %-20s %-8c %-10s", gui, count, replaceToString(listStack), a, sentence.substring(step));
                    }
                }
            }
        }
        //移进
        if (matrix.get(listStack.get(j) + "" + a).equals('<') || matrix.get(listStack.get(j) + "" + a).equals('=')) {
            count++;
            k++;
            status = 0;
            listStack.add(a);
        } else {
            switch (matrix.get(listStack.get(j) + "" + a)) {
                case 'a':
                    System.out.print("非法左括号! ");
                    return;
                case 'b':
                    System.out.print("缺少运算符! ");
                    return;
                case 'c':
                    System.out.print("缺少表达式! ");
                    return;
                default:
                    break;
            }
        }
        if (listStack.size() == 2 && a == '#') {
            break;
        }
        if (step < sentence.length()) {
            a = sentence.charAt(step++);
        } else {
            break;
        }
    } while (listStack.size() != 2 || a != '#');
    //结束条件：栈的长度位2（#E），读入的位#（最后一个字符）
    System.out.printf("%-8s\n", "分析成功");
}
/**
 * @description ：主函数
 * @author：hugang
 * @date ：2020/5/19 10:03
 * @param ：args
 * @return
 */
public static void main(String[] args) {
    input.add("E->E+T|E-T|T");
    input.add("T->T*F|T/F|F");
    input.add("F->P^F|P");
    input.add("P->(E)|i");
    //判断是否是算法优先文法
    if (isOperator()) {
        System.out.println("此文法是算符文法!");
    } else {
        System.out.println("此文法不是算符文法!请重新输入:");
    }
    //计算终结符
    getEnd();
    //计算非终结符
    getNoEnd();
    //将每一行的文法分离,如E->E+T|T分离成E和E+T,T
    getProduce();
    //显示FirstVt和LastVT
    DisplayFirstVT_LastVT();
    //构造算符优先矩阵并打印
    priorityMatrix();
    //分析句子
    analysisProcess();
}
}

