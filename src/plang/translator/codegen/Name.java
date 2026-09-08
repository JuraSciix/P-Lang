package plang.translator.codegen;

/**
 * Унифицированный объект идентификатора.
 * Создается парсером строго в единственном экземпляре на каждое значение {@code name}.
 */
public final class Name {
    private final String value;
    private Items.StableItem mItem = null;

    public Name(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    boolean hasItem() {
        return mItem != null;
    }

    Items.StableItem item() {
        return mItem;
    }

    void setItem(Items.StableItem item) {
        this.mItem = item;
    }
}
