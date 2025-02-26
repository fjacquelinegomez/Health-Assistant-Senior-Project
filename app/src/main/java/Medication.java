public class Medication {
    private String expirationDate;
    private int pillCount;

    public Medication(String expirationDate, int pillCount) {
        this.expirationDate = expirationDate;
        this.pillCount = pillCount;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public int getPillCount() {
        return pillCount;
    }
}