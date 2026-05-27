package server.utility;

import common.exceptions.NotExistException;
import common.models.Vehicle;

import java.time.LocalDate;
import java.util.*;

/**
 * Менеджер коллекции транспортных средств.
 */
public class CollectionManager{

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
    public void initializeArrayId(){
        ArrayList <Integer> ar = new ArrayList<>();
        for (int i=0; i<C.size();i++){
            ar.add(C.get(i).getId());
        }
        Collections.sort(ar);
        recentId=0;
        arrayId = ar;
    }

    /** Конструктор по умолчанию */
    public CollectionManager(){};

    /**
     * Устанавливает коллекцию.
     *
     * @param c новая коллекция
     */
    public void setCollection(Stack<Vehicle> c) {
        C = c;
    }

    /**
     * Возвращает текущую коллекцию.
     *
     * @return коллекция объектов
     */
    public Stack<Vehicle> getCollection() {
        return C;
    }

    /**
     * Возвращает список id.
     *
     * @return список идентификаторов
     */
    public ArrayList<Integer> getArrayId() {
        return arrayId;
    }

    /**
     * Устанавливает дату создания коллекции.
     *
     * @param creationDate дата создания
     */
    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Возвращает дату создания коллекции.
     *
     * @return дата создания
     */
    public LocalDate getCreationDate() {
        return creationDate;
    }

    /**
     * Выводит информацию о коллекции.
     */
    public void infoAboutCollection(){
        System.out.println("Type: " + C.getClass() + "\n" +
                "Creation date: " + getCreationDate() + "\n"+
                "Size: " + C.size() + "\n");
    }

    /**
     * Проверяет наличие элемента с данным id.
     *
     * @param id идентификатор
     * @return true если существует
     */
    public boolean existId(Integer id){
        for (int i=0; i<C.size();i++){
            if (arrayId.get(i)==id){
                return true;
            }
        }
        return false;
    }

    /**
     * Генерирует уникальный id.
     *
     * @return новый id
     */
    public Integer generateId() {
        if (arrayId.isEmpty()) {
            arrayId.add(1);
            recentId = 0;
            return 1;
        }

        Collections.sort(arrayId);
        if (arrayId.get(0) > 1) {
            int newId = 1;
            arrayId.add(0, newId);
            recentId = 0;
            return newId;
        }

        for (int i = 0; i < arrayId.size() - 1; i++) {
            if (arrayId.get(i + 1) - arrayId.get(i) > 1) {
                int newId = arrayId.get(i) + 1;
                arrayId.add(i + 1, newId);
                recentId = i + 1;
                return newId;
            }
        }

        int maxId = arrayId.get(arrayId.size() - 1);
        int newId = maxId + 1;
        arrayId.add(newId);
        recentId = arrayId.size() - 1;

        return newId;
    }


    public Integer generateId(ArrayList<Integer> externalArrayId) {
        if (externalArrayId == null || externalArrayId.isEmpty()) {
            externalArrayId.add(1);
            return 1;
        }

        Collections.sort(externalArrayId);

        if (externalArrayId.get(0) > 1) {
            int newId = 1;
            externalArrayId.add(0, newId);
            return newId;
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
    /**
     * Выводит все элементы коллекции.
     */
    public void showElementsOfCollection(){
        for (int i = 0; i < C.size(); i++) {
            System.out.println(C.get(i).toString());
        }
    }

    /**
     * Добавляет элемент в коллекцию.
     *
     * @param vehicle добавляемый объект
     * @return обновлённая коллекция
     */
    public Stack<Vehicle> addToCollection(Vehicle vehicle){
        Vehicle obj = vehicle;
        obj.setId(generateId());
        C.add(obj);
        return C;
    }

    /**
     * Обновляет элемент по id.
     *
     * @param id идентификатор элемента
     * @param element новый элемент
     * @return обновлённая коллекция
     * @throws NotExistException если элемент не найден
     */
    public Stack<Vehicle> updateElementById(Integer id, Vehicle element) throws NotExistException {
        for (int i=0;i<C.size();i++){
            if (C.get(i).getId().equals(id)==true){
                System.out.println("Старое значение элемента:");
                System.out.println(C.get(i).toString());
                Vehicle obj = element;
                obj.setLastUpdateDate(LocalDate.now());
                obj.setCreationDate(C.get(i).getCreationDate());
                obj.setId(id);
                System.out.println("Новое значение элемента:");
                System.out.println(obj.toString());
                C.set(i,element);
                return C;
            }
        }
        return C;
    }

    /**
     * Удаляет элемент по id.
     *
     * @param id идентификатор элемента
     * @return обновлённая коллекция
     */
    public Stack<Vehicle> removeById(Integer id){
        for (int i=0;i<C.size();i++){
            if (C.get(i).getId().equals(id)){
                C.remove(i);
                arrayId.remove(id);
                recentId = 0;
                return C;
            }
        }
        throw new NoSuchElementException("В коллекции нет элемента с таким же id!");
    }

    /**
     * Очищает коллекцию.
     *
     * @return очищенная коллекция
     */
    public Stack<Vehicle> clearCollection(){
        C.clear();
        arrayId.clear();
        recentId = 0;
        return C;
    }

    /**
     * Удаляет элементы больше заданного.
     *
     * @param element эталонный элемент
     * @return обновлённая коллекция
     */
    public Stack<Vehicle> removeGreater(Vehicle element){
        ArrayList <Vehicle> array = new ArrayList<>();
        for (int i=0;i<C.size();i++){
            if (C.get(i).compareTo(element)>0==false){
                array.add(C.get(i));
            }
            else{
                arrayId.remove(C.get(i).getId());
            }
        }
        C.clear();
        C.addAll(array);
        return C;
    }

    /**
     * Разворачивает порядок коллекции.
     *
     * @return коллекция в обратном порядке
     */
    public Stack<Vehicle> reorderCollection(){
        ArrayList <Vehicle> array = new ArrayList<>(C);
        Collections.sort(array,Collections.reverseOrder());
        C.clear();
        C.addAll(array);
        return C;
    }

    /**
     * Сортирует коллекцию.
     *
     * @return отсортированная коллекция
     */
    public Stack<Vehicle> sortCollection(){
        ArrayList <Vehicle> array = new ArrayList<>(C);
        Collections.sort(array);
        C.clear();
        C.addAll(array);
        return C;
    }

    /**
     * Суммирует enginePower.
     */
    public void sumEnginePower(){
        float s=0;
        for (int i=0;i<C.size();i++){
            s+=C.get(i).getEnginePower();
        }
        System.out.println(s);
    }

    /**
     * Вывод колёс по возрастанию.
     */
    public void printAscendingNumberOfWheels(){
        ArrayList<Long> ar = new ArrayList<>();
        for(int i=0;i<C.size();i++){
            ar.add(C.get(i).getNumberOfWheels());
        }
        Collections.sort(ar);
        System.out.println(ar);
    }

    /**
     * Вывод колёс по убыванию.
     */
    public void printDescendingNumberOfWheels(){
        ArrayList<Long> ar = new ArrayList<>();
        for(int i=0;i<C.size();i++){
            ar.add(C.get(i).getNumberOfWheels());
        }
        Collections.sort(ar,Collections.reverseOrder());
        System.out.println(ar);
    }
}