package org.demo;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.W32APIOptions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class MicaEffectJFrame extends JFrame {
    ClassLoader classLoader = MicaEffectJFrame.class.getClassLoader();
    ImageIcon exit_black = new FlatSVGIcon("close_16dp_1F1F1F_FILL0_wght400_GRAD0_opsz20.svg", 20, 20, classLoader);
    ImageIcon exit_white = new FlatSVGIcon("close_16dp_FFFFFF_FILL0_wght400_GRAD0_opsz20.svg", 20, 20, classLoader);
    ImageIcon exit_gray = new FlatSVGIcon("close_16dp_7A7A7A_FILL0_wght400_GRAD0_opsz20.svg", 20, 20, classLoader);
    ImageIcon max1_black = new FlatSVGIcon("check_box_outline_blank_16dp_000000_FILL0_wght400_GRAD0_opsz20.svg", 14, 14, classLoader);
    ImageIcon max1_gray = new FlatSVGIcon("check_box_outline_blank_16dp_7A7A7A_FILL0_wght400_GRAD0_opsz20.svg", 14, 14, classLoader);
    ImageIcon max2_black = new FlatSVGIcon("settings_backup_restore_16dp_000000_FILL0_wght400_GRAD0_opsz20.svg", 17, 17, classLoader);
    ImageIcon max2_gray = new FlatSVGIcon("settings_backup_restore_16dp_7A7A7A_FILL0_wght400_GRAD0_opsz20.svg", 17, 17, classLoader);
    ImageIcon mix_black = new FlatSVGIcon("remove_16dp_000000_FILL0_wght400_GRAD0_opsz20.svg", 20, 20, classLoader);
    ImageIcon mix_gray = new FlatSVGIcon("remove_16dp_7A7A7A_FILL0_wght400_GRAD0_opsz20.svg", 20, 20, classLoader);

    private HWND hwnd;
    private final JLabel titleBar = createTitleBar();
    private final JButton exit = createControlButton(1, e -> System.exit(0));
    private final JButton max = createControlButton(2, e -> toggleMaximize());
    private final JButton mix = createControlButton(3, e -> setExtendedState(ICONIFIED));
    private final JPanel ContentPane = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(255, 255, 255, 1));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    };

    int AccentState = 4;
    boolean onFocus = true;
    boolean onMax = false;

    static {
        System.setProperty("jna.encoding", "UTF-8");
    }

    public interface User32 extends Library {
        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);
        void SetWindowCompositionAttribute(HWND hWnd, WindowCompositionAttributeData data);
        int WCA_ACCENT_POLICY = 19;
    }

    public static class AccentPolicy extends Structure {
        public int nAccentState;
        public int nFlags;
        public int nColor;
        public int nAnimationId;

        @Override
        protected List<String> getFieldOrder() {
            return List.of("nAccentState", "nFlags", "nColor", "nAnimationId");
        }
    }

    public static class WindowCompositionAttributeData extends Structure {
        public int Attribute;
        public Pointer Data;
        public int SizeOfData;

        @Override
        protected List<String> getFieldOrder() {
            return List.of("Attribute", "Data", "SizeOfData");
        }
    }

    private void applyMicaEffect() {
        AccentPolicy accent = new AccentPolicy();
        accent.nAccentState = AccentState; //4或3
        accent.nFlags = 0x20;
        accent.nColor = 0x40f3f3f3;
        accent.nAnimationId = 0;
        accent.write();

        WindowCompositionAttributeData data = new WindowCompositionAttributeData();
        data.Attribute = User32.WCA_ACCENT_POLICY;
        data.SizeOfData = accent.size();
        data.Data = accent.getPointer();
        data.write();

        User32.INSTANCE.SetWindowCompositionAttribute(hwnd, data);
    }

    public MicaEffectJFrame() {
        initializeUI();
    }

    private void initializeUI() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        addWindowStateListener(e -> {
            if (!onMax) {
                onMax = true;
                max.setIcon(max2_black);
            } else {
                onMax = false;
                max.setIcon(max1_black);
            }
            addControlButton();
            addTitleBar();
        });

        addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                AccentState = 4;
                applyMicaEffect();
                onFocus = true;
                titleBar.setForeground(Color.BLACK);
                exit.setIcon(exit_black);
                if (onMax) {
                    max.setIcon(max2_black);
                } else {
                    max.setIcon(max1_black);
                }
                mix.setIcon(mix_black);
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                AccentState = 1;
                applyMicaEffect();
                onFocus = false;
                titleBar.setForeground(new Color(122, 122, 122));
                exit.setIcon(exit_gray);
                if (onMax) {
                    max.setIcon(max2_gray);
                } else {
                    max.setIcon(max1_gray);
                }
                mix.setIcon(mix_gray);
            }
        });

        ContentPane.setOpaque(false);
        setContentPane(ContentPane);
    }

    private JButton createControlButton(int style, ActionListener action) {
        JButton button = new JButton();
        button.setOpaque(false);
        if (style == 1) {
            button.setIcon(exit_black);
        } else if (style == 2) {
            button.setIcon(max1_black);
        } else if (style == 3) {
            button.setIcon(mix_black);
        }
        button.setBackground((style == 1) ? new Color(186, 41, 27) : new Color(219, 219, 219));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.addActionListener(action);

        // 悬停效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (style == 1) {
                    button.setIcon(exit_white);
                }
                button.setContentAreaFilled(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setContentAreaFilled(false);
                if (style == 1) {
                    if (onFocus) {
                        button.setIcon(exit_black);
                    } else {
                        button.setIcon(exit_gray);
                    }
                }
            }
        });
        return button;
    }

    int xOld = 0;
    int yOld = 0;
    private JLabel createTitleBar() {
        JLabel label = new JLabel();
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label.setForeground(Color.BLACK);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                xOld = e.getX();
                yOld = e.getY();
            }
        });
        label.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int xOnScreen = e.getXOnScreen();
                int yOnScreen = e.getYOnScreen();
                int xx = xOnScreen - xOld;
                int yy = yOnScreen - yOld;
                setLocation(xx, yy);
            }
        });
        return label;
    }

    private void addControlButton() {
        exit.setBounds(getWidth() - 47, 0, 47, 28);
        max.setBounds(getWidth() - 2 * 47, 0, 47, 28);
        mix.setBounds(getWidth() - 3 * 47, 0, 47, 28);
        ContentPane.add(exit);
        ContentPane.add(max);
        ContentPane.add(mix);
    }

    private void addTitleBar() {
        titleBar.setBounds(7, 0, getWidth() - 163, 28);
        ContentPane.add(titleBar);
    }

    private void toggleMaximize() {
        if ((getExtendedState() & MAXIMIZED_BOTH) == 0) {
            // 保存原始尺寸
            setExtendedState(MAXIMIZED_BOTH);
        } else {
            setExtendedState(NORMAL);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        addControlButton();
        addTitleBar();
        hwnd = new HWND(Native.getComponentPointer(this));
        applyMicaEffect();
    }

    @Override
    public JPanel getContentPane() {
        return ContentPane;
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        this.titleBar.setText(title);
    }

    @Override
    public void setUndecorated(boolean setUndecorated) {
        super.setUndecorated(true);
    }
}
