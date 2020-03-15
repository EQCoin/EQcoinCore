/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.eqcoin.avro;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public interface TransactionNetwork {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"TransactionNetwork\",\"namespace\":\"com.eqchains.avro\",\"types\":[{\"type\":\"record\",\"name\":\"O\",\"fields\":[{\"name\":\"o\",\"type\":\"bytes\"}]}],\"messages\":{\"ping\":{\"request\":[{\"name\":\"c\",\"type\":\"O\"}],\"response\":\"O\"},\"getMinerList\":{\"request\":[],\"response\":\"O\"},\"sendTransaction\":{\"request\":[{\"name\":\"r\",\"type\":\"O\"}],\"response\":\"O\"},\"getID\":{\"request\":[{\"name\":\"a\",\"type\":\"O\"}],\"response\":\"O\"},\"getAccount\":{\"request\":[{\"name\":\"a\",\"type\":\"O\"}],\"response\":\"O\"},\"getMaxNonce\":{\"request\":[{\"name\":\"n\",\"type\":\"O\"}],\"response\":\"O\"},\"getBalance\":{\"request\":[{\"name\":\"n\",\"type\":\"O\"}],\"response\":\"O\"},\"getSignHash\":{\"request\":[{\"name\":\"i\",\"type\":\"O\"}],\"response\":\"O\"},\"getPendingTransactionList\":{\"request\":[{\"name\":\"n\",\"type\":\"O\"}],\"response\":\"O\"}}}");
  /**
   */
  com.eqcoin.avro.O ping(com.eqcoin.avro.O c) throws org.apache.avro.AvroRemoteException;
  /**
   */
  com.eqcoin.avro.O getMinerList() throws org.apache.avro.AvroRemoteException;
  /**
   */
  com.eqcoin.avro.O sendTransaction(com.eqcoin.avro.O r) throws org.apache.avro.AvroRemoteException;
  /**
   */
  com.eqcoin.avro.O getID(com.eqcoin.avro.O a) throws org.apache.avro.AvroRemoteException;
  /**
   */
  com.eqcoin.avro.O getAccount(com.eqcoin.avro.O a) throws org.apache.avro.AvroRemoteException;
  /**
   */
  com.eqcoin.avro.O getMaxNonce(com.eqcoin.avro.O n) throws org.apache.avro.AvroRemoteException;
  /**
   */
  com.eqcoin.avro.O getBalance(com.eqcoin.avro.O n) throws org.apache.avro.AvroRemoteException;
  /**
   */
  com.eqcoin.avro.O getSignHash(com.eqcoin.avro.O i) throws org.apache.avro.AvroRemoteException;
  /**
   */
  com.eqcoin.avro.O getPendingTransactionList(com.eqcoin.avro.O n) throws org.apache.avro.AvroRemoteException;

  @SuppressWarnings("all")
  public interface Callback extends TransactionNetwork {
    public static final org.apache.avro.Protocol PROTOCOL = com.eqcoin.avro.TransactionNetwork.PROTOCOL;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void ping(com.eqcoin.avro.O c, org.apache.avro.ipc.Callback<com.eqcoin.avro.O> callback) throws java.io.IOException;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void getMinerList(org.apache.avro.ipc.Callback<com.eqcoin.avro.O> callback) throws java.io.IOException;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void sendTransaction(com.eqcoin.avro.O r, org.apache.avro.ipc.Callback<com.eqcoin.avro.O> callback) throws java.io.IOException;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void getID(com.eqcoin.avro.O a, org.apache.avro.ipc.Callback<com.eqcoin.avro.O> callback) throws java.io.IOException;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void getAccount(com.eqcoin.avro.O a, org.apache.avro.ipc.Callback<com.eqcoin.avro.O> callback) throws java.io.IOException;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void getMaxNonce(com.eqcoin.avro.O n, org.apache.avro.ipc.Callback<com.eqcoin.avro.O> callback) throws java.io.IOException;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void getBalance(com.eqcoin.avro.O n, org.apache.avro.ipc.Callback<com.eqcoin.avro.O> callback) throws java.io.IOException;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void getSignHash(com.eqcoin.avro.O i, org.apache.avro.ipc.Callback<com.eqcoin.avro.O> callback) throws java.io.IOException;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void getPendingTransactionList(com.eqcoin.avro.O n, org.apache.avro.ipc.Callback<com.eqcoin.avro.O> callback) throws java.io.IOException;
  }
}