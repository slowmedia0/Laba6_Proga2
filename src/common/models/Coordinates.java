package common.models;

/**
 * Класс координат транспортного средства.
 */
public class Coordinates {
    private Long x;
    private Double y;

    /**
     * Создаёт объект координат.
     *
     * @param x значение координаты X
     * @param y значение координаты Y
     */
    public Coordinates(Long x,Double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Возвращает значение координаты X.
     *
     * @return координата X
     */
    public Long getX() {
        return x;
    }

    /**
     * Устанавливает значение координаты X.
     *
     * @param x новое значение координаты X
     */
    public void setX(Long x) {
        this.x = x;
    }

    /**
     * Возвращает значение координаты Y.
     *
     * @return координата Y
     */
    public Double getY() {
        return y;
    }

    /**
     * Устанавливает значение координаты Y.
     *
     * @param y новое значение координаты Y
     */
    public void setY(Double y) {
        this.y = y;
    }

    /**
     * Возвращает строковое представление объекта координат.
     *
     * @return строковое представление координат
     */
    @Override
    public String toString(){
        return "Coordinates { x = " + x + "; y = " + y +" }";
    }
}