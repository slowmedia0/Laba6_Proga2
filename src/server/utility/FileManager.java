package server.utility;

import common.exceptions.FieldReadException;
import common.exceptions.NotExistException;
import client.utility.Validator;
import common.utility.Serializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import common.models.Coordinates;
import common.models.FuelType;
import common.models.Vehicle;
import common.models.VehicleType;

import javax.xml.parsers.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Менеджер работы с файлами.
 */
public class FileManager {
    static public enum ModeOfFileManager {
        READ_COLLECTION,
        WRITE_COLLECTION,
        READ_SCRIPT
    }

    private File loadFile;
    /** Менеджер коллекции. */
    private CollectionManager collectionManager;
    /** Имя файла коллекции. */

    /**
     * Создаёт файловый менеджер.
     *
     * @param collectionManager менеджер коллекции
     */
    public FileManager(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Устанавливает менеджер коллекции.
     *
     * @param collectionManager менеджер коллекции
     */
    public void setCollectionManager(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public Stack<Vehicle> readCollection(File loadFile) throws IOException, ParserConfigurationException, SAXException {
        Stack<Vehicle> C = new Stack<>();
        HashSet<String> setOfId = new HashSet<>();
        String text = "";
        File file = loadFile;
        this.loadFile=loadFile;
        List<String> CharsOfVehicle = List.of("id", "name", "coordinates", "creationDate", "enginePower", "numberOfWheels", "type", "fuelType");

        ArrayList<ArrayList<String>> multiarray = new ArrayList<>();
        for (int i = 0; i < CharsOfVehicle.size(); i++) {
            multiarray.add(new ArrayList<>());
        }

        int k = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                @Override
                public void warning(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
                }
                @Override
                public void error(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
                }
                @Override
                public void fatalError(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
                }
            });

            Document doc = builder.parse(new InputSource(reader));

            Element collection = (Element) doc.getElementsByTagName("collection").item(0);
            NodeList collections = doc.getElementsByTagName("collection");
            int collectionCount = collections.getLength();
            if (collectionCount==0) {
                throw new NoSuchElementException("В файле нет коллекции!");
            }
            if (collectionCount>1) {
                throw new NoSuchElementException("В файле более одной коллекции!");
            }
            int vehicleCount = collection.getElementsByTagName("vehicle").getLength();
            for (int i = 0; i < vehicleCount; i++) {
                Element vehicle = (Element) collection.getElementsByTagName("vehicle").item(i);
                for (int j = 0; j < CharsOfVehicle.size(); j++) {
                    String field = CharsOfVehicle.get(j);
                    NodeList nodes = vehicle.getElementsByTagName(field);
                    if (nodes.getLength() == 0) {
                        if (field.equals("coordinates")) {
                            multiarray.get(j).add("NULL");
                        } else {
                            multiarray.get(j).add("NULL");
                            if (field.equals("id")){
                                k+=1;
                            }
                        }
                    }
                    else if (field.equals("coordinates")) {
                        Element coordinates = (Element) nodes.item(0);
                        String x = "NULL";
                        String y = "NULL";
                        if (coordinates.getElementsByTagName("x").getLength() > 0) {
                            x = coordinates.getElementsByTagName("x").item(0).getTextContent();
                        }
                        if (coordinates.getElementsByTagName("y").getLength() > 0) {
                            y = coordinates.getElementsByTagName("y").item(0).getTextContent();
                        }
                        multiarray.get(j).add(x + "|" + y);
                    }
                    else if (field.equals("id")) {
                        try {
                            String id = nodes.item(0).getTextContent();
                            if (setOfId.contains(id)) {
                                throw new IllegalArgumentException("Дублирование поля 'id' = " + id + "!");
                            }
                            k += 1;
                            setOfId.add(id);
                            multiarray.get(j).add(id);
                        }
                        catch (IllegalArgumentException e){
                            System.out.println(e.getMessage());
                            break;
                        }
                    }
                    else {
                        String value = nodes.item(0).getTextContent();
                        multiarray.get(j).add(value.isEmpty() ? "" : value);
                    }
                }
            }
        }
        catch (FileNotFoundException e){
            System.out.println(e.getMessage() + " : Не удалось найти файл! Проверьте, что он действительно существует или что вы корректно указали файл!");
            return readCollection(null);
        }
        catch (NoSuchElementException ex){
            System.out.println(ex.getMessage());
            return readCollection(null);
        }
        catch (SAXException e){
            System.out.println(e.getMessage() + " : Файл XML не валиден!");
            return readCollection(null);
        }
        catch (ParserConfigurationException e){
            System.out.println(e.getMessage() + " : Ошибка парсинга!");
            return readCollection(null);
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage() + " : Непредвиденная ошибка! Возможно файл используется уже кем-то.");
            return readCollection(null);
        }

        for (int j = 0; j < k; j++) {
            ArrayList<String> ar = new ArrayList<>(CharsOfVehicle.size());
            for (int i = 0; i < CharsOfVehicle.size(); i++) {
                ar.add(multiarray.get(i).get(j));
            }
            try {
                Integer id = FieldReaderServer.readFieldId(ar.get(CharsOfVehicle.indexOf("id")),collectionManager);
                String name = FieldReaderServer.readFieldName(ar.get(CharsOfVehicle.indexOf("name")));
                try {
                    if (ar.get(CharsOfVehicle.indexOf("coordinates")).equals("NULL")) {
                        throw new NotExistException("Отсутствует тэг 'coordinates'!");
                    } else if (ar.get(CharsOfVehicle.indexOf("coordinates")).equals("NULL|NULL")) {
                        throw new NotExistException("В поле 'coordinates' поля 'x' и 'y' не инициализированы!");
                    }
                }
                catch (NullPointerException | NotExistException e){
                    throw new FieldReadException("Не удалось считать поле 'coordinates'!",e);
                }
                String x = ar.get(CharsOfVehicle.indexOf("coordinates")).indexOf("|")==0 ? "" : ar.get(CharsOfVehicle.indexOf("coordinates")).split("\\|")[0];
                String y = ar.get(CharsOfVehicle.indexOf("coordinates")).indexOf("|")==(ar.get(CharsOfVehicle.indexOf("coordinates")).length()-1) ? "" : ar.get(CharsOfVehicle.indexOf("coordinates")).split("\\|")[1];

                Coordinates coordinates = FieldReaderServer.askCoordinates(x,y);
                LocalDate creationDate = FieldReaderServer.readFieldCreationDate(ar.get(CharsOfVehicle.indexOf("creationDate")));
                float enginePower = FieldReaderServer.readFieldEnginePower(ar.get(CharsOfVehicle.indexOf("enginePower")));
                Long numberOfWheels = FieldReaderServer.readFieldNumberOfWheels(ar.get(CharsOfVehicle.indexOf("numberOfWheels")));
                VehicleType type = FieldReaderServer.readFieldType(ar.get(CharsOfVehicle.indexOf("type")));
                FuelType fuelType = FieldReaderServer.readFieldFuelType(ar.get(CharsOfVehicle.indexOf("fuelType")));
                C.add(new Vehicle(id, name, coordinates, creationDate, enginePower, numberOfWheels, type, fuelType));
            }
            catch (FieldReadException e) {
                System.out.println("Не удалось считать объект Vehicle! -> " + e.generateFullMessage());
            }
            catch (Exception e){
                System.out.println("Не удалось считать объект Vehicle! -> " + e.getMessage());
            }
        }
        return C;
    }


    public boolean writeCollection() {
        Stack<Vehicle> C = collectionManager.getCollection();
        File file = this.loadFile;
        StringBuilder xml = new StringBuilder();
        xml.append("<collection>\n");
        for (Vehicle v : C) {
            xml.append("  <vehicle>\n");
            xml.append("    <id>").append(v.getId()).append("</id>\n");
            xml.append("    <name>").append(v.getName()).append("</name>\n");
            xml.append("    <coordinates>\n");
            xml.append("      <x>").append(v.getCoordinates().getX()).append("</x>\n");
            xml.append("      <y>").append(v.getCoordinates().getY()).append("</y>\n");
            xml.append("    </coordinates>\n");
            xml.append("    <creationDate>").append(v.getCreationDate()).append("</creationDate>\n");
            xml.append("    <enginePower>").append(v.getEnginePower()).append("</enginePower>\n");
            xml.append("    <numberOfWheels>").append(v.getNumberOfWheels()).append("</numberOfWheels>\n");
            xml.append("    <type>").append(v.getType()).append("</type>\n");

            if (v.getFuelType() != null) {
                xml.append("    <fuelType>").append(v.getFuelType()).append("</fuelType>\n");
            }
            else{
                xml.append("    <fuelType>").append("</fuelType>\n");
            }

            xml.append("  </vehicle>\n");
        }
        xml.append("</collection>\n");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            if (xml.length() == 0) {
                throw new IllegalArgumentException("Записываемые данные пусты!");
            }
            byte[] buffer = xml.toString().getBytes("UTF-8");
            fos.write(buffer);
            return true;
        }
        catch (FileNotFoundException e){
            System.out.println(e.getMessage() + " : Не удалось найти файл! Проверьте, что он действительно существует или что вы корректно указали файл!");
            return false;
        }
        catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
            return false;
        }
        catch (IOException e) {
            System.out.println(e.getMessage() + " : Непредвиденная ошибка! Возможно файл используется уже кем-то.");
            return false;
        }
    }

    public ArrayList<String> readScript(File fileScript) {
        ArrayList<String> commands = new ArrayList<>();
        File file = fileScript;
        try (BufferedReader in = new BufferedReader(new FileReader(fileScript))) {
            String st;
            while ((st = in.readLine()) != null) {
                commands.add(st);
            }
            return commands;
        }
        catch (FileNotFoundException e){
            System.out.println(e.getMessage() + " : Не удалось найти файл! Проверьте, что он действительно существует или что вы корректно указали файл!");
            return null;
        }
        catch (IOException e) {
            System.out.println(e.getMessage() + " : Непредвиденная ошибка! Возможно файл используется уже кем-то.");
            return null;
        }
    }

    /**Возвращает текущую коллекцию в виде байтов (для отправки клиенту при exit)
     */
    public byte[] getCollectionAsBytes() {
        try {
            // Сериализуем коллекцию
            return Serializer.serialize(collectionManager.getCollection());
        } catch (Exception e) {
            System.err.println("Ошибка сериализации коллекции: " + e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Загружает коллекцию из байтов (альтернатива, если не хочешь создавать файл)
     */
    public void loadFromBytes(byte[] data) {
        try {
            @SuppressWarnings("unchecked")
            Stack<Vehicle> loadedCollection = (Stack<Vehicle>) Serializer.deserialize(data);
            collectionManager.setCollection(loadedCollection);
            collectionManager.initializeArrayId();
            System.out.println("Коллекция успешно загружена из байтов (" + loadedCollection.size() + " элементов)");
        } catch (Exception e) {
            System.err.println("Ошибка загрузки коллекции из байтов: " + e.getMessage());
        }
    }

    public File getLoadFile() {
        return loadFile;
    }
}