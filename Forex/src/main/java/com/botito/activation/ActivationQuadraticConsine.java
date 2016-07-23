package com.botito.activation;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.mathutil.BoundMath;
import org.encog.util.obj.ActivationUtil;

public class ActivationQuadraticConsine implements ActivationFunction {

	/**
	 * Serial id for this class.
	 */
	private static final long serialVersionUID = -2629956764601932670L;

	/**
	 * The parameters.
	 */
	private final double[] params;

	/**
	 * Construct a basic sigmoid function, with a slope of 1.
	 */
	public ActivationQuadraticConsine() {
		this.params = new double[0];
	}

	/**
	 * {@inheritDoc}
	 */

	public final void activationFunction(final double[] x, final int start,
			final int size) {
		for (int i = start; i < start + size; i++) {
			x[i] = BoundMath.pow(BoundMath.cos(x[i]),2);
		}
	}

	/**
	 * @return The object cloned;
	 */
	@Override
	public final ActivationFunction clone() {
		return new ActivationQuadraticSine();
	}

	/**
	 * {@inheritDoc}
	 */

	public final double derivativeFunction(final double b, final double a) {
		return 2 * BoundMath.sin(b) * -1.0 * BoundMath.sin(b);
	}

	/**
	 * {@inheritDoc}
	 */
	public final String[] getParamNames() {
		final String[] results = {};
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	public final double[] getParams() {
		return this.params;
	}

	/**
	 * @return True, sigmoid has a derivative.
	 */
	public final boolean hasDerivative() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setParam(final int index, final double value) {
		this.params[index] = value;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getFactoryCode() {
		return ActivationUtil.generateActivationFactory("QuadraticSine", this);
	}

//	@Override
//	public String getLabel() {
//		// TODO Auto-generated method stub
//		return null;
//	}

}