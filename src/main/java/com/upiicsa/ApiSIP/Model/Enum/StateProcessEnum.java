package com.upiicsa.ApiSIP.Model.Enum;

import lombok.Getter;

public enum StateProcessEnum {
    REGISTERED(1, "REGISTRADO", 2),
    INITIAL_DOC(2, "DOC_INICIAL", 3),
    LETTERS(3, "CARTAS", 4),
    FINAL_DOC(4, "DOC_FINAL", 5),
    RELEASED(5, "LIBERADO", -1),
    CANCELLATION(6, "BAJA",  -1);

    @Getter
    private int id;
    @Getter
    private String name;
    @Getter
    private int nextId;

    StateProcessEnum(int id, String name, int nextId) {
        this.id = id;
        this.name = name;
        this.nextId = nextId;
    }

    public static StateProcessEnum fromId(int id) {
        for (StateProcessEnum state : values()) {
            if (state.getId() == id) return state;
        }
        throw new IllegalArgumentException("ID for state not valid: " + id);
    }

    public static StateProcessEnum fromName(String name) {
        for (StateProcessEnum state : values()) {
            if (state.getName().equals(name)) return state;
        }
        throw new IllegalArgumentException("Name for state not valid: " + name);
    }

}
