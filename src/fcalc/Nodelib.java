package fcalc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

abstract class Node {
    // 抽象クラス
    abstract public void setOp(char c);

    abstract public void addLeft(Node n);

    abstract public void addRight(Node n);

    abstract public Node assign(final Node n);

    abstract public void setMinus(boolean f);

    abstract public Poly calc();
}

class Gvar extends Node {
    // グローバル変数
    static HashMap<Character, Poly> globalv_table = new HashMap<Character, Poly>();
    private char varname;
    boolean minusf = false;

    public Gvar(char c) {
        varname = c;
    }

    @Override
    public void setOp(char c) {
    }

    @Override
    public void addLeft(Node n) {
    }

    @Override
    public void addRight(Node n) {
    }

    public static void clear() {
        globalv_table.clear();
    }

    public static String getGvarList() {
        String str = "";
        Set<Character> keys = globalv_table.keySet();
        for (char k : keys) {
            str += "$" + k + "=" + globalv_table.get(k) + "\n";
        }
        return str;
    }

    @Override
    public Poly calc() {
        if (globalv_table.containsKey(varname)) {
            Poly p = new Poly(globalv_table.get(varname));
            if (minusf) p = p.mul(new Poly(-1));
            return p;
        } else throw new RuntimeException("未定義のグローバル変数 $" + varname);
    }

    @Override
    public Node assign(Node n) {
        if (!n.calc().isDouble()) throw new RuntimeException("グローバル変数に数値以外は入れられません");
        globalv_table.put(varname, n.calc());
        return this;
    }

    @Override
    public void setMinus(boolean f) {
        minusf = f;
    }
}

class Op extends Node {
    public static Node connectToLeft(Node n) {
        // 左の子にnが設定されたオペランドノードを返す
        final Op result = new Op();
        result.left = n;
        return result;
    }

    public Op() {
    }

    private char op;
    private Node left;
    private Node right;

    @Override
    public void setOp(char c) {
        op = c;
    }

    @Override
    public void addLeft(Node n) {
        left = n; // 左に設定する
    }

    @Override
    public void addRight(Node n) {
        right = n; // 右に設定する
    }

    @Override
    public Poly calc() {
        Poly lf = null, rf = null;
        if (op == '=') return (left.assign(right)).calc();
        if (left != null) lf = left.calc();
        if (right != null) rf = right.calc();
        switch (op) {
            case '+':
                return lf.add(rf);
            case '-':
                return lf.sub(rf);
            case '*':
                return lf.mul(rf);
            case '/':
                return lf.div(rf);
            case '^':
                return lf.pow(rf);
        }
        return lf;
    }

    @Override
    public Node assign(Node n) {
        return null;
    }

    @Override
    public void setMinus(boolean f) {
    }

}

class Term implements Comparable<Term> {
    // 項型 2x^2y^3を管理する．
    // 2x^(x+2x^2)y^3
    private int state = 0; // 0=Poly 1=定数a 2=1次式<Poly>x
    private double num;
    private char var;
    private Term coe = null;
    private Poly exp = null; // 指数部(exponantial)
    private Poly man = null; // 仮数部(mantissa)

    public Term() {
    }

    // Cv^(pow)作成
    public Term(double c, char v, double pow) {
        this(new Term(c), new Poly(v), new Poly(pow));
    }

    public Term(double c, final Poly v, final Poly e) {
        this(new Term(c), new Poly(v), new Poly(e));
    }

    public Term(Term c, char v, double pow) {
        this(new Term(c), new Poly(v), new Poly(pow));
    }

    public Term(Term c, char v, Poly pow) {
        this(new Term(c), new Poly(v), new Poly(pow));
    }

    public Term(Term c, final Poly v, final Poly e) {
        coe = new Term(c);
        man = new Poly(v);
        exp = new Poly(e);
    }

    public Term(double a) {
        state = 1;
        num = a;
    }

    public Term(char x) {
        coe = new Term(1);
        state = 2;
        var = x;
        exp = new Poly(1);
    }

    public Term(final Term clone) {
        this.state = clone.state;
        if (state == 0) {
            this.coe = new Term(clone.coe);
            this.man = new Poly(clone.man);
            this.exp = new Poly(clone.exp);
        } else if (state == 1) {
            this.num = clone.num;
        } else {
            if (clone.coe.match(1)) this.coe = new Term(1);
            else this.coe = clone.coe;
            this.var = clone.var;
            if (clone.exp.match(1)) this.exp = new Poly(1);
            else this.exp = clone.exp;
        }
    }

    public Term getCoefficientp() {
        Term t = this;
        while (!t.isDouble()) t = t.coe;
        return t;
    }

    public Poly add(final Term n) {
        Poly t = new Poly();
        if (this.termmatch(n)) {
            if (this.isDouble()) t.addterm(new Term(this.num + n.num));
            else if (this.isChar()) {
                if (n.isChar()) {
                    if (this.getChar() == n.getChar()) t.addterm(new Term(this.coe.num + n.coe.num, this.var, 1));
                    else {
                        t.addterm(new Term(this));
                        t.addterm(new Term(n));
                    }
                } else {
                    if (n.man.isChar() && this.getChar() == n.man.getChar())
                        t.addterm(new Term(this.coe.num + n.coe.num, this.var, 1));
                    else {
                        t.addterm(new Term(this));
                        t.addterm(new Term(n.man.getChar()));
                    }
                }
            } else {
                // Poly型一致 2xy+3xy
                Term t1 = new Term(this);
                Term tp;
                Term t2 = new Term(n);
                tp = t1.getCoefficientp();
                t2 = t2.getCoefficientp();
                if (!(tp.isDouble() && t2.isDouble())) System.out.println("aaa");
                tp.num = tp.num + t2.num;
                t.addterm(new Term(t1));
            }
        } else {
            t.addterm(new Term(this));
            t.addterm(new Term(n));
        }
        return t;
    }


    public Poly sub(final Term n) {
        return new Poly(this.add(n.invert()));
    }

    public Term invert() {
        Term tmp = new Term(this);
        Term p = tmp.getCoefficientp();
        p.num = -p.num; // 符号反転
        return tmp;
    }

    public Term reci() {
        // 逆数にして返す
        if (this.isDouble()) return new Term(1.0 / num);
        else if (this.isChar()) return new Term(coe.reci(), var, exp.invert());
        else return new Term(coe.reci(), man, exp.invert());
    }

    public void setPoly(Term c, Poly m, Poly e) {
        state = 0;
        coe = new Term(c);
        man = new Poly(m);
        exp = new Poly(e);
    }

    public void setPoly(Term c, char m, Poly e) {
        state = 0;
        coe = new Term(c);
        man = new Poly(m);
        exp = new Poly(e);
    }

    public void setChar(char x) {
        state = 2;
        var = x;
        exp = new Poly(1);
        coe = new Term(1);
    }

    public void setDouble(double x) {
        state = 1;
        num = x;
    }

    public int isState() {
        return state;
    }

    public boolean isChar() {
        return state == 2;
    }

    public char getChar() {
        if (isChar()) return var;
        return '@';
    }

    public boolean isDouble() {
        return state == 1;
    }

    public double getDouble() {
        if (isDouble()) return num;
        return 9999;
    }

    private Term getMatchMaintissap(Term p) {
        // pと仮数が一致する項のポインタを返す．
        // 見つからない場合または最後に一致した場合は存在するはずだった位置の前のポインタを返す
        Term t = this;
        Term prev = this;
        while (t.coe != null && t.compareTo(p) > 0) {
            if (t.manmatch(p)) return t;
            prev = t;
            t = t.coe;
        }
        if (t.manmatch(p)) return t;
        return prev;
    }

    public Term mul(final Term n) {
        if (this.isDouble()) {
            if (n.isDouble()) return new Term(this.num * n.num); // 3*2=6
            else if (n.isChar()) {
                return new Term(this.num, n.var, 1); // 3*x
            } else {
                // 2 * xy
                Term p = new Term(n);
                Term pd = p.getCoefficientp(); // 3
                pd.num = this.num * pd.getDouble(); // 係数操作
                return new Term(p);
            }
        } else if (this.isChar()) {
            if (n.isDouble()) return new Term(this.getCoefficientp().num * n.num, this.var, 1); // x*2=2x
            else if (n.isChar()) {
                if (this.termmatch(n))
                    return new Term(this.getCoefficientp().num * n.getCoefficientp().num, new Poly(this.var), this.exp.add(n.exp)); // x*x=x^2
                else if (this.compareTo(n) < 0)
                    return new Term(new Term(this.getCoefficientp().num * n.getCoefficientp().num, new Poly(this.var), this.exp), n.var, n.exp); // x*y=xy
                else
                    return new Term(new Term(this.getCoefficientp().num * n.getCoefficientp().num, new Poly(n.var), n.exp), new Poly(this.var), this.exp); // y*x=xy
            } else {
                Term p = new Term(n);
                Term p1 = p.getMatchMaintissap(this);
                if (p1.manmatch(this)) {
                    p1.exp = new Poly(p1.exp.add(new Poly(1)));
                } else {
                    if (p1.man != null && p1.man.compareTo(new Poly(this)) < 0) p = new Term(p, this.var, 1);
                    else p1.coe = new Term(p1.coe, this.var, 1);
                }
                return p;
            }
        } else {
            if (n.isDouble()) {
                Term p = new Term(this);
                Term pd = p.getCoefficientp(); // 3
                pd.num = n.num * pd.getDouble(); // 係数操作
                return new Term(p);
            } else if (n.isChar()) {
                Term p = new Term(this);
                Term p1 = p.getMatchMaintissap(n);
                if (p1.manmatch(n)) {
                    p1.exp = new Poly(p1.exp.add(new Poly(1)));
                    p.getCoefficientp().num = p.getCoefficientp().num * n.getCoefficientp().num;
                } else {
                    if (p1.man != null && p1.man.compareTo(new Poly(n)) < 0) p = new Term(p, n.var, 1);
                    else p1.coe = new Term(p1.coe, n.var, 1);
                }
                return p;
            } else {
                Term p = new Term(this);
                Term p1 = p.getMatchMaintissap(n);
                if (p1.manmatch(n)) {
                    p1.exp = new Poly(p1.exp.add(new Poly(n.exp)));
                } else {
                    if (p1.man != null && p1.man.compareTo(new Poly(n)) < 0) p1.setPoly(p1, new Poly(n), new Poly(1));
                    else p1.coe = new Term(p1.coe, new Poly(n), new Poly(1));
                }
                return p;
            }
        }
    }

    public Term div(final Term n) {
        if (this.isDouble()) {
            if (n.isDouble()) {
                if (n.num == 0) throw new RuntimeException("ゼロ除算が発生しました");
                return new Term(this.num / n.num); // 3/2=1.5
            } else if (n.isChar()) {
                return new Term(this.num, n.var, -1); // 3/x
            } else {
                Term p = new Term(n.reci());
                Term pd = p.getCoefficientp(); // 3
                pd.num = this.num / pd.getDouble(); // 係数操作
                return new Term(p);
            }
        } else if (this.isChar()) {
            if (n.isDouble()) {
                if (n.num == 0) throw new RuntimeException("ゼロ除算が発生しました");
                return new Term(1.0 / n.num, this.var, 1);
            } else if (n.isChar()) {
                if (this.compareTo(n) == 0) return new Term(1);
                else if (this.compareTo(n) < 0) return new Term(new Term(this.var), n.var, -1);
                else return new Term(new Term(n.coe, n.var, n.exp.invert()), this.var, 1);
            } else {
                Term p = new Term(n.reci());
                Term p1 = p.getMatchMaintissap(this);
                if (p1.manmatch(this)) {
                    p1.exp = new Poly(p1.exp.add(new Poly(1)));
                } else {
                    if (p1.man != null && p1.man.compareTo(new Poly(this)) < 0) p = new Term(p, this.var, 1);
                    else p1.coe = new Term(p1.coe, this.var, 1);
                }
                return p;
            }
        } else {
            if (n.isDouble()) {
                Term p = new Term(this);
                Term pd = p.getCoefficientp();
                if (pd.getDouble() == 0) throw new RuntimeException("ゼロ除算が発生しました");
                pd.num = n.num / pd.getDouble(); // 係数操作
                return new Term(p);
            } else if (n.isChar()) {
                Term p = new Term(this.reci());
                Term p1 = p.getMatchMaintissap(n);
                if (p1.manmatch(n)) {
                    p1.exp = new Poly(p1.exp.add(new Poly(1)));
                } else {
                    if (p1.man != null && p1.man.compareTo(new Poly(n)) < 0) p = new Term(p, n.var, -1);
                    else p1.coe = new Term(p1.coe, n.var, -1);
                }
                return p;
            } else {
                Term p = new Term(this.reci());
                Term p1 = p.getMatchMaintissap(n);
                if (p1.manmatch(n)) {
                    p1.exp = new Poly(p1.exp.add(new Poly(1)));
                } else {
                    if (p1.man != null && p1.man.compareTo(new Poly(this)) < 0) p = new Term(p, this.var, -1);
                    else p1.coe = new Term(p1.coe, this.var, -1);
                }
                return p;
            }
        }
    }

    public boolean isMono() {
        // coeに値が入っていない
        if (isDouble()) return true;
        else if (isChar()) return coe.match(1);
        else return coe.match(1) && exp.match(1);
    }

    public void aggregate() {
        if (this.getCoefficientp().num == 0) {
            state = 1;
            num = 0;
        } else if (this.state == 2 || this.state == 0) {
            if (this.exp.match(0)) {
                state = 1;
                num = 1;
            } else if (this.state == 0) {
                if (this.coe.match(1) && this.exp.match(new Poly(1))) {
                    if (this.man.isChar()) {
                        state = 2;
                        var = this.man.getChar();
                    } else if (this.man.isDouble()) {
                        state = 1;
                        num = this.man.getDouble();
                    }
                } else if (this.man.match(1)) {
                    this.man = new Poly(this.coe);
                }
            }
        }
    }


    @Override
    public String toString() {
        String str = "";
        if (isDouble()) str += num;
        else {
            if (!coe.match(1)) {
                if (coe.match(-1)) str += "-";
                else str += coe;
            }
            if (isChar()) str += var;
            else str += man;
            if (!exp.match(1)) str += "^" + exp;
        }
        return str;
    }

    public Poly Poly() {
        return new Poly(this);
    }

    @Override
    public int compareTo(Term o) {
        if (this.isDouble()) {
            if (o.isDouble()) {
                if (this.num > o.num) return 1;
                else if (this.num == o.num) return 0;
                else return -1;
            }
            return -1;
        }
        if (this.isChar()) {
            if (o.isDouble()) return 1;
            if (o.isChar()) {
                if (this.var > o.var) return 1;
                else if (this.var == o.var) return 0;
                return -1;
            } else {
                if (o.coe.isDouble()) {
                    if ((new Poly(this.var).match(o.man))) {
                        return (new Poly(1)).compareTo(o.exp);
                    } else {
                        return (new Poly(this.var)).compareTo(o.man);
                    }
                } else return -1;
            }
        }
        if (o.isDouble()) return 1;
        else if (o.isChar()) {
            // 2x x
            if (this.coe.isDouble()) {
                if (this.man.match(o.var)) {
                    return this.exp.compareTo(new Poly(1));
                } else {
                    return this.man.compareTo(new Poly(o.var));
                }
            } else return 1;
        }

        int a = this.man.compareTo(o.man);
        if (a != 0) return a;
        a = this.exp.compareTo(o.exp);
        if (a != 0) return a;
        a = this.coe.compareTo(o.coe);
        if (a != 0) return a;
        return 0; // 完全一致
    }

    public boolean match(Term o) {
        // 係数を含めて一致するかを返す
        if (this.isDouble()) {
            return o.isDouble() && this.num == o.num;
        } else if (this.isChar()) {
            if (o.isDouble()) return false;
            if (this.isChar()) return this.var == o.var;
            return this.coe.match(o.coe) && o.man.match(this.var) && this.exp.match(o.exp);
        } else {
            if (o.isDouble()) {
                return this.match(o.num);
            } else if (o.isChar()) {
                return this.coe.match(o.coe) && o.man.match(this.var) && this.exp.match(o.exp);
            }
            return this.coe.match(o.coe) && this.man.match(o.man) && this.exp.match(o.exp);
        }
    }

    public boolean match(double n) {
        return this.isDouble() && this.getDouble() == n;
    }

    public boolean termmatch(Term o) {
        // 積項のみあっているかを返す
        if (this.isDouble()) return o.isDouble();
        if (this.isChar()) {
            if (o.isDouble()) return false;
            else if (o.isChar()) return this.var == o.var;
            if (o.coe.isDouble() && o.exp.match(1)) {
                return o.man.match(this.var);
            }
            return false;
        }
        if (o.isDouble()) return false;
        else if (o.isChar()) {
            if (this.coe.isDouble() && this.exp.match(1)) {
                return this.man.match(o.var);
            } else {
                return !this.isChar() && this.coe.termmatch(o.coe) && this.man.match(o.var) && this.exp.match(o.exp);
            }
        }
        return this.coe.termmatch(o.coe) && this.man.match(o.man) && this.exp.match(o.exp);
    }

    public boolean manmatch(Term o) {
        if (this.isDouble()) return o.isDouble();

        if (this.isChar()) {
            if (this.isDouble()) return false;
            else if (this.isChar()) return this.var == o.var;
            if (o.coe.isDouble() && o.exp.match(1)) {
                return o.man.match(this.var);
            }
        }
        if (o.isDouble()) return false;
        else if (o.isChar()) return this.man.match(o.var);
        else return this.man.termmatch(o.man);
    }

    public Poly expand(final HashMap<Character, Poly> l) {
        if (state == 0) {
            Poly c = coe.expand(l);
            Poly m = man.expand(l);
            Poly e = exp.expand(l);
            return new Poly(c.mul(m.pow(e)));
        } else if (state == 1) {
            return new Poly(this);
        } else {
            if (l.containsKey(var)) {
                Poly m = new Poly(l.get(var));
                Poly c = coe.expand(l);
                Poly e = exp.expand(l);
                return new Poly(c.mul(m.pow(e)));
            } else return new Poly(this);
        }
    }

    public Poly getman() {
        return man;
    }

}

class Poly extends Node implements Comparable<Poly> {
    // 多項式型 2x+3x^2y^3+4z

    private boolean minusf = false;
    private TreeSet<Term> terms = new TreeSet<Term>();

    public Poly(final TreeSet<Term> terms) {
        this.terms = new TreeSet<Term>(terms);
    }

    public Poly(final Poly clone) {
        if (clone != null) this.terms = new TreeSet<Term>(clone.terms);
    }

    public Poly() {
    }

    // 定数aを追加 a*@^1
    public Poly(double a) {
        terms.add(new Term(a));
    }

    // 1次文字式cを追加
    public Poly(char c) {
        terms.add(new Term(c));
    }

    public Poly(Term term) {
        terms.add(new Term(term));
    }

    public Term first() {
        return terms.first();
    }

    @Override
    public String toString() {
        String str = "";
        Iterator<Term> it = terms.iterator();
        if (it.hasNext()) str += "(" + it.next();
        while (it.hasNext()) str += "+" + it.next();
        str += ")";
        return str;
    }

    @Override
    public void setOp(char c) {
    }

    @Override
    public void addLeft(Node n) {
    }

    @Override
    public void addRight(Node n) {
    }

    @Override
    public Poly calc() {
        Poly p = new Poly(this);
        if (minusf) p = p.invert();
        return p;
    }

    public boolean compareDouble(double x) {
        return isDouble() && terms.first().getDouble() == x;
    }

    public void addterm(Term t) {
        this.terms.add(t);
    }

    public void addterms(Poly p) {
        for (Term t : p.terms) terms.add(t);
    }

    public Poly invert() {
        Poly tmp = new Poly();
        for (Term t : terms) tmp.addterm(t.invert());
        return tmp;
    }

    public Poly reci() {
        if (this.isMono()) return new Poly(this.first().reci());
        Term t = new Term(new Term(1), this, new Poly(-1));
        return new Poly(t);
    }

    public Poly expand(final HashMap<Character, Poly> l) {
        Poly tmp = new Poly();
        for (Term t : terms) {
            tmp = tmp.add(new Poly(t.expand(l)));
        }
        return tmp;
    }

    public boolean isMono() {
        return terms.size() == 1;
    }

    public Poly add(final Poly n) {
        // (2a^1+3t^2)+(2a+4c+3d)
        Iterator<Term> it1 = this.terms.iterator();
        Iterator<Term> it2 = n.terms.iterator();
        Term t1, t2;
        Poly tmp = new Poly();
        if (it1.hasNext() && it2.hasNext()) {
            t1 = it1.next();
            t2 = it2.next();
        } else {
            while (it1.hasNext()) tmp.terms.add(new Term(it1.next()));
            while (it2.hasNext()) tmp.terms.add(new Term(it2.next()));
            return tmp;
        }
        boolean f1 = true;
        boolean f2 = true;
        while (it1.hasNext() || it2.hasNext()) {
            if (t1.termmatch(t2)) {
                tmp.addterms(t1.add(t2));
                f1 = f2 = false;
                if (it1.hasNext()) {
                    t1 = it1.next();
                    f1 = true;
                }
                if (it2.hasNext()) {
                    t2 = it2.next();
                    f2 = true;
                }
            } else {
                if (t1.compareTo(t2) < 0) {
                    tmp.terms.add(t1);
                    f1 = false;
                    if (it1.hasNext()) {
                        t1 = it1.next();
                        f1 = true;
                    } else break;
                } else {
                    tmp.terms.add(t2);
                    f2 = false;
                    if (it2.hasNext()) {
                        t2 = it2.next();
                        f2 = true;
                    } else break;
                }
            }
        }
        if (f1 && f2) {
            if (t1.termmatch(t2)) tmp.addterms(t1.add(t2));
            else {
                tmp.terms.add(t1);
                tmp.terms.add(t2);
            }
        } else {
            if (f1) tmp.terms.add(t1);
            if (f2) tmp.terms.add(t2);
        }
        while (it1.hasNext()) tmp.terms.add(it1.next());
        while (it2.hasNext()) tmp.terms.add(it2.next());
        tmp.aggregate();
        return tmp;
    }

    public void aggregate() {
        REF:
        for (Term t : terms) {
            t.aggregate();
            if (terms.size() > 1) {
                if (t.isDouble() && t.getDouble() == 0) {
                    if (!terms.remove(t)) terms.pollLast();
                    break REF;
                } else if (t.isMono() && !(t.isDouble() || t.isChar())) {
                    Poly q = new Poly(t.getman());
                    if (!terms.remove(t)) terms.pollLast();
                    for (Term tmp : q.terms) terms.add(tmp);
                    break REF;
                }
            }
        }
    }

    public Poly sub(final Poly n) {
        Poly tmp = this.add(n.invert());
        tmp.aggregate();
        return tmp;
    }

    public Poly generateMono() {
        // Poly型から1*Poly^1の単型Polyを生成する
        if (isDouble()) return new Poly(getDouble());
        else if (isChar()) return new Poly(getChar());
        if (isMono()) return this;
        return new Poly(new Term(new Term(1), this, new Poly(1)));
    }

    public Poly mul(Poly n) {
        Poly tmp = new Poly();
        for (Term t1 : this.terms) {
            for (Term t2 : n.terms) {
                tmp = tmp.add(new Poly(t1.mul(t2)));
            }
        }
        tmp.aggregate();
        return tmp;
    }

    public Poly div(Poly n) {
        Poly tmp = n.reci();
        if (n.match(0)) throw new RuntimeException("ゼロ除算が発生しました");
        tmp = tmp.mul(this);
        tmp.aggregate();
        return tmp;
    }

    public boolean isDouble() {
        if (this.terms.size() != 1) return false;
        return this.terms.first().isDouble();
    }

    public double getDouble() {
        if (this.isDouble()) return terms.first().getDouble();
        return 0;
    }

    public boolean isChar() {
        if (this.terms.size() != 1) return false;
        return this.terms.first().isChar();
    }

    public char getChar() {
        if (this.isChar()) return terms.first().getChar();
        return 0;
    }

    public Poly pow(Poly n) {
        Poly tmp = new Poly();
        if (this.match(0) && n.isDouble() && n.getDouble() < 0) throw new RuntimeException("ゼロ除算が発生しました");
        if (n.match(0)) return new Poly(1);
        if (isDouble() && n.isDouble()) {
            Term t = new Term(Math.pow(this.getDouble(), n.getDouble()));
            tmp.addterm(t);
        } else {
            Term t = new Term(1, new Poly(this), new Poly(n));
            tmp.addterm(t);
        }
        tmp.aggregate();
        return tmp;
    }

    @Override
    public Node assign(Node n) {
        throw new RuntimeException("グローバル変数または関数にのみ代入可能です");
    }

    @Override
    public int compareTo(Poly o) {
        if (this.terms.size() > o.terms.size()) return 1;
        else if (this.terms.size() < o.terms.size()) return -1;
        Iterator<Term> it1 = this.terms.iterator();
        Iterator<Term> it2 = o.terms.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            Term t1 = it1.next();
            Term t2 = it2.next();
            if (t1.compareTo(t2) != 0) return t1.compareTo(t2);
        }
        return 0;
    }

    public boolean match(Poly o) {
        // this.terms内にすべて同じのがあるか
        Iterator<Term> it = o.terms.iterator();
        for (Term t : this.terms) {
            if (!it.hasNext()) return false;
            if (!t.match(it.next())) return false;
        }
        return true;
    }

    public boolean match(double n) {
        return isDouble() && getDouble() == n;
    }

    public boolean match(char x) {
        return isChar() && getChar() == x;
    }

    public boolean termmatch(Poly o) {
        // this.terms内に係数以外同じのがあるか
        if (o.terms == null) return false;
        Iterator<Term> it = o.terms.iterator();
        for (Term t : this.terms) {
            if (!it.hasNext()) return false;
            if (!t.termmatch(it.next())) return false;
        }
        return true;
    }

    @Override
    public void setMinus(boolean f) {
        minusf = f;
    }

}

class Func extends Node {
    // 引数文字リスト<関数名,引数リスト> f,(x,y,z)
    static HashMap<Character, LinkedList<Character>> args_list = new HashMap<Character, LinkedList<Character>>();
    // 関数名,多項式 f,3x+4xy+5z
    static HashMap<Character, Poly> func_poly = new HashMap<Character, Poly>();
    boolean minusf = false;
    char fname;
    LinkedList<Poly> args = new LinkedList<Poly>(); // 引数ノードをポリ型に変換して入れる

    // useもdeclareも含む
    public Func(char fname, LinkedList<Node> args) {
        this.fname = fname;
        for (Node n : args) this.args.add(n.calc());
    }

    public void setMinus(boolean f) {
        minusf = f;
    }


    public static void clear() {
        args_list.clear();
        func_poly.clear();
    }

    public static String getFuncList() {
        String str = "";
        Set<Character> keys = args_list.keySet();
        for (char k : keys) {
            str += "@" + k + "(";
            Iterator<Character> it = args_list.get(k).iterator();
            if (it.hasNext()) str += it.next();
            while (it.hasNext()) str += "," + it.next();
            str += ")=" + func_poly.get(k) + "\n";
        }
        return str;
    }

    @Override
    public void setOp(char c) {
    }

    @Override
    public void addLeft(Node n) {
    }

    @Override
    public void addRight(Node n) {
    }

    @Override
    public Poly calc() {
        // 返す前に変換をする
        if (func_poly.containsKey(fname)) {
            LinkedList<Character> a = args_list.get(fname);
            Iterator<Character> it1 = a.iterator();
            Iterator<Poly> it2 = args.iterator();
            HashMap<Character, Poly> l = new HashMap<Character, Poly>();
            if (a.size() != args.size()) throw new RuntimeException("関数の引数が一致しません");
            while (it1.hasNext() && it2.hasNext()) {
                l.put(it1.next(), it2.next());
            }
            Poly p = new Poly(func_poly.get(fname).expand(l));
            if (minusf) p = p.invert();
            return p;
        } else throw new RuntimeException("未定義の関数 @" + fname);
    }

    @Override
    public Node assign(Node n) {
        func_poly.put(fname, n.calc());
        LinkedList<Character> v = new LinkedList<Character>();
        for (Poly p : args) {
            if (p != null && p.isChar() && !v.contains(p.getChar())) v.add(p.getChar());
            else throw new RuntimeException("関数の引数が不正です．");
        }
        args_list.put(fname, v);
        return this;
    }

}