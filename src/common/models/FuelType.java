package common.models;

import java.io.Serializable;

/**
 * Перечисление возможных типов топлива транспортного средства.
 */
public enum FuelType implements Serializable {
    KEROSENE,
    ELECTRICITY,
    ALCOHOL,
    MANPOWER,
    PLASMA;
}