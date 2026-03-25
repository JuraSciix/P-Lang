package plang.interpreter;

public interface OPCodeList {

    // ПК - Пул констант.
    // Кадр - память функции, отведенная под переменные.
    // 3 регистра: X, Y, Z

    // Бинарные операции берут значения из регистров X и Y соответственно,
    // а результат помещают в X.

    // Операция shift смещает все значения вправо:
    // X -> Y
    // Y -> Z
    // Z -> X

    // Операция rshift смещает все значения влево:
    // X -> Z
    // Y -> X
    // Z -> Y

    int add = 0;
    int sub = 0;
    int mul = 0;
    int div = 0;
    int rem = 0;
    int shift = 0;
    int rshift = 0;

    int _return = 0;
}
