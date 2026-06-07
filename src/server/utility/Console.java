package server.utility;

import common.ExitCodeCommand;
import common.exceptions.CommandNotExist;
import common.models.Vehicle;
import common.utility.ResponseBuilder;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Класс консольного интерфейса приложения.
 */
public class Console {

    public ExitCodeCommand exitCodeStatus = ExitCodeCommand.CTRL_C;

    private final CommandManger commandManager;
    private final server.utility.FileManager fileManager;
    private String loadFileName;
    private byte[] loadFileData;
    private File currentLoadFile;

    private boolean flagScript;
    private boolean flagReadCollection;

    private ArrayList<String> fields = new ArrayList<>(7);
    private CollectionManager collectionManager;

    public Console(CommandManger commandManager, server.utility.FileManager fileManager, CollectionManager collectionManager) {
        this.commandManager = commandManager;
        this.fileManager = fileManager;
        this.collectionManager = collectionManager;
    }

    // ==================== ГЕТТЕРЫ И СЕТТЕРЫ (без изменений) ====================
    public ExitCodeCommand getExitCodeStatus() { return exitCodeStatus; }
    public void setExitCodeStatus(ExitCodeCommand exitCodeStatus) { this.exitCodeStatus = exitCodeStatus; }
    public String getLoadFileName() { return loadFileName; }
    public byte[] getLoadFileData() { return loadFileData; }
    public ArrayList<String> getFields() { return fields; }
    public boolean isFlagScript() { return flagScript; }
    public boolean isFlagReadCollection() { return flagReadCollection; }

    // ==================== ЗАГРУЗКА КОЛЛЕКЦИИ (без изменений) ====================
    public void loadCollection(File loadFile) throws IOException, ParserConfigurationException, SAXException {
        this.currentLoadFile = loadFile;
        flagReadCollection = true;
        flagScript = false;
        System.out.println("Считываю коллекцию из загрузочного файла...");
        collectionManager.setCollection(fileManager.readCollection(loadFile));
        System.out.println("Коллекция успешно считана из загрузочного файла!");
        collectionManager.initializeArrayId();
        collectionManager.setCreationDate(java.time.LocalDate.now());
        flagReadCollection = false;
    }

    // ==================== НОВЫЕ МЕТОДЫ (switch убран) ====================

    public ExitCodeCommand launchCommand(String mnemonics, String argument) {
        try {
            if (isEmptyCommand(mnemonics, argument)) {
                exitCodeStatus = ExitCodeCommand.OK;
                return exitCodeStatus;
            }

            exitCodeStatus = commandManager.execute(mnemonics, argument);
            return exitCodeStatus;

        } catch (CommandNotExist e) {
            ResponseBuilder.appendLn(e.getMessage());
            exitCodeStatus = ExitCodeCommand.ERROR;
            return exitCodeStatus;
        } catch (Exception e) {
            ResponseBuilder.appendLn("Ошибка выполнения команды: " + e.getMessage());
            exitCodeStatus = ExitCodeCommand.ERROR;
            return exitCodeStatus;
        }
    }

    public ExitCodeCommand launchCommand(String mnemonics, String argument, Vehicle vehicle, String FileName, byte[] FileData) {
        try {
            if (isEmptyCommand(mnemonics, argument)) {
                exitCodeStatus = ExitCodeCommand.OK;
                return exitCodeStatus;
            }

            exitCodeStatus = commandManager.execute(mnemonics, argument, vehicle, FileName, FileData);
            return exitCodeStatus;

        } catch (CommandNotExist e) {
            ResponseBuilder.appendLn(e.getMessage());
            exitCodeStatus = ExitCodeCommand.ERROR;
            return exitCodeStatus;
        } catch (Exception e) {
            ResponseBuilder.appendLn("Ошибка выполнения команды: " + e.getMessage());
            exitCodeStatus = ExitCodeCommand.ERROR;
            return exitCodeStatus;
        }
    }

    private boolean isEmptyCommand(String mnemonics, String argument) {
        return (mnemonics == null || mnemonics.trim().isEmpty()) &&
                (argument == null || argument.trim().isEmpty());
    }
}