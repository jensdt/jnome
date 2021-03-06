package jnome.core.language;

import java.util.List;

import org.rejuse.java.collections.TypeFilter;

import chameleon.core.declaration.Declaration;
import chameleon.core.lookup.DeclarationSelector;
import chameleon.core.lookup.LocalLookupStrategy;
import chameleon.core.lookup.LookupException;
import chameleon.core.namespace.Namespace;

public class JavaNoRelativeNamespaceLookupStrategy extends LocalLookupStrategy<Namespace> {


	public JavaNoRelativeNamespaceLookupStrategy(Namespace element) {
		super(element);
	}

//	private Namespace _root;
//	
//	private LookupStrategy _local;
//
//	public <D extends Declaration> D lookUp(DeclarationSelector<D> selector) throws LookupException {
//		return _local.lookUp(new JavaNoRelativeNamespaceSelector<D>(selector, _root));
//	}
  protected <D extends Declaration> List<D> declarations(DeclarationSelector<D> selector) throws LookupException {
  	List<D> result = declarationContainer().declarations(selector);
  	if(declarationContainer() != declarationContainer().defaultNamespace()) {
  		new TypeFilter(Namespace.class).discard(result);
  	}
  	return result;
  }
  
  //PAPER: customize lookup

}
