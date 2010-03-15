package jnome.core.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import jnome.core.language.Java;
import jnome.core.method.JavaVarargsOrder;
import jnome.core.type.ArrayType;
import jnome.core.type.JavaTypeReference;
import jnome.core.variable.MultiFormalParameter;

import org.rejuse.association.OrderedMultiAssociation;
import org.rejuse.association.SingleAssociation;
import org.rejuse.logic.ternary.Ternary;
import org.rejuse.predicate.UnsafePredicate;

import chameleon.core.declaration.Declaration;
import chameleon.core.declaration.Signature;
import chameleon.core.expression.InvocationTarget;
import chameleon.core.lookup.DeclarationSelector;
import chameleon.core.lookup.LookupException;
import chameleon.core.method.MethodHeader;
import chameleon.core.reference.CrossReference;
import chameleon.core.relation.WeakPartialOrder;
import chameleon.core.type.Type;
import chameleon.core.type.TypeReference;
import chameleon.core.type.generics.ActualTypeArgument;
import chameleon.core.type.generics.BasicTypeArgument;
import chameleon.core.type.generics.ExtendsWildCard;
import chameleon.core.type.generics.InstantiatedTypeParameter;
import chameleon.core.type.generics.SuperWildCard;
import chameleon.core.type.generics.TypeParameter;
import chameleon.core.variable.FormalParameter;
import chameleon.oo.language.ObjectOrientedLanguage;
import chameleon.support.member.MoreSpecificTypesOrder;
import chameleon.support.member.simplename.SimpleNameMethodSignature;
import chameleon.support.member.simplename.method.NormalMethod;
import chameleon.support.member.simplename.method.RegularMethodInvocation;

public class JavaMethodInvocation extends RegularMethodInvocation<JavaMethodInvocation> {

	public JavaMethodInvocation(String name, InvocationTarget target) {
		super(name, target);
	}


	
  public class JavaMethodSelector extends DeclarationSelector<NormalMethod> {

    public boolean selectedRegardlessOfName(NormalMethod declaration) throws LookupException {
  		boolean result = declaration.is(language(ObjectOrientedLanguage.class).CONSTRUCTOR) != Ternary.TRUE;
  		if(result) {
  			Signature signature = declaration.signature();
  			if(signature instanceof SimpleNameMethodSignature) {
  				SimpleNameMethodSignature sig = (SimpleNameMethodSignature)signature;
  				List<Type> actuals = getActualParameterTypes();
  				List<FormalParameter> formals = declaration.formalParameters();
  				List<Type> formalTypes = sig.parameterTypes();
  				
          int nbActuals = actuals.size();
          int nbFormals = formals.size();
          if(nbActuals == nbFormals){
          	// POTENTIALLY
						result = MoreSpecificTypesOrder.create().contains(actuals,formalTypes);
          } else if
          // varargs rubbish
          	 (
          			 (formals.get(nbFormals - 1) instanceof MultiFormalParameter)
          			 && 
          			 (nbActuals >= nbFormals - 1)
          	 )
          	 {
          	// POTENTIALLY
						result = JavaVarargsOrder.create().contains(actuals,formalTypes);
          } else {
          	result = false;
          }
          if(result) {
          	List<ActualTypeArgument> actualTypeArguments = typeArguments();
          	int actualTypeArgumentsSize = actualTypeArguments.size();
						if(actualTypeArgumentsSize > 0) {
          		List<TypeParameter> formalTypeParameters = declaration.typeParameters();
          		result = actualTypeArgumentsSize == formalTypeParameters.size();
          		if(result) {
          			for(int i=0; result && i < actualTypeArgumentsSize; i++) {
          				result = formalTypeParameters.get(i).canBeAssigned(actualTypeArguments.get(i));
          			}
          		}
          	}
          }
  			}
  		}
  		return result;
    }
    
  	@Override
    public boolean selectedBasedOnName(Signature signature) throws LookupException {
  		boolean result = false;
  		if(signature instanceof SimpleNameMethodSignature) {
  			SimpleNameMethodSignature sig = (SimpleNameMethodSignature)signature;
  			result = sig.name().equals(name());
  		}
  		return result;
    }

    @Override
    public WeakPartialOrder<NormalMethod> order() {
      return new WeakPartialOrder<NormalMethod>() {
        @Override
        public boolean contains(NormalMethod first, NormalMethod second)
            throws LookupException {
          return MoreSpecificTypesOrder.create().contains(((MethodHeader) first.header()).getParameterTypes(), ((MethodHeader) second.header()).getParameterTypes());
        }
      };
    }
		@Override
		public Class<NormalMethod> selectedClass() {
			return NormalMethod.class;
		}

		@Override
		public String selectionName() {
			return name();
		}
  }
  
  public static class ConstraintSet {
  	
  	private OrderedMultiAssociation<ConstraintSet, Constraint> _constraints = new OrderedMultiAssociation<ConstraintSet, Constraint>(this);
  	
  	public List<Constraint> constraints() {
  		return _constraints.getOtherEnds();
  	}
  	
  	public void add(Constraint constraint) {
  		if(constraint != null) {
  			_constraints.add(constraint.parentLink());
  		}
  	}
  	
  	public void remove(Constraint constraint) {
  		if(constraint != null) {
  			_constraints.remove(constraint.parentLink());
  		}
  	}
  	
  	
  	public void replace(Constraint oldConstraint, Constraint newConstraint) {
  		if(oldConstraint != null && newConstraint != null) {
  			_constraints.replace(oldConstraint.parentLink(), newConstraint.parentLink());
  		}
  	}
  	
  	public List<TypeParameter> typeParameters() {
  		return _typeParameters;
  	}
  	
  	private List<TypeParameter> _typeParameters;
  }

  private static class Constraint {
  	
  	private SingleAssociation<Constraint, ConstraintSet> _parentLink = new SingleAssociation<Constraint, ConstraintSet>(this);
  	
  	public SingleAssociation<Constraint, ConstraintSet> parentLink() {
  		return _parentLink;
  	}
  	
  	public ConstraintSet parent() {
  		return _parentLink.getOtherEnd();
  	}
  	// resolve()
  }
  
  /**
   * A = type()
   * F = typeReference()
   * 
   * @author Marko van Dooren
   */
  private abstract static class FirstPhaseConstraint extends Constraint {
  	
  	/**
  	 * 
  	 * @param type
  	 * @param tref
  	 */
  	public FirstPhaseConstraint(Type type, JavaTypeReference tref) {
  	  _type = type;
  	  _typeReference = tref;
  	}
  	
  	private Type _type;
  	
  	public Type type() {
  		return _type;
  	}
  	
  	private JavaTypeReference _typeReference;
  	
  	public JavaTypeReference typeReference() {
  		return _typeReference;
  	}
  	
  	public List<SecondPhaseConstraint> process() throws LookupException {
  		List<SecondPhaseConstraint> result = new ArrayList<SecondPhaseConstraint>();
  		if(! type().equals(type().language(ObjectOrientedLanguage.class).getNullType())) {
  			result.addAll(processSpecifics());
  		}
  		return result;
  	}
  	
  	public abstract List<SecondPhaseConstraint> processSpecifics() throws LookupException;
  	
  	public boolean involvesTypeParameter(TypeReference tref) throws LookupException {
  		return ! involvedTypeParameters(tref).isEmpty();
  	}
  	
  	public List<TypeParameter> involvedTypeParameters(TypeReference tref) throws LookupException {
  		List<CrossReference> list = tref.descendants(CrossReference.class, new UnsafePredicate<CrossReference, LookupException>() {

				@Override
				public boolean eval(CrossReference object) throws LookupException {
					return parent().typeParameters().contains(object.getDeclarator());
				}
			});
  		List<TypeParameter> parameters = new ArrayList<TypeParameter>();
  		for(CrossReference cref: list) {
  			parameters.add((TypeParameter) cref.getElement());
  		}
  		return parameters;
  	}

  	public Java language() {
  		return type().language(Java.class);
  	}
  	
    protected Type typeWithSameBaseTypeAs(Type example, Collection<Type> toBeSearched) {
  		Type baseType = example.baseType();
    	for(Type type:toBeSearched) {
  			if(type.baseType().equals(baseType)) {
    			return type;
    		}
    	}
    	return null;
    }

  }
  /**
   * A << F
   * 
   * Type << JavaTypeReference
   * 
   * @author Marko van Dooren
   */
  private static class SSConstraint extends FirstPhaseConstraint {

		public SSConstraint(Type type, JavaTypeReference tref) {
			super(type,tref);
		}

		@Override
		public List<SecondPhaseConstraint> processSpecifics() throws LookupException {
			List<SecondPhaseConstraint> result = new ArrayList<SecondPhaseConstraint>();
			Declaration declarator = typeReference().getDeclarator();
			Type type = type();
			if(type().is(language().PRIMITIVE_TYPE) == Ternary.TRUE) {
				// If A is a primitive type, then A is converted to a reference type U via
				// boxing conversion and this algorithm is applied recursively to the constraint
				// U << F
				SSConstraint recursive = new SSConstraint(language().box(type()), typeReference());
				result.addAll(recursive.process());
			} else if(parent().typeParameters().contains(declarator)) {
				// Otherwise, if F=Tj, then the constraint Tj :> A is implied.
					result.add(new SupertypeConstraint((TypeParameter) declarator, type()));
			} else if(typeReference().arrayDimension() > 0) {
				// If F=U[], where the type U involves Tj, then if A is an array type V[], or
				// a type variable with an upper bound that is an array type V[], where V is a
				// reference type, this algorithm is applied recursively to the constraint V<<U

				// The "involves Tj" condition for U is the same as "involves Tj" for F.
				if(type instanceof ArrayType && involvesTypeParameter(typeReference())) {
					Type componentType = ((ArrayType)type).componentType();
					if(componentType.is(language().REFERENCE_TYPE) == Ternary.TRUE) {
						JavaTypeReference componentTypeReference = typeReference().clone();
						componentTypeReference.setUniParent(typeReference());
						componentTypeReference.decreaseArrayDimension(1);
						SSConstraint recursive = new SSConstraint(componentType, componentTypeReference);
						result.addAll(recursive.process());
						// FIXME: can't we unwrap the entire array dimension at once? This seems rather inefficient.
					}
				}
			} else {
				List<ActualTypeArgument> actuals = typeReference().typeArguments();
				Set<Type> supers = type().getAllSuperTypes();
				Type G = typeWithSameBaseTypeAs(type(), supers);
				if(G != null) {
					// i is the index of the parameter we are processing.
					// V= the type reference of the i-th type parameter of some supertype G of A.
					int i = 0;
					for(ActualTypeArgument typeArgumentOfFormalParameter: typeReference().typeArguments()) {
						i++;
						TypeParameter ithTypeParameterOfG = G.parameters().get(i);
						if(typeArgumentOfFormalParameter instanceof BasicTypeArgument) {
							caseSSFormalBasic(result, (BasicTypeArgument)typeArgumentOfFormalParameter, ithTypeParameterOfG);
						} else if(typeArgumentOfFormalParameter instanceof ExtendsWildCard) {
							caseSSFormalExtends(result, (ExtendsWildCard) typeArgumentOfFormalParameter, ithTypeParameterOfG);
						} else if(typeArgumentOfFormalParameter instanceof SuperWildCard) {
							caseSSFormalSuper(result, (SuperWildCard) typeArgumentOfFormalParameter, ithTypeParameterOfG);
						}
					}
				}
			}
 			return result;
		}

		/**
		 * If F has the form G<...,Yk-1,? super U,Yk+1....>, where U involves Tj, then if A has a supertype that is one of
		 * 
		 *  1) G<...,Xk-1,V,Xk+1,...>, where V is a type expression. Then this algorithm is
		 *     applied recursively to the constraint V>>U
		 *  2) G<...,Xk-1,? super V,Xk+1,...>, where V is a type expression. Then this algorithm is
		 *     applied recursively to the constraint V>>U
		 */
		private void caseSSFormalSuper(List<SecondPhaseConstraint> result, SuperWildCard typeArgumentOfFormalParameter,
				TypeParameter ithTypeParameterOfG) throws LookupException {
			JavaTypeReference U = (JavaTypeReference) typeArgumentOfFormalParameter.typeReference();
			if(involvesTypeParameter(U)) {
				if(ithTypeParameterOfG instanceof InstantiatedTypeParameter) {
					ActualTypeArgument arg = ((InstantiatedTypeParameter)ithTypeParameterOfG).argument();
					// 1)
					if(arg instanceof BasicTypeArgument) {
						Type V = arg.type();
						GGConstraint recursive = new GGConstraint(V, U);
						result.addAll(recursive.process());
					} 
					// 2)
					else if (arg instanceof ExtendsWildCard) {
						Type V = ((ExtendsWildCard)arg).upperBound();
						GGConstraint recursive = new GGConstraint(V, U);
						result.addAll(recursive.process());
					}
					// Otherwise, no constraint is implied on Tj.
				}
			}
		}
		
    /**
		 * If F has the form G<...,Yk-1,? extends U,Yk+1....>, where U involves Tj, then if A has a supertype that is one of
		 * 
		 *  1) G<...,Xk-1,V,Xk+1,...>, where V is a type expression. Then this algorithm is
		 *     applied recursively to the constraint V<<U
		 *  2) G<...,Xk-1,? extends V,Xk+1,...>, where V is a type expression. Then this algorithm is
		 *     applied recursively to the constraint V<<U
		 */
		private void caseSSFormalExtends(List<SecondPhaseConstraint> result, ExtendsWildCard typeArgumentOfFormalParameter,
				TypeParameter ithTypeParameterOfG) throws LookupException {
			JavaTypeReference U = (JavaTypeReference) typeArgumentOfFormalParameter.typeReference();
			if(involvesTypeParameter(U)) {
				if(ithTypeParameterOfG instanceof InstantiatedTypeParameter) {
					ActualTypeArgument arg = ((InstantiatedTypeParameter)ithTypeParameterOfG).argument();
					// 1)
					if(arg instanceof BasicTypeArgument) {
						Type V = arg.type();
						SSConstraint recursive = new SSConstraint(V, U);
						result.addAll(recursive.process());
					} 
					// 2)
					else if (arg instanceof ExtendsWildCard) {
						Type V = ((ExtendsWildCard)arg).upperBound();
						SSConstraint recursive = new SSConstraint(V, U);
						result.addAll(recursive.process());
					}
					// Otherwise, no constraint is implied on Tj.
				}
			}
		}

		/**
		 * If F has the form G<...,Yk-1,U,Yk+1....>, 1<=k<=n where U is a type expression that involves Tj,
		 * the in A has a supertype of the form G<...,Xk-1,V,Xk+1,...> where V is a type expression, this algorithm 
		 * is applied recursively to the constraint V = U. 
		 */
		private void caseSSFormalBasic(List<SecondPhaseConstraint> result, BasicTypeArgument typeArgumentOfFormalParameter,
				TypeParameter ithTypeParameterOfG) throws LookupException {
			// U = basic.typeReference()
			JavaTypeReference U = (JavaTypeReference) typeArgumentOfFormalParameter.typeReference();
			if(involvesTypeParameter(U)) {
				// Get the i-th type parameter of zuppa: V.
				if(ithTypeParameterOfG instanceof InstantiatedTypeParameter) {
					ActualTypeArgument arg = ((InstantiatedTypeParameter)ithTypeParameterOfG).argument();
					if(arg instanceof BasicTypeArgument) {
						Type V = arg.type();
						EQConstraint recursive = new EQConstraint(V, U);
						result.addAll(recursive.process());
					}
				}
			}
		}
  	
  }
  

  private static class GGConstraint extends FirstPhaseConstraint {

		public GGConstraint(Type type, JavaTypeReference tref) {
			super(type,tref);
		}

		@Override
		public List<SecondPhaseConstraint> processSpecifics() throws LookupException {
			return null;
		}
  	
  }

  private static class EQConstraint extends FirstPhaseConstraint {

		public EQConstraint(Type type, JavaTypeReference tref) {
			super(type,tref);
		}

		@Override
		public List<SecondPhaseConstraint> processSpecifics() throws LookupException {
			return null;
		}
  	
  }
 
  private abstract static class SecondPhaseConstraint extends Constraint {
  	
  	public SecondPhaseConstraint(TypeParameter param, Type type) {
  	  _type = type;	
  	  _typeParameter = param;
  	}
  	
  	private Type _type;
  	
  	public Type type() {
  		return _type;
  	}
  	
  	private TypeParameter _typeParameter;
  	
  	public TypeParameter typeParameter() {
  		return _typeParameter;
  	}
  	
  }

  private static class EqualTypeConstraint extends SecondPhaseConstraint {

		public EqualTypeConstraint(TypeParameter param, Type type) {
			super(param,type);
		}
  	
  }
  
  private static class SubtypeConstraint extends SecondPhaseConstraint {

		public SubtypeConstraint(TypeParameter param, Type type) {
			super(param,type);
		}
  	
  }
  
  private static class SupertypeConstraint extends SecondPhaseConstraint {

		public SupertypeConstraint(TypeParameter param, Type type) {
			super(param,type);
		}
  	
  }
}