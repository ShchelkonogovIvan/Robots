package gui;

import java.util.Locale;

import javax.swing.UIManager;

public final class GuiLocalization
{
    private GuiLocalization()
    {
    }

    public static void apply()
    {
        Locale.setDefault(Locale.forLanguageTag("ru-RU"));

        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");
        UIManager.put("OptionPane.cancelButtonText", "Отмена");
        UIManager.put("OptionPane.okButtonText", "ОК");
    }
}
