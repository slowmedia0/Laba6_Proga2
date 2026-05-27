package common.commands;

import common.models.Vehicle;

import java.io.File;
import java.io.Serializable;

public class CommandRequest implements Serializable {
    private final String nameOfCommand;
    private final String argument;
    private final Vehicle vehicle;
    private final File file;

    public CommandRequest(String nameOfCommand, String argument, Vehicle vehicle, File file) {
        this.nameOfCommand = nameOfCommand;
        this.argument = argument;
        this.vehicle = vehicle;
        this.file = file;
    }

    public CommandRequest(String nameOfCommand, String argument) {
        this.nameOfCommand = nameOfCommand;
        this.argument = argument;
        this.vehicle = null;
        this.file=null;
    }

    public CommandRequest(String nameOfCommand, String argument, Vehicle vehicle) {
        this.nameOfCommand = nameOfCommand;
        this.argument = argument;
        this.vehicle = vehicle;
        this.file=null;
    }


    public CommandRequest(String nameOfCommand, String argument, File file) {
        this.nameOfCommand = nameOfCommand;
        this.argument = argument;
        this.vehicle = null;
        this.file=file;
    }

    public String getNameOfCommand() {
        return nameOfCommand;
    }

    public String getArgument() {
        return argument;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public File getFile() {
        return file;
    }
}
