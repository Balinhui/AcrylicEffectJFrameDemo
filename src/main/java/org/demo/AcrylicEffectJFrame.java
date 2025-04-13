package org.demo;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AcrylicEffectJFrame extends JFrame {
    /**
     * 储存窗口的句柄，在窗口显示可见时获取值
     * Stores the handle of the window, and gets the value when the window is visible
     */
    private static HWND hWnd;

    /**
     * 窗口的标题栏，负责拖动和存放<code>Title</code>和三个控制按钮
     * The title bar of the window, which is responsible for dragging and storing the <code>Title</code> and the three control buttons
     */
    private final JLabel titleBar = createTitleBar();
    private final JButton exit = createControlButton("\uE653", 1, "关闭", e -> exit());
    private final JButton max = createControlButton("\uE655",2, "最大化", e -> toggleMaximize());
    private final JButton mix = createControlButton("\uE654", 3, "最小化", e -> mix());

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
     * Record the mouse position, use to drag the window
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

    /**
     * 记录窗口是否能改变大小的值
     * Records whether the window can change the value of the size
     */
    protected boolean resizable = true;

    /**
     * 记录标题栏高度的值（像素）
     */
    private int titlebarHeight = 29;

    private int defaultCloseOperation = HIDE_ON_CLOSE;

    /**
     * 记录按下的鼠标按键
     */
    private int ButtonNum = MouseEvent.NOBUTTON;

    private static final Color notOnFocus = new Color(141, 142, 142);

    static {
        System.setProperty("jna.encoding", "UTF-8");
        if (!System.getProperty("os.name").startsWith("Windows")) {
            throw new RuntimeException("系统不支持");
        }
    }

    //见https://learn.microsoft.com/zh-cn/windows/win32/api/dwmapi/ne-dwmapi-dwmwindowattribute
    public interface DWMWINDOWATTRIBUTE {
        int DWMWA_SYSTEMBACKDROP_TYPE = 38;  // Windows 11 新增的云母材质属性

        int DWMWA_WINDOW_CORNER_PREFERENCE = 33; //设置圆角
    }

    //见https://learn.microsoft.com/zh-cn/windows/win32/api/dwmapi/ne-dwmapi-dwm_systembackdrop_type
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

    //见https://learn.microsoft.com/zh-cn/windows/win32/api/dwmapi/ne-dwmapi-dwm_window_corner_preference
    public interface DWM_WINDOW_CORNER_PREFERENCE {
        @Deprecated
        int DWMWCP_DEFAULT = 0;            //默认边角

        @Deprecated
        int DWMWCP_DONOTROUND = 1;         //禁止圆角
        int DWMWCP_ROUND = 2;              //大圆角

        @Deprecated
        int DWMWCP_ROUNDSMALL = 3;         //小圆角
    }

    public interface SHOW_WINDOW {
        int SW_HIDE = 0;          //隐藏窗口并激活另一个窗口

        int SW_SHOWNORMAL = 1;    //激活并显示窗口。 如果窗口最小化、最大化或排列，系统会将其还原到其原始大小和位置。 应用程序应在首次显示窗口时指定此标志
        int SW_NORMAL = 1;  	  //上同

        int SW_SHOWMINIMIZED = 2; //激活窗口并将其显示为最小化窗口。

        int SW_SHOWMAXIMIZED = 3; //激活窗口并显示最大化的窗口。
        int SW_MAXIMIZE = 3;	  //上同

        int SW_SHOWNOACTIVATE = 4;//以最近的大小和位置显示窗口。 此值类似于 SW_SHOWNORMAL，只是窗口未激活。

        int SW_SHOW = 5;	      //激活窗口并以当前大小和位置显示窗口。

        int SW_MINIMIZE = 6;	  //最小化指定的窗口，并按 Z 顺序激活下一个顶级窗口。

        int SW_SHOWMINNOACTIVE = 7;//将窗口显示为最小化窗口。 此值类似于 SW_SHOWMINIMIZED，但窗口未激活。

        int SW_SHOWNA = 8;	      //以当前大小和位置显示窗口。 此值类似于 SW_SHOW，只是窗口未激活。

        int SW_RESTORE = 9;	      //激活并显示窗口。 如果窗口最小化、最大化或排列，系统会将其还原到其原始大小和位置。 还原最小化窗口时，应用程序应指定此标志。

        int SW_SHOWDEFAULT = 10;  //根据启动应用程序的程序传递给 CreateProcess 函数的 STARTUPINFO 结构中指定的SW_值设置显示状态。

        int SW_FORCEMINIMIZE = 11;//最小化窗口，即使拥有窗口的线程没有响应。 仅当最小化不同线程的窗口时，才应使用此标志。

    }

    public interface DwmApi extends StdCallLibrary {
        DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class);

        //见https://learn.microsoft.com/zh-cn/windows/win32/api/dwmapi/nf-dwmapi-dwmsetwindowattribute
        void DwmSetWindowAttribute(HWND hWnd, int dwAttribute, IntByReference pvAttribute, int cbAttribute);
    }

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class);

        //见https://learn.microsoft.com/zh-cn/windows/win32/api/winuser/nf-winuser-flashwindow
        boolean FlashWindow(HWND hWnd, boolean bInvert);

        //https://learn.microsoft.com/zh-cn/windows/win32/api/winuser/nf-winuser-showwindow
        boolean ShowWindow(HWND hWnd, int nCmdShow);
    }

    private void applyAcrylicEffect() {
        if (hWnd == null) {
            throw new RuntimeException("窗口句柄未成功获取");
        }

        IntByReference effectRef = new IntByReference(DWM_SYSTEMBACKDROP_TYPE.DWMSBT_TRANSIENTWINDOW);

        IntByReference roundRef = new IntByReference(DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_ROUND);

        //启用背景效果
        DwmApi.INSTANCE.DwmSetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_SYSTEMBACKDROP_TYPE, effectRef, 4);

        //启用圆角
        DwmApi.INSTANCE.DwmSetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_WINDOW_CORNER_PREFERENCE, roundRef, 4);
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
                max.setToolTipText("向下还原");
            } else {
                onMax = false;
                max.setText("\uE655");
                max.setToolTipText("最大化");
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
                ButtonNum = e.getButton();
                startBounds = getBounds();
                dragStart = e.getLocationOnScreen();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            // 记录鼠标的位置 Record the position of the mouse
            int edge = -1;

            @Override
            public void mouseMoved(MouseEvent e) {
                if (!resizable) return;

                updateCursor(e.getPoint(), getSize());
                edge = getEdgeType(e.getPoint(), getSize());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (edge == -1) return;
                if (startBounds ==null) return;
                if (ButtonNum != MouseEvent.BUTTON1) return;

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

    private JButton createControlButton(String text, int style, String tip, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe Fluent Icons", Font.PLAIN, 10));
        button.setForeground(Color.BLACK);
        button.setBackground((style == 1) ? new Color(186, 41, 27) : new Color(219, 219, 219));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.addActionListener(action);
        button.setToolTipText(tip);

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
                ButtonNum = e.getButton();
                dragStart = e.getPoint();
            }
        });
        label.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (ButtonNum != MouseEvent.BUTTON1) return;

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
        exit.setBounds(getWidth() - 47, 0, 47, titlebarHeight);
        max.setBounds(getWidth() - 2 * 47, 0, 47, titlebarHeight);
        mix.setBounds(getWidth() - 3 * 47, 0, 47, titlebarHeight);
        ContentPane.add(exit);
        ContentPane.add(max);
        ContentPane.add(mix);
    }

    private void addTitleBar() {
        titleBar.setBounds(8, 0, getWidth() - 149, titlebarHeight);
        ContentPane.add(titleBar);
    }

    private void mix() {
        setExtendedState(ICONIFIED);
    }

    private void toggleMaximize() {
        if ((getExtendedState() & MAXIMIZED_BOTH) == 0) {
            setExtendedState(MAXIMIZED_BOTH);
        } else {
            setExtendedState(NORMAL);
        }
    }

    private void exit() {
        if (defaultCloseOperation == EXIT_ON_CLOSE) {
            removeAll();
            System.exit(0);
        } else if (defaultCloseOperation == DISPOSE_ON_CLOSE) {
            dispose();
        } else if (defaultCloseOperation == HIDE_ON_CLOSE) {
            //hide window
            showWindow(SHOW_WINDOW.SW_HIDE);
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
     * 使用<code>SwingUtilities.invokeLater</code>来确保应用效果代码在事件分派线程中执行
     * Use <code>SwingUtilities.invokeLater</code> to ensure that the app effect
     * code is executed in the event dispatch thread
     */
    private void addAcrylic() {
        EventQueue.invokeLater(this::applyAcrylicEffect);
    }

    /**
     * 设置标题栏的高度，输入的值的范围为 15 ~ 44，需要在<code>setSize</code>和<code>setVisible</code>之前设置。
     * @param height 标题栏的高度
     */
    public void setTitlebarHeight(int height) {
        if (15 <= height && 44 >= height) {
            this.titlebarHeight = height;
        } else {
            throw new RuntimeException("标题栏高度设置错误, 应为15 ~ 44之间");
        }
    }

    /**
     * 获取标题栏高度
     * @return 标题栏高度
     */
    public int getTitlebarHeight() {
        return this.titlebarHeight;
    }

    public void flashWindow(boolean bInvent) {
        if (hWnd == null) {
            throw new RuntimeException("窗口句柄未获取");
        }
        boolean state = User32.INSTANCE.FlashWindow(hWnd, bInvent);
        if (!state && bInvent) {
            System.err.println("窗口闪烁失败");
        }
    }

    private void showWindow(int nCmdShow) {
        if (hWnd == null) {
            throw new RuntimeException("窗口句柄未获取");
        }
        boolean state = User32.INSTANCE.ShowWindow(hWnd, nCmdShow);
        if (!state) {
            System.err.println("show失败");
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!onFocus) {
            flashWindow(true);
        }
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
     * 防止修改<code>undecorated</code>参数
     * Prevent the <code>undecorated</code> parameter from being modified
     * @param undecorated {@code true} if no frame decorations are to be
     *         enabled; {@code false} if frame decorations are to be enabled
     *
     */
    @Override
    public void setUndecorated(boolean undecorated) {
        super.setUndecorated(true);
    }

    /**
     * 禁止改变窗口大小时，禁用<code>max</code>按钮
     * When disabling the change the size of window, disable the <code>max</code> button
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
     * <code>height + 28</code>是为了给标题栏留出位置
     * <code>height + 28</code> is to make room for the title bar
     * @param width the new width of this component in pixels
     * @param height the new height of this component in pixels
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height + titlebarHeight);
    }

    @Override
    public void setDefaultCloseOperation(int operation) {
        super.setDefaultCloseOperation(operation);
        defaultCloseOperation = operation;
    }
}
