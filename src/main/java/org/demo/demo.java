package org.demo;

import javax.swing.*;
import java.awt.*;

public class demo {

    public static void main(String[] args) {
        AcrylicEffectJFrame frame = new AcrylicEffectJFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Demo");
        frame.setTitlebarHeight(30);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setLayout(null);
        JLabel label = new JLabel();
        label.setBounds(241, 275, 318, 50);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 35));
        JButton button = new JButton("点我");
        button.setBounds(330, 285, 140, 30);
        button.setBackground(new Color(221, 232, 243, 90));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.addActionListener(e -> {
            frame.flashWindow(true);
            label.setText("你好！别来无恙啊！");
            frame.getContentPane().remove(button);
        });

        frame.setResizable(true);
        frame.getContentPane().add(button);
        frame.getContentPane().add(label);
        frame.setVisible(true);
    }
}
