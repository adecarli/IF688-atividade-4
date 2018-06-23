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
import br.ufpe.cin.if688.minijava.ast.Type;
import br.ufpe.cin.if688.minijava.ast.VarDecl;
import br.ufpe.cin.if688.minijava.ast.While;
import br.ufpe.cin.if688.minijava.symboltable.Method;
import br.ufpe.cin.if688.minijava.symboltable.SymbolTable;
import br.ufpe.cin.if688.minijava.symboltable.Class;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.Enumeration;

public class TypeCheckVisitor implements IVisitor<Type> {

    private SymbolTable symbolTable;
    private Class currClass;
    private Method currMethod;

    public TypeCheckVisitor(SymbolTable st) {
        symbolTable = st;
    }

    // MainClass m;
    // ClassDeclList cl;
    public Type visit(Program n) {
        n.m.accept(this);
        for (int i = 0; i < n.cl.size(); i++) {
            n.cl.elementAt(i).accept(this);
        }
        return null;
    }

    // Identifier i1,i2;
    // Statement s;
    public Type visit(MainClass n) {
        this.currClass = this.symbolTable.getClass(n.i1.toString());
        this.currMethod = this.currClass.getMethod("main");
        n.i1.accept(this);
        n.i2.accept(this);
        n.s.accept(this);
        this.currClass = null;
        this.currMethod = null;
        return null;
    }

    // Identifier i;
    // VarDeclList vl;
    // MethodDeclList ml;
    public Type visit(ClassDeclSimple n) {
        Class prevClass = this.currClass;
        this.currClass = this.symbolTable.getClass(n.i.toString());
        n.i.accept(this);
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }
        this.currClass = prevClass;
        return null;
    }

    // Identifier i;
    // Identifier j;
    // VarDeclList vl;
    // MethodDeclList ml;
    public Type visit(ClassDeclExtends n) {
        Class prevClass = this.currClass;
        this.currClass = this.symbolTable.getClass(n.i.toString());
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
    public Type visit(VarDecl n) {
        Type type = n.t.accept(this);
        n.i.accept(this);
        return type;
    }

    // Type t;
    // Identifier i;
    // FormalList fl;
    // VarDeclList vl;
    // StatementList sl;
    // Exp e;
    public Type visit(MethodDecl n) {
        this.currMethod = this.currClass.getMethod(n.i.toString());
        Type methodType = n.t.accept(this);

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
        Type returnType = n.e.accept(this);
        if (!this.symbolTable.compareTypes(methodType, returnType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo do método diferente do retorno.");
            System.out.println("Tipo de <" + n.i.toString() + "> é " + methodType.toString());
            System.out.println("Tipo de retorno é " + returnType);
            return null;
        }
        this.currMethod = null;
        return null;
    }

    // Type t;
    // Identifier i;
    public Type visit(Formal n) {
        Type type = n.t.accept(this);
        n.i.accept(this);
        return type;
    }

    public Type visit(IntArrayType n) {
        return n;
    }

    public Type visit(BooleanType n) {
        return n;
    }

    public Type visit(IntegerType n) {
        return n;
    }

    // String s;
    public Type visit(IdentifierType n) {
        return n;
    }

    // StatementList sl;
    public Type visit(Block n) {
        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.elementAt(i).accept(this);
        }
        return null;
    }

    // Exp e;
    // Statement s1,s2;
    public Type visit(If n) {
        Type type = n.e.accept(this);
        if (!(type instanceof BooleanType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: o tipo da expressão no IF <" + type + "> não condiz com tipo Bool");
            return null;
        }
        n.s1.accept(this);
        n.s2.accept(this);
        return null;
    }

    // Exp e;
    // Statement s;
    public Type visit(While n) {
        Type type = n.e.accept(this);
        if (!(type instanceof BooleanType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: o tipo da expressão no WHILE <" + type + "> não condiz com tipo Bool");
            return null;
        }
        n.s.accept(this);
        return null;
    }

    // Exp e;
    public Type visit(Print n) {
        n.e.accept(this);
        return null;
    }

    // Identifier i;
    // Exp e;
    public Type visit(Assign n) {
        Type idType = n.i.accept(this);
        Type expType = n.e.accept(this);
        if (!this.symbolTable.compareTypes(idType, expType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo da variavel <" + n.i.toString() + "> incompativel com <" + expType + ">");
            return null;
        }
        return idType;
    }

    // Identifier i;
    // Exp e1,e2;
    public Type visit(ArrayAssign n) {
        Type arrayType = n.i.accept(this);
        Type valueType = n.e2.accept(this);
        Type expType = n.e1.accept(this);
        if (!(arrayType instanceof IntArrayType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: <" + n.i.toString() + "> não é um Array");
            return null;
        }
        if (!(expType instanceof IntegerType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo da expressão não é um Integer");
            return null;
        }
        if (!(valueType instanceof IntegerType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo do array <" + n.i.toString() + "> incompativel com <" + valueType + ">");
            return null;
        }
        return new IntegerType();
    }

    // Exp e1,e2;
    public Type visit(And n) {
        Type t1 = n.e1.accept(this);
        if (!(t1 instanceof BooleanType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo da expressão1 do AND <" + t1 + "> não é Bool");
            return null;
        }
        Type t2 = n.e2.accept(this);
        if (!(t2 instanceof BooleanType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo da expressão2 do AND <" + t2 + "> não é Bool");
            return null;
        }
        return new BooleanType();
    }

    // Exp e1,e2;
    public Type visit(LessThan n) {
        Type t1 = n.e1.accept(this);
        if (!(t1 instanceof IntegerType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo da expressão1 do LT <" + t1 + "> não é Int");
            return null;
        }
        Type t2 = n.e2.accept(this);
        if (!(t2 instanceof IntegerType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo da expressão2 do LT <" + t2 + "> não é Int");
            return null;
        }
        return new BooleanType();
    }

    // Exp e1,e2;
    public Type visit(Plus n) {
        Type t1 = n.e1.accept(this);
        if (!(t1 instanceof IntegerType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo da expressão1 do PLUS <" + t1 + "> não é Int");
            return null;
        }
        Type t2 = n.e2.accept(this);
        if (!(t2 instanceof IntegerType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo da expressão2 do PLUS <" + t2 + "> não é Int");
            return null;
        }
        return new IntegerType();
    }

    // Exp e1,e2;
    public Type visit(Minus n) {
        Type t1 = n.e1.accept(this);
        if (!(t1 instanceof IntegerType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo da expressão1 do MINUS <" + t1 + "> não é Int");
            return null;
        }
        Type t2 = n.e2.accept(this);
        if (!(t2 instanceof IntegerType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo da expressão2 do MINUS <" + t2 + "> não é Int");
            return null;
        }
        return new IntegerType();
    }

    // Exp e1,e2;
    public Type visit(Times n) {
        Type t1 = n.e1.accept(this);
        if (!(t1 instanceof IntegerType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo da expressão1 do TIMES <" + t1 + "> não é Int");
            return null;
        }
        Type t2 = n.e2.accept(this);
        if (!(t2 instanceof IntegerType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo da expressão2 do TIMES <" + t2 + "> não é Int");
            return null;
        }
        return new IntegerType();
    }

    // Exp e1,e2;
    public Type visit(ArrayLookup n) {
        Type t1 = n.e1.accept(this);
        if (!(t1 instanceof IntArrayType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: <" + t1 + "> não é um Array de Int");
            return null;
        }
        Type t2 = n.e2.accept(this);
        if (!(t2 instanceof IntegerType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: <" + t1 + "> não é um inteiro");
            return null;
        }
        return new IntegerType();
    }

    // Exp e;
    public Type visit(ArrayLength n) {
        Type t1 = n.e.accept(this);
        if (!(t1 instanceof IntArrayType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: <" + t1 + "> não é um Array de Int");
            return null;
        }
        return new IntegerType();
    }

    // Exp e;
    // Identifier i;
    // ExpList el;
    public Type visit(Call n) {
        Type expType = n.e.accept(this);
        if (!(expType instanceof IdentifierType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: <" + expType + "> não é uma Classe");
            return null;
        }
        String classId = ((IdentifierType) expType).s;
        if (!(this.symbolTable.containsClass(classId))) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Classe <" + classId + "> não encontrada");
            return null;
        }
        Class tempClass = this.symbolTable.getClass(classId);
        String methodId = n.i.toString();
//        if (!(methodType instanceof IdentifierType)) {
//            System.out.println("ERRO");
//            System.out.println("CLASSE: " + this.currClass.getId());
//            System.out.println("METODO: " + this.currMethod.getId());
//            System.out.println("Erro: <" + methodType + "> não é um método");
//            return null;
//        }
//        String methodId = ((IdentifierType) methodType).s;
        Method tempMethod = tempClass.getMethod(methodId);
        Enumeration params = tempMethod.getParams();
        for (int i = 0; i < n.el.size(); i++) {
            Type paramType = n.el.elementAt(i).accept(this);
            if (!this.symbolTable.compareTypes(paramType, tempMethod.getParamAt(i).type())) {
                System.out.println("ERRO");
                System.out.println("CLASSE: " + this.currClass.getId());
                System.out.println("METODO: " + this.currMethod.getId());
                System.out.println("Erro: Parametro da funcao <" + methodId + "> não corresponde a <" + tempMethod.getParamAt(i).type());
                return null;
            }
        }
        return tempMethod.type();
    }

    // int i;
    public Type visit(IntegerLiteral n) {
        return new IntegerType();
    }

    public Type visit(True n) {
        return new BooleanType();
    }

    public Type visit(False n) {
        return new BooleanType();
    }

    // String s;
    public Type visit(IdentifierExp n) {
        return this.symbolTable.getVarType(this.currMethod, this.currClass, n.s);
    }

    public Type visit(This n) {
        return new IdentifierType(this.currClass.getId());
    }

    // Exp e;
    public Type visit(NewArray n) {
        Type t = n.e.accept(this);
        if (!(t instanceof IntegerType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Argumento do Array de Int é <" + t + "> e não Int");
        }
        return new IntArrayType();
    }

    // Identifier i;
    public Type visit(NewObject n) {
        if (!this.symbolTable.containsClass(n.i.toString())) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: <" + n.i.toString() + "> não é uma classe");
        }
        return new IdentifierType(n.i.toString());
    }

    // Exp e;
    public Type visit(Not n) {
        Type t = n.e.accept(this);
        if (!(t instanceof BooleanType)) {
            System.out.println("ERRO");
            System.out.println("CLASSE: " + this.currClass.getId());
            System.out.println("METODO: " + this.currMethod.getId());
            System.out.println("Erro: Tipo da expressão do NOT <" + t + "> não é Bool");
        }
        return new BooleanType();
    }

    // String s;
    public Type visit(Identifier n) {
        if (this.symbolTable.containsClass(n.toString())) {
            return new IdentifierType(n.toString());
        }
        if (this.currClass.containsMethod(n.toString())) {
            return this.currClass.getMethod(n.toString()).type();
        }
        if (this.currClass.containsVar(n.toString())) {
            return this.currClass.getVar(n.toString()).type();
        }
        return this.symbolTable.getVarType(this.currMethod, this.currClass, n.toString());
    }
}
