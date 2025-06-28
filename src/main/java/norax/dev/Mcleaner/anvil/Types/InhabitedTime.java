package norax.dev.Mcleaner.anvil.Types;

public record InhabitedTime(long ticks) {

    public long getSeconds() {
        return ticks / 20; // TODO: changeable tps maybe?
    }

}
