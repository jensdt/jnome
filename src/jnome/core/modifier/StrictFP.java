package jnome.core.modifier;

import jnome.core.language.Java;

import org.rejuse.property.PropertySet;

import chameleon.core.element.Element;
import chameleon.core.modifier.ModifierContainer;
import chameleon.core.modifier.ModifierImpl;

/**
 * @author Marko van Dooren
 */
public class StrictFP extends ModifierImpl<StrictFP,ModifierContainer> {

  public StrictFP() {
  }

	@Override
	public StrictFP clone() {
		return new StrictFP();
	}

	public PropertySet<Element> impliedProperties() {
		return createSet(((Java)language()).STRICTFP);
	}
  
}
