package com.anz.rer.etl.transform;

public interface ICriteria<P> {
  public boolean valid(P param);

}
