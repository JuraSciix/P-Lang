package plang.translator;

import plang.translator.Ast.Stmt;

import java.util.Collections;
import java.util.List;

public final class ParserResult {
    private final String sourceName;
    private final LineNumberMap lineNumberMap;
    private final Ast.Compound ast;

    public ParserResult(String sourceName, LineNumberMap lineNumberMap, Ast.Compound ast) {
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
