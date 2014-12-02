package com.anz.rer.etl.transform;

public interface ITransformer<R,P> {
 
	public R transform(P param);
	
}
