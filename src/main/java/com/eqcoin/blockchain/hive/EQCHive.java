/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 * @copyright 2018-present EQcoin Federation All rights reserved...
 * Copyright of all works released by EQcoin Federation or jointly released by
 * EQcoin Federation with cooperative partners are owned by EQcoin Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Federation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqcoin.org
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.eqcoin.blockchain.hive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.print.attribute.Size2DSyntax;

import com.eqcoin.avro.O;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.changelog.ChangeLog.Statistics;
import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.blockchain.seed.EQCSeed;
import com.eqcoin.blockchain.seed.EQcoinSeed;
import com.eqcoin.blockchain.seed.EQcoinSeedHeader;
import com.eqcoin.blockchain.seed.EQCSeed.SubchainType;
import com.eqcoin.blockchain.transaction.CoinbaseTransaction;
import com.eqcoin.blockchain.transaction.TransferOperationTransaction;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.TransferTransaction;
import com.eqcoin.blockchain.transaction.TxIn;
import com.eqcoin.blockchain.transaction.TxOut;
import com.eqcoin.blockchain.transaction.operation.UpdateLockOperation;
import com.eqcoin.crypto.MerkleTree;
import com.eqcoin.keystore.Keystore;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.serialization.EQCType.ARRAY;
import com.eqcoin.service.MinerService;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool.LockType;

/**
 * @author Xun Wang
 * @date Oct 1, 2018
 * @email 10509759@qq.com
 */
public class EQCHive implements EQCTypable {
	private EQCHeader eqcHeader;
	private EQCRoot eqcRoot;
	private EQcoinSeed eQcoinSeed;

	// The min size of the EQCHeader's is 142 bytes. 
	// Here need check the max size also need do more job
	private final int size = 142;

	public EQCHive(byte[] bytes, boolean isSegwit) throws Exception {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is, isSegwit);
		EQCType.assertNoRedundantData(is);
	}

	private void parse(ByteArrayInputStream is, boolean isSegwit) throws Exception {
		// Parse EqcHeader
		eqcHeader = new EQCHeader(EQCType.parseBIN(is));

		// Parse Root
		eqcRoot = new EQCRoot(EQCType.parseBIN(is));

		// Parse EQcoinSeed
		eQcoinSeed = new EQcoinSeed(EQCType.parseBIN(is), isSegwit);
	}

	public EQCHive() {
		init();
	}

	private void init() {
		eqcHeader = new EQCHeader();
		eqcRoot = new EQCRoot();
		eQcoinSeed = new EQcoinSeed();
	}

	public EQCHive(ID currentBlockHeight, byte[] previousBlockHeaderHash) throws Exception {
		init();
		// Create EQC block header
		eqcHeader.setPreHash(previousBlockHeaderHash);
		eqcHeader.setHeight(currentBlockHeight);
		eqcHeader.setTarget(Util.cypherTarget(currentBlockHeight));
		eqcHeader.setTimestamp(new ID(System.currentTimeMillis()));
		eqcHeader.setNonce(ID.ZERO);
	}

	/**
	 * @return the eqcHeader
	 */
	public EQCHeader getEqcHeader() {
		return eqcHeader;
	}

	/**
	 * @param eqcHeader the eqcHeader to set
	 */
	public void setEqcHeader(EQCHeader eqcHeader) {
		this.eqcHeader = eqcHeader;
	}

	public ID getHeight() {
		return eqcHeader.getHeight();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return

		"{\n" + toInnerJson() + "\n}";

	}

	public String toInnerJson() {
		return

		"\"EQCHive\":{\n" + eqcHeader.toInnerJson() + ",\n" + eqcRoot.toInnerJson() + ",\n" +
		eQcoinSeed.toInnerJson() + "\n" +
		 "}";
	}

	public void plantingEQCHive(ChangeLog changeLog) throws Exception {
		/**
		 * Heal Protocol If height equal to a specific height then update the ID No.1's
		 * Address to a new Address the more information you can reference to
		 * https://github.com/eqzip/eqcoin
		 */
		
		// Retrieve all transactions from transaction pool
		Vector<Transaction> pendingTransactionList = new Vector<Transaction>();
		// Get EQcoinSubchain Transaction list till now only handle this but in the
		// future will handle all Transactions together to meet balance less
		pendingTransactionList.addAll(Util.DB().getTransactionListInPool());
		Log.info("Current have " + pendingTransactionList.size() + " pending Transactions.");
				
		/**
		 * Begin handle EQcoinSeed
		 */
		eQcoinSeed.plantingTransaction(pendingTransactionList, changeLog);

		// Check Statistics
		Statistics statistics = changeLog.getStatistics();
		if (!statistics.isValid(changeLog)) {
			throw new IllegalStateException("Statistics is invalid!");
		}

		// Build AccountsMerkleTree and generate Root
		changeLog.buildPassportMerkleTreeBase();
		changeLog.generatePassportMerkleTreeRoot();

		// Initial Root
		eqcRoot.setAccountsMerkelTreeRoot(changeLog.getPassportMerkleTreeRoot());
		eqcRoot.setSubchainsMerkelTreeRoot(eQcoinSeed.getRoot());
		// Set EQCHeader's Root's hash
		eqcHeader.setRootHash(eqcRoot.getHash());

	}

//	public boolean isMeetPreconditions(Object... objects) throws Exception {
//		TransferOperationTransaction operationTransaction = (TransferOperationTransaction) objects[0];
//		ChangeLog changeLog = (ChangeLog) objects[1];
//		if (operationTransaction.getOperation() instanceof UpdateLockOperation) {
//			if (!operationTransaction.getOperation().isMeetPreconditions(changeLog)) {
//				return false;
//			}
//		} else {
//			return false;
//		}
//		return true;
//	}

	/**
	 * Deprecated use public boolean isValid(AccountsMerkleTree changeLog)
	 * instead of
	 * 
	 * @param eqcBlock
	 * @param changeLog
	 * @return
	 * @throws Exception
	 */
//	@Deprecated
	// Keep this only for reference&double check after used it then removed it
//	public static boolean verify(EQCHive eqcBlock, AccountsMerkleTree changeLog) throws Exception {
//		// Check if EQCHeader is valid
//		BigInteger target = Util.targetBytesToBigInteger(Util.cypherTarget(eqcBlock.getHeight()));
//		if (new BigInteger(1,
//				Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(eqcBlock.getEqcHeader().getBytes(), Util.HUNDRED_THOUSAND))
//						.compareTo(target) > 0) {
//			Log.Error("EQCHeader is invalid");
//			return false;
//		}
//
//		// Check if Transactions size is less than 1 MB
////		if (eqcBlock.getTransactions().getSize() > Util.ONE_MB) {
////			Log.Error("Transactions size  is invalid, size: " + eqcBlock.getTransactions().getSize());
////			return false;
////		}
//
//		// Check if AddressList is correct the first Address' Serial Number is equal to
//		// previous block's last Address' Serial Number + 1
//		// Every Address should be unique in current AddressList and doesn't exists in
//		// the history AddressList in H2
//		// Every Address's Serial Number should equal to previous Address'
//		// getNextSerialNumber
//
//		// Check if PublicKeyList is correct the first PublicKey's Serial Number is
//		// equal to previous block's last PublicKey's getNextSerialNumber
//		// Every PublicKey should be unique in current PublicKeyList and doesn't exists
//		// in the history PublicKeyList in H2
//		// Every PublicKey's Serial Number should equal to previous PublicKey's
//		// getNextSerialNumber
//
//		// Get Transaction list and Signature list
//		Vector<Transaction> transactinList = eqcBlock.getTransactions().getNewTransactionList();
//		Vector<byte[]> signatureList = eqcBlock.getSignatures().getSignatureList();
//
//		// In addition to the CoinBase transaction, the following checks are made for
//		// all other transactions.
//		// Check if every Transaction's PublicKey is exists
//		if (!eqcBlock.isEveryPublicKeyExists()) {
//			Log.Error("Every Transaction's PublicKey should exists");
//			return false;
//		}
//		// Check if every Transaction's Address is exists
//		if (!eqcBlock.isEveryAddressExists()) {
//			Log.Error("Every Transaction's Address should exists");
//			return false;
//		}
//
//		// Fill in every Transaction's PublicKey, Signature, relevant Address for verify
//		// Bad methods need change to every Transaction use itself's prepareVerify
////		eqcBlock.buildTransactionsForVerify();
//
//		// Check if only have CoinBase Transaction
//		if (signatureList == null) {
//			if (transactinList.size() != 1) {
//				Log.Error(
//						"Only have CoinBase Transaction but the number of Transaction isn't equal to 1, current size: "
//								+ transactinList.size());
//				return false;
//			}
//		} else {
//			// Check if every Transaction has it's Signature
//			if ((transactinList.size() - 1) != signatureList.size()) {
//				Log.Error("Transaction's number: " + (transactinList.size() - 1)
//						+ " doesn't equal to Signature's number: " + signatureList.size());
//				return false;
//			}
//		}
//
//		// Check if CoinBase is correct - CoinBase's Address&Value is valid
//		if (!transactinList.get(0).isCoinBase()) {
//			Log.Error("The No.0 Transaction isn't CoinBase");
//			return false;
//		}
//		// Check if CoinBase's TxOut Address is valid
//		if (!transactinList.get(0).isTxOutAddressValid()) {
//			Log.Error("The CoinBase's TxOut's Address is invalid: "
//					+ transactinList.get(0).getTxOutList().get(0).toString());
//			return false;
//		}
//		// Check if CoinBase's value is valid
//		long totalTxFee = 0;
//		for (int i = 1; i < transactinList.size(); ++i) {
//			totalTxFee += transactinList.get(i).getTxFee();
//		}
//		long coinBaseValue = 0;
//		if (eqcBlock.getHeight().compareTo(Util.getMaxCoinbaseHeight(eqcBlock.getHeight())) < 0) {
//			coinBaseValue = Util.COINBASE_REWARD + totalTxFee;
//			if (transactinList.get(0).getTxOutValues() != coinBaseValue) {
//				Log.Error("CoinBase's value: " + transactinList.get(0).getTxOutValues()
//						+ " doesn't equal to COINBASE_REWARD + totalTxFee: " + (Util.COINBASE_REWARD + totalTxFee));
//				return false;
//			}
//		} else {
//			coinBaseValue = totalTxFee;
//			if (transactinList.get(0).getTxOutValues() != coinBaseValue) {
//				Log.Error("CoinBase's value: " + transactinList.get(0).getTxOutValues()
//						+ " doesn't equal to totalTxFee: " + totalTxFee);
//				return false;
//			}
//		}
//
//		// Check if only have one CoinBase
//		for (int i = 1; i < transactinList.size(); ++i) {
//			if (transactinList.get(i).isCoinBase()) {
//				Log.Error("Every EQCBlock should has only one CoinBase but No. " + i + " is also CoinBase.");
//				return false;
//			}
//		}
//
//		// Check if Signature is unique in current Signatures and doesn't exists in the
//		// history Signature table in H2
//		for (int i = 0; i < signatureList.size(); ++i) {
//			for (int j = i + 1; j < signatureList.size(); ++j) {
//				if (Arrays.equals(signatureList.get(i), signatureList.get(j))) {
//					Log.Error("Signature doesn't unique in current  Signature list");
//					return false;
//				}
//			}
//		}
//
//		for (byte[] signature : signatureList) {
//			if (EQCBlockChainH2.getInstance().isSignatureExists(signature)) {
//				Log.Error("Signature doesn't unique in H2's history Signature list");
//				return false;
//			}
//		}
//
//		// Check if every Transaction is valid
//		for (Transaction transaction : eqcBlock.getTransactions().getNewTransactionList()) {
//			if (transaction.isCoinBase()) {
//
//			} else {
//				if (!transaction.isValid(changeLog, AddressShape.READABLE)) {
//					Log.Error("Every Transaction should valid");
//					return false;
//				}
//			}
//		}
//
//		return true;
//	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(eqcHeader.getBin());
			os.write(eqcRoot.getBin());
			os.write(eQcoinSeed.getBin());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public byte[] getBin() {
		return EQCType.bytesToBIN(getBytes());
	}

	public int getSize() {
		return getBytes().length;
	}

	/**
	 * @return the root
	 */
	public EQCRoot getRoot() {
		return eqcRoot;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(EQCRoot root) {
		this.eqcRoot = root;
	}

	public byte[] getHash() {
		return eqcHeader.getHash();
	}

	@Override
	public boolean isSanity() throws Exception {
		if (eqcHeader == null || !eqcHeader.isSanity() || eqcRoot == null || !eqcRoot.isSanity() || eQcoinSeed == null || eQcoinSeed.isSanity()) {
			return false;
		}
		return true;
	}

//	public byte[] getTransactionsMerkelTreeRoot() {
//		Vector<byte[]> transactionsMerkelTreeRootList = new Vector<>();
//		byte[] bytes = null;
//		transactionsMerkelTreeRootList.add(transactions.getNewTransactionListMerkelTreeRoot());
//
//		if ((signatures != null) && (bytes = signatures.getSignaturesMerkelTreeRoot()) != null) {
//			transactionsMerkelTreeRootList.add(bytes);
//		}
////		else {
////			transactionsMerkelTreeRootList.add(Util.NULL_HASH);
////		}
//
//		if ((bytes = transactions.getNewPassportListMerkelTreeRoot()) != null) {
//			transactionsMerkelTreeRootList.add(bytes);
//		}
////		else {
////			transactionsMerkelTreeRootList.add(Util.NULL_HASH);
////		}
//
//		if ((bytes = transactions.getNewCompressedPublickeyListMerkelTreeRoot()) != null) {
//			transactionsMerkelTreeRootList.add(bytes);
//		}
////		else {
////			transactionsMerkelTreeRootList.add(Util.NULL_HASH);
////		}
//		return Util.EQCCHA_MULTIPLE_DUAL(Util.getMerkleTreeRoot(transactionsMerkelTreeRootList), Util.ONE, false,
//				false);
//	}

	/**
	 * Auditing the EQCHive
	 * <p>
	 * @param changeLog
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean isValid(ChangeLog changeLog) {
		try {

			/**
			 * Heal Protocol If height equal to a specific height then update the ID No.1's
			 * Address to a new Address the more information you can reference to
			 * https://github.com/eqzip/eqchains
			 */

			// Check if Target is valid
			if (!eqcHeader.isDifficultyValid(changeLog)) {
				Log.Error("EQCHeader difficulty is invalid.");
				return false;
			}

			// Check if EQcoinSeed is valid
			if (!eQcoinSeed.isValid(changeLog)) {
				Log.Error("EQcoinSeed is invalid.");
				return false;
			}

			// Verify Statistics
			Statistics statistics = changeLog.getStatistics();
			if (!statistics.isValid(changeLog)) {
				Log.Error("Statistics data is invalid.");
				return false;
			}

			// Build AccountsMerkleTree and generate Root and Statistics
			changeLog.buildPassportMerkleTreeBase();
			changeLog.generatePassportMerkleTreeRoot();

			// Verify Root
//		// Check total supply
//		if (statistics.totalSupply != Util.cypherTotalSupply(eqcHeader.getHeight())) {
//			Log.Error("Total supply is invalid doesn't equal cypherTotalSupply.");
//			return false;
//		}
//		if(statistics.totalSupply != root.getTotalSupply()){
//			Log.Error("Total supply is invalid doesn't equal root.");
//			return false;
//		}

//			EQCHive previousBlock = EQCBlockChainRocksDB.getInstance()
//					.getEQCBlock(eqcHeader.getHeight().getPreviousID(), true);
			// Check total Account numbers
//		if (!previousBlock.getRoot().getTotalAccountNumbers()
//				.add(BigInteger.valueOf(transactions.getNewAccountList().size()))
//				.equals(changeLog.getTotalAccountNumbers())) {
//			Log.Error("Total Account numbers is invalid doesn't equal changeLog.");
//			return false;
//		}
//		if(!root.getTotalAccountNumbers().equals(changeLog.getTotalAccountNumbers())) {
//			Log.Error("Total Account numbers is invalid doesn't equal root.");
//			return false;
//		}

//		// Check total Transaction numbers
//		if (!previousBlock.getRoot().getTotalTransactionNumbers()
//				.add(BigInteger.valueOf(transactions.getNewTransactionList().size()))
//				.equals(statistics.totalTransactionNumbers)) {
//			Log.Error("Total Transaction numbers is invalid doesn't equal transactions.getNewTransactionList.");
//			return false;
//		}
//		if(!statistics.totalTransactionNumbers.equals(root.getTotalTransactionNumbers())) {
//			Log.Error("Total Transaction numbers is invalid doesn't equal root.getTotalTransactionNumbers.");
//			return false;
//		}
			// Check AccountsMerkelTreeRoot
			if (!Arrays.equals(eqcRoot.getAccountsMerkelTreeRoot(), changeLog.getPassportMerkleTreeRoot())) {
				Log.Error("EQCPassportStateRoot is invalid!");
				return false;
			}
			// Check TransactionsMerkelTreeRoot
			if (!Arrays.equals(eqcRoot.getSubchainsMerkelTreeRoot(), eQcoinSeed.getRoot())) {
				Log.Error("EQcoinSeedStateRoot is invalid!");
				return false;
			}
			// Verify EQCHeader
			if (!eqcHeader.isValid(changeLog, eqcRoot.getHash())) {
				Log.Error("EQCHeader is invalid!");
				return false;
			}

			// Merge shouldn't be done at here
//		// Merge AccountsMerkleTree relevant Account's status
//		if(!changeLog.merge()) {
//			Log.Error("Merge AccountsMerkleTree relevant Account's status error occur");
//			return false;
//		}
		} catch (Exception e) {
			Log.Error("EQCHive is invalid: " + e.getMessage());
			return false;
		}

		return true;
	}

//	public boolean isAddressListAddressUnique() {
//		for (int i = 0; i < transactions.getAddressList().size(); ++i) {
//			for (int j = i + 1; j < transactions.getAddressList().size(); ++j) {
//				if (transactions.getAddressList().get(i).equals(transactions.getAddressList().get(j))) {
//					return false;
//				}
//			}
//		}
//		return true;
//	}

	public O getO() {
		return new O(ByteBuffer.wrap(this.getBytes()));
	}

	/**
	 * @return the eqcRoot
	 */
	public EQCRoot getEqcRoot() {
		return eqcRoot;
	}

	/**
	 * @param eqcRoot the eqcRoot to set
	 */
	public void setEqcRoot(EQCRoot eqcRoot) {
		this.eqcRoot = eqcRoot;
	}

	/**
	 * @return the eQcoinSeed
	 */
	public EQcoinSeed geteQcoinSeed() {
		return eQcoinSeed;
	}

	/**
	 * @param eQcoinSeed the eQcoinSeed to set
	 */
	public void seteQcoinSeed(EQcoinSeed eQcoinSeed) {
		this.eQcoinSeed = eQcoinSeed;
	}
	
}