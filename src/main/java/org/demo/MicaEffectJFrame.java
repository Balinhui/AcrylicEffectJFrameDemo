package org.demo;

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
    /**
     * 储存窗口的句柄，在窗口显示可见时获取值
     */
    private HWND hwnd;
    /**
     * 窗口的标题栏，负责拖动和存放`Title`和三个控制按钮
     */
    private final JLabel titleBar = createTitleBar();
    private final JButton exit = createControlButton("\uE653", 1, e -> System.exit(0));
    private final JButton max = createControlButton("\uE655",2, e -> toggleMaximize());
    private final JButton mix = createControlButton("\uE654", 3, e -> setExtendedState(ICONIFIED));
    /**
     * 窗口的根面板
     */
    private final JPanel ContentPane = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(255, 255, 255, 1));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    };

    /**
     * 窗口状态的取值，1为空白面板，3为亚克力，4为Win11云母效果
     */
    private int AccentState = 4;
    /**
     * 记录窗口是否在焦点的值
     */
    private boolean onFocus = true;
    /**
     * 记录窗口是否最大化的值
     */
    private boolean onMax = false;

    private static final Color notOnFocus = new Color(141, 142, 142);

    static {
        System.setProperty("jna.encoding", "UTF-8");
        if (!System.getProperty("os.name").startsWith("Windows")) {
            throw new RuntimeException("系统不支持");
        }
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
        accent.nAccentState = AccentState;
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

    public MicaEffectJFrame(String title) {
        this.setTitle(title);
        initializeUI();
    }

    private void initializeUI() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        addWindowStateListener(e -> {
            if (!onMax) {
                onMax = true;
                max.setText("\uE656");
            } else {
                onMax = false;
                max.setText("\uE655");
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
                exit.setForeground(Color.BLACK);
                max.setForeground(Color.BLACK);
                mix.setForeground(Color.BLACK);
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                AccentState = 1;
                applyMicaEffect();
                onFocus = false;
                titleBar.setForeground(notOnFocus);
                exit.setForeground(notOnFocus);
                max.setForeground(notOnFocus);
                mix.setForeground(notOnFocus);
            }
        });

        ContentPane.setOpaque(false);
        setContentPane(ContentPane);
    }

    private JButton createControlButton(String text, int style, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe Fluent Icons", Font.PLAIN, 10));
        button.setForeground(Color.BLACK);
        button.setBackground((style == 1) ? new Color(186, 41, 27) : new Color(219, 219, 219));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.addActionListener(action);

        // 悬停效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setContentAreaFilled(true);
                if (style == 1) {
                    button.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setContentAreaFilled(false);
                if (style == 1) {
                    if (onFocus) {
                        button.setForeground(Color.BLACK);
                    } else {
                        button.setForeground(notOnFocus);
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

    /**
     * 在确保窗口已经创建好后为其添加组件以及获取句柄，并为其启用效果
     */
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

    /**
     * 防止修改`undecorated`参数
     * @param undecorated {@code true} if no frame decorations are to be
     *         enabled; {@code false} if frame decorations are to be enabled
     *
     */
    @Override
    public void setUndecorated(boolean undecorated) {
        super.setUndecorated(true);
    }

    /**
     * 禁止改变大小时，禁用`max`按钮
     * @param resizable   {@code true} if this frame is resizable;
     *                       {@code false} otherwise.
     */
    @Override
    public void setResizable(boolean resizable) {
        super.setResizable(resizable);
        max.setEnabled(resizable);
    }

    /**
     * `height + 28`是为了给标题栏留出位置
     * @param width the new width of this component in pixels
     * @param height the new height of this component in pixels
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height + 28);
    }
}
