public enum Cell {
    WATER,
    CARRIER,
    BATTLESHIP,
    DESTROYER,
    SUBMARINE,
    FRIGATE,
    HIT,
    MISS;

    // Returns the single-character symbol for this cell
    public String getSymbol() {
        switch (this) {
            case CARRIER:    return "C";
            case BATTLESHIP: return "B";
            case DESTROYER:  return "D";
            case SUBMARINE:  return "S";
            case FRIGATE:    return "F";
            case HIT:        return "✕";
            case MISS:       return "○";
            default:         return " ";
        }
    }
 
    // Returns the hex background color for this cell
    public String getColor() {
        switch (this) {
            case CARRIER:    return "#F4A636";
            case BATTLESHIP: return "#E8D44D";
            case DESTROYER:  return "#9be3a6ff";
            case SUBMARINE:  return "#C97FE8";
            case FRIGATE:    return "#F47B6E";
            case HIT:        return "#B71C1C";
            case MISS:       return "#ECEFF1";
            default:         return "#ffffffd5";
        }
    }

    // Returns white text for dark backgrounds, dark text for light
    public String getTextColor() {
        return (this == WATER || this == MISS) ? "#000000ff" : "white";
    }
} 