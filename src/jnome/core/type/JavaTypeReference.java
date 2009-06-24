package jnome.core.type;

import java.util.List;

import org.rejuse.association.ReferenceSet;

import chameleon.core.Config;
import chameleon.core.MetamodelException;
import chameleon.core.element.ChameleonProgrammerException;
import chameleon.core.element.Element;
import chameleon.core.expression.NamedTarget;
import chameleon.core.namespace.NamespaceOrType;
import chameleon.core.type.Type;
import chameleon.core.type.TypeReference;
import chameleon.core.type.generics.GenericArgument;

/**
 * A class for Java type references. They add support for array types and generic parameters.
 * 
 * @author Marko van Dooren
 */
public class JavaTypeReference extends TypeReference {

  public JavaTypeReference(String name) {
    this(name,0);
  }
  
  public JavaTypeReference(JavaTypeReference target, String name) {
  	super(target,name);
  }
  /**
   * THIS ONLY WORKS WHEN THE NAMED TARGET CONSISTS ENTIRELY OF NAMEDTARGETS.
   * @param target
   */
  public JavaTypeReference(NamedTarget target) {
  	super(new JavaTypeReference((NamedTarget)target.getTarget()),target.getName());
  }
  
  public JavaTypeReference(String name, int arrayDimension) {
  	super(name);
  	if(Config.DEBUG) {
  		if((name != null) && (name.contains("["))) {
  			throw new ChameleonProgrammerException("Initializing a type reference with a [ in the name.");
  		}
  	}
  	setArrayDimension(arrayDimension);
  }
  
  public List<GenericArgument> typeArguments() {
  	return _genericParameters.getOtherEnds();
  }
  
  public void addArgument(GenericArgument arg) {
  	if(arg != null) {
  		_genericParameters.add(arg.parentLink());
  	}
  }
  
  public void addAllArguments(List<GenericArgument> args) {
  	for(GenericArgument argument : args) {
  		addArgument(argument);
  	}
  }
  
  public void removeArgument(GenericArgument arg) {
  	if(arg != null) {
  		_genericParameters.remove(arg.parentLink());
  	}
  }
  
  private ReferenceSet<JavaTypeReference,GenericArgument> _genericParameters = new ReferenceSet<JavaTypeReference, GenericArgument>(this);
  
  public List<Element> children() {
  	List<Element> result = super.children();
  	result.addAll(_genericParameters.getOtherEnds());
  	return result;
  }
  
  public JavaTypeReference toArray(int arrayDimension) {
  	JavaTypeReference result = clone();
  	result.setArrayDimension(arrayDimension);
  	return result;
  }

  private int _arrayDimension;
  
  public int arrayDimension() {
  	return _arrayDimension;
  }
  
  public void setArrayDimension(int arrayDimension) {
  	_arrayDimension = arrayDimension;
  }
  
  public Type getType() throws MetamodelException {
    Type result = null;

//    result = getCache();
//    if (result != null) {
//      return result;
//    }

    if (getTarget() != null) {
      NamespaceOrType target = getTarget().getNamespaceOrType();
      if (target != null) {
        result = target.targetContext().lookUp(selector());// findType(getName());
      }
    } else {
      result = parent().lexicalContext(this).lookUp(selector()); // (getName());
    }
    
    // FILL IN GENERIC PARAMETERS
    
    
    // ARRAY TYPE
    if ((arrayDimension() != 0) && (result != null)) {
      result = new ArrayType(result,arrayDimension());
    }

    if (result != null) {
//      setCache(result);
      return result;
    } else {
      throw new MetamodelException();
    }
  }

  
//  public Type getType() throws MetamodelException {
//  	Type result = null;
//  	
//    result = getCache();
//    if(result != null) {
//    	return result;
//    }
//
//    if (getArrayDimension() == 0) {
//      if(getTarget() == null) {
//        result = getParent().getContext(this).findType(getName());
//      }
//      else {
//    	  NamespaceOrType target = getTarget().getNamespaceOrType();
//        if(target != null) {
//          result = target.getTargetContext().findType(getName());
//        }
//      }
//    }
//    else {
//      if(getTarget() == null) {
//        result = new ArrayType((Type)getParent().getContext(this).findType(getComponentName()), getArrayDimension());
//      }
//      else {
//    	  NamespaceOrType target = getTarget().getNamespaceOrType();
//        if(target != null) {
//          result = new ArrayType((Type)target.getTargetContext().findType(getComponentName()), getArrayDimension());
//        } 
//      }
//    }
//    setCache(result);
//    return result;
//  }

//  public Type getSuperType() throws NotResolvedException {
//    if (getArrayDimension() == 0) {
//      return getParent().getContext().getParentContext().findType(getName());
//    }
//    else {
//      return new ArrayType((Type)getParent().getContext().getParentContext().findType(getComponentName()), getArrayDimension());
//    }
//  }

//  private int getArrayDimension() {
//    int result = 0;
//    String name = getName();
//    int index = name.indexOf("[", 0);
//    while (index >= 0) {
//      result++;
//      index = name.indexOf("[", index + 1);
//    }
//    return result;
//  }

//  private String getComponentName() {
//    if (arrayDimension() == 0) {
//      return getName();
//    }
//    else {
//      try {
//        return getName().substring(0, getName().indexOf("["));
//      }
//      catch (RuntimeException e) {
//        e.printStackTrace();
//        arrayDimension();
//        throw e;
//      }
//    }
//  }
  
  public JavaTypeReference clone() {
    return new JavaTypeReference(getName());
  }
  
}
