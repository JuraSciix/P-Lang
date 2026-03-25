package plang.translator;

import plang.translator.Ast.Stmt;

import java.util.Collections;
import java.util.List;

public final class ParserResult {
    private final String sourceName;
    private final LineNumberMap lineNumberMap;
    private final List<Stmt> statements;

    public ParserResult(String sourceName, LineNumberMap lineNumberMap, List<Stmt> statements) {
        this.sourceName = sourceName;
        this.lineNumberMap = lineNumberMap;
        this.statements = statements;
    }

    public String getSourceName() {
        return sourceName;
    }

    public LineNumberMap getLineNumberMap() {
        return lineNumberMap;
    }

    public List<Stmt> getStatements() {
        return Collections.unmodifiableList(statements);
    }
}
