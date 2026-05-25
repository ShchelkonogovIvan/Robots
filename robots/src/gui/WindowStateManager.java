package gui;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

import log.Logger;

public class WindowStateManager
{
    public static final String WINDOW_STATE_KEY_PROPERTY = "windowStateKey";

    private static final String WINDOW_STATE_FILE_NAME = "window-state.properties";
    private static final String MAIN_FRAME_KEY = "main";
    private static final String STATE_SEPARATOR = ";";
    private static final int X_INDEX = 0;
    private static final int Y_INDEX = 1;
    private static final int WIDTH_INDEX = 2;
    private static final int HEIGHT_INDEX = 3;
    private static final int EXTENDED_STATE_INDEX = 4;
    private static final int ICON_INDEX = 4;
    private static final int MAXIMUM_INDEX = 5;

    private final JFrame mainFrame;
    private final JDesktopPane desktopPane;

    public WindowStateManager(JFrame mainFrame, JDesktopPane desktopPane)
    {
        this.mainFrame = mainFrame;
        this.desktopPane = desktopPane;
    }

    public void save()
    {
        Properties properties = new Properties();
        saveFrameState(properties, MAIN_FRAME_KEY, mainFrame);
        for (JInternalFrame frame : desktopPane.getAllFrames())
        {
            saveInternalFrameState(properties, getInternalFramePrefix(frame), frame);
        }

        try (FileOutputStream stream = new FileOutputStream(getWindowStateFile()))
        {
            properties.store(stream, "Window state");
        }
        catch (IOException e)
        {
            Logger.error("Ошибка сохранения состояния окон");
        }
    }

    public boolean restore()
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
            restoreFrameState(properties, MAIN_FRAME_KEY, mainFrame);
            for (JInternalFrame frame : desktopPane.getAllFrames())
            {
                restoreInternalFrameState(properties, getInternalFramePrefix(frame), frame);
            }
            return true;
        }
        catch (IOException e)
        {
            Logger.error("Ошибка восстановления состояния окон");
            return false;
        }
    }

    private File getWindowStateFile()
    {
        return new File(System.getProperty("user.home"), WINDOW_STATE_FILE_NAME);
    }

    private String getInternalFramePrefix(JInternalFrame frame)
    {
        Object key = frame.getClientProperty(WINDOW_STATE_KEY_PROPERTY);
        if (key != null)
        {
            return key.toString();
        }
        return frame.getClass().getSimpleName();
    }

    private void saveFrameState(Properties properties, String prefix, JFrame frame)
    {
        properties.setProperty(prefix,
                frame.getX() + STATE_SEPARATOR
                + frame.getY() + STATE_SEPARATOR
                + frame.getWidth() + STATE_SEPARATOR
                + frame.getHeight() + STATE_SEPARATOR
                + frame.getExtendedState());
    }

    private void restoreFrameState(Properties properties, String prefix, JFrame frame)
    {
        String[] values = getFrameStateValues(properties, prefix);
        if (values == null)
        {
            return;
        }

        frame.setBounds(
                getIntValue(values, X_INDEX, frame.getX()),
                getIntValue(values, Y_INDEX, frame.getY()),
                getPositiveIntValue(values, WIDTH_INDEX, frame.getWidth()),
                getPositiveIntValue(values, HEIGHT_INDEX, frame.getHeight()));
        frame.setExtendedState(getIntValue(values, EXTENDED_STATE_INDEX, frame.getExtendedState()));
    }

    private void saveInternalFrameState(Properties properties, String prefix, JInternalFrame frame)
    {
        properties.setProperty(prefix,
                frame.getX() + STATE_SEPARATOR
                + frame.getY() + STATE_SEPARATOR
                + frame.getWidth() + STATE_SEPARATOR
                + frame.getHeight() + STATE_SEPARATOR
                + frame.isIcon() + STATE_SEPARATOR
                + frame.isMaximum());
    }

    private void restoreInternalFrameState(Properties properties, String prefix, JInternalFrame frame)
    {
        String[] values = getFrameStateValues(properties, prefix);
        if (values == null)
        {
            return;
        }

        frame.setBounds(
                getIntValue(values, X_INDEX, frame.getX()),
                getIntValue(values, Y_INDEX, frame.getY()),
                getPositiveIntValue(values, WIDTH_INDEX, frame.getWidth()),
                getPositiveIntValue(values, HEIGHT_INDEX, frame.getHeight()));
        try
        {
            frame.setMaximum(getBooleanValue(values, MAXIMUM_INDEX, frame.isMaximum()));
            frame.setIcon(getBooleanValue(values, ICON_INDEX, frame.isIcon()));
        }
        catch (PropertyVetoException e)
        {
            Logger.error("Ошибка восстановления состояния окон");
        }
    }

    private String[] getFrameStateValues(Properties properties, String prefix)
    {
        String value = properties.getProperty(prefix);
        if (value == null)
        {
            return null;
        }
        return value.split(STATE_SEPARATOR);
    }

    private int getIntValue(String[] values, int index, int defaultValue)
    {
        if (index >= values.length)
        {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt(values[index]);
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    private int getPositiveIntValue(String[] values, int index, int defaultValue)
    {
        int value = getIntValue(values, index, defaultValue);
        if (value <= 0)
        {
            return defaultValue;
        }
        return value;
    }

    private boolean getBooleanValue(String[] values, int index, boolean defaultValue)
    {
        if (index >= values.length)
        {
            return defaultValue;
        }
        return Boolean.parseBoolean(values[index]);
    }
}
