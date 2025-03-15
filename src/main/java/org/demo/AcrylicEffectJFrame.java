package org.demo;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;

public class AcrylicEffectJFrame extends JFrame {
    /**
     * 储存窗口的句柄，在窗口显示可见时获取值
     * Stores the handle of the window, and gets the value when the window is visible
     */
    private static HWND hWnd;

    /**
     * 窗口的标题栏，负责拖动和存放`Title`和三个控制按钮
     * The title bar of the window, which is responsible for dragging and storing the 'Title' and the three control buttons
     */
    private final JLabel titleBar = createTitleBar();
    private final JButton exit = createControlButton("\uE653", 1, e -> System.exit(0));
    private final JButton max = createControlButton("\uE655",2, e -> toggleMaximize());
    private final JButton mix = createControlButton("\uE654", 3, e -> setExtendedState(ICONIFIED));

    /**
     * 窗口的根面板
     * The root panel of the window
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
     * 记录窗口是否在焦点的值
     * The value of recording whether the window is in focus
     */
    private boolean onFocus = true;

    /**
     * 记录窗口是否最大化的值
     * The value of recording whether the window is maximized
     */
    private boolean onMax = false;

    /**
     * 记录鼠标位置，拖动窗口
     * Record the mouse position, drag the window
     */
    private Point dragStart;

    /**
     * 记录窗口大小
     * Record the size of the window
     */
    private Rectangle startBounds;

    /**
     * 检测边框的灵敏度（像素）
     * Detect the sensitivity of the bezel(pixel)
     */
    private final int resizeMargin = 8;

    protected boolean resizable = true;

    private static final Color notOnFocus = new Color(141, 142, 142);

    static {
        System.setProperty("jna.encoding", "UTF-8");
        if (!System.getProperty("os.name").startsWith("Windows")) {
            throw new RuntimeException("系统不支持");
        }
    }

    public interface DWMAttributes {
        int DWMWA_SYSTEMBACKDROP_TYPE = 38;  // Windows 11 新增的云母材质属性
    }

    public interface DWM_SYSTEMBACKDROP_TYPE {
        @Deprecated
        int DWMSBT_AUTO = 0;               // 系统默认

        @Deprecated
        int DWMSBT_NONE = 1;               // 无材质

        @Deprecated
        int DWMSBT_MAINWINDOW = 2;         // 云母材质（主窗口）
        int DWMSBT_TRANSIENTWINDOW = 3;    // 亚克力材质（弹出窗口）

        @Deprecated
        int DWMSBT_TABBEDWINDOW = 4;       // 标签页材质
    }

    public interface DwmApi extends StdCallLibrary {
        DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class);

        void DwmExtendFrameIntoClientArea(HWND hWnd, MARGINS pMarInset);
        void DwmSetWindowAttribute(HWND hWnd, int dwAttribute, IntByReference pvAttribute, int cbAttribute);
    }

    public static class MARGINS extends Structure {
        public int cxLeftWidth;
        public int cxRightWidth;
        public int cyTopHeight;
        public int cyBottomHeight;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "cxLeftWidth",
                    "cxRightWidth",
                    "cyTopHeight",
                    "cyBottomHeight"
            );
        }
    }

    private void applyAcrylicEffect() {
        if (hWnd == null) {
            throw new NullPointerException("窗口句柄未成功获取");
        }
        MARGINS margins = new MARGINS();
        margins.cxLeftWidth = -1;
        margins.cxRightWidth = -1;
        margins.cyTopHeight = -1;
        margins.cyBottomHeight = -1;

        IntByReference ref = new IntByReference(DWM_SYSTEMBACKDROP_TYPE.DWMSBT_TRANSIENTWINDOW);

        DwmApi.INSTANCE.DwmExtendFrameIntoClientArea(hWnd, margins);

        DwmApi.INSTANCE.DwmSetWindowAttribute(hWnd, DWMAttributes.DWMWA_SYSTEMBACKDROP_TYPE, ref, 4);
    }

    /**
     * 无参构造，将要初始化窗口
     * There is no parameter construction, and initialized the window
     */
    public AcrylicEffectJFrame() {
        initializeUI();
    }

    /**
     * 初始化窗口时为窗口命名
     * Name the window when it is initialized
     * @param title 窗口标题 The title of the window
     */
    public AcrylicEffectJFrame(String title) {
        this.setTitle(title);
        initializeUI();
    }

    public AcrylicEffectJFrame(GraphicsConfiguration gc) {
        super(gc);
        initializeUI();
    }

    public AcrylicEffectJFrame(String title, GraphicsConfiguration gc) {
        super(gc);
        this.setTitle(title);
        initializeUI();
    }

    /**
     * 初始化窗口，为窗口去掉装饰，设置透明背景，添加三个控制按钮，添加监听
     * Initialize the window, remove the decoration from the window,
     * set the transparent background, add three control buttons, and add listeners
     */
    private void initializeUI() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        //窗口最大最小化 max and minimize
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
                onFocus = true;
                titleBar.setForeground(Color.BLACK);
                exit.setForeground(Color.BLACK);
                max.setForeground(Color.BLACK);
                mix.setForeground(Color.BLACK);
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                onFocus = false;
                titleBar.setForeground(notOnFocus);
                exit.setForeground(notOnFocus);
                max.setForeground(notOnFocus);
                mix.setForeground(notOnFocus);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startBounds = getBounds();
                dragStart = e.getLocationOnScreen();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            // 记录鼠标的位置 Record the position of the mouse
            int edge = -1;

            @Override
            public void mouseMoved(MouseEvent e) {
                if (!resizable) {
                    return;
                }
                updateCursor(e.getPoint(), getSize());
                edge = getEdgeType(e.getPoint(), getSize());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (startBounds == null && edge == -1) {
                    return;
                }

                Point current = e.getLocationOnScreen();
                int dx = current.x - dragStart.x;
                int dy = current.y - dragStart.y;

                // 根据鼠标位置判断调整方向 Adjust the direction according to the position of the mouse
                adjustWindowBounds(edge, dx, dy);
                addControlButton();
                addTitleBar();
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
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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

    private JLabel createTitleBar() {
        JLabel label = new JLabel();
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label.setForeground(Color.BLACK);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
            }
        });
        label.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point current = e.getLocationOnScreen();
                if (onMax) {
                    onMax = false;
                    setExtendedState(NORMAL);
                    max.setText("\uE655");
                }
                setLocation(current.x - dragStart.x, current.y - dragStart.y);
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
            // 保存原始尺寸 Save the original dimensions
            setExtendedState(MAXIMIZED_BOTH);
        } else {
            setExtendedState(NORMAL);
        }
    }

    /**
     * 更新鼠标样式
     * Update mouse styles
     * @param mousePos 鼠标位置 Mouse position
     * @param size 窗口大小 Window size
     */
    private void updateCursor(Point mousePos, Dimension size) {
        int edgeType = getEdgeType(mousePos, size);
        switch (edgeType) {
            case Cursor.N_RESIZE_CURSOR:
            case Cursor.S_RESIZE_CURSOR:
                setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                break;
            case Cursor.E_RESIZE_CURSOR:
            case Cursor.W_RESIZE_CURSOR:
                setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                break;
            case Cursor.NE_RESIZE_CURSOR:
            case Cursor.SW_RESIZE_CURSOR:
                setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                break;
            case Cursor.NW_RESIZE_CURSOR:
            case Cursor.SE_RESIZE_CURSOR:
                setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                break;
            default:
                setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * 判断鼠标位于哪个边缘或角落
     * Determine which edge or corner the mouse is on
     * @param mousePos 鼠标位置 Mouse position
     * @param size 窗口大小 Window size
     * @return 鼠标位于边框位置的值 The value of the mouse at the position of the border
     */
    private int getEdgeType(Point mousePos, Dimension size) {
        int x = mousePos.x;
        int y = mousePos.y;
        int width = size.width;
        int height = size.height;

        boolean isNorth = y < resizeMargin;
        boolean isSouth = y > height - resizeMargin;
        boolean isWest = x < resizeMargin;
        boolean isEast = x > width - resizeMargin;

        if (isNorth && isWest) return Cursor.NW_RESIZE_CURSOR;
        if (isNorth && isEast) return Cursor.NE_RESIZE_CURSOR;
        if (isSouth && isWest) return Cursor.SW_RESIZE_CURSOR;
        if (isSouth && isEast) return Cursor.SE_RESIZE_CURSOR;
        if (isNorth) return Cursor.N_RESIZE_CURSOR;
        if (isSouth) return Cursor.S_RESIZE_CURSOR;
        if (isWest) return Cursor.W_RESIZE_CURSOR;
        if (isEast) return Cursor.E_RESIZE_CURSOR;

        return -1; // 不在边缘
    }

    /**
     * 根据方向调整窗口尺寸
     * Adjust the window size according to the orientation
     * @param edgeType 鼠标位于边框位置的值 The value of the mouse at the position of the border
     * @param dx 鼠标在x轴上的移动 Movement of the mouse on the x-axis
     * @param dy 鼠标在y轴上的移动 Movement of the mouse on the y-axis
     */
    private void adjustWindowBounds(int edgeType, int dx, int dy) {
        Rectangle bounds = new Rectangle(startBounds);
        switch (edgeType) {
            case Cursor.N_RESIZE_CURSOR:
                bounds.y += dy;
                bounds.height -= dy;
                break;
            case Cursor.S_RESIZE_CURSOR:
                bounds.height += dy;
                break;
            case Cursor.W_RESIZE_CURSOR:
                bounds.x += dx;
                bounds.width -= dx;
                break;
            case Cursor.E_RESIZE_CURSOR:
                bounds.width += dx;
                break;
            case Cursor.NW_RESIZE_CURSOR:
                bounds.x += dx;
                bounds.width -= dx;
                bounds.y += dy;
                bounds.height -= dy;
                break;
            case Cursor.NE_RESIZE_CURSOR:
                bounds.width += dx;
                bounds.y += dy;
                bounds.height -= dy;
                break;
            case Cursor.SW_RESIZE_CURSOR:
                bounds.x += dx;
                bounds.width -= dx;
                bounds.height += dy;
                break;
            case Cursor.SE_RESIZE_CURSOR:
                bounds.width += dx;
                bounds.height += dy;
                break;
            default:
                return;
        }

        // 设置最小窗口尺寸 Sets the minimum window size
        if (bounds.width < 200) bounds.width = 200;
        if (bounds.height < 200) bounds.height = 200;

        setBounds(bounds);
    }

    /**
     * 使用SwingUtilities.invokeLater来确保应用效果代码在事件分派线程中执行
     * Use SwingUtilities.invokeLater to ensure that the app effect
     * code is executed in the event dispatch thread
     */
    private void addAcrylic() {
        EventQueue.invokeLater(this::applyAcrylicEffect);
    }

    /**
     * 窗口监听器，在确保窗口已经创建好后为其添加组件以及获取句柄，并为其启用效果
     * Window listener, add components and get handles to the window after
     * making sure it's already created, and enable effects for it
     */
    @Override
    public void addNotify() {
        super.addNotify();
        addControlButton();
        addTitleBar();
        hWnd = new HWND(Native.getComponentPointer(this));
        addAcrylic();
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
     * Prevent the 'undecorated' parameter from being modified
     * @param undecorated {@code true} if no frame decorations are to be
     *         enabled; {@code false} if frame decorations are to be enabled
     *
     */
    @Override
    public void setUndecorated(boolean undecorated) {
        super.setUndecorated(true);
    }

    /**
     * 禁止改变窗口大小时，禁用`max`按钮
     * When disabling the change the size of window, disable the 'max' button
     * @param resizable   {@code true} if this frame is resizable;
     *                       {@code false} otherwise.
     */
    @Override
    public void setResizable(boolean resizable) {
        super.setResizable(resizable);
        this.resizable = resizable;
        max.setEnabled(resizable);
    }

    /**
     * `height + 28`是为了给标题栏留出位置
     * 'height + 28' is to make room for the title bar
     * @param width the new width of this component in pixels
     * @param height the new height of this component in pixels
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height + 28);
    }
}
