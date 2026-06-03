package server.utility;

import common.exceptions.NotExistException;
import common.models.Vehicle;
import common.utility.ResponseBuilder;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Менеджер коллекции транспортных средств.
 */
public class CollectionManager {

    private Stack<Vehicle> C = new Stack<>();
    private LocalDate creationDate;
    private ArrayList<Integer> arrayId = new ArrayList<>();
    private Integer recentId = 0;

    public void initializeArrayId() {
        arrayId.clear();
        arrayId.addAll(C.stream()
                .map(Vehicle::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
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


    public void infoAboutCollection() {
        ResponseBuilder.clear();
        ResponseBuilder.append("Тип коллекции: " + C.getClass().getSimpleName());
        ResponseBuilder.append("Дата создания: " + creationDate);
        ResponseBuilder.append("Количество элементов: " + C.size());
    }

    public void showElementsOfCollection() {
        ResponseBuilder.clear();
        if (C.isEmpty()) {
            ResponseBuilder.append("Коллекция пуста.");
            return;
        }

        String elements = C.stream()
                .map(Vehicle::toString)
                .collect(Collectors.joining("\n"));

        ResponseBuilder.append("Элементы коллекции (" + C.size() + " шт.):");
        ResponseBuilder.append(elements);
    }

    // ==================== Stream API в остальных командах ====================


    public void sumEnginePower() {
        ResponseBuilder.clear();
        double sum = C.stream()
                .mapToDouble(Vehicle::getEnginePower)
                .sum();
        ResponseBuilder.append("Сумма enginePower всех элементов: " + sum);
    }

    public void printAscendingNumberOfWheels() {
        ResponseBuilder.clear();
        List<Long> wheels = C.stream()
                .map(Vehicle::getNumberOfWheels)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
        ResponseBuilder.append("Количество колес по возрастанию: " + wheels);
    }

    public void printDescendingNumberOfWheels() {
        ResponseBuilder.clear();
        List<Long> wheels = C.stream()
                .map(Vehicle::getNumberOfWheels)
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        ResponseBuilder.append("Количество колес по убыванию: " + wheels);
    }


    public Stack<Vehicle> addToCollection(Vehicle vehicle) {
        if (vehicle == null) return C;

        vehicle.setId(generateId());
        C.add(vehicle);
        ResponseBuilder.append("Элемент успешно добавлен в коллекцию. Присвоен id = " + vehicle.getId());
        return C;
    }

    public Stack<Vehicle> updateElementById(Integer id, Vehicle element) throws NotExistException {
        for (int i = 0; i < C.size(); i++) {
            if (C.get(i).getId().equals(id)) {
                element.setId(id);
                element.setCreationDate(C.get(i).getCreationDate());
                element.setLastUpdateDate(LocalDate.now());
                C.set(i, element);
                ResponseBuilder.append("Элемент с id = " + id + " успешно обновлён.");
                return C;
            }
        }
        throw new NotExistException("Элемент с id = " + id + " не найден в коллекции!");
    }

    public Stack<Vehicle> removeById(Integer id) {
        boolean removed = C.removeIf(v -> Objects.equals(v.getId(), id));
        if (removed) {
            arrayId.remove(id);
            ResponseBuilder.append("Элемент с id = " + id + " успешно удалён.");
        } else {
            ResponseBuilder.append("Элемент с id = " + id + " не найден в коллекции");
        }
        return C;
    }

    public Stack<Vehicle> clearCollection() {
        C.clear();
        arrayId.clear();
        ResponseBuilder.append("Коллекция успешно очищена");
        return C;
    }

    public Stack<Vehicle> removeGreater(Vehicle element) {
        if (element == null) return C;

        C.removeIf(v -> v.compareTo(element) > 0);
        ResponseBuilder.append("Элементы, превышающие заданный, успешно удалены");
        return C;
    }

    public Stack<Vehicle> reorderCollection() {
        List<Vehicle> list = new ArrayList<>(C);
        Collections.reverse(list);
        C.clear();
        C.addAll(list);
        ResponseBuilder.append("Коллекция успешно отсортирована в обратном порядке");
        return C;
    }

    public Stack<Vehicle> sortCollection() {
        List<Vehicle> list = new ArrayList<>(C);
        Collections.sort(list);
        C.clear();
        C.addAll(list);
        ResponseBuilder.append("Коллекция успешно отсортирована в естественном порядке");
        return C;
    }


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