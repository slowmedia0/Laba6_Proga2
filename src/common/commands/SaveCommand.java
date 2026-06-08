package common.commands;


import common.ExitCodeCommand;
import server.utility.FileManager;


public class SaveCommand extends AbstractCommand {
    
    private final FileManager fileManager;

    
    public SaveCommand(FileManager fileManager) {
        super("save","сохранить коллекцию в файл");
        this.fileManager = fileManager;
    }

    public ExitCodeCommand execute(){
        System.out.println("Выполняется сохранение коллекции в файл " + fileManager.getLoadFile());
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