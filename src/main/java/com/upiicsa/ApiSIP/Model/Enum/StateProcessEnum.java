package com.upiicsa.ApiSIP.Model.Enum;

import lombok.Getter;

public enum StateProcessEnum {
    REGISTERED(1, "Registrado", -1, 2),
    INITIAL_DOC(2, "Doc Inicial", 1, 3),
    ACCEPTANCE(3, "Carta Aceptacion", 2, 4),
    FINAL_DOC(4, "Doc Final", 3, -1),
    CANCELLATION(5, "Baja", -1, -1);

    @Getter
    private int id;
    @Getter
    private String name;
    @Getter
    private int previousId;
    @Getter
    private int nextId;

    StateProcessEnum(int id, String name, int previousId, int nextId) {
        this.id = id;
        this.name = name;
        this.previousId = previousId;
        this.nextId = nextId;
    }

    public static StateProcessEnum fromId(int id) {
        for (StateProcessEnum state : values()) {
            if (state.getId() == id) return state;
        }
        throw new IllegalArgumentException("ID de estado no válido: " + id);
    }

}
