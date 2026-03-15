package com.upiicsa.ApiSIP.Model.Enum;

import lombok.Getter;

public enum StateProcessEnum {
    REGISTERED(1, "REGISTRADO", -1, 2),
    INITIAL_DOC(2, "DOC_INICIAL", 1, 3),
    LETTERS(3, "CARTAS", 2, 4),
    FINAL_DOC(4, "DOC_FINAL", 3, 5),
    RELEASED(5, "LIBERADO", 4, -1),
    CANCELLATION(6, "BAJA", -1, -1);

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
