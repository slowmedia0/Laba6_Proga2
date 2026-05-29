package server.utility;

import common.exceptions.NotExistException;
import common.models.Vehicle;

import java.time.LocalDate;
import java.util.*;

/**
 * Менеджер коллекции транспортных средств.
 */
public class CollectionManager {

    /** Коллекция объектов Vehicle */
    private Stack<Vehicle> C = new Stack<>();

    /** Дата создания коллекции */
    private LocalDate creationDate;

    /** Список занятых идентификаторов */
    private ArrayList<Integer> arrayId = new ArrayList<>();

    /** Последний использованный индекс для генерации id */
    private Integer recentId = 0;

    /**
     * Инициализирует список идентификаторов на основе текущей коллекции.
     */
    public void initializeArrayId() {
        arrayId.clear();
        for (Vehicle v : C) {
            if (v.getId() != null) {
                arrayId.add(v.getId());
            }
        }
        Collections.sort(arrayId);
    }

    public void setCollection(Stack<Vehicle> c) {
        this.C = c != null ? c : new Stack<>();
    }

    public Stack<Vehicle> getCollection() {
        return C;
    }

    public ArrayList<Integer> getArrayId() {
        return arrayId;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    // ==================== Методы с использованием ResponseBuilder ====================

    public void infoAboutCollection() {
        ResponseBuilder.clear();
        ResponseBuilder.append("Тип коллекции: " + C.getClass().getSimpleName());
        ResponseBuilder.append("Дата создания: " + (creationDate != null ? creationDate : "не установлена"));
        ResponseBuilder.append("Количество элементов: " + C.size());
    }

    public void showElementsOfCollection() {
        ResponseBuilder.clear();
        if (C.isEmpty()) {
            ResponseBuilder.append("Коллекция пуста.");
            return;
        }

        ResponseBuilder.append("Элементы коллекции (" + C.size() + " шт.):");
        for (Vehicle vehicle : C) {
            ResponseBuilder.append(vehicle.toString());
        }
    }

    public void sortByName() {
        if (C.isEmpty()) return;

        List<Vehicle> list = new ArrayList<>(C);
        list.sort(Comparator.comparing(Vehicle::getName, Comparator.nullsLast(String::compareTo)));

        C.clear();
        C.addAll(list);

        ResponseBuilder.append("Коллекция успешно отсортирована по полю 'name'.");
    }

    public void sumEnginePower() {
        ResponseBuilder.clear();
        float sum = 0;
        for (Vehicle v : C) {
            sum += v.getEnginePower();
        }
        ResponseBuilder.append("Сумма enginePower всех элементов: " + sum);
    }

    public void printAscendingNumberOfWheels() {
        ResponseBuilder.clear();
        List<Long> wheels = new ArrayList<>();
        for (Vehicle v : C) {
            if (v.getNumberOfWheels() != null) {
                wheels.add(v.getNumberOfWheels());
            }
        }
        Collections.sort(wheels);
        ResponseBuilder.append("Количество колёс по возрастанию: " + wheels);
    }

    public void printDescendingNumberOfWheels() {
        ResponseBuilder.clear();
        List<Long> wheels = new ArrayList<>();
        for (Vehicle v : C) {
            if (v.getNumberOfWheels() != null) {
                wheels.add(v.getNumberOfWheels());
            }
        }
        wheels.sort(Collections.reverseOrder());
        ResponseBuilder.append("Количество колёс по убыванию: " + wheels);
    }

    // ==================== Методы изменения коллекции ====================

    public Stack<Vehicle> addToCollection(Vehicle vehicle) {
        if (vehicle == null) return C;

        vehicle.setId(generateId());
        C.add(vehicle);
        ResponseBuilder.append("Элемент успешно добавлен в коллекцию. ID = " + vehicle.getId());
        return C;
    }

    public Stack<Vehicle> updateElementById(Integer id, Vehicle element) throws NotExistException {
        for (int i = 0; i < C.size(); i++) {
            if (C.get(i).getId().equals(id)) {
                element.setId(id);
                element.setCreationDate(C.get(i).getCreationDate());
                element.setLastUpdateDate(LocalDate.now());

                C.set(i, element);
                ResponseBuilder.append("Элемент с ID " + id + " успешно обновлён.");
                return C;
            }
        }
        throw new NotExistException("Элемент с ID " + id + " не найден!");
    }

    public Stack<Vehicle> removeById(Integer id) {
        for (int i = 0; i < C.size(); i++) {
            if (C.get(i).getId().equals(id)) {
                C.remove(i);
                arrayId.remove(id);
                ResponseBuilder.append("Элемент с ID " + id + " успешно удалён.");
                return C;
            }
        }
        ResponseBuilder.appendError("Элемент с ID " + id + " не найден.");
        throw new NoSuchElementException("Элемент с ID " + id + " не найден!");
    }

    public Stack<Vehicle> clearCollection() {
        C.clear();
        arrayId.clear();
        ResponseBuilder.append("Коллекция успешно очищена.");
        return C;
    }

    public Stack<Vehicle> removeGreater(Vehicle element) {
        if (element == null) return C;

        C.removeIf(v -> v.compareTo(element) > 0);
        ResponseBuilder.append("Элементы, превышающие заданный, успешно удалены.");
        return C;
    }

    public Stack<Vehicle> reorderCollection() {
        List<Vehicle> list = new ArrayList<>(C);
        Collections.reverse(list);
        C.clear();
        C.addAll(list);
        ResponseBuilder.append("Коллекция успешно переупорядочена (в обратном порядке).");
        return C;
    }

    public Stack<Vehicle> sortCollection() {
        sortByName(); // используем уже существующий метод
        return C;
    }

    // ==================== Генерация ID ====================

    public Integer generateId() {
        return generateId(arrayId);
    }

    public Integer generateId(ArrayList<Integer> externalArrayId) {
        if (externalArrayId == null || externalArrayId.isEmpty()) {
            externalArrayId.add(1);
            return 1;
        }

        Collections.sort(externalArrayId);

        if (externalArrayId.get(0) > 1) {
            externalArrayId.add(0, 1);
            return 1;
        }

        for (int i = 0; i < externalArrayId.size() - 1; i++) {
            if (externalArrayId.get(i + 1) - externalArrayId.get(i) > 1) {
                int newId = externalArrayId.get(i) + 1;
                externalArrayId.add(i + 1, newId);
                return newId;
            }
        }

        int newId = externalArrayId.get(externalArrayId.size() - 1) + 1;
        externalArrayId.add(newId);
        return newId;
    }

    public boolean existId(Integer id) {
        return arrayId.contains(id);
    }
}