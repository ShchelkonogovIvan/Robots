package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import log.Logger;

/**
 * Что требуется сделать:
 * 1. Метод создания меню перегружен функционалом и трудно читается. 
 * Следует разделить его на серию более простых методов (или вообще выделить отдельный класс).
 *
 */
public class MainApplicationFrame extends JFrame
{
    private static final int SCREEN_INSET = 50;
    private static final int GAME_WINDOW_WIDTH = 400;
    private static final int GAME_WINDOW_HEIGHT = 400;
    private static final int LOG_WINDOW_X = 10;
    private static final int LOG_WINDOW_Y = 10;
    private static final int LOG_WINDOW_WIDTH = 300;
    private static final int LOG_WINDOW_HEIGHT = 800;

    private static final String APPLICATION_MENU_TEXT = "Приложение";
    private static final String APPLICATION_MENU_DESCRIPTION = "Управление приложением";
    private static final String EXIT_MENU_TEXT = "Выход";
    private static final String LOOK_AND_FEEL_MENU_TEXT = "Режим отображения";
    private static final String LOOK_AND_FEEL_MENU_DESCRIPTION = "Управление режимом отображения приложения";
    private static final String SYSTEM_LOOK_AND_FEEL_TEXT = "Системная схема";
    private static final String CROSS_PLATFORM_LOOK_AND_FEEL_TEXT = "Универсальная схема";
    private static final String TEST_MENU_TEXT = "Тесты";
    private static final String TEST_MENU_DESCRIPTION = "Тестовые команды";
    private static final String ADD_LOG_MESSAGE_TEXT = "Сообщение в лог";
    private static final String EXIT_CONFIRMATION_MESSAGE = "Вы действительно хотите выйти?";
    private static final String EXIT_CONFIRMATION_TITLE = "Подтверждение выхода";

    private final JDesktopPane desktopPane = new JDesktopPane();
    private final WindowStateManager windowStateManager = new WindowStateManager(this, desktopPane);
    private final Map<Class<? extends JInternalFrame>, Integer> internalFrameCounters = new HashMap<>();
    private final RobotModel robotModel = new RobotModel();
    private final RobotController robotController = new RobotController(robotModel);
    private RobotCoordinatesPresenter robotCoordinatesPresenter;
    
    public MainApplicationFrame() {
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(SCREEN_INSET, SCREEN_INSET,
            screenSize.width  - SCREEN_INSET * 2,
            screenSize.height - SCREEN_INSET * 2);

        setContentPane(desktopPane);
        
        
        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow(robotModel, robotController);
        gameWindow.setSize(GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);
        addWindow(gameWindow);

        RobotCoordinatesWindow coordinatesWindow = new RobotCoordinatesWindow();
        robotCoordinatesPresenter = new RobotCoordinatesPresenter(robotModel, coordinatesWindow);
        coordinatesWindow.setLocation(GAME_WINDOW_WIDTH + 20, 10);
        addWindow(coordinatesWindow);
        robotController.start();

        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent event)
            {
                onExit();
            }
        });

        windowStateManager.restore();
    }
    
    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(LOG_WINDOW_X, LOG_WINDOW_Y);
        logWindow.setSize(LOG_WINDOW_WIDTH, LOG_WINDOW_HEIGHT);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }
    
    protected void addWindow(JInternalFrame frame)
    {
        registerInternalFrame(frame);
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    private void registerInternalFrame(JInternalFrame frame)
    {
        Class<? extends JInternalFrame> frameClass = frame.getClass();
        int frameNumber = internalFrameCounters.getOrDefault(frameClass, 0);
        internalFrameCounters.put(frameClass, frameNumber + 1);
        frame.putClientProperty(WindowStateManager.WINDOW_STATE_KEY_PROPERTY,
                frameClass.getSimpleName() + "." + frameNumber);
    }
    
//    protected JMenuBar createMenuBar() {
//        JMenuBar menuBar = new JMenuBar();
// 
//        //Set up the lone menu.
//        JMenu menu = new JMenu("Document");
//        menu.setMnemonic(KeyEvent.VK_D);
//        menuBar.add(menu);
// 
//        //Set up the first menu item.
//        JMenuItem menuItem = new JMenuItem("New");
//        menuItem.setMnemonic(KeyEvent.VK_N);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(
//                KeyEvent.VK_N, ActionEvent.ALT_MASK));
//        menuItem.setActionCommand("new");
////        menuItem.addActionListener(this);
//        menu.add(menuItem);
// 
//        //Set up the second menu item.
//        menuItem = new JMenuItem("Quit");
//        menuItem.setMnemonic(KeyEvent.VK_Q);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(
//                KeyEvent.VK_Q, ActionEvent.ALT_MASK));
//        menuItem.setActionCommand("quit");
////        menuItem.addActionListener(this);
//        menu.add(menuItem);
// 
//        return menuBar;
//    }
    
    private JMenuBar generateMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        JMenu applicationMenu = new JMenu(APPLICATION_MENU_TEXT);
        applicationMenu.setMnemonic(KeyEvent.VK_A);
        applicationMenu.getAccessibleContext().setAccessibleDescription(
                APPLICATION_MENU_DESCRIPTION);

        {
            JMenuItem exitItem = new JMenuItem(EXIT_MENU_TEXT, KeyEvent.VK_Q);
            exitItem.addActionListener((event) -> {
                onExit();
            });
            applicationMenu.add(exitItem);
        }
        
        JMenu lookAndFeelMenu = new JMenu(LOOK_AND_FEEL_MENU_TEXT);
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                LOOK_AND_FEEL_MENU_DESCRIPTION);
        
        {
            JMenuItem systemLookAndFeel = new JMenuItem(SYSTEM_LOOK_AND_FEEL_TEXT, KeyEvent.VK_S);
            systemLookAndFeel.addActionListener((event) -> {
                setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                this.invalidate();
            });
            lookAndFeelMenu.add(systemLookAndFeel);
        }

        {
            JMenuItem crossplatformLookAndFeel = new JMenuItem(CROSS_PLATFORM_LOOK_AND_FEEL_TEXT, KeyEvent.VK_S);
            crossplatformLookAndFeel.addActionListener((event) -> {
                setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                this.invalidate();
            });
            lookAndFeelMenu.add(crossplatformLookAndFeel);
        }

        JMenu testMenu = new JMenu(TEST_MENU_TEXT);
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(
                TEST_MENU_DESCRIPTION);
        
        {
            JMenuItem addLogMessageItem = new JMenuItem(ADD_LOG_MESSAGE_TEXT, KeyEvent.VK_S);
            addLogMessageItem.addActionListener((event) -> {
                Logger.debug("Новая строка");
            });
            testMenu.add(addLogMessageItem);
        }

        menuBar.add(applicationMenu);
        menuBar.add(lookAndFeelMenu);
        menuBar.add(testMenu);
        return menuBar;
    }
// Метод для для выхода из приложения
    private void onExit()
    {
        int result = JOptionPane.showConfirmDialog(
                this,
                EXIT_CONFIRMATION_MESSAGE,
                EXIT_CONFIRMATION_TITLE,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION)
        {
            windowStateManager.save();
            System.exit(0);
        }
    }
    
    private void setLookAndFeel(String className)
    {
        try
        {
            UIManager.setLookAndFeel(className);
            GuiLocalization.apply();
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
            | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            // just ignore
        }
    }

}
