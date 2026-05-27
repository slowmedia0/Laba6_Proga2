package common.commands;

import common.models.Vehicle;

import java.io.File;
import java.io.Serializable;

public class CommandRequest implements Serializable {
    private final String nameOfCommand;
    private final String argument;
    private final Vehicle vehicle;
    private  String FileName;
    private  byte[] FileData;

    public CommandRequest(String nameOfCommand, String argument, Vehicle vehicle, String fileName, byte[] fileData) {
        this.nameOfCommand = nameOfCommand;
        this.argument = argument;
        this.vehicle = vehicle;
        this.FileName = fileName;
        this.FileData = fileData;
    }

    public CommandRequest(String nameOfCommand, String argument) {
        this.nameOfCommand = nameOfCommand;
        this.argument = argument;
        this.vehicle = null;
        this.FileName = null;
        this.FileData = null;
    }

    public CommandRequest(String nameOfCommand, String argument, Vehicle vehicle) {
        this.nameOfCommand = nameOfCommand;
        this.argument = argument;
        this.vehicle = vehicle;
        this.FileName = null;
        this.FileData = null;
    }


    public CommandRequest(String nameOfCommand, String argument, String fileName, byte[] fileData) {
        this.nameOfCommand = nameOfCommand;
        this.argument = argument;
        this.vehicle = null;
        this.FileName = fileName;
        this.FileData = fileData;
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

    public String getFileName() {
        return FileName;
    }

    public byte[] getFileData() {
        return FileData;
    }
}
