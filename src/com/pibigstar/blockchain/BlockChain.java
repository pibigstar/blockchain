package com.pibigstar.blockchain;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 创建区块链
 * @author pibigstar
 *
 */
public class BlockChain {
	//存放所有的区块集合
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	//未使用的交易作为可用的输入
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	//挖矿的难度，数字越大越难
	public static int difficulty = 5;
	//最低的交易金额
	public static float minimumTransaction = 0.1f;


	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction genesisTransaction;



	public static void main(String[] args) {	
		//创建区块链
		//createChain();

		//交易测试
		//将我们的block添加到区块链ArrayList:
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //设置Bouncey作为安全提供程序

		//创建钱包
		walletA = new Wallet();
		walletB = new Wallet();		
		Wallet coinbase = new Wallet();

		//创建创世交易（第一笔交易），向walletA发送100个星币:
		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);//手动签署《创世纪》交易。
		genesisTransaction.transactionId = "0"; //手动设置事务id。

		//手动添加事务输出。
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); 
		//在UTXOs列表中存储第一个事务，非常重要！
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); 

		System.out.println("#info:创造和开采创世纪块...... ");
		Block genesis = new Block("0");//创世区块
		genesis.addTransaction(genesisTransaction);//把事务放到区块里
		addBlock(genesis);//把区块放大区块链中

		//测试
		Block block1 = new Block(genesis.hash);
		System.out.println("钱包A的 金钱为: " + walletA.getBalance());
		System.out.println("WalletA正试图向WalletB发送资金(40)……");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		addBlock(block1);
		System.out.println("钱包A的 金钱为:" + walletA.getBalance());
		System.out.println("钱包B的 金钱为: " + walletB.getBalance());

		Block block2 = new Block(block1.hash);
		System.out.println("WalletA尝试发送更多的资金(1000)……");
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
		addBlock(block2);
		System.out.println("钱包A的 金钱为: " + walletA.getBalance());
		System.out.println("钱包B的 金钱为: " + walletB.getBalance());

		Block block3 = new Block(block2.hash);
		System.out.println("WalletB正试图向WalletA发送资金(20)……");
		block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
		System.out.println("钱包A的 金钱为:  " + walletA.getBalance());
		System.out.println("钱包B的 金钱为:  " + walletB.getBalance());

		isChainValid();

	}




	private static void createChain() {
		System.out.println("正在创建第一个区块链....... ");
		//addBlock(new Block("我是第一个区块链", "0"));//创世块

		System.out.println("正在创建第二个区块链....... ");
		//addBlock(new Block("我是第二个区块链",blockchain.get(blockchain.size()-1).hash));

		System.out.println("正在创建第三个区块链.......");
		//addBlock(new Block("我是第三个区块链",blockchain.get(blockchain.size()-1).hash));	

		System.out.println("区块链是否有效的: " + isChainValid());

		String blockchainJson = StringUtil.getJson(blockchain);
		System.out.println(blockchainJson);
	}



	/**
	 * 检查区块链的完整性
	 * @return
	 */
	public static Boolean isChainValid() {
		Block currentBlock = null; 
		Block previousBlock = null;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');

		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));


		//循环区块链检查散列:
		for(int i=1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//比较注册散列和计算散列:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("Current Hashes not equal");			
				return false;
			}
			//比较以前的散列和注册的先前的散列
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("Previous Hashes not equal");
				return false;
			}
			//检查哈希是否被使用
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("这个区块还没有被开采。。。");
				return false;
			}

		}

		//检查事务的合法性
		TransactionOutput tempOutput;
		for(int t=0; t <currentBlock.transactions.size(); t++) {
			Transaction currentTransaction = currentBlock.transactions.get(t);

			if(!currentTransaction.verifySignature()) {
				System.out.println("#Signature on Transaction(" + t + ") is Invalid");
				return false; 
			}
			if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
				System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
				return false; 
			}

			for(TransactionInput input: currentTransaction.inputs) {	
				tempOutput = tempUTXOs.get(input.transactionOutputId);

				if(tempOutput == null) {
					System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
					return false;
				}

				if(input.UTXO.value != tempOutput.value) {
					System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
					return false;
				}

				tempUTXOs.remove(input.transactionOutputId);
			}

			for(TransactionOutput output: currentTransaction.outputs) {
				tempUTXOs.put(output.id, output);
			}

			if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
				System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
				return false;
			}
			if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
				System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
				return false;
			}

		}
		return true;
	}
	/**
	 * 增加一个新的区块
	 * 并将改去放放到区块链中
	 * @param newBlock
	 */
	public static void addBlock(Block newBlock) {
		//将newBlock建造成一个区块
		newBlock.mineBlock(difficulty);
		//将newBlock 放到区块链中
		blockchain.add(newBlock);
	}
}
