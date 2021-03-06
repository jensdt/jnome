package jnome.core.modifier;

import org.rejuse.property.PropertySet;

import chameleon.core.element.Element;
import chameleon.core.property.ChameleonProperty;
import chameleon.oo.language.ObjectOrientedLanguage;
import chameleon.support.modifier.Constructor;
/**
 * A class for Java constructor modifiers.
 * 
 * Difference with super class:
 *   1) A Java constructor is not inheritable. 
 *  
 * @author Marko
 */
public class JavaConstructor extends Constructor {

	/**
	 * A Java constructor is not inheritable.
	 */
 /*@
   @ also public behavior
   @
   @ post \result.contains(language().CONSTRUCTOR);
   @*/
  public PropertySet<Element,ChameleonProperty> impliedProperties() {
  	PropertySet<Element,ChameleonProperty> result = super.impliedProperties();
    result.add(language(ObjectOrientedLanguage.class).INHERITABLE.inverse());
    return result;
  }

}
