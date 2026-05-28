package common.models;

import java.time.LocalDate;

/**
 * Класс транспортного средства.
 */
public class Vehicle implements Comparable<Vehicle> {
    private Integer id;
    private String name;
    private Coordinates coordinates;
    private java.time.LocalDate creationDate;
    private java.time.LocalDate lastUpdateDate = null;
    private float enginePower;
    private Long numberOfWheels;
    private VehicleType type;
    private FuelType fuelType;

    /**
     * Создаёт объект транспортного средства со всеми полями.
     *
     * @param id идентификатор
     * @param name название
     * @param coordinates координаты
     * @param creationDate дата создания
     * @param enginePower мощность двигателя
     * @param numberOfWheels количество колёс
     * @param type тип
     * @param fuelType тип топлива
     */
    public Vehicle(Integer id,String name, Coordinates coordinates, java.time.LocalDate creationDate, float enginePower, Long numberOfWheels, VehicleType type, FuelType fuelType) {
        this.id = id;
        this.name=name;
        this.coordinates=coordinates;
        this.creationDate=creationDate;
        this.enginePower=enginePower;
        this.numberOfWheels=numberOfWheels;
        this.type=type;
        this.fuelType=fuelType;
    }

    /**
     * Создаёт объект транспортного средства без указания автоматически генерируемых полей.
     *
     * @param name название
     * @param coordinates координаты
     * @param enginePower мощность двигателя
     * @param numberOfWheels количество колёс
     * @param type тип
     * @param fuelType тип топлива
     */
    public Vehicle(String name, Coordinates coordinates, float enginePower, Long numberOfWheels, VehicleType type, FuelType fuelType) {
        this.name=name;
        this.coordinates=coordinates;
        this.creationDate=LocalDate.now();
        this.enginePower=enginePower;
        this.numberOfWheels=numberOfWheels;
        this.type=type;
        this.fuelType=fuelType;
    }

    /**
     * Сравнивает текущее транспортное средство с другим по мощности двигателя.
     *
     * @param element объект для сравнения
     * @return результат сравнения
     */
    public int compareTo(Vehicle element){
        return this.name.compareTo(element.getName());
    }

    /**
     * Возвращает строковое представление объекта.
     *
     * @return строковое представление
     */
    @Override
    public String toString() {
        return "Vehicle { id = " + id +
                "; name = " + name +
                "; coordinates = " + coordinates.toString() +
                "; creationDate = " + creationDate +
                "; lastUpdateDate = " + lastUpdateDate +
                "; enginePower = " + enginePower +
                "; numberOfWheels = " + numberOfWheels +
                "; type = " + type +
                "; fuelType = " + fuelType + " }";
    }

    /**
     * Возвращает идентификатор.
     *
     * @return идентификатор
     */
    public Integer getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор.
     *
     * @param id новый идентификатор
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Возвращает название.
     *
     * @return название
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название.
     *
     * @param name новое название
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает координаты.
     *
     * @return координаты
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Устанавливает координаты.
     *
     * @param coordinates новые координаты
     */
    public void setFakeCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Возвращает дату создания.
     *
     * @return дата создания
     */
    public LocalDate getCreationDate() {
        return creationDate;
    }

    /**
     * Устанавливает дату создания.
     *
     * @param creationDate новая дата создания
     */
    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Возвращает мощность двигателя.
     *
     * @return мощность двигателя
     */
    public float getEnginePower() {
        return enginePower;
    }

    /**
     * Устанавливает мощность двигателя.
     *
     * @param enginePower новое значение
     */
    public void setEnginePower(float enginePower) {
        this.enginePower = enginePower;
    }

    /**
     * Возвращает количество колёс.
     *
     * @return количество колёс
     */
    public Long getNumberOfWheels() {
        return numberOfWheels;
    }

    /**
     * Устанавливает количество колёс.
     *
     * @param numberOfWheels новое количество колёс
     */
    public void setNumberOfWheels(Long numberOfWheels) {
        this.numberOfWheels = numberOfWheels;
    }

    /**
     * Возвращает тип транспортного средства.
     *
     * @return тип
     */
    public VehicleType getType() {
        return type;
    }

    /**
     * Устанавливает тип транспортного средства.
     *
     * @param type новый тип
     */
    public void setType(VehicleType type) {
        this.type = type;
    }

    /**
     * Возвращает тип топлива.
     *
     * @return тип топлива
     */
    public FuelType getFuelType() {
        return fuelType;
    }

    /**
     * Устанавливает тип топлива.
     *
     * @param fuelType новый тип топлива
     */
    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    /**
     * Выводит все характеристики объекта.
     */
    public void getAllChar() {
        System.out.println();
        System.out.println("id: " + this.id);
        System.out.println("name: " + this.name);
        System.out.println("coordinates: " + "x: " + this.coordinates.getX() + "; y: " + this.coordinates.getY());
        System.out.println("creationDate: " + this.creationDate);
        System.out.println("enginePower: " + this.enginePower);
        System.out.println("numberOfWheels: " + this.numberOfWheels);
        System.out.println("type: " + this.type);
        System.out.println("fuelType: " + this.fuelType);
    }

    /**
     * Генерирует дату создания объекта.
     *
     * @return текущая дата
     *
     */


    public LocalDate generateCreationDate(){
        return LocalDate.now();
    }

    public LocalDate getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDate lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
}