package common.commands;


import common.ExitCodeCommand;
import server.utility.FileManager;

/**
 * Команда сохранения коллекции в файл.
 */
public class SaveCommand extends AbstractCommand {
    /** Файловый менеджер. */
    private final FileManager fileManager;

    /**
     * Создаёт команду {@code save}.
     *
     * @param fileManager файловый менеджер
     */
    public SaveCommand(FileManager fileManager) {
        super("save","сохранить коллекцию в файл");
        this.fileManager = fileManager;
    }

    public ExitCodeCommand execute(){
        System.out.println("Выполняется сохранение коллекции");
        fileManager.writeCollection();
        return ExitCodeCommand.OK;
    }

    @Override
    public String getArgument() {
        return "";
    }

    @Override
    public ExitCodeCommand validate() {
        return ExitCodeCommand.OK;
    }

}