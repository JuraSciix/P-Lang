package plang.translator;

/**
 * Данные, примыкающие к токенам.
 * <p>
 * Между токенами и позицияими сохраняется биективность,
 * поэтому поиск данных осуществляется по позиции.
 */
public final class TokenData {
    public final int pos;
    public final CharSequence data;

    public TokenData(int pos, CharSequence data) {
        this.pos = pos;
        this.data = data;
    }
}
