package fcalc;
/*
 * GUI電卓機能
 * https://github.com/takubokudori/fcalc
 */

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class Calculator extends JFrame {
    private static final String[] NAME_numbtn = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private static final String[] NAME_opbtn = {"+", "-", "*", "/", "^", "=", "(", ")", "."};
    private static final String[] NAME_combtn = {"EXECUTE", "MEMORY_RESET", "GVAR_RESET", "FUNC_RESET"};
    private static final int NUM_numbtn = NAME_numbtn.length;
    private static final int NUM_opbtn = NAME_opbtn.length;
    private static final int NUM_combtn = NAME_combtn.length;
    private Keyboard key = new Keyboard();
    JPanel inputpanel = new JPanel(new BorderLayout()); // 入力テキストフィールド
    JPanel outputpanel = new JPanel(new BorderLayout()); // 出力テキストフィールド
    JPanel operationpanel = new JPanel(new BorderLayout());
    JPanel buttonpanel = new JPanel(new GridBagLayout()); // ボタンフィールド
    JPanel funcpanel = new JPanel(new BorderLayout());
    JDialog func_dlg;
    JPanel gvarpanel = new JPanel(new BorderLayout());
    JPanel commandpanel = new JPanel(new GridBagLayout());
    JTextArea inputfield;
    JTextArea outputfield;
    JTextArea funcfield;
    JTextArea gvarfield;

    public static void main(String[] args) {
        Calculator frame = new Calculator("関数電卓 FX-t4kb09dor1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public Calculator(String title) {
        setTitle(title);
        setBounds(10, 10, 650, 800); // サイズ設定
        setLayout(new BorderLayout());
        func_dlg = new JDialog(this, "関数", true);
        operationpanel.add(buttonpanel, BorderLayout.PAGE_START);
        operationpanel.add(commandpanel, BorderLayout.CENTER);
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.insets = new Insets(3, 15, 10, 15); // 上左下右
        inputfield = new JTextArea(10, 20);
        outputfield = new JTextArea(10, 20);
        funcfield = new JTextArea(52, 10);
        gvarfield = new JTextArea(52, 10);

        inputpanel.add(new JLabel("入力", SwingConstants.CENTER), BorderLayout.PAGE_START);
        inputpanel.add(new JScrollPane(inputfield), BorderLayout.CENTER);
        outputpanel.add(new JLabel("出力", SwingConstants.CENTER), BorderLayout.PAGE_START);
        outputpanel.add(new JScrollPane(outputfield), BorderLayout.CENTER);
        funcpanel.add(new JLabel("関数一覧", SwingConstants.CENTER), BorderLayout.PAGE_START);

        funcpanel.add(new JScrollPane(funcfield), BorderLayout.CENTER);

        gvarpanel.add(new JLabel("変数一覧", SwingConstants.CENTER), BorderLayout.PAGE_START);
        gvarpanel.add(new JScrollPane(gvarfield), BorderLayout.CENTER);
        JButton[] num_btn = new JButton[NUM_numbtn]; // 0-9
        JButton[] com_btn = new JButton[NUM_combtn];
        for (int i = 0; i < NUM_numbtn; i++) {
            num_btn[i] = new JButton(NAME_numbtn[i]);
            num_btn[i].setActionCommand(NAME_numbtn[i]);
            num_btn[i].addActionListener(new alis());
        }
        // ボタン追加
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                gbc1.gridx = j;
                gbc1.gridy = i;
                buttonpanel.add(num_btn[i * 3 + j + 1], gbc1);
            }
        }
        gbc1.gridx = 1;
        gbc1.gridy = 3;
        buttonpanel.add(num_btn[0], gbc1); // 0ボタンは例外
        JButton[] op_btn = new JButton[NUM_opbtn]; // +-*/^=()
        for (int i = 0; i < NUM_opbtn; i++) {
            op_btn[i] = new JButton(NAME_opbtn[i]);
            op_btn[i].setActionCommand(NAME_opbtn[i]);
            op_btn[i].addActionListener(new alis());
        }

        gbc1.gridx = 3;
        gbc1.gridy = 0;
        buttonpanel.add(op_btn[0], gbc1);
        gbc1.gridx = 3;
        gbc1.gridy = 1;
        buttonpanel.add(op_btn[1], gbc1);
        gbc1.gridx = 3;
        gbc1.gridy = 2;
        buttonpanel.add(op_btn[2], gbc1);
        gbc1.gridx = 3;
        gbc1.gridy = 3;
        buttonpanel.add(op_btn[3], gbc1);
        gbc1.gridx = 4;
        gbc1.gridy = 0;
        buttonpanel.add(op_btn[4], gbc1);
        gbc1.gridx = 2;
        gbc1.gridy = 3;
        buttonpanel.add(op_btn[5], gbc1);
        gbc1.gridx = 4;
        gbc1.gridy = 1;
        buttonpanel.add(op_btn[6], gbc1);
        gbc1.gridx = 4;
        gbc1.gridy = 2;
        buttonpanel.add(op_btn[7], gbc1);
        gbc1.gridx = 4;
        gbc1.gridy = 3;
        buttonpanel.add(op_btn[8], gbc1);
        gbc1.gridx = 0;
        gbc1.gridy = 4;

        for (int i = 0; i < NUM_combtn; i++) {
            com_btn[i] = new JButton(NAME_combtn[i]);
            com_btn[i].setActionCommand(NAME_combtn[i]);
            com_btn[i].addActionListener(new alis());
        }
        for (int i = 0; i < NUM_combtn; i++) {
            gbc1.gridx = 0;
            gbc1.gridy = i;
            commandpanel.add(com_btn[i], gbc1);
        }

        getContentPane().add(inputpanel, BorderLayout.PAGE_START);
        getContentPane().add(operationpanel, BorderLayout.CENTER);
        getContentPane().add(funcpanel, BorderLayout.EAST);
        getContentPane().add(gvarpanel, BorderLayout.WEST);
        getContentPane().add(outputpanel, BorderLayout.PAGE_END);
    }

    class alis implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String com = e.getActionCommand();
            switch (com) { // java7以降でないと使用不可
                case "EXECUTE": // execute
                    int c = 1;
                    try {
                        outputfield.setText("");
                        String[] inputs = inputfield.getText().split("\n");
                        for (String str : inputs) {
                            key.scan(str);
                            Calcparser parser = new Calcparser(key);
                            final Node tree = parser.start();
                            if (tree != null) {
                                outputfield.append(tree.calc().toString() + "\n");
                            }
                            c++;
                        }
                    } catch (TokenMgrError e1) {
                        e1.printStackTrace();
                        outputfield.append(c + "行目:トークンエラーが発生しました\n");
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                        outputfield.append(c + "行目:構文エラーが発生しました\n");
                    } catch (RuntimeException e1) {
                        e1.printStackTrace();
                        outputfield.append(c + "行目:" + e1.getMessage() + "\n");
                    } finally {
                        funcfield.setText(Func.getFuncList() + "\n");
                        gvarfield.setText(Gvar.getGvarList() + "\n");
                    }
                    break;
                case "MEMORY_RESET":
                    Func.clear();
                    Gvar.clear();
                    funcfield.setText(Func.getFuncList());
                    gvarfield.setText(Gvar.getGvarList());
                    break;
                case "GVAR_RESET":
                    Gvar.clear();
                    gvarfield.setText(Gvar.getGvarList());
                    break;
                case "FUNC_RESET":
                    Func.clear();
                    funcfield.setText(Func.getFuncList());
                    break;
                default:
                    inputfield.append(com);
                    break;
            }

        }
    }
}
