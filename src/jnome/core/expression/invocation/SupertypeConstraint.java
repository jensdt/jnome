/**
 * 
 */
package jnome.core.expression.invocation;

import java.util.HashSet;
import java.util.Set;

import jnome.core.type.JavaTypeReference;
import chameleon.core.lookup.LookupException;
import chameleon.oo.type.Type;
import chameleon.oo.type.generics.TypeParameter;

public class SupertypeConstraint extends SecondPhaseConstraint {

	public SupertypeConstraint(TypeParameter param, JavaTypeReference type) {
		super(param,type);
	}

	/**
	 * Do nothing for a supertype constraint
	 */
	@Override
	public void process() {
	}
	
	
}