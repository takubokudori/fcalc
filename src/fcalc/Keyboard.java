package fcalc;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

interface KeyboardConstant {
    char[] keys = new char[]{'0'};
    long INTERVAL = 100;
}

public class Keyboard extends InputStream {
    private LinkedList<Integer> queue = new LinkedList<Integer>();

    public Keyboard() {
        super();
    }

    public int available() throws IOException {
        return 0;
    }

    @Override
    public int read() throws IOException {
        if (queue.isEmpty()) return -1;
        int t = queue.remove();
        return t;
    }

    public void scan(String str) {
        // リストにプッシュしていく
        queue.clear(); // ストリーム内を空にする
        for (int i = 0; i < str.length(); i++) {
            queue.addLast((int) str.charAt(i));
        }
    }

    // デバッグ用
    public void show() {
        System.out.print("remains:");
        for (int c : queue) System.out.print(c + " ");
        System.out.println();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int c;
        int i = 0;
        while ((c = read()) != -1) {
            b[i++] = (byte) c;
        }
        b[i] = -1;
        return i;
    }
}
