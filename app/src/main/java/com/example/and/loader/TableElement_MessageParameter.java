package com.example.and.loader;

/**
 * Created by Андрей on 02.06.2017.
 * -------------------------------
 * Таблица параметров команд
 * -------------------------------
 * name     -
 * value    -
 * -------------------------------
 */

public class TableElement_MessageParameter {
    private String  name;
    private String  value;
    public TableElement_MessageParameter() {

    }

    public TableElement_MessageParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
