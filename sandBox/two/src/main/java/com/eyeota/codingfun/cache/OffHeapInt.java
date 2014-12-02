package com.eyeota.codingfun.cache;

public interface OffHeapInt {
   
   public void setParent(int parent);
   public void setIndex(int index);
   public void setKey(int key);
   public void setValue(int value);
   
   public int getIndex();
   public int getIndex(int  value); //get object index using value
   public int[] getIndex(int  key, int value); //get object index using value
   
   public int[] getIndex(int  key, int value, int parent); //get object index using value
   
   
   public int[] getIndexV(int value);
   public int[] getIndexK(int key);
   
   
   public int getKey();
   public int getKey(int index);   //get value using index
   
   public int getParent();
   public int getParent(int index);   //get parent using index
   
   
   public int getValue();
   public int getValue(int index); //get value using index
   
   public void jump(int index);
   
   public boolean exist(int value);
   
   public int create(int key, int value);
  
   public int create(int key, int value, int parent);
   
   
   public int[] getKV(int index);
   
   public int getV(int K);
   public int[] getVs(int K);
   
   public int getK(int V);
   public int[] getKs(int V);
   
   public boolean containsKey(int key);
   public boolean containsValue(int value);
   public boolean containsKeyValue(int key,int value);
   
    
   
}