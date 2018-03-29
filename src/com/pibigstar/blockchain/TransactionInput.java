package com.pibigstar.blockchain;

public class TransactionInput {
	public String transactionOutputId; //引用transactionoutput -> transactionId。
	public TransactionOutput UTXO; //包含未使用的事务输出。
	
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
}
