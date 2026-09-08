package plang.translator;

public final class ParseResult {
    private final String sourceName;
    private final LineNumberMap lineNumberMap;
    private final Ast.Compound ast;

    public ParseResult(String sourceName, LineNumberMap lineNumberMap, Ast.Compound ast) {
        this.sourceName = sourceName;
        this.lineNumberMap = lineNumberMap;
        this.ast = ast;
    }

    public String getSourceName() {
        return sourceName;
    }

    public LineNumberMap getLineNumberMap() {
        return lineNumberMap;
    }

    public Ast.Compound getAst() {
        return ast;
    }
}
