package com.anz.rer.etl.transform;

public interface ITransform<R> {
	public R doTransform( ITransformer<R,?> transform );

	
}
