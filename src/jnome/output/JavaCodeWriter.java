package jnome.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jnome.core.expression.ArrayAccessExpression;
import jnome.core.expression.ArrayCreationExpression;
import jnome.core.expression.ArrayInitializer;
import jnome.core.expression.ClassLiteral;
import jnome.core.expression.ConstructorInvocation;
import jnome.core.expression.DimensionInitializer;
import jnome.core.modifier.Default;
import jnome.core.modifier.StrictFP;
import jnome.core.modifier.Synchronized;
import jnome.core.type.ArrayType;

import org.rejuse.java.collections.RobustVisitor;
import org.rejuse.java.collections.Visitor;
import org.rejuse.predicate.PrimitivePredicate;
import org.rejuse.predicate.PrimitiveTotalPredicate;

import chameleon.core.MetamodelException;
import chameleon.core.compilationunit.CompilationUnit;
import chameleon.core.element.Element;
import chameleon.core.expression.ActualParameter;
import chameleon.core.expression.ConditionalAndExpression;
import chameleon.core.expression.ConditionalOrExpression;
import chameleon.core.expression.Expression;
import chameleon.core.expression.Invocation;
import chameleon.core.expression.Literal;
import chameleon.core.expression.NamedTarget;
import chameleon.core.expression.VariableReference;
import chameleon.core.method.Implementation;
import chameleon.core.method.Method;
import chameleon.core.method.NativeImplementation;
import chameleon.core.method.RegularImplementation;
import chameleon.core.method.exception.ExceptionClause;
import chameleon.core.method.exception.ExceptionDeclaration;
import chameleon.core.method.exception.TypeExceptionDeclaration;
import chameleon.core.modifier.Modifier;
import chameleon.core.namespace.NamespaceOrTypeReference;
import chameleon.core.namespacepart.DemandImport;
import chameleon.core.namespacepart.NamespacePart;
import chameleon.core.namespacepart.TypeImport;
import chameleon.core.statement.Block;
import chameleon.core.type.StaticInitializer;
import chameleon.core.type.Type;
import chameleon.core.type.TypeReference;
import chameleon.core.type.inheritance.InheritanceRelation;
import chameleon.core.variable.FormalParameter;
import chameleon.core.variable.RegularMemberVariable;
import chameleon.core.variable.Variable;
import chameleon.output.Syntax;
import chameleon.support.expression.ArrayIndex;
import chameleon.support.expression.AssignmentExpression;
import chameleon.support.expression.ClassCastExpression;
import chameleon.support.expression.ConditionalExpression;
import chameleon.support.expression.EmptyArrayIndex;
import chameleon.support.expression.FilledArrayIndex;
import chameleon.support.expression.InstanceofExpression;
import chameleon.support.expression.SuperConstructorDelegation;
import chameleon.support.expression.SuperTarget;
import chameleon.support.expression.ThisConstructorDelegation;
import chameleon.support.expression.ThisLiteral;
import chameleon.support.member.simplename.SimpleNameMethodSignature;
import chameleon.support.member.simplename.method.RegularMethodInvocation;
import chameleon.support.member.simplename.operator.infix.InfixOperatorInvocation;
import chameleon.support.member.simplename.operator.postfix.PostfixOperatorInvocation;
import chameleon.support.member.simplename.operator.prefix.PrefixOperatorInvocation;
import chameleon.support.modifier.Abstract;
import chameleon.support.modifier.Constructor;
import chameleon.support.modifier.Final;
import chameleon.support.modifier.Interface;
import chameleon.support.modifier.Private;
import chameleon.support.modifier.Protected;
import chameleon.support.modifier.Public;
import chameleon.support.modifier.Static;
import chameleon.support.statement.BreakStatement;
import chameleon.support.statement.CaseLabel;
import chameleon.support.statement.CatchClause;
import chameleon.support.statement.ContinueStatement;
import chameleon.support.statement.DefaultLabel;
import chameleon.support.statement.DoStatement;
import chameleon.support.statement.EmptyStatement;
import chameleon.support.statement.FinallyClause;
import chameleon.support.statement.ForStatement;
import chameleon.support.statement.IfThenElseStatement;
import chameleon.support.statement.LabeledStatement;
import chameleon.support.statement.LocalClassStatement;
import chameleon.support.statement.LocalVariableDeclarationStatement;
import chameleon.support.statement.ReturnStatement;
import chameleon.support.statement.StatementExprList;
import chameleon.support.statement.StatementExpression;
import chameleon.support.statement.SwitchCase;
import chameleon.support.statement.SwitchLabel;
import chameleon.support.statement.SwitchStatement;
import chameleon.support.statement.SynchronizedStatement;
import chameleon.support.statement.ThrowStatement;
import chameleon.support.statement.TryStatement;
import chameleon.support.statement.WhileStatement;
import chameleon.support.variable.LocalVariable;
import chameleon.tool.Arguments;

import com.sun.org.apache.bcel.internal.classfile.JavaClass;

/**
 * @author marko
 */
public class JavaCodeWriter extends Syntax {
  
  public String toCode(Element element) throws MetamodelException {
    String result = null;
    if(isAnonymousClass(element)) {
      result = toCodeAnonymousClass((Type) element);
    } else if(isClass(element)) {
      result = toCodeClass((Type)element);
    } else if(isInterface(element)) {
      result = toCodeInterface((Type)element);
    } else if(isModifier(element)) {
      result = toCodeModifier((Modifier)element);
    } else if(isBreak(element)) {
      result = toCodeBreak((BreakStatement)element);
    } else if(isConstructorInvocation(element)) {
      result = toCodeConstructorInvocation((ConstructorInvocation)element);
    } else if(isCondAnd(element)) {
      result = toCodeCondAnd((ConditionalAndExpression)element);
	} else if(isEmptyArrayIndex(element)) {
		result = toCodeEmptyArrayIndex((EmptyArrayIndex)element);
	} else if(isFilledArrayIndex(element)) {
		result = toCodeFilledArrayIndex((FilledArrayIndex)element);
    } else if(isCondOr(element)) {
      result = toCodeCondOr((ConditionalOrExpression)element);
    } else if(isInfixInvocation(element)) {
      result = toCodeInfixInvocation((InfixOperatorInvocation)element);
    } else if(isPrefixInvocation(element)) {
      result = toCodePrefixInvocation((PrefixOperatorInvocation)element);
    } else if(isPostfixInvocation(element)) {
      result = toCodePostfixInvocation((PostfixOperatorInvocation)element);
    } else if(isClassLiteral(element)) {
      result = toCodeClassLiteral((ClassLiteral)element);
    } else if(isThisLiteral(element)) {
      result = toCodeThisLiteral((ThisLiteral)element);
    } else if(isLiteral(element)) {
      result = toCodeLiteral((Literal)element);
    } else if(isNamedTarget(element)) {
      result = toCodeNamedTarget((NamedTarget)element);
    } else if(isRegulaMethodInvocation(element)) {
      result = toCodeRegularMethodInvocation((RegularMethodInvocation)element);
    } else if(isVarRef(element)) {
      result = toCodeVarRef((VariableReference)element);
    } else if(isThisConstructorDelegation(element)) {
      result = toCodeThisConstructorDelegation((ThisConstructorDelegation)element);
    } else if(isSuperTarget(element)) {
      result = toCodeSuperTarget((SuperTarget)element);
    } else if(isSuperConstructorDelegation(element)) {
      result = toCodeSuperConstructorDelegation((SuperConstructorDelegation)element);
    } else if(isInstanceOf(element)) {
      result = toCodeInstanceOf((InstanceofExpression)element);
    } else if(isDimInit(element)) {
      result = toCodeDimInit((DimensionInitializer)element);
    } else if(isCondExpr(element)) {
      result = toCodeCondExpr((ConditionalExpression)element);
    } else if(isCast(element)) {
      result = toCodeCast((ClassCastExpression)element);
    } else if(isArrayInit(element)) {
      result = toCodeArrayInit((ArrayInitializer)element);
    } else if(isArrayCreation(element)) {
      result = toCodeArrayCreation((ArrayCreationExpression)element);
    } else if(isArrayAccess(element)) {
      result = toCodeArrayAccess((ArrayAccessExpression)element);
    } else if(isIf(element)) {
      result = toCodeIf((IfThenElseStatement)element);
    } else if(isBlock(element)) {
      result = toCodeBlock((Block)element);
    } else if(isWhile(element)) {
      result = toCodeWhile((WhileStatement)element);
    } else if(isTry(element)) {
      result = toCodeTry((TryStatement)element);
    } else if(isThrow(element)) {
      result = toCodeThrow((ThrowStatement)element);
    } else if(isSynchronized(element)) {
      result = toCodeSynchronized((SynchronizedStatement)element);
    } else if(isSwitch(element)) {
      result = toCodeSwitch((SwitchStatement)element);
    } else if(isStatementExprList(element)) {
      result = toCodeStatementExprList((StatementExprList)element);
    } else if(isReturn(element)) {
      result = toCodeReturn((ReturnStatement)element);
    } else if(isLocalVar(element)) {
      result = toCodeLocalVar((LocalVariableDeclarationStatement)element);
    } else if(isLocalClass(element)) {
      result = toCodeLocalClass((LocalClassStatement)element);
    } else if(isLabeledStatement(element)) {
      result = toCodeLabeledStatement((LabeledStatement)element);
    } else if(isFor(element)) {
      result = toCodeFor((ForStatement)element);
    } else if(isContinue(element)) {
      result = toCodeContinue((ContinueStatement)element);
    } else if(isEmpty(element)) {
      result = toCodeEmptyStatement((EmptyStatement)element);
    } else if(isDo(element)) {
      result = toCodeDo((DoStatement)element);
    } else if(isStatementExpression(element)) {
      result = toCodeStatementExpression((StatementExpression)element);
    } else if(isAssignment(element)) {
      result = toCodeAssignment((AssignmentExpression)element);
    } else if(isMethod(element)) {
      result = toCodeMethod((Method)element);
    } else if(isMemberVariable(element)) {
      result = toCodeMemberVariable((RegularMemberVariable)element);
    } else if(isStaticInitializer(element)) {
      result = toCodeStaticInitializer((StaticInitializer)element);
    } else if(isCompilationUnit(element)) {
      result = toCodeCompilationUnit((CompilationUnit)element);
    } else if(isNamespaceOrTypeReference(element)) {
      result = toCodeNamespaceOrTypeReference((NamespaceOrTypeReference)element);
    } else if(isNamespacePart(element)) {
    	result = toCodeNamespacePart((NamespacePart) element);
    } else if(isActualParameter(element)) {
    	result = toCodeActualParameter((ActualParameter) element);
    }
    else if(element == null) {
      result = "";
    }
    else {
      throw new IllegalArgumentException("The given element is not know by the Java syntax: "+element.getClass().getName());
    }
    return result;
  }
  
  public boolean isActualParameter(Element element) {
  	return element instanceof ActualParameter;
  }
  
  public String toCodeActualParameter(ActualParameter parameter) throws MetamodelException {
  	return toCode(parameter.getExpression());
  }
  
  public boolean isNamespaceOrTypeReference(Element element) {
    return element instanceof NamespaceOrTypeReference;
  }
  
  public String toCodeNamespaceOrTypeReference(NamespaceOrTypeReference typeReference) throws MetamodelException {
    String result = toCode(typeReference.getTarget());
    if(result.length() > 0) {
      result = result + ".";
    }
    return result + typeReference.getName();
  }
  
  public boolean isMemberVariable(Element element) {
    return element instanceof RegularMemberVariable;
  }
  
  public boolean isStaticInitializer(Element element) {
    return element instanceof StaticInitializer;
  }
  
  public String toCodeStaticInitializer(StaticInitializer init) throws MetamodelException {
    return "static " + toCode(init.getBlock());
  }
  
 
  public JavaCodeWriter(){
	  _tabSize = 4;
  }
  
  public JavaCodeWriter(int tabSize) {
    _tabSize = tabSize;
  }
  
  private int _tabSize;
  
  public int getTabSize() {
    return _tabSize;
  }
  
  public int getIndent() {
    return _indent;
  }
  
  private int _indent;
  
  public void indent() {
    _indent += getTabSize();
  }
  
  public void undent() {
    _indent -= getTabSize();
  }
  
  public StringBuffer startLine() {
    StringBuffer result = new StringBuffer();
    int indent = getIndent();
    for(int i = 0; i < indent; i++) {
      result.append(' ');
    }
    return result;
  }
  
  public String wrapLine(String line) {
    return startLine() + line + "\n";
  }
  
  public boolean isCompilationUnit(Element element) {
    return element instanceof CompilationUnit;
  }
  
  public boolean isNamespacePart(Element element) {
  	return element instanceof NamespacePart;
  }
  
  public String toCodeNamespacePart(NamespacePart part) throws MetamodelException {
    StringBuffer result = new StringBuffer();
    result.append("package "+part.getDeclaredNamespace().getFullyQualifiedName() +";\n\n");
    
    Iterator iter = part.imports().iterator();
    while(iter.hasNext()) {
    	if(iter instanceof TypeImport) {
        result.append("import "+toCode(((TypeImport)iter.next()).getTypeReference()) +";\n");
    	}
    	else if(iter instanceof DemandImport) {
        result.append("import "+toCode(((DemandImport)iter.next()).getNamespaceOrTypeReference()) +".*;\n");
    	}
    }
    result.append("\n");
    Collection types = part.types();
    new PrimitiveTotalPredicate() {
      public boolean eval(Object o) {
        return !(o instanceof ArrayType);
      }
    }.filter(types);
    iter = types.iterator();
    while(iter.hasNext()) {
      result.append(toCode((Element)iter.next()));
      if(iter.hasNext()) {
        result.append("\n\n");
      }
    }
    return result.toString();
  }
  
  public String toCodeCompilationUnit(CompilationUnit cu) throws MetamodelException {
    StringBuffer result = new StringBuffer();
  	for(NamespacePart part: cu.getNamespaceParts()) {
  		result.append(toCodeNamespacePart(part));
  	}
  	return result.toString();
  }

  private String toCodeModifier(Modifier element) {
    if(element instanceof Public) {
      return "public";
    }
    else if(element instanceof Protected) {
      return "protected";
    }
    else if(element instanceof Private) {
      return "private";
    }
    else if(element instanceof Default) {
      return "";
    }
    else if(element instanceof Abstract) {
      return "abstract";
    }
    else if(element instanceof Static) {
      return "static";
    }
    else if(element instanceof Final) {
      return "final";
    }
    else if(element instanceof StrictFP) {
      return "strictfp";
    }
    else if(element instanceof Synchronized) {
      return "synchronized";
    }
    else if(element instanceof Constructor) {
    	return "";
    }
    else {
      throw new IllegalArgumentException("The given element is not know by the Java syntax: "+element.getClass().getName());
    }
  }

  public boolean isModifier(Element element) {
    return element instanceof Modifier;
  }

  public boolean isClass(Element element) {
    return (element instanceof JavaClass);
  }
  
  public boolean isInterface(Element element) {
    if(element instanceof Type){
    	return ((Type)element).hasModifier(new Interface());
    }
    return false;
    	
  }
  
  public String toCodeClassBlock(Type type) throws MetamodelException {
    try {
      
    final StringBuffer result = new StringBuffer();
    result.append("{\n");
    indent();
    
    Set members = type.directlyDeclaredElements();
    // Members
    new RobustVisitor() {
      public Object visit(Object element) throws MetamodelException {
        result.append(toCode((Element)element));
        result.append("\n\n");
        return null;
      }

      public void unvisit(Object element, Object undo) {
        //NOP
      }
    }.applyTo(members);
    
    undent();
    result.append(startLine());
    result.append("}");
    
    return result.toString();
    }
    catch (MetamodelException e) {
      throw e;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new Error();
    }
  }
  
  public boolean isAnonymousClass(Element element) {
    return (element instanceof Type) && (element.getParent() instanceof ConstructorInvocation);
  }
  
  public String toCodeAnonymousClass(Type type) throws MetamodelException {
    return toCodeClassBlock(type);
  }
  
  public String toCodeClass(Type type) throws MetamodelException {
    try {
    final StringBuffer result = startLine();
    
    //Modifiers
    
    new Visitor() {
      public void visit(Object element) {
        result.append((toCodeModifier((Modifier)element)));
        result.append(" ");
      }
    }.applyTo(type.modifiers());
    
    //Name
    result.append("class ");
    result.append(type.getName());
    List<InheritanceRelation> superTypes = type.inheritanceRelations();
    final List<TypeReference> classRefs = new ArrayList<TypeReference>();
    final List<TypeReference> interfaceRefs = new ArrayList<TypeReference>();
    for(InheritanceRelation rel:superTypes) {
    	TypeReference typeRef = rel.superClassReference();
      if(isClass(typeRef.getType())) {
        classRefs.add(typeRef);
      } else {
        interfaceRefs.add(typeRef);
      }
    }
    if(classRefs.size() > 0) {
      result.append(" extends ");
      result.append(toCode(classRefs.get(0)));
    }
    
    if(interfaceRefs.size() > 0) {
      result.append(" implements ");
      Iterator<TypeReference> iter = interfaceRefs.iterator();
      while(iter.hasNext()) {
        result.append(toCode(iter.next()));
        if(iter.hasNext()) {
          result.append(", ");
        }
      }
    }
    result.append(" ");
    result.append(toCodeClassBlock(type));
    
    return result.toString();
    }
    catch (MetamodelException e) {
      throw e;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new Error();
    }
    
  }

  public String toCodeInterface(Type type) throws MetamodelException {
    try {
    final StringBuffer result = startLine();
    
    //Modifiers
    
    new Visitor() {
      public void visit(Object element) {
        result.append((toCodeModifier((Modifier)element)));
        result.append(" ");
      }
    }.applyTo(type.modifiers());
    
    //Name
    result.append("interface ");
    result.append(type.getName());
    List<InheritanceRelation> superTypes = type.inheritanceRelations();
    new PrimitivePredicate<InheritanceRelation>() {
      public boolean eval(InheritanceRelation rel) throws MetamodelException {
        return ! toCode(rel.superClassReference()).equals("java.lang.Object");
      }
    }.filter(superTypes);
    if(superTypes.size() > 0) {
      result.append(" extends ");
      Iterator iter = superTypes.iterator();
      while(iter.hasNext()) {
        TypeReference tr = (TypeReference)iter.next();
          result.append(toCode(tr));
          if (iter.hasNext()) {
            result.append(", ");
          }
      }
    }
    result.append(" ");
    result.append(toCodeClassBlock(type));
    
    return result.toString();
    }
    catch (MetamodelException e) {
      throw e;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new Error();
    }
    
  }

  public String toCodeMethod(Method method) throws MetamodelException {
   //XXX modifiers
	    final StringBuffer result = startLine();
	    //Modifiers
	    
	    new Visitor() {
	      public void visit(Object element) {
	        result.append((toCodeModifier((Modifier)element)));
	        result.append(" ");
	      }
	    }.applyTo(method.modifiers());
	    
	    if(! method.hasModifier(new Constructor())) {
	        result.append(toCode(method.getReturnTypeReference()));
	        result.append(" ");
	      }
	    
	    result.append(((SimpleNameMethodSignature)method.signature()).getName());
	    result.append("(");
	    Iterator iter = method.getParameters().iterator();
	    while(iter.hasNext()) {
	      FormalParameter param = (FormalParameter)iter.next();
	      result.append(toCodeVariable(param));
	      if(iter.hasNext()) {
	        result.append(", ");
	      }
	    }
	    result.append(") ");
	    result.append(toCodeExceptionClause(method.getExceptionClause()));
	    if(! method.getExceptionClause().getDeclarations().isEmpty()) {
	      result.append(" ");
	    }
	    result.append(toCodeImplementation(method.getImplementation()));
	    indent();
	    undent();
	    return result.toString();
  }
//	 final StringBuffer result = startLine();
//    //Modifiers
//    
//    new Visitor() {
//      public void visit(Object element) {
//        result.append((toCodeModifier((Modifier)element)));
//        result.append(" ");
//      }
//    }.applyTo(method.getModifiers());
//    
//    if(method instanceof NonConstructor) {
//      result.append(toCode(((NonConstructor)method).getReturnTypeReference()));
//      result.append(" ");
//    }
//    
//    result.append(method.getName());
//    result.append("(");
//    Iterator iter = method.getParameters().iterator();
//    while(iter.hasNext()) {
//      FormalParameter param = (FormalParameter)iter.next();
//      result.append(toCodeVariable(param));
//      if(iter.hasNext()) {
//        result.append(", ");
//      }
//    }
//    result.append(") ");
//    result.append(toCodeExceptionClause(method.getExceptionClause()));
//    if(! method.getExceptionClause().getDeclarations().isEmpty()) {
//      result.append(" ");
//    }
//    result.append(toCodeImplementation(method.getImplementation()));
//    indent();
//    undent();
//    return result.toString();
//  }
  
  public String toCodeImplementation(Implementation impl) throws MetamodelException {
    if((impl == null) || (impl instanceof NativeImplementation)) {
      return ";";
    }
    else {
      return toCode(((RegularImplementation)impl).getBody());
    }
  }
  
  public boolean isMethod(Element element) {
    return element instanceof Method;
  }
  
  public String toCodeExceptionClause(ExceptionClause ec) throws MetamodelException {
    final StringBuffer result = new StringBuffer();
    List decls = ec.getDeclarations();
    if(! decls.isEmpty()) {
      result.append("throws ");
    }
    Iterator iter = (decls.iterator());
    while(iter.hasNext()) {
      result.append(toCodeExceptionDeclaration((ExceptionDeclaration)iter.next()));
      if(iter.hasNext()) {
        result.append(", ");
      }
    }
    return result.toString();
  }
  
  public String toCodeExceptionDeclaration(ExceptionDeclaration ed) throws MetamodelException {
    if(ed instanceof TypeExceptionDeclaration) {
      return toCode(((TypeExceptionDeclaration)ed).getTypeReference());
    }
    else {
      throw new IllegalArgumentException("The given element is not know by the Java syntax.");
    }
  }
  
  /********************
   * MEMBER VARIABLES *
   ********************/
  
  public String toCodeMemberVariable(RegularMemberVariable var) throws MetamodelException {
    return startLine() + toCodeVariable(var);
  }
  
  public String toCodeVariable(Variable var) throws MetamodelException {
    final StringBuffer result = new StringBuffer();
    new Visitor() {
      public void visit(Object element) {
        result.append((toCodeModifier((Modifier)element)));
        result.append(" ");
      }
    }.applyTo(var.modifiers());
    result.append(toCode(var.getTypeReference()));
    result.append(" ");
    result.append(var.getName());
      if(var.getInitialization() != null) {
      result.append(" = ");
      result.append(toCode(var.getInitialization()));
      }
      result.append(";");
    return result.toString();
  }
  
  /**************
   * STATEMENTS *
   **************/
  
  public boolean isStatementExpression(Element element) {
    return element instanceof StatementExpression;
  }
  
  public String toCodeStatementExpression(StatementExpression stat) throws MetamodelException {
    return toCode(stat.getExpression())+";";
  }
  
  public boolean isBreak(Element element) {
    return element instanceof BreakStatement;
  }
  
  public String toCodeBreak(BreakStatement st) {
    return "break" + (st.getLabel() == null ? "":" "+st.getLabel()) + ";";
  }
  
  public boolean isContinue(Element element) {
    return element instanceof ContinueStatement;
  }
  
  public String toCodeContinue(ContinueStatement st) {
    return "continue" + (st.getLabel() == null ? "":" "+st.getLabel()) + ";";
  }
  
  public boolean isBlock(Element element) {
    return element instanceof Block;
  }
  
  public String toCodeBlock(Block block) throws MetamodelException {
    StringBuffer result = new StringBuffer();
    result.append("{\n");
    indent();
    Iterator iter = block.getStatements().iterator();
    while(iter.hasNext()) {
      result.append(startLine());
      result.append(toCode((Element)iter.next()));
      result.append("\n");
    }
    undent();
    result.append(startLine());
    result.append("}");
    return result.toString();
  }
  
  public boolean isIf(Element element) {
    return element instanceof IfThenElseStatement;
  }
  
  public String toCodeIf(IfThenElseStatement stat) throws MetamodelException {
    StringBuffer result = new StringBuffer();
    result.append("if(");
    result.append(toCode(stat.getExpression()));
    result.append(") ");
    result.append(toCode(stat.getIfStatement()));
    if(stat.getElseStatement() != null) {
      result.append("\n");
      result.append(startLine());
      result.append("else ");
      result.append(toCode(stat.getElseStatement()));
    }
    return result.toString();
  }
  
  public boolean isWhile(Element element) {
    return element instanceof WhileStatement;
  }
  
  public String toCodeWhile(WhileStatement element) throws MetamodelException {
    StringBuffer result = new StringBuffer();
    result.append("while (");
    result.append(toCode(element.getExpression()));
    result.append(") ");
    result.append(toCode(element.getStatement()));
    return result.toString();
  }
  
  public boolean isTry(Element element) {
    return element instanceof TryStatement;
  }
  
  public String toCodeTry(TryStatement statement) throws MetamodelException {
    StringBuffer result = new StringBuffer();
    result.append("try ");
    result.append(toCode(statement.getStatement()));
    Iterator iter = statement.getCatchClauses().iterator();
    while(iter.hasNext()) {
      result.append("\n");
      result.append(startLine());
      result.append(toCodeCatchClause((CatchClause)iter.next()));
    }
    if(statement.getFinallyClause() != null) {
      result.append(toCodeFinally(statement.getFinallyClause()));
    }
    return result.toString();
  }
  
  public String toCodeCatchClause(CatchClause cc) throws MetamodelException {
    return "catch ("+toCodeVariable(cc.getExceptionParameter()) + ") " + toCode(cc.getStatement());
  }
  
  public String toCodeFinally(FinallyClause cc) throws MetamodelException {
    return "finally " + toCode(cc.getStatement());
  }
  
  public boolean isThrow(Element element) {
    return element instanceof ThrowStatement;
  }
  
  public String toCodeThrow(ThrowStatement ts) throws MetamodelException {
    return "throw "+toCode(ts.getExpression())+";";
  }
  
  public boolean isSynchronized(Element element) {
    return element instanceof SynchronizedStatement;
  }
  
  public String toCodeSynchronized(SynchronizedStatement ts) throws MetamodelException {
    return "synchronized("+toCode(ts.getExpression())+") "+toCode(ts.getStatement());
  }
  
  public boolean isSwitch(Element element) {
    return element instanceof SwitchStatement;
  }
  
  public String toCodeSwitch(SwitchStatement st) throws MetamodelException {
    final StringBuffer result = new StringBuffer();
    result.append("switch(" + toCode(st.getExpression()) + ") {\n");
    indent();
    try {
      new RobustVisitor() {
        public Object visit(Object o) throws MetamodelException {
          result.append(startLine());
          result.append(toCodeSwitchCase((SwitchCase)o));
          result.append("\n");
          return null;
        }

        public void unvisit(Object o, Object undo) {
          //NOP
        }
      }.applyTo(st.getSwitchCases());
    }
    catch (MetamodelException e) {
      throw e;
    }
    catch (Exception e) {
      throw new Error();
    }
    undent();
    result.append(startLine());
    result.append("}");
    return result.toString();
  }
  
  public String toCodeSwitchCase(SwitchCase sc) throws MetamodelException {
    final StringBuffer result = new StringBuffer();
    result.append(startLine());
    try {
      new RobustVisitor() {
        public Object visit(Object o) throws MetamodelException {
          result.append(toCodeSwitchLabel((SwitchLabel)o));
          return null;
        }

        public void unvisit(Object o, Object e) {
          //NOP
        }
      }.applyTo(sc.getLabels());
    }
    catch (MetamodelException e1) {
      throw e1;
    }
    catch (Exception e1) {
      throw new Error();
    }
    result.append("\n");
    indent();
    try {
      new RobustVisitor() {
        public Object visit(Object o) throws MetamodelException {
          result.append(toCode((Element)o));
          return null;
        }

        public void unvisit(Object o, Object undo) {
          //NOP
        }
      }.applyTo(sc.getStatements());
    }
    catch (MetamodelException e) {
      throw e;
    }
    catch (Exception e) {
      throw new Error();
    }
    undent();
    return result.toString();
  }
  
  public String toCodeSwitchLabel(SwitchLabel sl) throws MetamodelException {
    if(sl instanceof DefaultLabel) {
      return "default:";
    }
    else {
      return "case " + toCode(((CaseLabel)sl).getExpression())+":";
    }
  }
  
  public boolean isStatementExprList(Element element) {
    return element instanceof StatementExprList;
  }
  
  public String toCodeStatementExprList(StatementExprList sel) throws MetamodelException {
    StringBuffer result = new StringBuffer();
    Iterator iter = sel.getStatements().iterator();
    while(iter.hasNext()) {
      result.append(toCode(((StatementExpression)iter.next()).getExpression()));
      if(iter.hasNext() ) {
        result.append(", ");
      }
    }
    return result.toString();
  }
  
  public boolean isReturn(Element element) {
    return element instanceof ReturnStatement;
  }
  
  public String toCodeReturn(ReturnStatement ts) throws MetamodelException {
    return "return "+toCode(ts.getExpression())+";";
  }
  
  public boolean isLocalVar(Element element) {
    return element instanceof LocalVariableDeclarationStatement;
  }

  public String toCodeLocalVar(LocalVariableDeclarationStatement local) throws MetamodelException {
    return toCodeLocalVarForInit(local) + ";";
  }
  
  public String toCodeLocalVarForInit(LocalVariableDeclarationStatement local) throws MetamodelException {
    Variable var = (Variable)local.getVariables().get(0);
    final StringBuffer result = new StringBuffer();
    List modifiers = var.modifiers();
    if (modifiers.size() != 0) {
      new Visitor() {
        public void visit(Object o) {
          result.append(toCodeModifier((Modifier)o) + " ");
        }
      }.applyTo(modifiers);
    }
    result.append(toCode(var.getTypeReference()));
    result.append(" ");
    try {
      new RobustVisitor() {
        private boolean first = true;

        public Object visit(Object element) throws MetamodelException {
          LocalVariable variable = (LocalVariable)element;
          if (!first) {
            result.append(", ");
          }
          else {
            first = false;
          }
          result.append(variable.getName());
          Expression initCode = variable.getInitialization();
          if (initCode != null) {
            result.append(" = ");
            result.append(toCode(initCode));
          }
          return null;
        }

        public void unvisit(Object el, Object undo) {
          //NOP
        }
      }.applyTo(local.getVariables());
    }
    catch (MetamodelException e) {
      throw e;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new Error();
    }
    return result.toString();
  }
  
  public boolean isLocalClass(Element element) {
    return element instanceof LocalClassStatement;
  }
  
  public String toCodeLocalClass(LocalClassStatement local) throws MetamodelException {
    return toCode(local.getType());
  }
  
  public boolean isLabeledStatement(Element element) {
    return element instanceof LabeledStatement;
  }
  
  public String toCodeLabeledStatement(LabeledStatement local) throws MetamodelException {
    return local.getLabel() +": "+toCode(local.getStatement());
  }
  
  public boolean isFor(Element element) {
    return element instanceof ForStatement;
  }
  
  public String toCodeFor(ForStatement statement) throws MetamodelException {
    return "for ("+toCodeForInit((Element)statement.getForInit())+"; "+toCode(statement.getExpression())+"; "+toCode(statement.getUpdate())+") " + toCode(statement.getStatement());
  }
  
  public String toCodeForInit(Element element) throws MetamodelException {
    if(element instanceof LocalVariableDeclarationStatement) {
      return toCodeLocalVarForInit((LocalVariableDeclarationStatement)element);
    } else {
      return toCode(element);
    }
  }
  
  public boolean isEmpty(Element element) {
    return element instanceof EmptyStatement;
  }
  
  public String toCodeEmptyStatement(EmptyStatement empty) {
    return ";";
  }
  
  public boolean isDo(Element element) {
    return element instanceof DoStatement;
  }
  
  public String toCodeDo(DoStatement statement) throws MetamodelException {
    return "do "+toCode(statement.getStatement())+ "while("+toCode(statement.getCondition())+");";
  }
  
  
  /***************
   * EXPRESSIONS *
   ***************/
  
  public boolean isConstructorInvocation(Element element) {
    return element instanceof ConstructorInvocation;
  }
  
  public String toCodeConstructorInvocation(ConstructorInvocation inv) throws MetamodelException {
    StringBuffer result = new StringBuffer();
    if(inv.getTarget() != null) {
      result.append(toCode(inv.getTarget()));
      result.append(".");
    }
    result.append("new ");
    result.append(toCode(inv.getTypeReference()));
    result.append(getActualArgs(inv));
    if(inv.getAnonymousInnerType() != null) {
      result.append(toCode(inv.getAnonymousInnerType()));
    }
    return result.toString();
  }
  
  public String getActualArgs(Invocation inv) throws MetamodelException {
    StringBuffer result = new StringBuffer();
    result.append("(");
    Iterator iter = inv.getActualParameters().iterator();
    while(iter.hasNext()) {
      Expression expr = ((ActualParameter)iter.next()).getExpression();
      result.append(toCode(expr));
      if(iter.hasNext()) {
        result.append(", ");
      }
    }
    result.append(")");
    return result.toString();
  }
  
  public boolean isCondAnd(Element element) {
    return element instanceof ConditionalAndExpression;
  }
  
  public String toCodeCondAnd(ConditionalAndExpression cae) throws MetamodelException {
    return "(" + toCode(cae.getFirst())+" && " + toCode(cae.getSecond()) +")";
  }
  
  public boolean isCondOr(Element element) {
    return element instanceof ConditionalOrExpression;
  }
  
  public String toCodeCondOr(ConditionalOrExpression cae) throws MetamodelException {
    return "(" + toCode(cae.getFirst())+" || " + toCode(cae.getSecond()) +")";
  }
  
  public boolean isInfixInvocation(Element element) {
    return element instanceof InfixOperatorInvocation;
  }
  
  public String toCodeInfixInvocation(InfixOperatorInvocation inv) throws MetamodelException {
    return "(" + toCode(inv.getTarget())+") " + inv.getName()+ " (" + toCode((Element)inv.getActualParameters().get(0)) +")";
  }
  
  public boolean isPrefixInvocation(Element element) {
    return element instanceof PrefixOperatorInvocation;
  }
  
  public String toCodePrefixInvocation(PrefixOperatorInvocation inv) throws MetamodelException {
    return inv.getName()+"("+toCode(inv.getTarget())+")";
  }
  
  public boolean isPostfixInvocation(Element element) {
    return element instanceof PostfixOperatorInvocation;
  }
  
  public String toCodePostfixInvocation(PostfixOperatorInvocation inv) throws MetamodelException {
    return toCode(inv.getTarget()) + inv.getName();
  }
  
  public boolean isClassLiteral(Element element) {
    return element instanceof ClassLiteral;
  }
  
  public String toCodeClassLiteral(ClassLiteral literal) throws MetamodelException {
    return toCode(literal.getTypeReference())+".class";
  }
  
  public boolean isThisLiteral(Element element) {
    return element instanceof ThisLiteral;
  }
  
  public String toCodeThisLiteral(ThisLiteral literal) throws MetamodelException {
    if(literal.getTypeReference() != null) {
      return toCode(literal.getTypeReference())+".this";
    } else {
      return "this";
    }
  }
  
  public boolean isLiteral(Element element) {
    return element instanceof Literal;
  }
  
  public String toCodeLiteral(Literal literal) {
    return literal.getValue();
  }
  
  public boolean isNamedTarget(Element element) {
    return element instanceof NamedTarget;
  }
  
  public String toCodeNamedTarget(NamedTarget nt) throws MetamodelException {
    StringBuffer result = new StringBuffer();
    if(nt.getTarget() != null) {
      result.append(toCode(nt.getTarget()));
      result.append(".");
    }
    result.append(nt.getName());
    return result.toString();
  }
  
  public boolean isRegulaMethodInvocation(Element element) {
    return element instanceof RegularMethodInvocation;
  }
  
  public String toCodeRegularMethodInvocation(RegularMethodInvocation inv) throws MetamodelException {
    StringBuffer result = new StringBuffer();
    if(inv.getTarget() != null) {
      result.append(toCode(inv.getTarget()));
      result.append(".");
    }
    result.append(inv.getName());
    result.append(getActualArgs(inv));
    return result.toString();
  }
  
  public boolean isVarRef(Element element) {
    return element instanceof VariableReference;
  }
  
  public String toCodeVarRef(VariableReference var) throws MetamodelException {
    return toCode(var.getTarget());
  }
  
  /*********
   * JNOME *
   *********/
  
  public boolean isThisConstructorDelegation(Element element) {
    return element instanceof ThisConstructorDelegation;
  }
  
  public String toCodeThisConstructorDelegation(ThisConstructorDelegation deleg) throws MetamodelException {
    return "this" + getActualArgs(deleg);
  }
  
  public boolean isSuperTarget(Element element) {
    return element instanceof SuperTarget;
  }
  
  public String toCodeSuperTarget(SuperTarget nt) throws MetamodelException {
    StringBuffer result = new StringBuffer();
    if(nt.getTarget() != null) {
      result.append(toCode(nt.getTarget()));
      result.append(".");
    }
    result.append("super");
    return result.toString();
  }
  
  public boolean isSuperConstructorDelegation(Element element) {
    return element instanceof SuperConstructorDelegation;
  }
  
  public String toCodeSuperConstructorDelegation(SuperConstructorDelegation deleg) throws MetamodelException {
    return "super" + getActualArgs(deleg);
  }
  
  public boolean isInstanceOf(Element element) {
    return element instanceof InstanceofExpression;
  }
  
  public String toCodeInstanceOf(InstanceofExpression ioe) throws MetamodelException {
    return "(" + toCode(ioe.getExpression()) + " instanceof " + toCode(ioe.getTypeReference())+")";
  }
  
  public boolean isDimInit(Element element) {
    return element instanceof DimensionInitializer;
  }
  
  public String toCodeDimInit(DimensionInitializer init) throws MetamodelException {
    return "["+(init.getExpression() != null ? toCode(init.getExpression()) : "")+"]";
  }
  
  public boolean isCondExpr(Element element) {
    return element instanceof ConditionalExpression;
  }
  
  public String toCodeCondExpr(ConditionalExpression ce) throws MetamodelException {
    return "(" +toCode(ce.getCondition())+" ? "+toCode(ce.getFirst()) + " : "+ toCode(ce.getSecond())+")";
  }
  
  public boolean isCast(Element element) {
    return element instanceof ClassCastExpression;
  }
  
  public String toCodeCast(ClassCastExpression cc) throws MetamodelException {
    return "(("+toCode(cc.getTypeReference())+")" + toCode(cc.getExpression()) +")";
  }
  
  public boolean isArrayInit(Element element) {
    return element instanceof ArrayInitializer;
  }
  
  public String toCodeArrayInit(ArrayInitializer init) throws MetamodelException {
    StringBuffer result = new StringBuffer();
    result.append("{");
    Iterator iter = init.getVariableInitializers().iterator();
    while(iter.hasNext()) {
      result.append(toCode((Element)iter.next()));
      if(iter.hasNext()) {
        result.append(", ");
      }
    }
    result.append("}");
    return result.toString();
  }
  
  public boolean isArrayCreation(Element element) {
    return element instanceof ArrayCreationExpression;
  }
  
  public String toCodeArrayCreation(ArrayCreationExpression expr) throws MetamodelException {
    StringBuffer result = new StringBuffer();
    result.append("new ");
    result.append(toCode(expr.getTypeReference()).substring(0, toCode(expr.getTypeReference()).indexOf("[")));
    Collection inits = expr.getDimensionInitializers();
    Iterator iter = inits.iterator();
    while(iter.hasNext()) {
      //result.append(toCodeDimInit((DimensionInitializer)iter.next()));
    	result.append(toCode((ArrayIndex)iter.next()));
    }
    if(expr.getInitializer() != null) {
      result.append(toCodeArrayInit(expr.getInitializer()));
    }
    return result.toString();
  }
  
  public boolean isArrayAccess(Element element) {
    return element instanceof ArrayAccessExpression;
  }
  
  public String toCodeArrayAccess(ArrayAccessExpression expr) throws MetamodelException {
    StringBuffer result = new StringBuffer();
    result.append(toCode(expr.getTarget()));
    Iterator iter = expr.getIndices().iterator();
    while(iter.hasNext()) {
      result.append("[");
      result.append(toCode((Element)iter.next()));
      result.append("]");
    }
    return result.toString();
  }
  
  public boolean isAssignment(Element element) {
    return element instanceof AssignmentExpression;
  }
  
  public String toCodeAssignment(AssignmentExpression expr) throws MetamodelException {
    return toCode((Element)expr.getVariable()) + " = " + toCode(expr.getValue());
  }
  
	public boolean isEmptyArrayIndex(Element element) {
		return element instanceof EmptyArrayIndex;
	}
	
	public String toCodeEmptyArrayIndex(EmptyArrayIndex ai){
		StringBuffer result = new StringBuffer();
		int dimension = ai.getDimension();
		result.append("[");
		while(dimension > 1){
			result.append(",");
			dimension--;
		}
		result.append("]");
		return result.toString();
	}
	
	public boolean isFilledArrayIndex(Element element) {
		return element instanceof FilledArrayIndex;
	}
	
	
	public String toCodeFilledArrayIndex(FilledArrayIndex ai) throws MetamodelException{
		StringBuffer result = new StringBuffer();
		result.append("[");
		List expressions = ai.getIndices();
		result.append(toCode((Expression)expressions.get(0)));
		expressions.remove(0);
		while(expressions.size() > 0){
			result.append(",");
			result.append(toCode((Expression)expressions.get(0)));
			expressions.remove(0);
		}
		result.append("]");
		return result.toString();
	}


  public static void writeCode(Arguments arguments) throws IOException, MetamodelException {
    JavaCodeWriter writer = new JavaCodeWriter(2);
    Set<Type> types = arguments.getTypes();
    new PrimitiveTotalPredicate<Type>() {
    	public boolean eval(Type t) {
    		return t.hasModifier(new Public());
    	}
    }.filter(types);
    int i = 1;
    for(Type type:types) {
      String fileName = type.getName()+".java";
      String packageFQN = type.getNamespace().getFullyQualifiedName();
      String relDirName = packageFQN.replace('.', File.separatorChar);
      File out = new File(arguments.getOutputDirName()+File.separatorChar + relDirName + File.separatorChar + fileName);
      
      System.out.println(i + " Writing: "+out.getAbsolutePath());
      
      File parent = out.getParentFile();
      parent.mkdirs();
      out.createNewFile();
      FileWriter fw = new FileWriter(out);
      fw.write(writer.toCode((Element)type.getParent()));
      fw.close();
      
      
      i++;
    }
  }
}
