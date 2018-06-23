package br.ufpe.cin.if688.minijava.visitor;

import br.ufpe.cin.if688.minijava.ast.And;
import br.ufpe.cin.if688.minijava.ast.ArrayAssign;
import br.ufpe.cin.if688.minijava.ast.ArrayLength;
import br.ufpe.cin.if688.minijava.ast.ArrayLookup;
import br.ufpe.cin.if688.minijava.ast.Assign;
import br.ufpe.cin.if688.minijava.ast.Block;
import br.ufpe.cin.if688.minijava.ast.BooleanType;
import br.ufpe.cin.if688.minijava.ast.Call;
import br.ufpe.cin.if688.minijava.ast.ClassDeclExtends;
import br.ufpe.cin.if688.minijava.ast.ClassDeclSimple;
import br.ufpe.cin.if688.minijava.ast.False;
import br.ufpe.cin.if688.minijava.ast.Formal;
import br.ufpe.cin.if688.minijava.ast.Identifier;
import br.ufpe.cin.if688.minijava.ast.IdentifierExp;
import br.ufpe.cin.if688.minijava.ast.IdentifierType;
import br.ufpe.cin.if688.minijava.ast.If;
import br.ufpe.cin.if688.minijava.ast.IntArrayType;
import br.ufpe.cin.if688.minijava.ast.IntegerLiteral;
import br.ufpe.cin.if688.minijava.ast.IntegerType;
import br.ufpe.cin.if688.minijava.ast.LessThan;
import br.ufpe.cin.if688.minijava.ast.MainClass;
import br.ufpe.cin.if688.minijava.ast.MethodDecl;
import br.ufpe.cin.if688.minijava.ast.Minus;
import br.ufpe.cin.if688.minijava.ast.NewArray;
import br.ufpe.cin.if688.minijava.ast.NewObject;
import br.ufpe.cin.if688.minijava.ast.Not;
import br.ufpe.cin.if688.minijava.ast.Plus;
import br.ufpe.cin.if688.minijava.ast.Print;
import br.ufpe.cin.if688.minijava.ast.Program;
import br.ufpe.cin.if688.minijava.ast.This;
import br.ufpe.cin.if688.minijava.ast.Times;
import br.ufpe.cin.if688.minijava.ast.True;
import br.ufpe.cin.if688.minijava.ast.VarDecl;
import br.ufpe.cin.if688.minijava.ast.While;
import br.ufpe.cin.if688.minijava.symboltable.Class;
import br.ufpe.cin.if688.minijava.symboltable.Method;
import br.ufpe.cin.if688.minijava.symboltable.SymbolTable;

public class BuildSymbolTableVisitor implements IVisitor<Void> {

    SymbolTable symbolTable;

    public BuildSymbolTableVisitor() {
        symbolTable = new SymbolTable();
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    private Class currClass;
    private Method currMethod;

    // MainClass m;
    // ClassDeclList cl;
    public Void visit(Program n) {
        // Visita a classe que contem o main()
        n.m.accept(this);
        // Visita as classes declaradas posteriormente
        for (int i = 0; i < n.cl.size(); i++) {
            n.cl.elementAt(i).accept(this);
        }
        return null;
    }

    // Identifier i1,i2;
    // Statement s;
    public Void visit(MainClass n) {
        String mainId = n.i1.toString();
        this.symbolTable.addClass(mainId, null);
        this.currClass = this.symbolTable.getClass(mainId);
        this.currClass.addMethod("main", null);
        this.currMethod = this.currClass.getMethod("main");
        n.i1.accept(this);

        String paramId = n.i2.toString();
        this.currMethod.addParam(paramId, null);
        n.i2.accept(this);

        n.s.accept(this);
        // Após visitar a classe que contem o main(), "sai" de todos os métodos e da classe
        this.currMethod = null;
        this.currClass = null;
        return null;
    }

    // Identifier i;
    // VarDeclList vl;
    // MethodDeclList ml;
    public Void visit(ClassDeclSimple n) {
//        String parentId = this.currClass != null ? this.currClass.getId() : null;
        String classId = n.i.toString();
        if(!this.symbolTable.addClass(classId, null)) {
            System.err.println("Erro: Classe já declarada: <" + classId + ">");
            return null;
        }
        Class prevClass = this.currClass;
        this.currClass = this.symbolTable.getClass(classId);
        n.i.accept(this);
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }
        // Sai da classe
        this.currClass = prevClass;
        return null;
    }

    // Identifier i;
    // Identifier j;
    // VarDeclList vl;
    // MethodDeclList ml;
    public Void visit(ClassDeclExtends n) {
//        String parentId = this.currClass != null ? this.currClass.getId() : null;
        String classId = n.i.toString();
        String classExtId = n.j.toString();
        if (!this.symbolTable.containsClass(classExtId)) {
            System.err.println("Erro: Classe não declarada: <" + classExtId + ">");
            return null;
        }
        if(!this.symbolTable.addClass(classId, classExtId)) {
            System.err.println("Erro: Classe já declarada: <" + classId + ">");
            return null;
        }
        Class prevClass = this.currClass;
        this.currClass = this.symbolTable.getClass(classId);
        n.i.accept(this);
        n.j.accept(this);
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }
        this.currClass = prevClass;
        return null;
    }

    // Type t;
    // Identifier i;
    public Void visit(VarDecl n) {
        String id = n.i.toString();
        if (this.currMethod == null) {
            if(!this.currClass.addVar(id, n.t)){
                System.err.println("Variavel <" + id + "> já declarada na classe <" + this.currClass.getId() + ">");
                return null;
            }
        }
        else {
            if (!this.currMethod.addVar(id, n.t)) {
                System.err.println("Variavel <" + id + "> já declarada no método <" + this.currMethod.getId() + ">");
                return null;
            }
        }
        n.t.accept(this);
        n.i.accept(this);
        return null;
    }

    // Type t;
    // Identifier i;
    // FormalList fl;
    // VarDeclList vl;
    // StatementList sl;
    // Exp e;
    public Void visit(MethodDecl n) {
        String methodId = n.i.toString();
        if (!this.currClass.addMethod(methodId, n.t)) {
            System.err.println("Erro: Método <" + methodId + "> já foi declarado na classe <" + this.currClass.getId() + ">");
            return null;
        }
        this.currMethod = this.currClass.getMethod(methodId);
        n.t.accept(this);
        n.i.accept(this);
        for (int i = 0; i < n.fl.size(); i++) {
            n.fl.elementAt(i).accept(this);
        }
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }
        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.elementAt(i).accept(this);
        }
        n.e.accept(this);
        this.currMethod = null;
        return null;
    }

    // Type t;
    // Identifier i;
    public Void visit(Formal n) {
        String id = n.i.toString();
        if (!this.currMethod.addParam(id, n.t)) {
            System.err.println("Erro: Parãmetro <" + id + "> já foi declarado no método <" + this.currMethod.getId() + ">");
            return null;
        }
        n.t.accept(this);
        n.i.accept(this);
        return null;
    }

    public Void visit(IntArrayType n) {
        return null;
    }

    public Void visit(BooleanType n) {
        return null;
    }

    public Void visit(IntegerType n) {
        return null;
    }

    // String s;
    public Void visit(IdentifierType n) {
        return null;
    }

    // StatementList sl;
    public Void visit(Block n) {
        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.elementAt(i).accept(this);
        }
        return null;
    }

    // Exp e;
    // Statement s1,s2;
    public Void visit(If n) {
        n.e.accept(this);
        n.s1.accept(this);
        n.s2.accept(this);
        return null;
    }

    // Exp e;
    // Statement s;
    public Void visit(While n) {
        n.e.accept(this);
        n.s.accept(this);
        return null;
    }

    // Exp e;
    public Void visit(Print n) {
        n.e.accept(this);
        return null;
    }

    // Identifier i;
    // Exp e;
    public Void visit(Assign n) {
        n.i.accept(this);
        n.e.accept(this);
        return null;
    }

    // Identifier i;
    // Exp e1,e2;
    public Void visit(ArrayAssign n) {
        n.i.accept(this);
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Exp e1,e2;
    public Void visit(And n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Exp e1,e2;
    public Void visit(LessThan n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Exp e1,e2;
    public Void visit(Plus n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Exp e1,e2;
    public Void visit(Minus n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Exp e1,e2;
    public Void visit(Times n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Exp e1,e2;
    public Void visit(ArrayLookup n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Exp e;
    public Void visit(ArrayLength n) {
        n.e.accept(this);
        return null;
    }

    // Exp e;
    // Identifier i;
    // ExpList el;
    public Void visit(Call n) {
        n.e.accept(this);
        n.i.accept(this);
        for (int i = 0; i < n.el.size(); i++) {
            n.el.elementAt(i).accept(this);
        }
        return null;
    }

    // int i;
    public Void visit(IntegerLiteral n) {
        return null;
    }

    public Void visit(True n) {
        return null;
    }

    public Void visit(False n) {
        return null;
    }

    // String s;
    public Void visit(IdentifierExp n) {
        return null;
    }

    public Void visit(This n) {
        return null;
    }

    // Exp e;
    public Void visit(NewArray n) {
        n.e.accept(this);
        return null;
    }

    // Identifier i;
    public Void visit(NewObject n) {
        return null;
    }

    // Exp e;
    public Void visit(Not n) {
        n.e.accept(this);
        return null;
    }

    // String s;
    public Void visit(Identifier n) {
        return null;
    }
}
