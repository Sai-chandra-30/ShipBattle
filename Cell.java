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
            default:         return "~";
        }
    }
 
    // Returns the hex background color for this cell
    public String getColor() {
        switch (this) {
            case CARRIER:    return "#1c8eb7ff";
            case BATTLESHIP: return "#8bb71cff";
            case DESTROYER:  return "#b71c76ff";
            case SUBMARINE:  return "#1c43b7ff";
            case FRIGATE:    return "#f9a216ff";
            case HIT:        return "#B71C1C";
            case MISS:       return "#ECEFF1";
            default:         return "#ffffffd5";
        }
    }

    // Returns white text for dark backgrounds, dark text for light
    public String getTextColor() {
        return (this == WATER || this == MISS) ? "#37474F" : "white";
    }
}