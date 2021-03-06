///**
// * EQcoin core - EQcoin Federation's EQcoin core library
// * @copyright 2018-present EQcoin Federation All rights reserved...
// * Copyright of all works released by EQcoin Federation or jointly released by
// * EQcoin Federation with cooperative partners are owned by EQcoin Federation
// * and entitled to protection available from copyright law by country as well as
// * international conventions.
// * Attribution — You must give appropriate credit, provide a link to the license.
// * Non Commercial — You may not use the material for commercial purposes.
// * No Derivatives — If you remix, transform, or build upon the material, you may
// * not distribute the modified material.
// * For any use of above stated content of copyright beyond the scope of fair use
// * or without prior written permission, EQcoin Federation reserves all rights to
// * take any legal action and pursue any right or remedy available under applicable
// * law.
// * https://www.eqcoin.org
// * 
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//package org.eqcoin.wallet.persistence;
//
//import java.math.BigInteger;
//import java.security.PrivateKey;
//import java.security.PublicKey;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.sql.Types;
//import java.util.Vector;
//
//import org.eqcoin.keystore.Keystore.ECCTYPE;
//import org.eqcoin.lock.Lock;
//import org.eqcoin.lock.LockMate;
//import org.eqcoin.lock.T1Lock;
//import org.eqcoin.lock.T2Lock;
//import org.eqcoin.lock.LockTool.LockType;
//import org.eqcoin.lock.publickey.Publickey;
//import org.eqcoin.passport.ExpendablePassport;
//import org.eqcoin.passport.Passport;
//import org.eqcoin.passport.Passport.PassportType;
//import org.eqcoin.persistence.globalstate.GlobalStateH2;
//import org.eqcoin.serialization.EQCType;
//import org.eqcoin.transaction.Transaction;
//import org.eqcoin.util.ID;
//import org.eqcoin.util.Log;
//import org.eqcoin.util.Util;
//
///**
// * @author Xun Wang
// * @date May 31, 2020
// * @email 10509759@qq.com
// */
//@Deprecated
//public abstract class EQCWalletH2 {
//
//	private static final String JDBC_URL = "jdbc:h2:" + Util.WALLET_DATABASE_NAME;
//	private static final String USER = "Believer";
//	private static final String PASSWORD = "God bless us...";
//	private static Connection connection;
//	private static final int ONE_ROW = 1;
//	private static final String LOCK_WALLET = "LOCK_WALLET";
//	private static final String PASSPORT_WALLET = "PASSPORT_WALLET";
//
//	private String createLockTable(String tableName) {
//		return "CREATE TABLE IF NOT EXISTS " + tableName + " (" + "id BIGINT PRIMARY KEY CHECK id >= 0,"
//				+ "type TINYINT NOT NULL CHECK type >= 0," + "status TINYINT NOT NULL CHECK status >= 0,"
//				+ "proof BINARY NOT NULL  UNIQUE," + "publickey BINARY(67) UNIQUE" + ")";
//	}
//
//	private String createPassportTable(String tableName) {
//		return "CREATE TABLE IF NOT EXISTS " + tableName + " (" + "id BIGINT PRIMARY KEY CHECK id >= 0,"
//				+ "lock_id BIGINT NOT NULL UNIQUE CHECK lock_id >= 0," + "type TINYINT NOT NULL CHECK type >= 0,"
//				+ "balance BIGINT NOT NULL CHECK balance >= 510000," + "nonce BIGINT NOT NULL CHECK nonce >= 0,"
//				+ "storage BINARY," + "state_proof BINARY(64)" + ")";
//	}
//
//	public EQCWalletH2() throws ClassNotFoundException, SQLException {
//		connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
//		connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
//		createTable();
//	}
//
//	private synchronized void createTable() throws SQLException {
//		Statement statement = connection.createStatement();
//		boolean result = statement.execute(createLockTable(LOCK_WALLET));
//		result = statement.execute(createPassportTable(PASSPORT_WALLET));
//		statement.execute("CREATE TABLE IF NOT EXISTS ALAIS(" + "alais BIGINT NOT NULL UNIQUE CHECK alais >=0" + ")");
//
//		// EQC Transaction Pool table
//		statement.execute("CREATE TABLE IF NOT EXISTS TRANSACTION_POOL(" + "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
//				+ "passport_id BIGINT," + "nonce BIGINT," + "rawdata BINARY," + "witness BINARY," + "proof BINARY(4),"
//				+ "priority_value BIGINT," + "receieved_timestamp BIGINT," + "record_status BOOLEAN,"
//				+ "record_height BIGINT" + ")");
//
//		statement.close();
//
//		if (result) {
//			Log.info("Create table");
//		}
//	}
//
//	@Override
//	public Connection getConnection() throws Exception {
//		return connection;
//	}
//
//	@Override
//	public boolean close() throws Exception {
//		if (connection != null) {
//			connection.close();
//			connection = null;
//		}
//		return true;
//	}
//
//	@Override
//	public boolean saveLock(LockMate lockMate) throws Exception {
//		int rowCounter = 0;
//		PreparedStatement preparedStatement = null;
//		if (isLockExists(lockMate.getId())) {
//			preparedStatement = connection.prepareStatement("UPDATE "
//					+ LOCK_WALLET
//					+ " SET id = ?,  type = ?, status = ?, proof = ?, publickey = ? WHERE id = ?");
//			preparedStatement.setLong(6, lockMate.getId().longValue());
//		} else {
//			preparedStatement = connection
//					.prepareStatement("INSERT INTO " + LOCK_WALLET
//							+ " (id, type, status, proof, publickey) VALUES (?, ?, ?, ?, ?)");
//		}
//		
//		preparedStatement.setLong(1, lockMate.getId().longValue());
//		preparedStatement.setByte(2, (byte) lockMate.getLock().getLockType().ordinal());
//		preparedStatement.setByte(3, lockMate.getStatus());
//		preparedStatement.setBytes(4, lockMate.getLock().getLockProof());
//		if(lockMate.getPublickey().isNULL()) {
//			preparedStatement.setNull(5, Types.BINARY);
//		}
//		else {
//			preparedStatement.setBytes(5, lockMate.getPublickey().getBytes());
//		}
//		rowCounter = preparedStatement.executeUpdate();
//		
//		EQCType.assertEqual(rowCounter, ONE_ROW);
//		return true;
//	}
//
//	@Override
//	public LockMate getLock(ID id) throws Exception {
//		LockMate lockMate = null;
//		byte[] publickey = null;
//		PreparedStatement preparedStatement = connection
//				.prepareStatement("SELECT * FROM " + LOCK_WALLET + " WHERE id=?");
//		preparedStatement.setLong(1, id.longValue());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		if (resultSet.next()) {
//			lockMate = new LockMate();
//			lockMate.setId(new ID(resultSet.getLong("id")));
//			LockType lockType = LockType.get(resultSet.getByte("type"));
//			Lock lock = null;
//			if(lockType == LockType.T1) {
//				lock = new T1Lock();
//			}
//			else if(lockType == LockType.T2) {
//				lock = new T2Lock();
//			}
//			lockMate.setLock(lock);
//			lockMate.setStatus(resultSet.getByte("status"));
//			lockMate.getLock().setLockProof(resultSet.getBytes("proof"));
//			publickey = resultSet.getBytes("publickey");
//			if (publickey == null) {
//				lockMate.setPublickey(new Publickey());
//			} else {
//				lockMate.setPublickey(new Publickey().setLockType(lockMate.getLock().getLockType()).Parse(publickey));
//			}
//		}
//		return lockMate;
//	}
//
//	@Override
//	public Vector<LockMate> getLockList() throws Exception {
//		Vector<LockMate> lockList = new Vector<>();
//		LockMate lockMate = null;
//		byte[] publickey = null;
//		PreparedStatement preparedStatement = connection
//				.prepareStatement("SELECT * FROM " + LOCK_WALLET);
//		ResultSet resultSet = preparedStatement.executeQuery();
//		while(resultSet.next()) {
//			lockMate = new LockMate();
//			lockMate.setId(new ID(resultSet.getLong("id")));
//			LockType lockType = LockType.get(resultSet.getByte("type"));
//			Lock lock = null;
//			if(lockType == LockType.T1) {
//				lock = new T1Lock();
//			}
//			else if(lockType == LockType.T2) {
//				lock = new T2Lock();
//			}
//			lockMate.setLock(lock);
//			lockMate.setStatus(resultSet.getByte("status"));
//			lockMate.getLock().setLockProof(resultSet.getBytes("proof"));
//			publickey = resultSet.getBytes("publickey");
//			if (publickey == null) {
//				lockMate.setPublickey(new Publickey());
//			} else {
//				lockMate.setPublickey(new Publickey().setLockType(lockMate.getLock().getLockType()).Parse(publickey));
//			}
//			lockList.add(lockMate);
//		}
//		return lockList;
//	}
//
//	@Override
//	public boolean isLockExists(ID id) throws Exception {
//		boolean isExists = false;
//		PreparedStatement preparedStatement = connection.prepareStatement(
//				"SELECT * FROM " + LOCK_WALLET + " WHERE id=?");
//		preparedStatement.setLong(1, id.longValue());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		if (resultSet.next()) {
//			isExists = true;
//		}
//		return isExists;
//	}
//
//	@Override
//	public ID isLockExists(Lock lock) throws Exception {
//		ID lockId = null;
//		PreparedStatement preparedStatement = connection.prepareStatement(
//				"SELECT * FROM " + LOCK_WALLET + " WHERE proof=?");
//		preparedStatement.setBytes(1, lock.getLockProof());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		if (resultSet.next()) {
//			lockId = new ID(resultSet.getLong("id"));
//		}
//		return lockId;
//	}
//
//	@Override
//	public boolean deleteLock(ID id) throws Exception {
//		int rowCounter = 0;
//		PreparedStatement preparedStatement;
//		preparedStatement = connection.prepareStatement("DELETE FROM " + LOCK_WALLET + " WHERE id =?");
//		preparedStatement.setLong(1, id.longValue());
//		rowCounter = preparedStatement.executeUpdate();
//		Log.info("rowCounter: " + rowCounter);
//		EQCType.assertEqual(rowCounter, ONE_ROW);
//		return true;
//	}
//
//	@Override
//	public boolean savePassport(Passport passport) throws Exception {
//		int rowCounter = 0;
//		PreparedStatement preparedStatement = null;
//		if (isPassportExists(passport.getId())) {
//			preparedStatement = connection.prepareStatement(
//					"UPDATE " + PASSPORT_WALLET + " SET id = ?, lock_id = ?, type = ?, balance = ?, nonce = ?, storage = ?, state_proof = ? WHERE id = ?");
//			preparedStatement.setLong(8, passport.getId().longValue());
//		} else {
//			preparedStatement = connection.prepareStatement(
//					"INSERT INTO " + PASSPORT_WALLET + "  (id, lock_id, type, balance, nonce, storage, state_proof) VALUES (?, ?, ?, ?, ?, ?, ?)");
//		}
//		preparedStatement.setLong(1, passport.getId().longValue());
//		preparedStatement.setLong(2, passport.getLockID().longValue());
//		preparedStatement.setByte(3, (byte) passport.getType().ordinal());
//		preparedStatement.setLong(4, passport.getBalance().longValue());
//		preparedStatement.setLong(5, passport.getNonce().longValue());
//		if(passport.getType() == PassportType.EXTENDABLE || passport.getType() == PassportType.EQCOINROOT ) {
//			ExpendablePassport expendablePassport = (ExpendablePassport) passport;
//			preparedStatement.setBytes(6, expendablePassport.getStorageBytes());
//		}
//		else {
//			preparedStatement.setNull(6, Types.NULL);
//		}
//		preparedStatement.setNull(7, Types.NULL);
//		rowCounter = preparedStatement.executeUpdate();
//		
//		EQCType.assertEqual(rowCounter, ONE_ROW);
//		return true;
//	}
//
//	@Override
//	public Passport getPassport(ID id) throws Exception {
//		Passport passport = null;
//		PreparedStatement preparedStatement = connection.prepareStatement(
//				"SELECT * FROM " + PASSPORT_WALLET + " WHERE id=?");
//		preparedStatement.setLong(1, id.longValue());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		if (resultSet.next()) {
//			passport = Passport.parsePassport(resultSet);
//		}
//		return passport;
//	}
//
//	@Override
//	public boolean isPassportExists(ID id) throws Exception {
//		boolean isExists = false;
//		PreparedStatement preparedStatement = connection.prepareStatement(
//				"SELECT * FROM " + PASSPORT_WALLET + " WHERE id = ?");
//		preparedStatement.setLong(1, id.longValue());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		if (resultSet.next()) {
//			isExists = true;
//		}
//		return isExists;
//	}
//
//	@Override
//	public boolean deletePassport(ID id) throws Exception {
//		int rowCounter = 0;
//		PreparedStatement preparedStatement;
//		preparedStatement = connection.prepareStatement("DELETE FROM " + PASSPORT_WALLET + " WHERE id =?");
//		preparedStatement.setLong(1, id.longValue());
//		rowCounter = preparedStatement.executeUpdate();
//		Log.info("rowCounter: " + rowCounter);
//		EQCType.assertEqual(rowCounter, ONE_ROW);
//		return true;
//	}
//
//	@Override
//	public synchronized boolean isTransactionExistsInPool(Transaction transaction) throws Exception {
//		boolean isExists = false;
//		PreparedStatement preparedStatement = null;
//		ResultSet resultSet = null;
//		preparedStatement = connection
//				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE witness=? AND nonce=? AND priority_value<=?");
//		preparedStatement.setBytes(1, transaction.getWitness().getWitness());
//		preparedStatement.setLong(2, transaction.getNonce().longValue());
//		preparedStatement.setLong(3, transaction.getPriorityValue().longValue());
//		resultSet = preparedStatement.executeQuery();
//		if (resultSet.next()) {
//			isExists = true;
//		}
//		return isExists;
//	}
//	
//	@Override
//	public boolean saveTransactionInPool(Transaction transaction) throws Exception {
//		int result = 0;
//		PreparedStatement preparedStatement = null;
//		if (!isTransactionExistsInPool(transaction)) {
//			preparedStatement = connection.prepareStatement(
//					"INSERT INTO TRANSACTION_POOL (passport_id, nonce, rawdata, witness, proof, priority_value, receieved_timestamp, record_status, record_height) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
//			preparedStatement.setLong(1, transaction.getWitness().getPassport().getId().longValue());
//			preparedStatement.setLong(2, transaction.getNonce().longValue());
//			preparedStatement.setBytes(3, transaction.getBytes());
//			preparedStatement.setBytes(4, transaction.getWitness().getWitness());
//			preparedStatement.setBytes(5, transaction.getProof());
//			preparedStatement.setLong(6, transaction.getPriorityValue().longValue());
//			preparedStatement.setLong(7, System.currentTimeMillis());
//			preparedStatement.setBoolean(8, false);
//			preparedStatement.setNull(9, Types.BIGINT);
//			result = preparedStatement.executeUpdate();
//		} else {
//			preparedStatement = connection.prepareStatement(
//					"UPDATE TRANSACTION_POOL SET rawdata=?, witness=?, proof=?, priority_value=?, receieved_timestamp=?, record_status=?, record_height=? WHERE passport_id=? AND nonce=?");
//			preparedStatement.setBytes(1, transaction.getBytes());
//			preparedStatement.setBytes(2, transaction.getWitness().getWitness());
//			preparedStatement.setBytes(3, transaction.getProof());
//			preparedStatement.setLong(4, transaction.getPriorityValue().longValue());
//			preparedStatement.setLong(5, System.currentTimeMillis());
//			preparedStatement.setBoolean(6, false);
//			preparedStatement.setNull(7, Types.BIGINT);
//			preparedStatement.setLong(8, transaction.getWitness().getPassport().getId().longValue());
//			preparedStatement.setLong(9, transaction.getNonce().longValue());
//			result = preparedStatement.executeUpdate();
//		}
//		Log.info("result: " + result);
//		return result == ONE_ROW;
//	}
//
//	@Override
//	public boolean deleteTransactionInPool(Transaction transaction) throws Exception {
//		int result = 0;
//		if(Util.IsDeleteTransactionInPool) {
//			PreparedStatement preparedStatement = connection
//					.prepareStatement("DELETE FROM TRANSACTION_POOL WHERE witness= ?");
//			preparedStatement.setBytes(1, transaction.getWitness().getWitness());
//			result = preparedStatement.executeUpdate();
//		}
//		Log.info("result: " + result);
//		return result == ONE_ROW;
//	}
//
//	@Override
//	public Vector<Transaction> getPendingTransactionListInPool(ID id) throws Exception {
//		Vector<Transaction> transactionList = new Vector<>();
//		Transaction transaction = null;
//		PreparedStatement preparedStatement = connection
//				.prepareStatement("SELECT rawdata FROM TRANSACTION_POOL WHERE passport_id=?");
//		preparedStatement.setLong(1, id.longValue());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		while (resultSet.next()) {
//			transaction = new Transaction().Parse(resultSet.getBytes("rawdata"));
//			transactionList.add(transaction);
//		}
//		return transactionList;
//	}
//
//	@Override
//	public <T> boolean generateKeyPair(T alais, ECCTYPE eccType) throws Exception {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public <T> PrivateKey getPrivateKey(T alais) throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <T> PublicKey getPublicKey(T alais) throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ID generateAlais() throws Exception {
//		ID alais = null;
//		alais = getAlais();
//		if(alais == null) {
//			alais = ID.ZERO;
//			saveAlais(alais);
//		}
//		else {
//			alais = alais.getNextID();
//		}
//		return alais;
//	}
//	
//	private boolean saveAlais(ID alais) throws SQLException {
//		int rowCounter = 0;
//		PreparedStatement preparedStatement = null;;
//			if (getAlais() != null) {
//				preparedStatement = connection.prepareStatement("UPDATE ALAIS SET alais=?");
//				preparedStatement.setLong(1, alais.longValue());
//			} else {
//				preparedStatement = connection.prepareStatement("INSERT INTO ALAIS(alais) VALUES(?)");
//				preparedStatement.setLong(1, alais.longValue());
//			}
//			rowCounter = preparedStatement.executeUpdate();
//			EQCType.assertEqual(rowCounter, ONE_ROW);
//		return true;
//	}
//	
//	private ID getAlais() throws SQLException {
//		ID id = null;
//		Statement statement = connection.createStatement();
//		ResultSet resultSet = statement.executeQuery("SELECT * FROM ALAIS");
//		if (resultSet.next()) {
//			id = new ID(resultSet.getLong("alais"));
//		}
//		statement.close();
//		return id;
//	}
//
//}
