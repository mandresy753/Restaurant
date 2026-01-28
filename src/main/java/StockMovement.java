import java.time.Instant;

public class StockMovement {
    private int id;
    private StockValue value;
    private MovementType type;
    private Instant creationDateTime;

    public StockMovement(int id, StockValue value, MovementType type, Instant creationDateTime) {
        this.id = id;
        this.value = value;
        this.type = type;
        this.creationDateTime = creationDateTime;
    }

    public StockMovement() {
        // Constructeur vide pour initialisation sans valeurs
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public StockValue getValue() {
        return value;
    }

    public void setValue(StockValue value) {
        this.value = value;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public Instant getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Instant creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", value=" + value +
                ", type=" + type +
                ", creationDateTime=" + creationDateTime +
                '}';
    }
}
