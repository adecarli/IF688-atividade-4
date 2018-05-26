package br.ufpe.cin.if688.minijava.visitor;

import br.ufpe.cin.if688.minijava.antlr.MiniJavaParser;
import br.ufpe.cin.if688.minijava.antlr.MiniJavaVisitor;
import br.ufpe.cin.if688.minijava.ast.*;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class MJVisitor implements MiniJavaVisitor {

    @Override
    public Object visitGoal(MiniJavaParser.GoalContext ctx) {
        MainClass main = (MainClass) ctx.mainClass().accept(this);
        ClassDeclList classList = new ClassDeclList();

        for (MiniJavaParser.ClassDeclarationContext cdc: ctx.classDeclaration())
            classList.addElement((ClassDecl) cdc.accept(this));

        return new Program(main, classList);
    }

    @Override
    public Object visitMainClass(MiniJavaParser.MainClassContext ctx) {
        Identifier id1 = (Identifier) ctx.identifier(0).accept(this);
        Identifier id2 = (Identifier) ctx.identifier(1).accept(this);
        Statement st = (Statement) ctx.statement().accept(this);
        return new MainClass(id1, id2, st);
    }

    @Override
    public Object visitClassDeclaration(MiniJavaParser.ClassDeclarationContext ctx) {
        ClassDecl c;
        VarDeclList varList = new VarDeclList();
        MethodDeclList methodList = new MethodDeclList();

        for (MiniJavaParser.VarDeclarationContext vdc: ctx.varDeclaration())
            varList.addElement((VarDecl) vdc.accept(this));

        for (MiniJavaParser.MethodDeclarationContext mdc: ctx.methodDeclaration())
            methodList.addElement((MethodDecl) mdc.accept(this));

        if (ctx.identifier().size() == 1) {
            Identifier id1 = (Identifier) ctx.identifier(0).accept(this);
            c = new ClassDeclSimple(id1, varList, methodList);
        } else {
            Identifier id1 = (Identifier) ctx.identifier(0).accept(this);
            Identifier id2 = (Identifier) ctx.identifier(1).accept(this);
            c = new ClassDeclExtends(id1, id2, varList, methodList);
        }

        return c;
    }

    @Override
    public Object visitVarDeclaration(MiniJavaParser.VarDeclarationContext ctx) {
        Type type = (Type) ctx.type().accept(this);
        Identifier id = (Identifier) ctx.identifier().accept(this);
        return new VarDecl(type, id);
    }

    @Override
    public Object visitMethodDeclaration(MiniJavaParser.MethodDeclarationContext ctx) {

        Type type = (Type) ctx.type(0).accept(this);
        Identifier id = (Identifier) ctx.identifier(0).accept(this);
        FormalList formalList = new FormalList();
        VarDeclList varList = new VarDeclList();
        StatementList statementList = new StatementList();
        Exp exp = (Exp) ctx.expression().accept(this);

        for (int i = 1; i < ctx.identifier().size(); i++) {
            Type t1 = (Type) ctx.type(i).accept(this);
            Identifier i1 = (Identifier) ctx.identifier(i).accept(this);
            formalList.addElement(new Formal(t1, i1));
        }

        for (MiniJavaParser.VarDeclarationContext vdc: ctx.varDeclaration())
            varList.addElement((VarDecl) vdc.accept(this));

        for (MiniJavaParser.StatementContext s: ctx.statement())
            statementList.addElement((Statement) s.accept(this));


        return new MethodDecl(type, id, formalList, varList, statementList, exp);
    }

    @Override
    public Object visitType(MiniJavaParser.TypeContext ctx) {
        switch (ctx.getText()) {
            case "boolean":
                return new BooleanType();
            case "int":
                return new IntegerType();
            case "int[]":
                return new IntArrayType();
            default:
                return new IdentifierType(ctx.getText());
        }
    }

    @Override
    public Object visitStatement(MiniJavaParser.StatementContext ctx) {
        switch (ctx.getStart().getText()) {
            case "{":
                StatementList statementList = new StatementList();
                for (MiniJavaParser.StatementContext sc: ctx.statement())
                    statementList.addElement((Statement) sc.accept(this));

                return new Block(statementList);
            case "if":
                Exp exp = (Exp) ctx.expression(0).accept(this);
                Statement st1 = (Statement) ctx.statement(0).accept(this);
                Statement st2 = (Statement) ctx.statement(1).accept(this);
                return new If(exp, st1, st2);
            case "while":
                Exp exp1 = (Exp) ctx.expression(0).accept(this);
                Statement st = (Statement) ctx.statement(0).accept(this);
                return new While(exp1, st);
            case "System.out.println":
                return new Print((Exp) ctx.expression(0).accept(this));
            default:
                if (ctx.expression().size() == 1) {
                    Identifier id = (Identifier) ctx.identifier().accept(this);
                    Exp exp2 = (Exp) ctx.expression(0).accept(this);
                    return new Assign(id, exp2);
                } else {
                    Identifier id = (Identifier) ctx.identifier().accept(this);
                    Exp exp2 = (Exp) ctx.expression(0).accept(this);
                    Exp exp3 = (Exp) ctx.expression(1).accept(this);
                    return new ArrayAssign(id, exp2, exp3);
                }
        }
    }

    @Override
    public Object visitExpression(MiniJavaParser.ExpressionContext ctx) {
        if (ctx.getChild(0) instanceof MiniJavaParser.ExpressionContext) {
            if (ctx.getChild(1).getText().equals("[")) {
                Exp exp1 = (Exp) ctx.expression(0).accept(this);
                Exp exp2 = (Exp) ctx.expression(1).accept(this);
                return new ArrayLookup(exp1, exp2);
            } else if (ctx.getChild(1).getText().equals(".")) {
                if (ctx.getChild(2) instanceof MiniJavaParser.IdentifierContext) {
                    Exp exp1 = (Exp) ctx.expression(0).accept(this);
                    Identifier id = (Identifier) ctx.identifier().accept(this);
                    ExpList expList = new ExpList();
                    for (int i = 1; i < ctx.expression().size(); i++) {
                        expList.addElement((Exp) ctx.expression(i).accept(this));
                    }
                    return new Call(exp1, id, expList);
                } else {
                    return new ArrayLength((Exp) ctx.expression(0).accept(this));
                }
            } else {
                Exp exp1 = (Exp) ctx.expression(0).accept(this);
                Exp exp2 = (Exp) ctx.expression(1).accept(this);
                switch (ctx.getChild(1).getText()) {
                    case "&&":
                        return new And(exp1, exp2);
                    case "<":
                        return new LessThan(exp1, exp2);
                    case "+":
                        return new Plus(exp1, exp2);
                    case "-":
                        return new Minus(exp1, exp2);
                    case "*":
                        return new Times(exp1, exp2);
                }
            }
        } else if (ctx.getChild(0) instanceof MiniJavaParser.IdentifierContext) {
            return new IdentifierExp(((Identifier) ctx.identifier().accept(this)).toString());
        } else {
            if (ctx.getChild(0).getText().equals("true")) {
                return new True();
            } else if (ctx.getChild(0).getText().equals("false")) {
                return new False();
            } else if (ctx.getChild(0).getText().equals("this")) {
                return new This();
            } else if (ctx.getChild(0).getText().equals("new")) {
                if (ctx.getChild(1) instanceof MiniJavaParser.IdentifierContext) {
                    return new NewObject((Identifier) ctx.identifier().accept(this));
                } else {
                    return new NewArray((Exp) ctx.expression(0).accept(this));
                }
            } else if (ctx.getChild(0).getText().equals("!")) {
                return new Not((Exp) ctx.expression(0).accept(this));
            } else if (ctx.getChild(0).getText().equals("(")) {
                return (Exp) ctx.expression(0).accept(this);
            } else {
                return new IntegerLiteral(Integer.valueOf(ctx.INTEGER().getText()));
            }
        }
        return null;
    }

    @Override
    public Object visitIdentifier(MiniJavaParser.IdentifierContext ctx) {
        return new Identifier(ctx.getText());
    }

    @Override
    public Object visit(ParseTree tree) {
        return tree.accept(this);
    }

    @Override
    public Object visitChildren(RuleNode node) {
        return null;
    }

    @Override
    public Object visitTerminal(TerminalNode node) {
        return null;
    }

    @Override
    public Object visitErrorNode(ErrorNode node) {
        return null;
    }
}
