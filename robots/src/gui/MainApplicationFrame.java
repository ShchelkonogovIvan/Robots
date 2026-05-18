package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

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
    private static final String WINDOW_STATE_FILE_NAME = "window-state.properties";

    private final JDesktopPane desktopPane = new JDesktopPane();
    private LogWindow logWindow;
    private GameWindow gameWindow;
    
    public MainApplicationFrame() {
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
            screenSize.width  - inset*2,
            screenSize.height - inset*2);

        setContentPane(desktopPane);
        
        
        logWindow = createLogWindow();
        addWindow(logWindow);

        gameWindow = new GameWindow();
        gameWindow.setSize(400,  400);
        addWindow(gameWindow);

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

        restoreWindowState();
    }
    
    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }
    
    protected void addWindow(JInternalFrame frame)
    {
        desktopPane.add(frame);
        frame.setVisible(true);
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

        JMenu applicationMenu = new JMenu("Приложение");
        applicationMenu.setMnemonic(KeyEvent.VK_A);
        applicationMenu.getAccessibleContext().setAccessibleDescription(
                "Управление приложением");

        {
            JMenuItem exitItem = new JMenuItem("Выход", KeyEvent.VK_Q);
            exitItem.addActionListener((event) -> {
                onExit();
            });
            applicationMenu.add(exitItem);
        }
        
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                "Управление режимом отображения приложения");
        
        {
            JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
            systemLookAndFeel.addActionListener((event) -> {
                setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                this.invalidate();
            });
            lookAndFeelMenu.add(systemLookAndFeel);
        }

        {
            JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_S);
            crossplatformLookAndFeel.addActionListener((event) -> {
                setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                this.invalidate();
            });
            lookAndFeelMenu.add(crossplatformLookAndFeel);
        }

        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(
                "Тестовые команды");
        
        {
            JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
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
                "Вы действительно хотите выйти?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION)
        {
            saveWindowState();
            System.exit(0);
        }
    }

    private File getWindowStateFile()
    {
        return new File(System.getProperty("user.home"), WINDOW_STATE_FILE_NAME);
    }

    private void saveWindowState()
    {
        Properties properties = new Properties();
        saveFrameState(properties, "main", this);
        saveInternalFrameState(properties, "logWindow", logWindow);
        saveInternalFrameState(properties, "gameWindow", gameWindow);

        try (FileOutputStream stream = new FileOutputStream(getWindowStateFile()))
        {
            properties.store(stream, "Statement");
        }
        catch (IOException e)
        {
            Logger.error("Не удалось сохранить состояние окон");
        }
    }

    private boolean restoreWindowState()
    {
        File stateFile = getWindowStateFile();
        if (!stateFile.exists())
        {
            return false;
        }

        Properties properties = new Properties();
        try (FileInputStream stream = new FileInputStream(stateFile))
        {
            properties.load(stream);
            restoreFrameState(properties, "main", this);
            restoreInternalFrameState(properties, "logWindow", logWindow);
            restoreInternalFrameState(properties, "gameWindow", gameWindow);
            return true;
        }
        catch (IOException e)
        {
            Logger.error("Не удалось восстановить состояние окон");
            return false;
        }
    }

    private void saveFrameState(Properties properties, String prefix, JFrame frame)
    {
        properties.setProperty(prefix + ".x", Integer.toString(frame.getX()));
        properties.setProperty(prefix + ".y", Integer.toString(frame.getY()));
        properties.setProperty(prefix + ".width", Integer.toString(frame.getWidth()));
        properties.setProperty(prefix + ".height", Integer.toString(frame.getHeight()));
        properties.setProperty(prefix + ".extendedState", Integer.toString(frame.getExtendedState()));
    }

    private void restoreFrameState(Properties properties, String prefix, JFrame frame)
    {
        frame.setBounds(
                getIntProperty(properties, prefix + ".x", frame.getX()),
                getIntProperty(properties, prefix + ".y", frame.getY()),
                getIntProperty(properties, prefix + ".width", frame.getWidth()),
                getIntProperty(properties, prefix + ".height", frame.getHeight()));
        frame.setExtendedState(getIntProperty(properties, prefix + ".extendedState", frame.getExtendedState()));
    }

    private void saveInternalFrameState(Properties properties, String prefix, JInternalFrame frame)
    {
        properties.setProperty(prefix + ".x", Integer.toString(frame.getX()));
        properties.setProperty(prefix + ".y", Integer.toString(frame.getY()));
        properties.setProperty(prefix + ".width", Integer.toString(frame.getWidth()));
        properties.setProperty(prefix + ".height", Integer.toString(frame.getHeight()));
        properties.setProperty(prefix + ".icon", Boolean.toString(frame.isIcon()));
        properties.setProperty(prefix + ".maximum", Boolean.toString(frame.isMaximum()));
    }

    private void restoreInternalFrameState(Properties properties, String prefix, JInternalFrame frame)
    {
        frame.setBounds(
                getIntProperty(properties, prefix + ".x", frame.getX()),
                getIntProperty(properties, prefix + ".y", frame.getY()),
                getIntProperty(properties, prefix + ".width", frame.getWidth()),
                getIntProperty(properties, prefix + ".height", frame.getHeight()));
        try
        {
            frame.setIcon(getBooleanProperty(properties, prefix + ".icon", frame.isIcon()));
            frame.setMaximum(getBooleanProperty(properties, prefix + ".maximum", frame.isMaximum()));
        }
        catch (PropertyVetoException e)
        {
            Logger.error("Не удалось восстановить состояние внутреннего окна");
        }
    }

    private int getIntProperty(Properties properties, String name, int defaultValue)
    {
        String value = properties.getProperty(name);
        if (value == null)
        {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    private boolean getBooleanProperty(Properties properties, String name, boolean defaultValue)
    {
        String value = properties.getProperty(name);
        if (value == null)
        {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
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
