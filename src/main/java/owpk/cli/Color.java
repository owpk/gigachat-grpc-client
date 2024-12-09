package owpk.cli;

import lombok.Getter;

@Getter
public enum Color {
    NO_COLOR(""),
    RED("red"),
    GREEN("green"),
    YELLOW("yellow");
    
    private final String color;
    
    Color(String value) {
        this.color = value;
    }
}
