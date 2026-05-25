package gui;

import java.util.Locale;

import javax.swing.UIManager;

public final class GuiLocalization
{
    private static final String RUSSIAN_LOCALE = "ru-RU";
    private static final String YES_BUTTON_KEY = "OptionPane.yesButtonText";
    private static final String NO_BUTTON_KEY = "OptionPane.noButtonText";
    private static final String CANCEL_BUTTON_KEY = "OptionPane.cancelButtonText";
    private static final String OK_BUTTON_KEY = "OptionPane.okButtonText";
    private static final String YES_BUTTON_TEXT = "Да";
    private static final String NO_BUTTON_TEXT = "Нет";
    private static final String CANCEL_BUTTON_TEXT = "Отмена";
    private static final String OK_BUTTON_TEXT = "ОК";

    private GuiLocalization()
    {
    }

    public static void apply()
    {
        Locale.setDefault(Locale.forLanguageTag(RUSSIAN_LOCALE));

        UIManager.put(YES_BUTTON_KEY, YES_BUTTON_TEXT);
        UIManager.put(NO_BUTTON_KEY, NO_BUTTON_TEXT);
        UIManager.put(CANCEL_BUTTON_KEY, CANCEL_BUTTON_TEXT);
        UIManager.put(OK_BUTTON_KEY, OK_BUTTON_TEXT);
    }
}
