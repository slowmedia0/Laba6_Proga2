package common.commands;

import java.io.Serializable;

/**
 * Абстрактный базовый класс для всех команд приложения.
 * <p>
 * Содержит общие поля: имя команды и её описание.
 * </p>
 */
public abstract class AbstractCommand implements Command {
    String nameOfCommand;
    String descriptionOfCommand;
    public AbstractCommand(String nameOfCommand, String descriptionOfCommand) {
        this.descriptionOfCommand = descriptionOfCommand;
        this.nameOfCommand = nameOfCommand;
    }
    public String getNameOfCommand() {
        return nameOfCommand;
    }
    public String getDescriptionOfCommand() {
        return descriptionOfCommand;
    }
}