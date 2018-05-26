package br.ufpe.cin.if688.minijava.main;

import br.ufpe.cin.if688.minijava.antlr.MiniJavaLexer;
import br.ufpe.cin.if688.minijava.antlr.MiniJavaParser;
import br.ufpe.cin.if688.minijava.ast.*;
import br.ufpe.cin.if688.minijava.visitor.MJVisitor;
import br.ufpe.cin.if688.minijava.visitor.PrettyPrintVisitor;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.FileInputStream;
import java.io.InputStream;

public class Main {

	public static void main(String[] args) {
		MainClass main = new MainClass(
				new Identifier("Teste"), 
				new Identifier("Testando"), 
				new Print(new IntegerLiteral(2))
		);
		
		VarDeclList vdl1 = new VarDeclList();
		vdl1.addElement(new VarDecl(
			new BooleanType(),
			new Identifier("flag")
		));
		vdl1.addElement(new VarDecl(
				new IntegerType(),
				new Identifier("num")
		));
		
		MethodDeclList mdl = new MethodDeclList();
		
		ClassDeclSimple A = new ClassDeclSimple(
					new Identifier("A"), vdl1, mdl
		);
		
		ClassDeclExtends B = new ClassDeclExtends(
				new Identifier("B"), new Identifier("A"), 
				new VarDeclList(), new MethodDeclList()
		);
		
		VarDeclList vdl2 = new VarDeclList();
		vdl2.addElement(new VarDecl(
				new IdentifierType("A"),
				new Identifier("obj")
		));
		ClassDeclSimple C = new ClassDeclSimple(
				new Identifier("C"), vdl2, new MethodDeclList()
		);
		
		ClassDeclList cdl = new ClassDeclList();
		cdl.addElement(A);
		cdl.addElement(B);
		cdl.addElement(C);


		//change test program here
        try {
            InputStream stream = new FileInputStream("src/main/java/test/BubbleSort.java");
            ANTLRInputStream input = new ANTLRInputStream(stream);
            MiniJavaLexer lexer = new MiniJavaLexer(input);
            CommonTokenStream token = new CommonTokenStream(lexer);

            Program p = (Program) new MJVisitor().visit(new MiniJavaParser(token).goal());

            PrettyPrintVisitor ppv = new PrettyPrintVisitor();
            ppv.visit(p);
        } catch (Exception e) {
            e.printStackTrace();
        }

	}

}
